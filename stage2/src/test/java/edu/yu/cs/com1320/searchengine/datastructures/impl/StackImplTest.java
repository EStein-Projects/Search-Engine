package edu.yu.cs.com1320.searchengine.datastructures.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StackImplTest {

    private StackImpl<Integer> numbers;

    @BeforeEach
    void setUp() {
        numbers = new StackImpl<>();
    }

    @Test
    void push() {
        numbers.push(0);
        numbers.push(1);
        numbers.push(2);
        numbers.push(3);
        numbers.push(4);
        numbers.push(5);
        numbers.push(6);
        numbers.push(7);
        numbers.push(8);
        numbers.push(9);
        numbers.push(10);
        numbers.push(11);
        numbers.push(12);
        numbers.push(13);
        numbers.push(14);
    }

    // @Test
    // void pop() {
    // }

    // @Test
    // void peek() {
    // }

    // @Test
    // void size() {
    // }
}