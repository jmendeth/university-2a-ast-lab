package ast.practica2;

import ast.practica1.Channel;
import ast.practica1.CircularQueue;
import ast.protocols.tcp.TCPSegment;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements a crappy 'thread-safe', blocking channel that uses
 * test & test and set to synchronize accesses to a {@link CircularQueue}
 * structure.
 * This would work reliably if {@link CircularQueue}'s pointers
 * and buffers were marked {@code volatile}, AFAIK.
 *
 * @author Xavier Mendez
 */
public class AwaitChannel implements Channel {

    private final CircularQueue<TCPSegment> queue;
    private final AtomicBoolean locked;

    public AwaitChannel() {
        this.queue = new CircularQueue<>(15);
        this.locked = new AtomicBoolean(false);
    }

    protected void lock() {
        do {
            while (locked.get());
        } while (locked.getAndSet(true));
    }
    
    protected void unlock() {
        locked.set(false);
    }

    @Override
    public void send(TCPSegment seg) {
        while (true) {
            lock();
            try {
                queue.put(seg);
                return;
            } catch (IllegalStateException e) {
            } finally {
                unlock();
            }
        }
    }

    @Override
    public TCPSegment receive() {
        while (true) {
            lock();
            try {
                return queue.get();
            } catch (IllegalStateException e) {
            } finally {
                unlock();
            }
        }
    }

}
