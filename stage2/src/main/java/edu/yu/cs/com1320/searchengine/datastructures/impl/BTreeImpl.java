package edu.yu.cs.com1320.searchengine.datastructures.impl;

import edu.yu.cs.com1320.searchengine.datastructures.BTree;
import edu.yu.cs.com1320.searchengine.core.PersistenceManager;

import java.io.FileNotFoundException;
import java.io.IOException;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {
    
    //max children per B-tree node = MAX-1 (must be an even number and greater than 2)
    private static final int MAX = 4;
    private Node root;
    private int height;
    private PersistenceManager<Key, Value> yogi;
    public BTreeImpl (){
        this.root = new Node();
        this.height = 0;
        this.yogi = null;
    }

    /**
     * @param key the key whose value should be returned
     * @return the value that is stored in the BTreeImpl for key, or null if there is no such key in the tree
     */
    @Override
    public Value get(Key key) {
        if (key == null) {
            throw new IllegalArgumentException("argument to get() is null");
        }
        Entry<Key, Value> entry;
        try { entry = this.get(this.root, key, this.height);}
        catch (IOException e) { throw new RuntimeException(e);}
        if (entry == null) {
            // entry does not exist
            return null;
        }
        if (entry.val == null){
            if (this.yogi == null){
                throw new IllegalStateException("No Persistence Manager");
            }
            // checks if entries with null values are on disk
            try {
                entry.val = this.yogi.deserialize(key);
                this.yogi.delete(key);
            } catch (FileNotFoundException e){
                // entry does not exist anymore
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return entry.val;
    }

    private Entry<Key, Value> get(Node currentNode, Key key, int height) throws IOException {
        Entry<?, ?>[] entries = currentNode.entries;
        //current node is external (i.e. height == 0)
        if (height == 0) {
            for (int i = 0; i < currentNode.entryCount; i++) {
                if(!entries[i].isSentinel && isEqual(key, entries[i].key)) {
                    //found desired key. Return the entry
                    return (Entry<Key, Value>) entries[i];
                }
            }
        }
        //current node is internal (height > 0)
        else {
            for (int i = 0; i < currentNode.entryCount; i++) {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be in the subtree below the current entry),
                //then recurse into the current entry’s child
                if (i + 1 == currentNode.entryCount || less(key, entries[i + 1].key)){
                    return this.get(entries[i].child, key, height - 1);
                }
            }
        }
        //didn't find the key
        return null;
    }

    // comparison functions - make Comparable instead of Key to avoid casts
    private static boolean isEqual(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }

    private static boolean less(Comparable k1, Comparable k2)
    {
        return k1.compareTo(k2) < 0;
    }

    @Override
    public Value put(Key key, Value val) {
        if (key == null) {
            throw new IllegalArgumentException("argument key to put() is null");
        }
        Entry<Key, Value> alreadyThere;
        try { alreadyThere = this.get(this.root, key, this.height);}
        catch (IOException e) { throw new RuntimeException(e);}
        //if the key already exists in the b-tree, simply replace the value
        if(alreadyThere != null) {
            return replaceOrDelete(key, val, alreadyThere);
        }
        if (val == null){
            return null;
        }
        Node newNode = this.put(this.root, key, val, this.height);
        if (newNode == null) {
            return null;
        }
        //split the root:
        //Create a new node to be the root.
        //Set the old root to be new root's first entry.
        //Set the node returned from the call to put to be new root's second entry
        Node newRoot = new Node(MAX/2);
        newRoot.entries[0] = new Entry<Key, Value>((Key)this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry<Key, Value>((Key)newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        //a split at the root always increases the tree height by 1
        this.height++;
        return null;
    }

    private Value replaceOrDelete(Key key, Value val, Entry<Key, Value> alreadyThere) {
        if (alreadyThere.val == null){
            if (this.yogi == null){
                throw new IllegalStateException("No Persistence Manager");
            }
            // checks if entries with null values are on disk
            try {
                alreadyThere.val = this.yogi.deserialize(key);
                this.yogi.delete(key);
            } catch (FileNotFoundException e){
                // entry does not exist anymore
                alreadyThere.val = val;
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Value temp = alreadyThere.val;
        alreadyThere.val = val;
        return temp;
    }

    /**
     *
     * @param currentNode
     * @param key
     * @param val
     * @param height
     * @return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node
     */
    private Node put(Node currentNode, Key key, Value val, int height) {
        int i;
        Entry<Key, Value> newEntry = new Entry<>(key, val, null);
        // external node
        if (height == 0) {
            i = this.newEntryLocation(currentNode, key);
        }
        // internal node
        else {
            //find index in node entry array to insert the new entry
            for (i = 0; i < currentNode.entryCount; i++) {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be added to the subtree below the current entry),
                //then do a recursive call to put on the current entry’s child
                if ((i + 1 == currentNode.entryCount) || less(key, currentNode.entries[i + 1].key)) {
                    //increment i (i++) after the call so that a new entry created by a split
                    //will be inserted in the next slot
                    Node newNode = this.put(currentNode.entries[i++].child, key, val, height - 1);
                    if (newNode == null) {
                        return null;
                    }
                    //if the call to put returned a node, it means I need to add a new entry to
                    //the current node
                    newEntry.key = (Key)newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        this.newEntryPutter(currentNode, i, newEntry);
        if (currentNode.entryCount < MAX) {
            //no structural changes needed in the tree
            //so just return null
            return null;
        }
        else {
            //will have to create a new entry in the parent due
            //to the split, so return the new node, which is
            //the node for which the new entry will be created
            return this.split(currentNode, height);
        }
    }

    private int newEntryLocation(Node currentNode, Key key) {
        int i;
        //find index in currentNode’s entry[] to insert new entry
        //we look for key < entry.key since we want to leave i
        //pointing to the slot to insert the new entry, hence we want to find
        //the first entry in the current node that key is LESS THAN
        for (i = 0; i < currentNode.entryCount; i++) {
            if (!currentNode.entries[i].isSentinel && less(key, currentNode.entries[i].key)) {
                break;
            }
        }
        return i;
    }

    private void newEntryPutter(Node currentNode, int i, Entry<Key, Value> newEntry) {
        //shift entries over one place to make room for new entry
        for (int j = currentNode.entryCount; j > i; j--) {
            currentNode.entries[j] = currentNode.entries[j - 1];
        }
        //add new entry
        currentNode.entries[i] = newEntry;
        currentNode.entryCount++;
    }

    /**
     * split node in half
     * @param currentNode
     * @return new node
     */
    private Node split(Node currentNode, int height) {
        Node newNode = new Node(MAX / 2);
        //by changing currentNode.entryCount, we will treat any value
        //at index higher than the new currentNode.entryCount as if
        //it doesn't exist
        currentNode.entryCount = MAX / 2;
        //copy top half into new node
        for (int i = 0; i < MAX / 2; i++) {
            newNode.entries[i] = currentNode.entries[MAX / 2 + i];
            currentNode.entries[MAX / 2 + i] = null;
        }
        return newNode;
    }

    @Override
    public void moveToDisk(Key k) throws IOException {
        if (k == null) {
            throw new IllegalArgumentException("Argument to moveToDisk() is null");
        }
        Entry <Key, Value> entry = this.get(this.root, k, this.height);
        if (entry == null){
            throw new IllegalArgumentException("Entry does not exist for this key");
        }
        if (entry.val == null){
            return;
        }
        if (this.yogi == null){
            throw new IllegalStateException("No Persistence Manager");
        }
        this.yogi.serialize(k, this.put(k, null));
    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key, Value> pm) {
        this.yogi = pm;
    }

    private class Node {
        private int entryCount;
        private Entry<?, ?>[] entries;

        private Node(int k) {
            this.entryCount = k;
            this.entries = new Entry[MAX];
        }
        private Node(){
            this.entryCount = 1;
            this.entries = new Entry[MAX];
            this.entries[0] = new Entry<>(null, null, null);
        }
    }

    //internal nodes: only use key and child
    //external nodes: only use key and value
    private class Entry<Key extends Comparable<Key>, Value> {
        private Key key;
        private Value val;
        private Node child;
        private boolean isSentinel;

        private Entry(Key key, Value val, Node child) {
            if (key != null) {
                this.key = key;
                this.val = val;
                this.child = child;
                this.isSentinel = false;
            } else {
                this.key = null;
                this.val = null;
                this.child = child;
                this.isSentinel = true;
            }
        }
    }
}