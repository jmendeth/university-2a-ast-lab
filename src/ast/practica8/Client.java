package ast.practica8;

import java.util.Random;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import ast.logging.Log;
import ast.logging.LogFactory;

/**
 * Main class for the client. Creates many cars against a
 * bridge server.
 * 
 * Each car connects to the server (localhost by default)
 * and enters the bridge in a random direction, stays there
 * for some time and exits.
 *
 * Arguments: {@code [<server IP>] [<port>]}
 *
 * @author Xavier Mendez
 */
public class Client {

    public static Log log = LogFactory.getLog(Client.class);

    public static final int COTXES = 5;

    public static class Cotxe implements Runnable {

        private final int id;
        private final Pont pont;
        private final Random r;

        public Cotxe(int id, Pont pont, Random r) {
            this.id = id;
            this.pont = pont;
            this.r = r;
        }

        @Override
        public void run() {
            try {
                boolean sentit = r.nextBoolean();

                System.out.printf("Cotxe %s: Entrant al pont en sentit %s...\n", id, sentit);
                System.out.flush();
                pont.entrar(sentit);
                System.out.printf("Cotxe %s: S'ha entrat al pont.\n", id);

                double waitTime = r.nextGaussian() * 1.0 + 3.0;
                Thread.sleep((int) Math.round(waitTime * 1000));

                System.out.printf("Cotxe %s: Sortint-hi...\n", id);
                System.out.flush();
                pont.sortir();
                //System.out.printf("Cotxe %s: Acabat correctament.\n", id);
            } catch (InterruptedException ex) {
                log.error(ex);
            } finally {
                try {
                    pont.close();
                } catch (IOException ex) {
                    log.error(ex);
                }
            }
        }

    }

    public static void main(String[] args) {
        Random r = new Random();

        try {
            InetAddress addr = InetAddress.getLoopbackAddress();
            if (args.length >= 1) addr = InetAddress.getByName(args[0]);
            
            int port = Server.DEFAULT_PORT;
            if (args.length >= 2) port = Integer.parseInt(args[1]);

            for (int i = 0; i < COTXES; i++) {
                Socket socket = new Socket(addr, port);
                log.debug("Connected to %s", socket.getRemoteSocketAddress());
                new Thread(new Cotxe(i+1, new PontStub(socket), r)).start();
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException ex) {
            log.error(ex);
        }
    }

}
