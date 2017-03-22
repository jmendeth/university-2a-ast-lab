package ast.practica8;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import ast.logging.Log;
import ast.logging.LogFactory;

import ast.practica8.net.CallReply;
import ast.practica8.net.CallRequest;

/**
 * Proxy for a remote {@code Pont} instance accessed via RPC.
 *
 * @author Xavier Mendez
 */
public final class PontStub implements Pont {

    public static Log log = LogFactory.getLog(PontStub.class);

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
        CallRequest request = new CallRequest();
        request.op = CallRequest.Operation.ENTER;
        request.sentit = sentit;
        sendRequest(request);
    }

    @Override
    public void sortir() {
        CallRequest request = new CallRequest();
        request.op = CallRequest.Operation.LEAVE;
        sendRequest(request);
    }

    protected CallReply sendRequest(CallRequest request) {
        try {
            oStream.writeObject(request);
            log.debug("Sent request: %s", request);

            Object o = iStream.readObject();
            if (!(o instanceof CallReply)) {
                log.error("Expected reply object but got %s", o);
                throw new RuntimeException("Malformed response");
            }

            log.debug("Received response: %s", o);
            return (CallReply)o;
        } catch (IOException ex) {
            log.error(ex);
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            log.error("Malformed response: %s", ex);
            throw new RuntimeException("Malformed response");
        }
    }

}
