package ast.practica8;

import java.util.Random;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import ast.logging.Log;
import ast.logging.LogFactory;

/**
 * Main class for the client. Connects to the passed
 * server (localhost by default) and enters the bridge
 * in a random direction, stays there for some time and
 * exits.
 *
 * Arguments: {@code [<server IP>] [<port>]}
 *
 * @author Xavier Mendez
 */
public class Client {

    public static Log log = LogFactory.getLog(Client.class);

    public static void main(String[] args) {
        Random r = new Random();
        Socket socket;

        try {
            InetAddress addr = InetAddress.getLoopbackAddress();
            if (args.length >= 1) addr = InetAddress.getByName(args[0]);
            
            int port = Server.DEFAULT_PORT;
            if (args.length >= 2) port = Integer.parseInt(args[1]);
            
            socket = new Socket(addr, port);
            log.info("Connected to %s", socket.getRemoteSocketAddress());
        } catch (UnknownHostException ex) {
            log.error(ex);
            return;
        } catch (IOException ex) {
            log.error(ex);
            return;
        }
        
        try {
            Pont pont = new PontStub(socket);
            boolean sentit = r.nextBoolean();
            
            System.out.printf("Entrant al pont en sentit %s...", sentit);
            System.out.flush();
            pont.entrar(sentit);
            System.out.println(" fet.");
            
            double waitTime = r.nextGaussian() * 1.0 + 3.0;
            Thread.sleep((int) Math.round(waitTime * 1000));
            
            System.out.printf("Sortint-hi...");
            System.out.flush();
            pont.sortir();
            System.out.println(" fet.");
        } catch (InterruptedException | IOException ex) {
            log.error(ex);
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {}
        }
    }

}
