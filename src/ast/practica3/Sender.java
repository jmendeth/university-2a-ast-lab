package ast.practica3;

import ast.logging.Log;
import ast.logging.LogFactory;

public class Sender implements Runnable {

    public static Log log = LogFactory.getLog(Sender.class);

    protected TSocketSend output;
    protected int sendNum, sendSize, sendInterval;

    public Sender(Channel c, int sendNum, int sendSize, int sendInterval) {
        this.output = new TSocketSend(c);
        this.sendNum = sendNum;
        this.sendSize = sendSize;
        this.sendInterval = sendInterval;
    }

    public Sender(Channel c) {
        this(c, 20, 50, 100);
    }

    @Override
    public void run() {
        try {
            byte n = 0;
            byte[] buf = new byte[sendSize];
            for (int i = 0; i < sendNum; i++) {
                Thread.sleep(sendInterval * 10);
                // stamp data to send
                for (int j = 0; j < sendSize; j++) {
                    buf[j] = n;
                    n = (byte) (n + 1);
                }
                output.sendData(buf, 0, buf.length);
            }
            log.info("Sender: transmission finished");
        } catch (Exception e) {
            log.error("Excepcio a Sender: %s", e);
            e.printStackTrace(System.err);
        }
    }
}
