package edu.yu.cs.com1320.searchengine.datastructures.impl;

import edu.yu.cs.com1320.searchengine.core.Document;
import edu.yu.cs.com1320.searchengine.core.impl.DocumentImpl;
import edu.yu.cs.com1320.searchengine.core.impl.DocumentPersistenceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

class BTreeImplTest {
    private BTreeImpl<URI, Document> bTree;
    DocumentImpl sampleDoc;
    DocumentImpl sampleImg;

    @BeforeEach
    void setUp() {
        bTree = new BTreeImpl<URI, Document>();
        sampleDoc = new DocumentImpl(URI.create("HW"), "Hello World", null);
        sampleImg = new DocumentImpl(URI.create("Bla"), new byte[0xd]);
        bTree.setPersistenceManager(new DocumentPersistenceManager(null));
    }

    // @Test
    // void get() {
    // }

    // @Test
    // void put() {
    // }

    // @Test
    // void moveToDisk() {
    // }

    // @Test
    // void setPersistenceManager() {
    // }
}