package ast.practica4;

import ast.practica3.Channel;
import ast.practica3.MonitorChannel;

public class Main {

    public static void main(String[] args){
        Channel c = new MonitorChannel();

        ProtocolRecv proto1 = new ProtocolRecv(c);
        new Thread(new Host1(proto1)).start();

        ProtocolSend proto2 = new ProtocolSend(c);
        new Thread(new Host2(proto2)).start();
    }

}


class Host1 implements Runnable {

    public static final int PORT = 10;

    protected ProtocolRecv proto;

    public Host1(ProtocolRecv proto) {
        this.proto = proto;
    }

    @Override
    public void run() {
        TSocketRecv socket1 = proto.openForInput(PORT, Host2.PORT1);
        new Thread(new Receiver(socket1)).start();
        TSocketRecv socket2 = proto.openForInput(PORT, Host2.PORT2);
        new Thread(new Receiver(socket2)).start();
    }

}


class Host2 implements Runnable {

    public static final int PORT1 = 10;
    public static final int PORT2 = 50;

    protected ProtocolSend proto;
    
    public Host2(ProtocolSend proto) {
        this.proto = proto;
    }
    
    @Override
    public void run() {
        TSocketSend socket1 = proto.openForOutput(PORT1, Host1.PORT);
        new Thread(new Sender(socket1)).start();
        TSocketSend socket2 = proto.openForOutput(PORT2, Host1.PORT);
        new Thread(new Sender(socket2)).start();
    }
    
}
