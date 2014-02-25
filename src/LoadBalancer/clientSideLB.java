import java.net.*;
import java.io.*;
import org.hyperic.sigar.*;

public class clientSideLB extends Thread {

	public clientSideLB(){

	}

	public void run(){

		try (ServerSocket serverSocket = new ServerSocket(3500)) { 
            while (true) {
                System.out.println("Listening for Clients");
                Socket s = serverSocket.accept();

                MasterObject master = getMasterInfo();

                PrintWriter outToClient = new PrintWriter(s.getOutputStream(), true);
                outToClient.println(master.ia.getHostName());
                outToClient.println(master.p);
                //outToClient.println("127.0.0.1");
                ///outToClient.println("40000");
                s.close();
        
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
        
	}



}