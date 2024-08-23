package edu.yu.cs.com1320.searchengine.datastructures.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class HashTableImplTest {
    private HashTableImpl<Integer, String> sample;

    @BeforeEach
    void setUp() {
        sample = new HashTableImpl<>();
        sample.put(0, "Hello");
    }

    @Test
    void get() {
        assertNull(sample.get(1));
        assertEquals("Hello", sample.get(0));
    }

    @Test
    void put() {
        assertNull(sample.put(6, "World"));
        assertEquals("Hello", sample.put(0, "H"));
        assertEquals(2, sample.size());
        assertEquals(2, sample.keySet().size());
        assertEquals(2, sample.values().size());
        assertNull(sample.put(1, "W2"));
        assertEquals(3, sample.size());
        assertEquals(3, sample.keySet().size());
        assertEquals(3, sample.values().size());
        assertEquals("World", sample.get(6));
        assertEquals("W2", sample.get(1));
        assertNull(sample.get(11));
        assertEquals("World", sample.put(6, null));
        assertEquals(2, sample.size());
        assertNull(sample.get(6));
        assertEquals(2, sample.keySet().size());
        assertEquals(2, sample.values().size());
        assertEquals("W2", sample.get(1));
        assertEquals("H", sample.put(0, null));
        assertEquals(1, sample.size());
        assertNull(sample.get(0));
        assertEquals(1, sample.keySet().size());
        assertEquals(1, sample.values().size());
        assertNull(sample.get(6));
        assertEquals("W2", sample.get(1));
        assertTrue(sample.containsKey(1));
        assertFalse(sample.containsKey(0));
        assertFalse(sample.containsKey(6));
    }

    @Test
    void containsKey() {
        assertTrue(sample.containsKey(0));
        assertFalse(sample.containsKey(1));
        assertThrows(NullPointerException.class,() -> sample.containsKey(null));
    }

    @Test
    void keySet() {
        for (Integer i : sample.keySet()){
            assertEquals(0, i);
        }
    }

    @Test
    void values() {
        for (String s : sample.values()){
            assertEquals("Hello", s);
        }
    }

    @Test
    void size() {
        assertEquals(1, sample.size());
        assertEquals(1, sample.keySet().size());
        assertEquals(1, sample.values().size());
        HashTableImpl<Object, Object> blank = new HashTableImpl<>();
        assertEquals(0, blank.size());
        assertEquals(0, blank.keySet().size());
        assertEquals(0, blank.values().size());
    }

    @Test
    void rehash() {
        sample.put(1, "One");
        sample.put(2, "Two");
        sample.put(3, "Three");
        sample.put(4, "Four");
        sample.put(5, "World");
        assertEquals("Hello", sample.get(0));
        assertEquals("World", sample.get(5));
        assertEquals(6, sample.size());
        sample.put(6, "One");
        sample.put(7, "Two");
        sample.put(8, "Three");
        sample.put(9, "Four");
        sample.put(10, "Wld");
        sample.put(11, "One");
        sample.put(12, "Two");
        sample.put(13, "Three");
        sample.put(14, "Four");
        sample.put(15, "Five");
        sample.put(16, "One");
        sample.put(17, "Two");
        sample.put(18, "Three");
        sample.put(19, "Four");
        sample.put(20, ":)");
        assertEquals(21, sample.size());
        assertEquals("Hello", sample.get(0));
        assertEquals("World", sample.get(5));
        assertEquals("Wld", sample.get(10));
        assertEquals("Five", sample.get(15));
    }
}