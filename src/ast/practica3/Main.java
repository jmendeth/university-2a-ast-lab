package ast.practica3;

/**
 * Implements the main class, which tests the current implementation by sending
 * and receiving a serialized hard-coded set of data.
 *
 * @author Xavier Mendez
 */
public class Main {
    public static void main(String[] args) {
        Channel c = new MonitorChannel();
        new Thread(new Sender(c)).start();
        new Thread(new Receiver(c)).start();
    }
}
