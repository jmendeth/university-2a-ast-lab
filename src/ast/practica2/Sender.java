package ast.practica2;

import ast.practica1.Channel;
import ast.practica1.TSocketSend;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Implements a thread that constantly copies data from
 * the passed input stream into a {@code TSocketSend} instance.
 * Data will be transferred line by line.
 *
 * @author xavier
 */
public class Sender implements Runnable {

    private final TSocketSend sender;
    private final BufferedReader reader;

    /**
     * Construct a new Sender runnable.
     *
     * @param channel Channel to send data over.
     * @param reader Data source.
     */
    public Sender(Channel channel, Reader reader) {
        this.sender = new TSocketSend(channel);
        this.reader = new BufferedReader(reader);
    }
    
    @Override
    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                byte[] buffer = line.getBytes();
                sender.sendData(buffer, 0, buffer.length);
            }
            sender.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
