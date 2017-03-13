package ast.practica1;

import ast.protocols.tcp.TCPSegment;

/**
 * Implements a thread unsafe, non-blocking channel backed by
 * {@link CircularQueue}.
 * @author Xavier Mendez
 */
public class QueueChannel implements Channel {

    private final CircularQueue<TCPSegment> queue;

    public QueueChannel(int n) {
        this.queue = new CircularQueue<>(n);
    }
    
    public QueueChannel() {
        this(15);
    }

    @Override
    public void send(TCPSegment seg) {
        queue.put(seg);
    }

    @Override
    public TCPSegment receive() {
        return queue.get();
    }

}
