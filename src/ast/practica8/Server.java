package ast.practica8;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ast.logging.Log;
import ast.logging.LogFactory;

import ast.practica8.net.CallRequest;
import ast.practica8.net.CallReply;
import java.io.EOFException;

/**
 * Implements the server part for {@link PontStub}.
 *
 * @author Xavier Mendez
 */
public class Server implements Runnable {

    public static Log log = LogFactory.getLog(Server.class);
    public static final int DEFAULT_PORT = 5555;

    protected final ServerSocket serverSocket;
    protected final Pont pont;

    public Server(ServerSocket serverSocket, Pont pont) {
        this.serverSocket = serverSocket;
        this.pont = pont;
    }

    protected CallReply handleRequest(CallRequest request) {
        log.info("Handling request: %s", request);
        switch (request.op) {
            case ENTER:
                pont.entrar(request.sentit);
                return new CallReply();
            case LEAVE:
                pont.sortir();
                return new CallReply();
            default:
                throw new AssertionError("Should never happen");
        }
    }

    @Override
    public void run() {
        try {
            log.info("Server running at %s", serverSocket.getLocalSocketAddress());
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientTask(socket)).start();
            }
        } catch (IOException ex) {
            log.error(ex);
        } finally {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    class ClientTask implements Runnable {

        private final Socket socket;

        public ClientTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                ObjectOutputStream oStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream iStream = new ObjectInputStream(socket.getInputStream());
                log.info("Accepted connection from %s", socket.getRemoteSocketAddress());

                while (true) {
                    Object o = iStream.readObject();
                    if (!(o instanceof CallRequest)) {
                        log.error("Expected request object but got %s", o);
                        return;
                    }
                    oStream.writeObject(handleRequest((CallRequest)o));
                }
            } catch (EOFException ex) {
                log.info("Connection from %s ended", socket.getRemoteSocketAddress());
            } catch (IOException ex) {
                log.error(ex);
            } catch (ClassNotFoundException ex) {
                log.error("Malformed request: %s", ex);
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    log.error(ex);
                }
            }
        }

    }

    public static Server openServer(int port) throws IOException {
        return new Server(new ServerSocket(port), new PontImpl());
    }

    public static void main(String[] args) {
        try {
            openServer(DEFAULT_PORT).run();
        } catch (IOException ex) {
            log.error(ex);
        }
    }

}
