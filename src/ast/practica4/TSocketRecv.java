package ast.practica4;

import ast.protocols.tcp.TCPSegment;
import ast.practica1.CircularQueue;

/**
 * Implements the receiving interface of the transport layer.
 *
 * @author Xavier Mendez
 */
public class TSocketRecv extends TSocketBase {

    private TCPSegment segment;
    private final CircularQueue<TCPSegment> rcvQueue;

    /**
     * Construct and establish a new connection over the passed channel.
     */
    protected TSocketRecv(ProtocolRecv p, int localPort, int remotePort) {
        super(p, localPort, remotePort);
        rcvQueue = new CircularQueue<>(20);
    }

    /**
     * Receives data from the peer.
     *
     * @param buf buffer where received data is to be written to
     * @param offset offset of first byte to write to
     * @param length maximum count of bytes to receive
     * @return bytes actually received, or -1 on EOF
     */
    public int receiveData(byte[] buf, int offset, int length) {
        lk.lock();
        try {
            if (rcvQueue.empty())
                appCV.await();
            return consumeSegment(buf, offset, length);
        } catch (InterruptedException ex) {
            log.error(ex); // FIXME: shouldn't be swallowed
            return 0;
        } finally {
            lk.unlock();
        }
    }

    protected int consumeSegment(byte[] buf, int offset, int length) {
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
        System.arraycopy(segment.getData(), segment.getDataOffset(), buf, offset, length);
        
        // Update (and possibly drop) segment
        segment.setData(segment.getData(), segment.getDataOffset() + length,
                segment.getDataLength() - length);
        if (segment.getDataLength() == 0)
            segment = null;
        
        return length;
    }

    /**
     * Segment arrival.
     * @param rseg segment of received packet
     */
    protected void processReceivedSegment(TCPSegment rseg) {
        lk.lock();
        try {
            if (!rcvQueue.full())
                rcvQueue.put(rseg);
            appCV.signal();
        } finally {
            lk.unlock();
        }
    }

}
