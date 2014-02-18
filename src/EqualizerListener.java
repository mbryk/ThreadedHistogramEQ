import java.net.*;
import java.io.*;

public class EqualizerListener extends Thread {
	private int portNumber;
	private CubbyHole cubbyhole;

    public EqualizerListener(CubbyHole c, int port) {
        super("TwoListeners");
        cubbyhole = c;
        portNumber = port;
    }

    public void run() {

    	Data data = null;
        boolean haveData = false;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (true) {
                Socket s = serverSocket.accept();
                
                if (haveData ||  ((data=cubbyhole.get())!=null)  ) { //value = null if contents of cubbyhole have been removed
                
                	if(s.isConnected()){
                        PrintWriter outToEqualizer = new PrintWriter(s.getOutputStream(), true);
                        outToEqualizer.println(data.ia.getHostName());
                        outToEqualizer.println(data.p);

                		haveData = false;
                		s.close();
                	} else haveData = true;
            	
            	} else {s.close(); break;}
            }
            serverSocket.close();
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
    }
}

		

        	
                
        