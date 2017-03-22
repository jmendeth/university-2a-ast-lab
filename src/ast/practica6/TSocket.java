package ast.practica6;

import ast.logging.Log;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

import ast.util.Timer;
import java.util.concurrent.TimeUnit;

import ast.protocols.tcp.TCPSegment;

/**
 * We assume an IP layer with errors or losses in packets.
 * @author AST's teachers
 */
public class TSocket {

    public static Log log = Protocol.log;

    protected Protocol proto;
    protected int localPort;
    protected int remotePort;

    protected Lock lk;
    protected Condition sndReady;
    protected Condition rcvReady;

    // Sender variables:
    protected static final int SND_RTO = 500; // Retransmission timeout in milliseconds
    protected Timer timerService;
    protected Timer.Task sndRtTimer;
    protected int sndMSS;   // Send maximum segment size
    protected int sndNxt;   // Sequence number not yet transmitted
    protected TCPSegment sndUnackedSegment; // Transmitted segment not yet acknowledged

    // Receiver variables:
    protected TCPSegment rcvSegment; // Received segment not yet consumed
    protected int rcvSegUnc;         // Received segment's offset not yet consumed
    protected int rcvNxt;            // Expected sequence number to be received


    /**
     * Create an endpoint bound to the given TCP ports.
     */
    protected TSocket(Protocol p, int localPort, int remotePort) {
        proto = p;
        this.localPort = localPort;
        this.remotePort = remotePort;
        lk = new ReentrantLock();
        sndReady = lk.newCondition();
        rcvReady = lk.newCondition();
        // init sender variables
        sndMSS = proto.net.getMMS() - TCPSegment.HEADER_SIZE; // IP maximum message size - TCP header size
        sndNxt = 0;
        sndUnackedSegment = null;
        timerService = new Timer();
        // init receiver variables
        rcvSegment = null;
        rcvSegUnc = 0;
        rcvNxt = 0;
    }


    // -------------  SENDER PART  ---------------
    public void sendData(byte[] data, int offset, int length) {
        lk.lock();
        try {
            log.debug("%s->sendData(count=%d)", this, length);
            while (length > 0) {
                int size = length;
                if (size > sndMSS) size = sndMSS;
                sndUnackedSegment = segmentize(data, offset, size);
                sendSegment(sndUnackedSegment);
                while (sndUnackedSegment != null) sndReady.await();
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
        segment.setSeqNum(sndNxt++);
        segment.setData(copy);
        return segment;
    }

    protected void sendSegment(TCPSegment segment) {
        log.debug("%s->sendSegment(%s)", this, segment);
        segment.setSourcePort(localPort);
        segment.setDestinationPort(remotePort);
        proto.net.send(segment);
        // start timer
        startRTO();
    }

    /**
     * Timeout elapsed.
     */
    protected void timeout() {
        lk.lock();
        try{
            log.debug("%s->timeout()", this);
            if (sndUnackedSegment != null) {
                sendSegment(sndUnackedSegment);
            }
        } finally {
            lk.unlock();
        }
    }

    protected void startRTO() {
        if (sndRtTimer != null) sndRtTimer.cancel();
        sndRtTimer = timerService.startAfter(
            new Runnable() {
                @Override public void run() { timeout(); }
            },
            SND_RTO, TimeUnit.MILLISECONDS);
    }

    protected void stopRTO() {
        if (sndRtTimer != null) sndRtTimer.cancel();
        sndRtTimer = null;
    }


    // -------------  RECEIVER PART  ---------------
    /**
     * Places received data in buf
     */
    public int receiveData(byte[] buf, int offset, int maxlen) {
        lk.lock();
        try {
            log.debug("%s->receiveData(maxlen=%d)", this, maxlen);
            while (rcvSegment == null) rcvReady.await();
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

    protected int consumeSegment(byte[] buf, int offset, int maxcount) {
        // assertion: rcvSegment != null && rcvSegment.getDataLength() > rcvSegUnc
        // get data from rcvSegment and copy to buf
        int n = rcvSegment.getDataLength() - rcvSegUnc;
        if (n > maxcount) {
            // receiveData's buffer is small. Consume a fragment of the received segment
            n = maxcount;
        }
        // n == min(maxcount, rcvSegment.getDataLength() - rcvSegUnc)
        System.arraycopy(rcvSegment.getData(), rcvSegment.getDataOffset() + rcvSegUnc, buf, offset, n);
        rcvSegUnc += n;
        if (rcvSegUnc == rcvSegment.getDataLength()) {
            // rcvSegment is totally consumed. Remove it
            rcvSegment = null;
            rcvSegUnc = 0;
        }
        return n;
    }

    protected void sendAck() {
        TCPSegment ack = new TCPSegment();
        ack.setSourcePort(localPort);
        ack.setDestinationPort(remotePort);
        ack.setFlags(TCPSegment.ACK);
        ack.setAckNum(rcvNxt);
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
                stopRTO();
                if (rseg.getAckNum() != sndNxt) {
                    assert(sndUnackedSegment != null);
                    sendSegment(sndUnackedSegment);
                } else {
                    sndUnackedSegment = null;
                    sndReady.signal();
                }
                logDebugState();
                return;
            }
            // Process segment data
            if (rseg.getDataLength() > 0) {
                if (rseg.getSeqNum() != rcvNxt) {
                    sendAck(); // Why ?
                    return;
                }
                if (rcvSegment != null) {
                    log.warn("%s->processReceivedSegment: no free space: %d lost bytes",
                                this, rseg.getDataLength());
                    return;
                }
                rcvNxt++;
                rcvSegment = rseg;
                rcvReady.signal();
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
        if (sndUnackedSegment == null) {
            buf.append("{sndUnackedSegment=null");
        } else {
            buf.append("{sndUnackedSegment.seqNum=").append(sndUnackedSegment.getSeqNum());
        }
        buf.append(",sndNxt=").append(sndNxt);
        buf.append(",rcvNxt=").append(rcvNxt);
        if (rcvSegment == null) {
            buf.append(",rcvSegment=null");
        } else {
            buf.append(",rcvSegment.seqNum=").append(rcvSegment.getSeqNum());
            buf.append(",rcvSegment.dataLength=").append(rcvSegment.getDataLength());
            buf.append(",rcvSegUnc=").append(rcvSegUnc);
        }
        return buf.append("}").toString();
    }

}
