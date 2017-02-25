package ast.practica1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main class.
 * @author Xavier Mendez
 */
public class Main {

    public static void main(String[] args) {
        try {
            
            byte[] send;
            byte[] recv = new byte[256];
            int recvLength;
            
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            final Channel channel = new QueueChannel();
            final TSocketRecv receiver = new TSocketRecv(channel);
            final TSocketSend sender = new TSocketSend(channel);

            String line;
            while ((line = reader.readLine()) != null) {
                send = line.getBytes();
                sender.sendData(send, 0, send.length);
                
                recvLength = receiver.receiveData(recv, 0, recv.length);
                System.out.write(recv, 0, recvLength);
            }
            
            sender.close();
            recvLength = receiver.receiveData(recv, 0, recv.length);
            assert(recvLength == -1);
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
