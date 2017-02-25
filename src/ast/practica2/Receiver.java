package ast.practica2;

import ast.practica1.Channel;
import ast.practica1.TSocketRecv;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Implements a read that copies from a {@link TSocketRecv}
 * instance into the passed output stream until EOF.
 *
 * @author Xavier Mendez
 */
public class Receiver implements Runnable {

    private final TSocketRecv receiver;
    private final OutputStream stream;

    /**
     * Constructs a new Receiver runnable.
     *
     * @param channel Channel to receive data from.
     * @param stream Stream to output to.
     */
    public Receiver(Channel channel, OutputStream stream) {
        this.receiver = new TSocketRecv(channel);
        this.stream = stream;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[256];
            int length;
            while ((length = receiver.receiveData(buffer, 0, buffer.length)) != -1)
                stream.write(buffer, 0, length);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
