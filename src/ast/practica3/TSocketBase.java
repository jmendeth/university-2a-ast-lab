package ast.practica3;

import ast.logging.Log;
import ast.logging.LogFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base class for the transport layer implementation, {@link TSocketRecv}
 * and {@link TSocketSend}.
 *
 * @author Xavier Mendez
 */
public class TSocketBase {

    public static final Log log = LogFactory.getLog(TSocketBase.class);

    protected final Lock lk;
    protected final Condition appCV;
    protected final Channel channel;

    protected TSocketBase(Channel channel) {
        this.lk = new ReentrantLock();
        this.appCV = lk.newCondition();
        this.channel = channel;
    }

}
