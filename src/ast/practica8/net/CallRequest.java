package ast.practica8.net;

import java.io.Serializable;

/**
 * The client sends a serialized instance of this object to request an RPC call.
 *
 * @author Xavier Mendez
 */
public class CallRequest implements Serializable {
    public enum Operation {
        ENTER,
        LEAVE,
    }

    public Operation op;
    public boolean sentit;
}
