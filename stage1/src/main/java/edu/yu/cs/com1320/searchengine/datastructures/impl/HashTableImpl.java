package edu.yu.cs.com1320.searchengine.datastructures.impl;

import edu.yu.cs.com1320.searchengine.datastructures.HashTable;

import java.util.*;

/**
 * Instances of HashTable should be constructed with two type parameters,
 * one for the type of the keys in the table and one for the type of the values
 *
 * @param <Key>
 * @param <Value>
 */
public class HashTableImpl<Key, Value> implements HashTable <Key, Value> {

    private Entry <?, ?> [] table;
    private int tableSize;

    public HashTableImpl() {
        this.table = new Entry [5];
    }

    private int hashFunction (Key key) {
        return (key.hashCode() & 0x7fffffff) % this.table.length;
    }

    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    @Override
    public Value get(Key k) {
        Entry test = this.table[this.hashFunction(k)];
        while (true) {
            if (test == null) {
                return null;
            }
            if ((test.key == null && k == null) || (test.key != null && k != null && test.key.equals(k))) {
                return (Value) test.value;
            }
            test = test.shpeter;
        }
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store
     *          To delete an entry, put a null value.
     * @return if the key was already present in the HashTable, return the previous value stored for the key.
     * If the key was not already present, return null.
     */
    @Override
    public Value put(Key k, Value v) {
        putNullKeyDetector(k); // Note - shouldn't be possible for k to be null
        Entry<Key, Value> test = (Entry<Key, Value>) this.table[this.hashFunction(k)];
        Entry<Key, Value> prev = null;
        while (true) {
            if (test == null) {
                if (v != null) {
                    if (this.size()/4 >= this.table.length){
                        rehash();
                    }
                    if (prev == null) {
                        this.table[this.hashFunction(k)] = new Entry<>(k, v);
                    } else {
                        prev.shpeter = new Entry<>(k, v);
                    }
                    tableSize++;
                }
                return null;
            }
            //assumes both test.key and k aren't null
            if (test.key.equals(k)) {
                if (v != null) {
                    return test.setValue(v);
                }
                tableSize--;
                return this.putDeleter(k, test, prev);
            }
            prev = test;
            test = test.shpeter;
        }
    }

    private void putNullKeyDetector(Key k){
        if (k == null){
            throw new IllegalArgumentException("Null Key is Invalid");
        }
    }

    private Value putDeleter(Key k, Entry<Key, Value> test, Entry<Key, Value> prev){
        Value oldVal = test.value;
        if (prev == null) {
            if (test.shpeter != null) {
                this.table[this.hashFunction(k)] = test.shpeter;
            } else {
                this.table[this.hashFunction(k)] = null;
            }
        } else {
            if (test.shpeter != null) {
                prev.shpeter = test.shpeter;
            } else {
                prev.shpeter = null;
            }
        }
        return oldVal;
    }

    /**
     * @param key the key whose presence in the hashtable we are inquiring about
     * @return true if the given key is present in the hashtable as a key, false if not
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public boolean containsKey(Key key) {
        if (key == null) {
            throw new NullPointerException();
        }
        Entry<?, ?> test = this.table[this.hashFunction(key)];
        while (true) {
            // assumes test.key can't be null
            if (test == null) {
                return false;
            }
            if (test.key.equals(key)) {
                return true;
            }
            test = test.shpeter;
        }
    }

    /**
     * @return an unmodifiable set of all the keys in this HashTable
     * @see Collections#unmodifiableSet(Set)
     */
    @Override
    public Set<Key> keySet() {
        HashSet <Key> keys = new HashSet<>();
        for (Entry<?, ?> i : this.table){
            while (i != null){
                keys.add((Key) i.key);
                i = i.shpeter;
            }
        }
        return Collections.unmodifiableSet(keys);
    }

    /**
     * @return an unmodifiable collection of all the values in this HashTable
     * @see Collections#unmodifiableCollection(Collection)
     */
    @Override
    public Collection<Value> values() {
        ArrayList<Value> valuesList = new ArrayList<>();
        for (Entry<?, ?> i : table){
            while (i != null){
                valuesList.add((Value) i.value);
                i = i.shpeter;
            }
        }
        return Collections.unmodifiableCollection(valuesList);
    }

    /**
     * @return how many entries there currently are in the HashTable
     */
    @Override
    public int size() {
        return this.tableSize;
    }

    private class Entry <Key, Value> {
        private final Key key;
        private Value value;
        private Entry <Key, Value> shpeter;

        private Entry (Key key, Value value) {
            this.key = key;
            this.value = value;
        }

        /**
         * @param value new value to be stored in this Entry
         * @return the old value stored in the Entry
         */
        private Value setValue (Value value) {
            Value temp = this.value;
            this.value = value;
            return temp;
        }
    }

    private void rehash(){
        Entry<?,?>[] temp = this.table;
        this.table = new Entry[this.table.length*2];
        for (Entry<?,?> e : temp){
            while (e != null) {
                Entry<Key, Value> test = (Entry<Key, Value>) this.table[this.hashFunction((Key) e.key)];
                Entry<Key, Value> prev = null;
                while (test != null){
                    prev = test;
                    test = test.shpeter;
                }
                if (prev == null) {
                    this.table[this.hashFunction((Key) e.key)] = e;
                } else {
                    prev.shpeter = (Entry<Key, Value>) e;
                }
                Entry<Key, Value> next = (Entry<Key, Value>) e.shpeter;
                e.shpeter = null;
                e = next;
            }
        }
    }
}
