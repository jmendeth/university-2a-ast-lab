package ast.practica4;

import ast.protocols.tcp.TCPSegment;
import ast.practica1.CircularQueue;

/**
 * Socket for receiving endpoint.
 *
 * @author upcnet
 */
public class TSocketRecv extends TSocketBase {

    protected CircularQueue<TCPSegment> rcvQueue;
    protected int rcvSegUnc;

    /**
     * Create an endpoint bound to the local IP address and the given TCP port.
     * The local IP address is determined by the networking system.
     * @param ch
     */
    protected TSocketRecv(ProtocolRecv p, int localPort, int remotePort) {
        super(p, localPort, remotePort);
        rcvQueue = new CircularQueue<>(20);
        rcvSegUnc = 0;
    }

    /**
     * Places received data in buf
     */
    public int receiveData(byte[] buf, int offset, int length) {
        //...
        //treu aquesta sentencia en completar el codi:
        return -1;
    }

    protected int consumeSegment(byte[] buf, int offset, int length) {
        //...
        //treu aquesta sentencia en completar el codi:
        return -1;
    }

    /**
     * Segment arrival.
     * @param rseg segment of received packet
     */
    protected void processReceivedSegment(TCPSegment rseg) {
        //...
    }

}
