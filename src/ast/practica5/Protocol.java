package ast.practica5;

import ast.protocols.tcp.TCPSegment;

import ast.logging.Log;
import ast.logging.LogFactory;

import ast.util.FDuplexChannel;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;

public class Protocol {

    public static Log log = LogFactory.getLog(Protocol.class);

    protected ArrayList<TSocket> sockets;
    protected Thread task;
    protected Lock lk;
    protected FDuplexChannel.Peer net;

    public Protocol(FDuplexChannel.Peer ch) {
        sockets = new ArrayList<>();
        task = new Thread(new ReceiverTask());
        task.start();
        lk = new ReentrantLock();
        net = ch;
    }

    public TSocket openWith(int localPort, int remotePort) {
        lk.lock();
        try {
            // A completar per l'estudiant (veieu practica 4):
            ...
        } finally {
            lk.unlock();
        }
    }

    protected void ipInput(TCPSegment segment) {
        // A completar per l'estudiant (veieu practica 4):
        ...
    }

    protected TSocket getMatchingTSocket(int localPort, int remotePort) {
        lk.lock();
        try {
            // A completar per l'estudiant (veieu practica 4):
            ...
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
