package ast.practica4;

import java.util.ArrayList;

import ast.practica3.Channel;

public class ProtocolSend extends ProtocolBase {

    protected ArrayList<TSocketSend> sockets;

    public ProtocolSend(Channel ch) {
        super(ch);
        sockets = new ArrayList<>();
    }

    public TSocketSend openForOutput(int localPort, int remotePort) {
        lk.lock();
        try {
            for (TSocketSend socket : sockets) {
                if (socket.localPort == localPort && socket.remotePort == remotePort)
                    return null;
            }
            TSocketSend socket = new TSocketSend(this, localPort, remotePort);
            sockets.add(socket);
            return socket;
        } finally {
            lk.unlock();
        }
    }

}
