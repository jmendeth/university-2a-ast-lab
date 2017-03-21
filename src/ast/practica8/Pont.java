package ast.practica8;

/**
 * A bridge that only allows cars to cross it on one direction.
 *
 * @author Xavier Mendez
 */
public interface Pont {

    public void entrar(boolean sentit);

    public void sortir();

}
