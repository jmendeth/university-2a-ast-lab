package ast.practica2;

import ast.practica1.Channel;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Main class.
 *
 * @author Xavier Mendez
 */
public class Main {

    public static void main(String[] args) {
        try {

            final Reader reader = new InputStreamReader(System.in);
            //final Channel channel = new BusyWaitChannel();
            final Channel channel = new AwaitChannel();

            final Thread senderThread = new Thread(new Sender(channel, reader));
            final Thread receiverThread = new Thread(new Receiver(channel, System.out));

            senderThread.start();
            receiverThread.start();

            senderThread.join();
            receiverThread.join();

        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

}
