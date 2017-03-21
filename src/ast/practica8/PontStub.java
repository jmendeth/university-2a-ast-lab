package ast.practica8;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Proxy for a remote {@code Pont} instance accessed via RPC.
 *
 * @author Xavier Mendez
 */
public final class PontStub implements Pont, Closeable {

    private final Socket socket;
    private final ObjectOutputStream oStream;
    private final ObjectInputStream iStream;

    public PontStub(Socket socket) throws IOException {
        this.socket = socket;
        this.oStream = new ObjectOutputStream(socket.getOutputStream());
        this.iStream = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void entrar(boolean sentit) {
        //TODO
    }

    @Override
    public void sortir() {
        //TODO
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

}
