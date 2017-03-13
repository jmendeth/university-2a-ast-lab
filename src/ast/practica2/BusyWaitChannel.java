package ast.practica2;

import ast.practica1.Channel;
import ast.practica1.CircularQueue;
import ast.protocols.tcp.TCPSegment;

/**
 * Implements a crappy 'thread-safe', blocking channel that uses busy-wait
 * on a {@link CircularQueue} structure.
 * This would work reliably if {@link CircularQueue}'s pointers
 * and buffers were marked {@code volatile}, AFAIK.
 *
 * @author Xavier Mendez
 */
public class BusyWaitChannel implements Channel {

    private final CircularQueue<TCPSegment> queue;

    public BusyWaitChannel() {
        this.queue = new CircularQueue<>(15);
    }

    @Override
    public void send(TCPSegment seg) {
        while (true) {
            while (queue.full());
            try {
                queue.put(seg);
                return;
            } catch (IllegalStateException e) {}
        }
    }

    @Override
    public TCPSegment receive() {
        while (true) {
            while (queue.empty());
            try {
                return queue.get();
            } catch (IllegalStateException e) {}
        }
    }

}
