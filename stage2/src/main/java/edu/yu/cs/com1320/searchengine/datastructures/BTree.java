package edu.yu.cs.com1320.searchengine.datastructures;

import edu.yu.cs.com1320.searchengine.core.PersistenceManager;

import java.io.IOException;

public interface BTree<Key extends Comparable<Key>, Value> {
    Value get(Key k);
    Value put(Key k, Value v);
    void moveToDisk(Key k) throws IOException;
    void setPersistenceManager(PersistenceManager<Key,Value> pm);
}