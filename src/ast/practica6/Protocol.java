package ast.practica6;

import ast.protocols.tcp.TCPSegment;

import ast.logging.Log;
import ast.logging.LogFactory;

import ast.util.FDuplexChannel;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;

/**
 * @author AST's teachers
 */
public class Protocol {

    public static Log log = LogFactory.getLog(Protocol.class);

    protected Lock lk;
    protected FDuplexChannel.Peer net;
    protected Thread task;
    protected ArrayList<TSocket> sockets;

    public Protocol(FDuplexChannel.Peer ch) {
        lk = new ReentrantLock();
        net = ch;
        sockets = new ArrayList<>();
        task = new Thread(new ReceiverTask());
        task.start();
    }

    public TSocket openWith(int localPort, int remotePort) {
        lk.lock();
        try {
            if (getMatchingTSocket(localPort, remotePort) != null)
                return null;
            TSocket socket = new TSocket(this, localPort, remotePort);
            sockets.add(socket);
            return socket;
        } finally {
            lk.unlock();
        }
    }

    protected void ipInput(TCPSegment segment) {
        TSocket socket = getMatchingTSocket(segment.getDestinationPort(), segment.getSourcePort());
        if (socket != null) socket.processReceivedSegment(segment);
    }

    protected TSocket getMatchingTSocket(int localPort, int remotePort) {
        lk.lock();
        try {
            for (TSocket socket : sockets) {
                if (socket.localPort == localPort && socket.remotePort == remotePort)
                    return socket;
            }
            return null;
        } finally {
            lk.unlock();
        }
    }

    class ReceiverTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                TCPSegment rseg = net.receive();
                ipInput(rseg);
            }
        }
    }

}
