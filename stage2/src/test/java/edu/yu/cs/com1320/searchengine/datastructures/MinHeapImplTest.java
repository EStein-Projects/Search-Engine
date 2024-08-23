package edu.yu.cs.com1320.searchengine.datastructures;

import edu.yu.cs.com1320.searchengine.datastructures.impl.MinHeapImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinHeapImplTest {
    MinHeap<Spl> sample = new MinHeapImpl<>();
    Spl one = new Spl(1);
    Spl three = new Spl(3);
    Spl five = new Spl(5);

    class Spl implements Comparable<Spl> {
        Integer value;
        Spl(int v){
            this.value = v;
        }
        @Override
        public int compareTo(Spl o) {
            return this.value.compareTo(o.value);
        }
    }

    @BeforeEach
    void setUp() {
        sample.insert(one);
        sample.insert(five);
        sample.insert(three);
    }

    @Test
    void reHeapify() {
        sample.reHeapify(one);
        sample.reHeapify(five);
        sample.reHeapify(three);
        assertEquals(1, sample.getArrayIndex(one));
        one.value = 0;
        sample.reHeapify(one);
        sample.reHeapify(five);
        sample.reHeapify(three);
        assertEquals(1, sample.getArrayIndex(one));
        one.value = 3;
        five.value = 3;
        sample.reHeapify(one);
        sample.reHeapify(five);
        sample.reHeapify(three);
        assertEquals(1, sample.getArrayIndex(one));
        one.value = 5;
        five.value = 1;
        sample.reHeapify(one);
        sample.reHeapify(five);
        sample.reHeapify(three);
        assertEquals(1, sample.getArrayIndex(five));
        one.value = 1;
        sample.reHeapify(one);
        sample.reHeapify(five);
        sample.reHeapify(three);
        assertEquals(1, sample.getArrayIndex(five));
        assertEquals(five, sample.peek());
    }

    @Test
    void getArrayIndex() {
        assertEquals(1, sample.getArrayIndex(one));
        assertEquals(3, sample.getArrayIndex(three));
        assertEquals(2, sample.getArrayIndex(five));
    }

    @Test
    void doubleArraySize() {
        assertEquals(3, sample.count);
        Comparable[] sampEls = sample.elements;
        assertEquals(8, sampEls.length);
        sample.insert(new Spl(2));
        sample.insert(new Spl(4));
        sample.insert(new Spl(6));
        Spl seven = new Spl(7);
        Spl eight = new Spl(8);
        sample.insert(seven);
        sampEls = sample.elements;
        assertEquals(8, sampEls.length);
        sample.insert(eight);
        sampEls = sample.elements;
        assertEquals(15, sampEls.length);
        assertEquals(8, sample.count);
        assertEquals(15, sample.getArrayIndex(seven) + sample.getArrayIndex(eight));
    }
}