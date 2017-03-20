package ast.practica4;

import ast.protocols.tcp.TCPSegment;

/**
 * @author AST's teachers
 */
public class TSocketSend extends TSocketBase {

    protected int sndMSS;       // Send maximum segment size

    /**
     * Create an endpoint bound to the local IP address and the given TCP port.
     * The local IP address is determined by the networking system.
     */
    protected TSocketSend(ProtocolSend p, int localPort, int remotePort) {
        super(p, localPort, remotePort);
        sndMSS = p.channel.getMMS() - TCPSegment.HEADER_SIZE; // IP maximum message size - TCP header size
    }

    public void sendData(byte[] data, int offset, int length) {
        //...
    }

    protected TCPSegment segmentize(byte[] data, int offset, int length) {
        TCPSegment seg = new TCPSegment();
        //...
        return seg;
    }

    protected void sendSegment(TCPSegment segment) {
        proto.channel.send(segment);
    }

}
