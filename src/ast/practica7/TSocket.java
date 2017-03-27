package ast.practica7;

import ast.protocols.tcp.TCPSegment;
import ast.logging.Log;
import ast.util.CircularQueue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Connection oriented Protocol Control Block.
 *
 * Each instance of TSocket maintains all the status of an endpoint.
 * 
 * Interface for application layer defines methods for passive/active opening and for closing the connection.
 * Interface lower layer defines methods for processing of received segments and for sending of segments.
 * We assume an ideal lower layer with no losses and no errors in packets.
 *
 * State diagram:<pre>
                              +---------+
                              |  CLOSED |-------------
                              +---------+             \
                           LISTEN  |                   \
                           ------  |                    | CONNECT
                                   V                    | -------
                              +---------+               | snd SYN
                              |  LISTEN |               |
                              +---------+          +----------+
                                   |               | SYN_SENT |
                                   |               +----------+
                         rcv SYN   |                    |
                         -------   |                    | rcv SYN
                         snd SYN   |                    | -------
                                   |                    |
                                   V                   /
                              +---------+             /
                              |  ESTAB  |<------------
                              +---------+
                       CLOSE    |     |    rcv FIN
                      -------   |     |    -------
 +---------+          snd FIN  /       \                    +---------+
 |  FIN    |<-----------------           ------------------>|  CLOSE  |
 |  WAIT   |------------------           -------------------|  WAIT   |
 +---------+          rcv FIN  \       /   CLOSE            +---------+
                      -------   |      |  -------
                                |      |  snd FIN 
                                V      V
                              +----------+
                              |  CLOSED  |
                              +----------+
 * </pre>
 *
 * @author AST's teachers
 */
public class TSocket {

    public static Log log = Protocol.log;

    protected Protocol proto;
    protected Lock lk;
    protected Condition appCV;

    protected int localPort;
    protected int remotePort;

    protected int state;
    protected CircularQueue<TSocket> acceptQueue;

    // States of FSM:
    protected final static int CLOSED = 0,
                               LISTEN = 1,
                               SYN_SENT = 2,
                               ESTABLISHED = 3,
                               FIN_WAIT = 4,
                               CLOSE_WAIT = 5;

    /**
     * Create an endpoint bound to the local IP address and the given TCP port.
     * The local IP address is determined by the networking system.
     * @param ch
     */
    protected TSocket(Protocol p, int localPort) {
        lk = new ReentrantLock();
        appCV = lk.newCondition();
        proto = p;
        this.localPort = localPort;
        state = CLOSED;
    }

    /**
     * Passive open
     */
    protected void listen() {
        lk.lock();
        try {
            log.debug("%s->listen()", this);
            acceptQueue = new CircularQueue<>(5);
            state = LISTEN;
            proto.addListenTSocket(this);
            logDebugState();
        } finally {
            lk.unlock();
        }
    }

    public TSocket accept() {
        lk.lock();
        try {
            log.debug("%1$s->accept()", this);
            while (acceptQueue.empty()) appCV.await();
            log.debug("%1$s->accepted", this);
            return acceptQueue.get();
        } catch (InterruptedException ex) {
            log.error(ex);
            return null;
        } finally {
            lk.unlock();
        }
    }

    /**
     * Active open
     */
    protected void connect(int remPort) {
        lk.lock();
        try {
            log.debug("%s->connect(%d)", this, remPort);
            remotePort = remPort;
            proto.addActiveTSocket(this);
            state = SYN_SENT;

            TCPSegment syn = new TCPSegment();
            syn.setSourcePort(localPort);
            syn.setDestinationPort(remotePort);
            syn.setSyn(true);
            sendSegment(syn);

            logDebugState();

            while (state != ESTABLISHED) appCV.await();
        } catch (InterruptedException ex) {
            log.error(ex);
        } finally {
            lk.unlock();
        }
    }

    public void close() {
        lk.lock();
        try {
          log.debug("%s->close()", this);
          switch (state) {
            case ESTABLISHED:
            case CLOSE_WAIT: {
                if (state == ESTABLISHED)
                    state = FIN_WAIT;
                else
                    state = CLOSED;
                TCPSegment fin = new TCPSegment();
                fin.setSourcePort(localPort);
                fin.setDestinationPort(remotePort);
                fin.setFin(true);
                sendSegment(fin);
                break;
            }
            default:
                log.error("%s->close: connection does not exist", this);
          }
        } finally {
            lk.unlock();
        }
    }


    /**
     * Segment arrival.
     * @param rseg segment of received packet
     */
    protected void processReceivedSegment(TCPSegment rseg) {
        lk.lock();
        try {
            switch (state) {
            case LISTEN: {
                if (rseg.isSyn()) {
                    //FIXME: what if acceptQueue is full?
                    TSocket socket = new TSocket(proto, localPort);
                    socket.remotePort = rseg.getSourcePort();
                    proto.addActiveTSocket(socket);
                    socket.state = ESTABLISHED;

                    acceptQueue.put(socket);
                    appCV.signal();

                    TCPSegment syn = new TCPSegment();
                    syn.setSourcePort(localPort);
                    syn.setDestinationPort(socket.remotePort);
                    syn.setSyn(true);
                    socket.sendSegment(syn);
                }
                break;
            }
            case SYN_SENT: {
                if (rseg.isSyn()) {
                    state = ESTABLISHED;
                    appCV.signal();
                    logDebugState();
                }
                break;
            }
            case ESTABLISHED:
            case FIN_WAIT:
            case CLOSE_WAIT: {
                // Process segment text
                if (rseg.getDataLength() > 0) {
                    if (state == ESTABLISHED || state == FIN_WAIT) {
                        // Here should go the segment's data processing
                    } else {
                        // This should not occur, since a FIN has been received from the
                        // remote side.  Ignore the segment text.
                    }
                }
                // Check FIN bit
                if (rseg.isFin() && state != CLOSE_WAIT) {
                    if (state == ESTABLISHED)
                        state = CLOSE_WAIT;
                    else
                        state = CLOSED;
                    appCV.signal();
                }
                break;
            }
            }
        } finally {
            lk.unlock();
        }
    }

    protected void sendSegment(TCPSegment segment) {
        log.debug("%s->sendSegment(%s)", this, segment);
		proto.channel.send(segment);
    }

    protected void logDebugState() {
        if (log.debugEnabled()) {
            log.debug("%s=> state: %s", this, stateToString());
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(proto.channel.getAddr()).append("/{local=").append(localPort);
        buf.append(",remote=").append(remotePort).append("}");
        return buf.toString(); 
    }

    public String stateToString() {
        String sst;
        switch (state) {
            case CLOSED:      sst = "CLOSED"; break;
            case LISTEN:      sst = "LISTEN"; break;
            case SYN_SENT:    sst = "SYN_SENT"; break;
            case ESTABLISHED: sst = "ESTABLISHED"; break;
            case FIN_WAIT:    sst = "FIN_WAIT"; break;
            case CLOSE_WAIT:  sst = "CLOSE_WAIT"; break;
            default: sst = "?";
        }
        return sst;
    }

}
