package ast.practica5;

import ast.logging.Log;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ast.protocols.tcp.TCPSegment;

/**
 * @author AST's teachers
 */
public class TSocket {

    public static Log log = Protocol.log;

    protected Protocol proto;
    protected Lock lk;
    protected Condition txReady;
    protected Condition rxReady;

    protected int localPort;
    protected int remotePort;
    
    // Sender variables:
    protected int sndMSS;       // Send maximum segment size
    protected boolean sndIsUna; // segment not yet acknowledged ?

    // Receiver variables:
    protected TCPSegment rcvSegment;
    protected int rcvSegConsumedBytes;

    /**
     * Create an endpoint bound to the given TCP ports.
     */
    protected TSocket(Protocol p, int localPort, int remotePort) {
        lk = new ReentrantLock();
        txReady = lk.newCondition();
        rxReady = lk.newCondition();
        proto = p;
        this.localPort = localPort;
        this.remotePort = remotePort;
        // init sender variables
        sndMSS = p.net.getMMS() - TCPSegment.HEADER_SIZE; // IP maximum message size - TCP header size
        sndIsUna = false;
        // init receiver variables
        rcvSegment = null;
        rcvSegConsumedBytes = 0;
    }


    // -------------  SENDER PART  ---------------
    public void sendData(byte[] data, int offset, int length) {
        lk.lock();
        try {
            log.debug("%s->sendData(length=%d)", this, length);
            while (length > 0) {
                int size = length;
                if (size > sndMSS) size = sndMSS;
                sendSegment(segmentize(data, offset, size));
                sndIsUna = true;
                while (sndIsUna) txReady.await();
                offset += size;
                length -= size;
            }
        } catch (InterruptedException ex) {
            log.error(ex);
        } finally {
            lk.unlock();
        }
    }

    protected TCPSegment segmentize(byte[] data, int offset, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(data, offset, copy, 0, length);
        TCPSegment segment = new TCPSegment();
        segment.setData(copy);
        return segment;
    }

    protected void sendSegment(TCPSegment segment) {
        log.debug("%s->sendSegment(%s)", this, segment);
        segment.setSourcePort(localPort);
        segment.setDestinationPort(remotePort);
        proto.net.send(segment);
    }


    // -------------  RECEIVER PART  ---------------
    /**
     * Places received data in buf
     */
    public int receiveData(byte[] buf, int offset, int maxlen) {
        lk.lock();
        try {
            log.debug("%s->receiveData(maxlen=%d)", this, maxlen);
            while (rcvSegment == null) rxReady.await();
            int ret = consumeSegment(buf, offset, maxlen);
            if (rcvSegment == null) sendAck();
            return ret;
        } catch (InterruptedException ex) {
            log.error(ex);
            return 0;
        } finally {
            lk.unlock();
        }
    }

    protected int consumeSegment(byte[] buf, int offset, int maxlen) {
        // assertion: rcvSegment != null && rcvSegment.getDataLength() > rcvSegConsumedBytes
        // get data from rcvSegment and copy to receiveData's buffer
        int n = rcvSegment.getDataLength() - rcvSegConsumedBytes;
        if (n > maxlen) {
            // receiveData's buffer is small. Consume a fragment of the received segment
            n = maxlen;
        }
        // n == min(length, rcvSegment.getDataLength() - rcvSegConsumedBytes)
        System.arraycopy(rcvSegment.getData(), rcvSegment.getDataOffset() + rcvSegConsumedBytes, buf, offset, n);
        rcvSegConsumedBytes += n;
        if (rcvSegConsumedBytes == rcvSegment.getDataLength()) {
            // rcvSegment is totally consumed. Remove it
            rcvSegment = null;
            rcvSegConsumedBytes = 0;
        }
        return n;
    }

    protected void sendAck() {
        TCPSegment ack = new TCPSegment();
        ack.setSourcePort(localPort);
        ack.setDestinationPort(remotePort);
        ack.setFlags(TCPSegment.ACK);
        log.debug("%s->sendAck(%s)", this, ack);
	proto.net.send(ack);
    }


    // -------------  SEGMENT ARRIVAL  -------------
    /**
     * Segment arrival.
     * @param rseg segment of received packet
     */
    protected void processReceivedSegment(TCPSegment rseg) {
        lk.lock();
        try {
            // Check ACK
            if (rseg.isAck()) {
                sndIsUna = false; //FIXME: should send and look at seg number
                txReady.signal();
                logDebugState();
            } else if (rseg.getDataLength() > 0) {
                // Process segment data
                if (rcvSegment != null) {
                    log.warn("%s->processReceivedSegment: no free space: %d lost bytes",
                                this, rseg.getDataLength());
                    return;
                }
                rcvSegment = rseg;
                rxReady.signal();
                logDebugState();
            }
        } finally {
            lk.unlock();
        }
    }


    // -------------  LOG SUPPORT  ---------------
    protected void logDebugState() {
        if (log.debugEnabled()) {
            log.debug("%s=> state: %s", this, stateToString());
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(proto.net.getAddr()).append("/{local=").append(localPort);
        buf.append(",remote=").append(remotePort).append("}");
        return buf.toString(); 
    }

    public String stateToString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{sndIsUna=").append(sndIsUna);
        if (rcvSegment == null) {
            buf.append(",rcvSegment=null");
        } else {
            buf.append(",rcvSegment.dataLength=").append(rcvSegment.getDataLength());
            buf.append(",rcvSegConsumedBytes=").append(rcvSegConsumedBytes);
        }
        return buf.append("}").toString();
    }

}
