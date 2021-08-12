package Chapter2.item7;

import java.util.Arrays;
import java.util.EmptyStackException;

public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object object) {
        ensureCapacity();
        elements[size++] = object;
    }

    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }

        Object value = this.elements[--size];
        this.elements[size] = null;
        return value;
    }

    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}
