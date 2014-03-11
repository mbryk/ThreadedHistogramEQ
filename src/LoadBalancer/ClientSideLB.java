import java.net.*;
import java.io.*;

public class ClientSideLB extends Thread {
	private ActiveMasters masters;
	private int portNumber;

	public ClientSideLB(ActiveMasters masters, int port) {
		this.masters = masters;
		portNumber = port;
	}

	public void run(){

		try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (true) {
                System.out.println("Listening for Clients");
                Socket s = serverSocket.accept();

                MasterObject master = masters.requestStats();

                PrintWriter outToClient = new PrintWriter(s.getOutputStream(), true);
                outToClient.println(master.ia.getHostName());
                outToClient.println(master.p);
                s.close();
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
        
	}



}