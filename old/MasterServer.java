import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.io.*;

public class MasterServer extends Thread{
    private CubbyHole cubbyhole;
    private int numProduced;
    public int portNumber;    
    
  
    public MasterServer(CubbyHole c,
                           int portNumber) {
        cubbyhole = c;
        cubbyhole.addProducer();
        this.portNumber = portNumber;
        numProduced = 0;
    }
    
    protected void putData(Data data) {
        numProduced++;
        cubbyhole.put(data);
        try {
            Thread.sleep((int)(Math.random() * 100));
        } catch (InterruptedException e) { }
    }
  
    @Override
    public void run() {

        boolean listening = true;
        
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (listening) {
                
                Socket s = serverSocket.accept();
                int p = s.getPort();
                System.out.println("Port: "+p);
                InetAddress ia = s.getInetAddress();
                s.close();
                System.out.println("HERE");
                putData(new Data(ia,p));
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
        cubbyhole.subProducer();
        System.out.println("Producer #" + this.number + " produced: " + this.numProduced);
    }
}