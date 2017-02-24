/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.practica1;

import ast.util.Queue;
import java.util.Iterator;

/**
 *
 * @author xavier
 */
public class CircularQueue<T> implements Queue<T> {

    public CircularQueue(int n) {
    }
    
    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasFree(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean empty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean full() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T peekFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T peekLast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T get() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void put(T e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static void main(String[] args) {
        final CircularQueue<String> queue = new CircularQueue<>(4);
        
        // Initial state
        //TODO: change assert to _assert
        _assert(queue.size(), 0);
        //TODO
        
        // Normal get / put
        queue.put("test");
        _assert(queue.get(), "test");
        
        // Overflow queue
        queue.put("one");
        queue.put("two");
        queue.put("three");
        try {
            queue.put("four");
            throw new AssertionError();
        } catch (IllegalStateException ex) {
            _assert(ex.getMessage(), "Queue is full");
        }
        
        // Underflow queue
        _assert(queue.get(), "three");
        _assert(queue.get(), "two");
        _assert(queue.get(), "one");
        try {
            queue.get();
            throw new AssertionError();
        } catch (IllegalStateException ex) {
            _assert(ex.getMessage(), "Queue is full");
        }
        
        // Null elements
        queue.put(null);
        queue.put(null);
        _assert(queue.get(), null);
        _assert(queue.size(), 1);
    }
    
}
