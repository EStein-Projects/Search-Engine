package edu.yu.cs.com1320.searchengine.datastructures.impl;

import edu.yu.cs.com1320.searchengine.datastructures.MinHeap;

import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {

    public MinHeapImpl(){
        this.elements = (E[]) new Comparable[8];
    }
    @Override
    public void reHeapify(E element) {
        int loc = this.getArrayIndex(element);
        this.upHeap(loc);
        loc = this.getArrayIndex(element);
        this.downHeap(loc);
    }

    @Override
    protected int getArrayIndex(E element) {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        for (int i = 1; i < this.elements.length; i++){
            if (element.equals(this.elements[i])){
                return i;
            }
        }
        throw new NoSuchElementException("Searched element is not in the heap");
    }

    @Override
    protected void doubleArraySize() {
        E[] temp = (E[]) new Comparable[this.elements.length*2-1];
        for (int i = 1; i < this.elements.length; i++){
            temp[i] = this.elements[i];
        }
        this.elements = temp;
    }
}
