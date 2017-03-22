package ast.practica8.net;

import java.io.Serializable;

/**
 * The server sends a serialized instance of this object as a result to
 * a {@link CallRequest} request.
 *
 * @author Xavier Mendez
 */
public class CallReply implements Serializable {
    @Override
    public String toString() {
        return "CallReply{" + '}';
    }
}
