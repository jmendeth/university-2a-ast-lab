package ast.practica3;

import ast.logging.Log;
import ast.logging.LogFactory;
import ast.practica1.CircularQueue;
import ast.protocols.ip.IPPacket;
import ast.protocols.tcp.TCPSegment;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements a thread-safe, blocking, ideal network channel.
 *
 * @author Xavier Mendez
 */
public class MonitorChannel implements Channel {

    public static final Log log = LogFactory.getLog(TSocketBase.class);

    private final Lock lk;
    private final Condition sendCV;
    private final Condition recvCV;
    private final CircularQueue<TCPSegment> queue;
    protected double lossRatio;

    public MonitorChannel(double lossRatio) {
        this.lk = new ReentrantLock();
        this.sendCV = lk.newCondition();
        this.recvCV = lk.newCondition();
        this.queue = new CircularQueue<>(15);
        this.lossRatio = lossRatio;
    }
    
    public MonitorChannel() {
        this(0);
    }

    @Override
    public void send(TCPSegment seg) {
        lk.lock();
        try {
            while (queue.full()) sendCV.await();
            if (Math.random() < lossRatio) return;
            queue.put(seg);
            recvCV.signal();
        } catch (InterruptedException ex) {
            log.error(ex); // FIXME: shouldn't be swallowed
        } finally {
            lk.unlock();
        }
    }

    @Override
    public TCPSegment receive() {
        lk.lock();
        try {
            while (queue.empty()) recvCV.await();
            TCPSegment result = queue.get();
            sendCV.signal();
            return result;
        } catch (InterruptedException ex) {
            log.error(ex); // FIXME: shouldn't be swallowed
            return null;
        } finally {
            lk.unlock();
        }
    }

    public static final int MAX_MSG_SIZE = 1500 - IPPacket.HEADER_SIZE;

    @Override
    public int getMMS() {
        return MAX_MSG_SIZE;
    }

}
