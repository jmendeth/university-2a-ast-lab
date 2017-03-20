package ast.practica4;

import ast.logging.Log;
import ast.logging.LogFactory;
import ast.practica3.Channel;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProtocolBase {

    public static Log log = LogFactory.getLog(ProtocolBase.class);

    protected Lock lk;
    protected Channel channel;

    protected ProtocolBase(Channel ch) {
        lk = new ReentrantLock();
        channel = ch;
    }

}
