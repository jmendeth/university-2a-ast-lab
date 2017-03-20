package ast.practica4;

import ast.protocols.tcp.TCPSegment;

/**
 * Implements the sending interface of the transport layer.
 *
 * @author Xavier Mendez
 */
public class TSocketSend extends TSocketBase {

    protected int sndMSS;

    /**
     * Construct and establish a new connection over the passed channel.
     */
    protected TSocketSend(ProtocolSend p, int localPort, int remotePort) {
        super(p, localPort, remotePort);
        this.sndMSS = p.channel.getMMS() - TCPSegment.HEADER_SIZE;
    }

    /**
     * Sends data to the peer.
     *
     * @param data buffer where data is to be read from
     * @param offset offset of first byte to read
     * @param length count of bytes to send
     */
    public void sendData(byte[] data, int offset, int length) {
        while (length > 0) {
            int size = length;
            if (size > sndMSS) size = sndMSS;
            sendSegment(segmentize(data, offset, size));
            offset += size;
            length -= size;
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
        segment.setSourcePort(localPort);
        segment.setDestinationPort(remotePort);
        this.proto.channel.send(segment);
    }

    /**
     * Closes the connection and frees associated resources.
     */
    public void close() {
        sendSegment(new TCPSegment());
    }

}
