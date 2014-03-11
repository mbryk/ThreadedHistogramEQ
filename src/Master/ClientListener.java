import java.net.*;
import java.io.*;

public class ClientListener extends Thread {
	private int portNumber;
	private CubbyHole cubbyhole;

    public ClientListener(CubbyHole c, int port) {
        this.cubbyhole = c;
        this.portNumber = port;
        cubbyhole.addProducer();
    }

    private void putData(Data data) {
        cubbyhole.put(data);
    }
    
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (true) {
                System.out.println("Listening for Clients");
                Socket s = serverSocket.accept();
                BufferedReader inFromC = new BufferedReader(
                    new InputStreamReader(s.getInputStream()));
                String multiples_str = inFromC.readLine();
                int multiples = Integer.parseInt(multiples_str);

                int p = s.getPort();
                System.out.println("New Client at Port "+p);
                InetAddress ia = s.getInetAddress();
                s.close();
                
                for (int i = 0; i<multiples; i++){
                    putData(new Data(ia,p,0,-1));
                }
            }
        } catch (IOException e) {
            System.err.println("ClientListener Error: "+e);
            System.exit(-1);
        }
        
        cubbyhole.subProducer();
    }
}