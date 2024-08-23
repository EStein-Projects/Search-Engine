package edu.yu.cs.com1320.searchengine.datastructures.impl;

import edu.yu.cs.com1320.searchengine.datastructures.Stack;

/**
 * @param <T>
 */
public class StackImpl<T> implements Stack<T> {
    private T[] frames;
    private int stackLen;

    public StackImpl(){
        this.frames = (T[]) new Object[10];
        this.stackLen = 0;
    }

    /**
     * @param element object to add to the Stack
     */
    @Override
    public void push(T element) {
        if (element == null){
            throw new IllegalArgumentException("Null Element");
        }
        if (this.stackLen < this.frames.length){
            this.frames[this.stackLen] = element;
            this.stackLen++;
            return;
        }
        this.doubler();
        this.frames[this.stackLen] = element;
        this.stackLen++;
    }

    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    @Override
    public T pop() {
        if (this.stackLen == 0) {
            return null;
        } else {
            T temp = this.frames[this.stackLen-1];
            this.frames[this.stackLen-1] = null;
            this.stackLen--;
            return temp;
        }
    }

    /**
     * @return the element at the top of the stack without removing it
     */
    @Override
    public T peek() {
        if (this.stackLen == 0) {
            return null;
        } else {
            return this.frames[this.stackLen - 1];
        }
    }

    /**
     * @return how many elements are currently in the stack
     */
    @Override
    public int size() {
        return this.stackLen;
    }

    /**
     * doubles the length of the array
     * void bec not using an outside copy method
     */
    private void doubler(){
        T[] temp = (T[]) new Object[this.frames.length*2];
        for (int i = 0; i < this.frames.length; i++){
            temp[i] = this.frames[i];
        }
        this.frames = temp;
    }
}
