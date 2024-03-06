package com.nico.integerstack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class StackTest {

    private MyStack<Integer> stack;

    @BeforeEach
    void init() {
        stack = new MyStack<>();
    }

    @Test
    @DisplayName("when creating a new stack, then it's empty")
    void test01() {
        assertThat(stack.isEmpty())
                .isTrue();
    }

    @Test
    @DisplayName("when pushing an item, then stack is no more empty")
    void test02() {
        stack.push(0);

        assertThat(stack.isEmpty())
                .isFalse();
    }

    @Test
    @DisplayName("when pushing an item and poping it, then stack should be empty")
    void test03() {
        stack.push(0);
        stack.pop();

        assertThat(stack.isEmpty())
                .isTrue();
    }

    @Test
    @DisplayName("when pushing two items, then stack size should be : 2")
    void test04() {
        stack.push(0);
        stack.push(0);

        assertThat(stack.getSize())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("when stack is empty, then size should be : 0")
    void test05() {
        stack.push(0);
        stack.pop();

        assertThat(stack.isEmpty())
                .isTrue();
        assertThat(stack.getSize())
                .isZero();
    }

    @Test
    @DisplayName("when popping on an empty stack, then an exception should be thrown")
    void test06() {
        assertThatExceptionOfType(EmptyStackException.class)
                .isThrownBy(stack::pop);
    }

    @Test
    @DisplayName("when push item : 99 on stack, then popped value should be : 99")
    void test07() {
        int pushedValue = 99;
        stack.push(pushedValue);

        int poppedValue = stack.pop();

        assertThat(poppedValue)
                .isEqualTo(pushedValue);
    }

    @Test
    @DisplayName("when 99 is pushed followed by a pop and 98 is pushed after, then popped value should be : 98")
    void test08() {
        int pushedValue = 99;
        stack.push(pushedValue);
        stack.pop();
        pushedValue = 98;
        stack.push(pushedValue);
        int poppedValue = stack.pop();

        assertThat(poppedValue)
                .isEqualTo(pushedValue);
    }

    @Test
    @DisplayName("when pushing 98 and 99, then first pop should return 99 and second pop 98")
    void test09() {
        stack.push(98);
        stack.push(99);

        assertThat(stack.pop())
                .isEqualTo(99);
        assertThat(stack.pop())
                .isEqualTo(98);
    }
}
