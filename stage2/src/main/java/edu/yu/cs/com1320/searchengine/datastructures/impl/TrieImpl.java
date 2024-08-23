package edu.yu.cs.com1320.searchengine.datastructures.impl;

import edu.yu.cs.com1320.searchengine.datastructures.Trie;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {
    private static final int alphabetSize = 256; // extended ASCII
    private Node<Value> root; // root of trie

    private static class Node<Value> {
        private HashSet<Value> vals = new HashSet<>();
        private Node<?>[] links = new Node<?>[alphabetSize];
    }

    private boolean validWord(String key){
        if (key == null){
            throw new IllegalArgumentException("Invalid Word");
        }
        return !key.isBlank();
    }

    /**
     * add the given value at the given key
     *
     * @param key
     * @param val
     */
    @Override
    public void put(String key, Value val) {
        if (validWord(key) && val != null) {
            String checker = "";
            for (int c = 0; c < key.length(); c++){
                if (Character.isLetterOrDigit(key.charAt(c))){
                    checker += key.charAt(c);
                }
            }
            this.root = put(this.root, checker, val, 0);
        }
    }

    private Node<Value> put(Node<Value> x, String key, Value val, int i) {
        //create a new node
        if (x == null) {
            x = new Node<>();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (i == key.length()) {
            x.vals.add(val);
        } else {
            //proceed to the next node in the chain of nodes that
            //forms the desired key
            char c = key.charAt(i);
            x.links[c] = this.put((Node<Value>) x.links[c], key, val, i + 1);
        }
        return x;
    }

    /**
     * get all exact matches for the given key.
     * Search is CASE SENSITIVE.
     *
     * @param key
     * @return a Set of matching Values. Empty set if no matches.
     */
    @Override
    public Set<Value> get(String key) {
        if (!validWord(key)){
            return Collections.emptySet();
        }
        Node<Value> x = this.get(this.root, key, 0);
        if (x == null) {
            return Collections.emptySet();
        }
        return x.vals;
    }

    private Node<Value> get(Node<Value> x, String key, int i) {
        //link was null - return null, indicating a miss
        if (x == null) {
            return null;
        }
        //we've reached the last node in the key,
        //return the node
        if (i == key.length()) {
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(i);
        return this.get((Node<Value>) x.links[c], key, i + 1);
    }

    /**
     * Get all exact matches for the given key, sorted in descending order, where "descending" is defined by the comparator.
     * NOTE FOR COM1320 PROJECT: FOR PURPOSES OF A *KEYWORD* SEARCH, THE COMPARATOR SHOULD DEFINE ORDER AS HOW MANY TIMES THE KEYWORD APPEARS IN THE DOCUMENT.
     * Search is CASE SENSITIVE.
     *
     * @param key
     * @param comparator used to sort values
     * @return a List of matching Values. Empty List if no matches.
     */
    @Override
    public List<Value> getSorted(String key, Comparator<Value> comparator) {
        if (comparator == null){
            throw new IllegalArgumentException("Invalid Comparator");
        }
        if (!validWord(key)){
            return Collections.emptyList();
        }
        Node<Value> x = this.get(this.root, key, 0);
        if (x == null){
            return Collections.emptyList();
        }
        ArrayList<Value> docs = new ArrayList<>(x.vals);
        Collections.sort(docs, comparator);
        return docs;
    }

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order, where "descending" is defined by the comparator.
     * NOTE FOR COM1320 PROJECT: FOR PURPOSES OF A *KEYWORD* SEARCH, THE COMPARATOR SHOULD DEFINE ORDER AS HOW MANY TIMES THE KEYWORD APPEARS IN THE DOCUMENT.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE SENSITIVE.
     *
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order. Empty List if no matches.
     */
    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        if (!validWord(prefix)){
            return Collections.emptyList();
        }
        if (comparator == null){
            throw new IllegalArgumentException("Invalid Comparator");
        }
        Node<Value> x = this.get(this.root, prefix, 0);
        if (x == null) {
            return Collections.emptyList();
        }
        ArrayList<Value> docs = new ArrayList<>(this.getPrefixValues(x));
        Collections.sort(docs, comparator);
        return docs;
    }

    private Set<Value> getPrefixValues(Node<Value> x) {
        HashSet<Value> values = new HashSet<>(x.vals);
        //visit each non-null child/link
        for (char c = 0; c < alphabetSize; c++) {
            if(x.links[c]!=null){
                values.addAll(getPrefixValues((Node<Value>) x.links[c]));
            }
        }
        return values;
    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE SENSITIVE.
     *
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        if (!validWord(prefix)){
            return Collections.emptySet();
        }
        Node<Value> x = this.get(this.root, prefix, 0);
        if (x == null) {
            return Collections.emptySet();
        }
        HashSet<Value> lostValues = (HashSet<Value>) this.getPrefixValues(x);
        this.root = deleteNode(this.root, prefix, 0);
        return lostValues;
    }

    private Node<Value> deleteNode(Node<Value> x, String key, int i) {
        //if at the node, sets link to null in parent, thereby deleting it
        //we're not at the node to del yet
        if (i != key.length()) {
            //so continue down the trie to the target node
            char c = key.charAt(i);
            x.links[c] = this.deleteNode((Node<Value>) x.links[c], key, i + 1);
            //parent has a val – do nothing, return the node
            if (!x.vals.isEmpty()) {
                return x;
            }
            //remove subtrie rooted at x if it is completely empty
            for (int ch = 0; ch <alphabetSize; ch++) {
                if (x.links[ch] != null) {
                    return x; //not empty
                }
            }
            //empty - set this link to null in the parent
        }
        //set this link to null in the parent
        return null;
    }

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     *
     * @param key
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAll(String key) {
        if (!validWord(key)){
            return Collections.emptySet();
        }
        Set<Value> lostValues = this.get(key);
        this.root = deleteAll(this.root, key, 0);
        return lostValues;
    }

    private Node<Value> deleteAll(Node<Value> x, String key, int i) {
        if (x == null) {
            return null;
        }
        //we're at the node to del - set the val to empty
        if (i == key.length()) {
            x.vals = new HashSet<>();
        } else {
            //continue down the trie to the target node
            char c = key.charAt(i);
            x.links[c] = this.deleteAll((Node<Value>) x.links[c], key, i + 1);
        }
        //parent has a val – do nothing, return the node
        if (!x.vals.isEmpty()) {
            return x;
        }
        //remove subtrie rooted at x if it is completely empty
        for (int c = 0; c <alphabetSize; c++) {
            if (x.links[c] != null) {
                return x; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     *
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    @Override
    public Value delete(String key, Value val) {
        if (!validWord(key) || val == null){
            return null;
        }
        Node<Value> x = this.get(this.root, key, 0);
        if (x == null || /*not possible*/x.vals.isEmpty() || !x.vals.contains(val)) {
            return null;
        }
        Value deleted = deleteValue(val, x);
        if (x.vals.isEmpty()){
            //remove subtrie rooted at x if it is completely empty
            boolean empty = true;
            for (int c = 0; c <alphabetSize; c++) {
                if (x.links[c] != null) {
                    empty = false;
                    break;
                }
            }
            if (empty){
                this.root = deleteNode(this.root, key, 0);
            }
        }
        return deleted;
    }

    private Value deleteValue(Value val, Node<Value> x) {
        Value deleted = null;
        for (Value v : x.vals) {
            if (v.equals(val)) {
                deleted = v;
                x.vals.remove(v);
                break;
            }
        }
        return deleted;
    }
}