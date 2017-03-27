package ast.practica8;

import java.io.Closeable;

/**
 * A bridge that only allows cars to cross it on one direction.
 *
 * @author Xavier Mendez
 */
public interface Pont extends Closeable {

    public void entrar(boolean sentit);

    public void sortir();

}
