package com.nico.integerstack;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

public class MyStack<I extends Number> {

    private int size;
    private final List<I> elements = new ArrayList<>();

    public boolean isEmpty() {
        return size <= 0;
    }

    public void push(I element) {
        this.elements.add(size++, element);
    }

    public I pop() {
        if(isEmpty()) {
            throw new EmptyStackException();
        }
        return this.elements.get(--size);
    }

    public int getSize() {
        return size;
    }
}
