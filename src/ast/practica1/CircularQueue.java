package ast.practica1;

import static ast.practica1.Util.*;
import ast.util.Queue;
import java.util.Iterator;

/**
 * Implements a (non thread-safe) circular queue.
 * @param <T> Element type
 *
 * @author Xavier Mendez
 */
public class CircularQueue<T> implements Queue<T> {

    private final T[] buffer;
    private int head, tail;

    public CircularQueue(int n) {
        this.buffer = (T[]) new Object[n+1];
        this.head = this.tail = 0;
    }

    // Basic queries

    @Override
    public int size() {
        return (buffer.length + tail - head) % buffer.length;
    }

    @Override
    public boolean hasFree(int n) {
        if (n < 0) throw new IllegalArgumentException("Invalid n passed");
        return (size() + n) < buffer.length;
    }

    @Override
    public boolean empty() {
        return tail == head;
    }

    @Override
    public boolean full() {
        return (tail + 1) % buffer.length == head;
    }

    // Peeking & iterating

    @Override
    public T peekFirst() {
        if (empty()) return null;
        return buffer[head];
    }

    @Override
    public T peekLast() {
        if (empty()) return null;
        return buffer[(buffer.length + tail - 1) % buffer.length];
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    // Insert / remove elements

    @Override
    public T get() {
        if (empty()) throw new IllegalStateException("Queue is empty");
        T result = buffer[head];
        head = (head + 1) % buffer.length;
        return result;
    }

    @Override
    public void put(T e) {
        if (full()) throw new IllegalStateException("Queue is full");
        buffer[tail] = e;
        tail = (tail + 1) % buffer.length;
    }

    public static void main(String[] args) {
        final CircularQueue<String> queue = new CircularQueue<>(3);

        // Initial state
        expect(queue.size(), 0);
        expect(queue.empty());
        expect(!queue.full());
        expect(queue.hasFree(3));
        expect(!queue.hasFree(4));
        expect(queue.peekFirst(), null);
        expect(queue.peekLast(), null);

        // Normal get / put
        queue.put("test");
        expect(queue.size(), 1);
        expect(!queue.empty());
        expect(!queue.full());
        expect(queue.hasFree(2));
        expect(!queue.hasFree(3));
        expect(queue.peekFirst(), "test");
        expect(queue.peekLast(), "test");
        expect(queue.get(), "test");

        // Overflow queue
        queue.put("one");
        queue.put("two");
        queue.put("three");
        try {
            queue.put("four");
            throw new AssertionError();
        } catch (IllegalStateException ex) {
            expect(ex.getMessage(), "Queue is full");
        }

        expect(queue.size(), 3);
        expect(queue.full());
        expect(!queue.empty());
        expect(queue.hasFree(0));
        expect(!queue.hasFree(1));
        expect(!queue.hasFree(3));

        // Underflow queue
        expect(queue.get(), "three");
        expect(queue.get(), "two");
        expect(queue.get(), "one");
        try {
            queue.get();
            throw new AssertionError();
        } catch (IllegalStateException ex) {
            expect(ex.getMessage(), "Queue is full");
        }

        expect(queue.size(), 0);
        expect(queue.empty());
        expect(!queue.full());
        expect(queue.hasFree(3));
        expect(!queue.hasFree(4));
        expect(queue.peekFirst(), null);
        expect(queue.peekLast(), null);

        // Null elements
        queue.put(null);
        queue.put(null);
        expect(queue.get(), null);
        expect(queue.size(), 1);
        expect(!queue.empty());
        expect(!queue.full());
        expect(queue.hasFree(2));
        expect(!queue.hasFree(3));
        expect(queue.peekFirst(), null);
        expect(queue.peekLast(), null);
    }

}
