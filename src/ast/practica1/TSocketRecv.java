package ast.practica1;

import ast.protocols.tcp.TCPSegment;

/**
 * Implements the receiving interface of the transport layer.
 *
 * @author Xavier Mendez
 */
public class TSocketRecv {

    private final Channel channel;
    private TCPSegment segment;

    /**
     * Construct and establish a new connection over the passed channel.
     * @param channel the communication channel to use
     */
    public TSocketRecv(Channel channel) {
        this.channel = channel;
    }

    /**
     * Receives data from the peer.
     *
     * @param data buffer where received data is to be written to
     * @param offset offset of first byte to write to
     * @param length maximum count of bytes to receive
     * @return bytes actually received, or -1 on EOF
     */
    public int receiveData(byte[] data, int offset, int length) {
        if (segment == null) {
            try {
                segment = channel.receive();
            } catch (IllegalStateException e) {
                return 0;
            }
        }
        
        // Check for EOF
        if (segment.getData() == null && segment.getDataLength() == 0)
            return -1;
        
        // Copy data
        if (length > segment.getDataLength())
            length = segment.getDataLength();
        System.arraycopy(segment.getData(), segment.getDataOffset(), data, offset, length);
        
        // Update (and possibly drop) segment
        segment.setData(segment.getData(), segment.getDataOffset() + length,
                segment.getDataLength() - length);
        if (segment.getDataLength() == 0)
            segment = null;
        
        return length;
    }

}
