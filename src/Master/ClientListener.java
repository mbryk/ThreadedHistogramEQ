import java.net.*;
import java.io.*;

public class ClientListener extends Thread {
	private int portNumber;
	private CubbyHole cubbyhole;

    public ClientListener(CubbyHole c, int port) {
        super("TwoListeners");
        this.cubbyhole = c;
        this.portNumber = port;
        cubbyhole.addProducer();
    }

    protected void putData(Data data) {
        cubbyhole.put(data);
    }
    
    public void run() {
       
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (true) {
                System.out.println("Listening for Clients");
                Socket s = serverSocket.accept();
                int p = s.getPort();
                System.out.println("New Client at Port "+p);
                InetAddress ia = s.getInetAddress();
                s.close();
                
                putData(new Data(ia,p,0));
            }
        } catch (IOException e) {
            System.err.println("ClientListener Error: "+e);
            System.exit(-1);
        }
        
        cubbyhole.subProducer();
    }
}