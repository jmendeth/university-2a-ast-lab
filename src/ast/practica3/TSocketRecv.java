package ast.practica3;

import ast.practica1.CircularQueue;
import ast.protocols.tcp.TCPSegment;

/**
 * Implements the receiving interface of the transport layer.
 *
 * @author Xavier Mendez
 */
public class TSocketRecv extends TSocketBase {

    private TCPSegment segment;
    private final CircularQueue<TCPSegment> rcvQueue;
    private final Thread thread;

    /**
     * Construct and establish a new connection over the passed channel.
     * @param channel the communication channel to use
     */
    public TSocketRecv(Channel channel) {
        super(channel);
        this.rcvQueue = new CircularQueue<>(20);
        this.thread = new Thread(new ReceiverTask());
        thread.start();
    }

    /**
     * Receives data from the peer.
     *
     * @param data buffer where received data is to be written to
     * @param offset offset of first byte to write to
     * @param length maximum count of bytes to receive
     * @return bytes actually received, or -1 on EOF
     */
    public int receiveData(byte[] data, int offset, int length) {
        lk.lock();
        try {
            if (rcvQueue.empty())
                appCV.await();
            return consumeSegment(data, offset, length);
        } catch (InterruptedException ex) {
            log.error(ex); // FIXME: shouldn't be swallowed
            return 0;
        } finally {
            lk.unlock();
        }
    }

    protected int consumeSegment(byte[] data, int offset, int length) {
        if (segment == null) {
            try {
                segment = rcvQueue.get();
            } catch (IllegalStateException e) {
                return 0;
            }
        }
        
        // Check for EOF
        if (segment.getData() == null && segment.getDataLength() == 0)
            return -1;
        
        // Copy data
        if (length > segment.getDataLength())
            length = segment.getDataLength();
        System.arraycopy(segment.getData(), segment.getDataOffset(), data, offset, length);
        
        // Update (and possibly drop) segment
        segment.setData(segment.getData(), segment.getDataOffset() + length,
                segment.getDataLength() - length);
        if (segment.getDataLength() == 0)
            segment = null;
        
        return length;
    }

    protected void processReceivedSegment(TCPSegment segment) {
        lk.lock();
        try {
            if (!rcvQueue.full())
                rcvQueue.put(segment);
            appCV.signal();
        } finally {
            lk.unlock();
        }
    }

    class ReceiverTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                TCPSegment segment = channel.receive();
                processReceivedSegment(segment);
            }
        }
    }

}
