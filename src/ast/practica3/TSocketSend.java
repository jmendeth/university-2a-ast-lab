package ast.practica3;

import ast.protocols.tcp.TCPSegment;

/**
 * Implements the sending interface of the transport layer.
 *
 * @author Xavier Mendez
 */
public class TSocketSend extends TSocketBase {

    /**
     * Construct and establish a new connection over the passed channel.
     * @param channel the communication channel to use
     */
    public TSocketSend(Channel channel) {
        super(channel);
    }

    /**
     * Sends data to the peer.
     *
     * @param data buffer where data is to be read from
     * @param offset offset of first byte to read
     * @param length count of bytes to send
     */
    public void sendData(byte[] data, int offset, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(data, offset, copy, 0, length);
        TCPSegment segment = new TCPSegment();
        segment.setData(copy);
        channel.send(segment);
    }

    /**
     * Closes the connection and frees associated resources.
     */
    public void close() {
        channel.send(new TCPSegment());
    }

}
