package ast.practica8;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of {@code Pont} using locks.
 *
 * @author Xavier Mendez
 */
public class PontImpl implements Pont {

    protected final Lock lk;
    protected final Condition appCV;
    protected int cars;
    protected boolean sentit;

    public PontImpl() {
        this.lk = new ReentrantLock();
        this.appCV = lk.newCondition();
        this.cars = 0;
    }

    @Override
    public void entrar(boolean sentit) {
        lk.lock();
        try {
            while (cars > 0 && this.sentit != sentit)
                appCV.await();
            cars++;
            this.sentit = sentit;
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } finally {
            lk.unlock();
        }
    }

    @Override
    public void sortir() {
        lk.lock();
        try {
            assert(cars > 0);
            cars--;
            if (cars == 0) appCV.signalAll();
        } finally {
            lk.unlock();
        }
    }

}
