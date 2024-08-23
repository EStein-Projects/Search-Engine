package edu.yu.cs.com1320.searchengine.datastructures.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrieImplTest {

    private TrieImpl<Integer> sample;
    private HashSet<Integer> numbers;
    @BeforeEach
    void setUp() {
        sample = new TrieImpl<>();
        numbers = new HashSet<>();
        sample.put("Hello", 1);
        sample.put("World", 2);
        numbers.add(1);
        numbers.add(2);
    }

    @Test
    void put() {
        assertThrows(IllegalArgumentException.class, () ->
                sample.put(null, 0));
        sample.put("Hi", null);
        sample.put(" ", 0);
        sample.put("Hello World", 2);
        assertEquals(Collections.emptySet(), sample.get("Hi"));
        assertEquals(Collections.emptySet(), sample.get(""));
        assertEquals(Collections.emptySet(), sample.get(" "));
        assertEquals(Collections.emptySet(), sample.get("Hello! World"));
        numbers.remove(1);
        assertEquals(numbers, sample.get("HelloWorld"));
    }

    @Test
    void get() {
        assertThrows(IllegalArgumentException.class, () ->
                sample.get(null));
        assertEquals(Collections.emptySet(), sample.get("H"));
        assertEquals(Collections.emptySet(), sample.get("Hello!"));
        assertEquals(Collections.emptySet(), sample.get("Hello "));
        numbers.remove(2);
        assertEquals(numbers, sample.get("Hello"));
    }

    @Test
    void getSorted() {
        assertEquals(Collections.emptyList(), sample.getSorted("", (a, b) -> {
            if (a < b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        }));
        assertEquals(Collections.emptyList(), sample.getSorted(" ", (a, b) -> {
            if (a < b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        }));
        assertThrows(IllegalArgumentException.class, () ->
                sample.getSorted("Hello",null));
        assertThrows(IllegalArgumentException.class, () -> {
            sample.getSorted(null, (a, b) -> {
                if (a < b) {
                    return -1;
                } else if (a == b) {
                    return 0;
                } else {
                    return 1;
                }
            });
        });
        assertEquals(Collections.emptyList(), sample.getSorted("H", (a, b) -> {
            if (a < b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        }));
        assertEquals(Collections.emptyList(), sample.getSorted("Hello!", (a, b) -> {
            if (a < b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        }));
        assertEquals(Collections.emptyList(), sample.getSorted("Hello ", (a, b) -> {
            if (a < b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        }));
        sample.put("Hello", 2);
        List<Integer> nums = sample.getSorted("Hello", (a, b) -> {
            if (a > b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        });
        assertEquals(2, nums.get(0));
        assertEquals(1, nums.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> nums.get(2));
    }

    @Test
    void getAllWithPrefixSorted() {
        assertEquals(Collections.emptyList(), sample.getAllWithPrefixSorted("", (a, b) -> {
            if (a < b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        }));
        assertEquals(Collections.emptyList(), sample.getAllWithPrefixSorted(" ", (a, b) -> {
            if (a < b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        }));
        assertThrows(IllegalArgumentException.class, () ->
                sample.getAllWithPrefixSorted("Hello",null));
        assertThrows(IllegalArgumentException.class, () -> {
            sample.getAllWithPrefixSorted(null, (a, b) -> {
                if (a < b) {
                    return -1;
                } else if (a == b) {
                    return 0;
                } else {
                    return 1;
                }
            });
        });
        assertEquals(Collections.emptyList(), sample.getAllWithPrefixSorted("Hello!", (a, b) -> {
            if (a < b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        }));
        assertEquals(Collections.emptyList(), sample.getAllWithPrefixSorted("Hello ", (a, b) -> {
            if (a < b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        }));
        sample.put("Hello", 2);
        List<Integer> nums1 = sample.getAllWithPrefixSorted("Hello", (a, b) -> {
            if (a > b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        });
        assertEquals(2, nums1.get(0));
        assertEquals(1, nums1.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> nums1.get(2));
        List<Integer> nums2 = sample.getAllWithPrefixSorted("H", (a, b) -> {
            if (a > b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        });
        assertEquals(2, nums2.get(0));
        assertEquals(1, nums2.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> nums2.get(2));
        sample.put("H", 2);
        sample.put("H", 3);
        List<Integer> nums3 = sample.getAllWithPrefixSorted("H", (a, b) -> {
            if (a > b) {
                return -1;
            } else if (a == b) {
                return 0;
            } else {
                return 1;
            }
        });
        assertEquals(3, nums3.get(0));
        assertEquals(2, nums3.get(1));
        assertEquals(1, nums3.get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> nums3.get(3));
    }

    @Test
    void deleteAll() {
        assertThrows(IllegalArgumentException.class, () ->
                sample.deleteAll(null));
        assertEquals(Collections.emptySet(), sample.deleteAll("H"));
        assertEquals(Collections.emptySet(), sample.deleteAll("Hello!"));
        assertEquals(Collections.emptySet(), sample.deleteAll("Hello "));
        sample.put("Hello", 2);
        assertEquals(numbers, sample.deleteAll("Hello"));
        assertEquals(Collections.emptySet(), sample.deleteAll("Hello"));
    }

    @Test
    void deleteAllWithPrefix() {
        assertThrows(IllegalArgumentException.class, () ->
                sample.deleteAllWithPrefix(null));
        assertEquals(Collections.emptySet(), sample.deleteAllWithPrefix(""));
        assertEquals(Collections.emptySet(), sample.deleteAllWithPrefix("Hello!"));
        assertEquals(Collections.emptySet(), sample.deleteAllWithPrefix("Hello "));
        sample.put("Hello", 2);
        assertEquals(numbers, sample.deleteAllWithPrefix("Hello"));
        assertEquals(Collections.emptySet(), sample.deleteAllWithPrefix("Hello"));
        sample.put("Hello", 1);
        sample.put("Hello", 2);
        sample.put("H", 2);
        sample.put("H", 3);
        numbers.add(3);
        assertEquals(numbers, sample.deleteAllWithPrefix("H"));
        assertEquals(Collections.emptySet(), sample.deleteAllWithPrefix("H"));
    }

    @Test
    void delete() {
        assertThrows(IllegalArgumentException.class, () ->
                sample.delete(null, 0));
        assertNull(sample.delete("", 0));
        assertNull(sample.delete("Hello", null));
        assertNull(sample.delete("H", 1));
        assertNull(sample.delete("Hello!", 1));
        assertNull(sample.delete("Hello ", 1));
        assertNull(sample.delete("Hello", 2));
        assertEquals(1, sample.delete("Hello", 1));
        assertNull(sample.delete("Hello", 1));
    }
}