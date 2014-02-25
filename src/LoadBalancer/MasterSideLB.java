import java.io.*;
import java.net.*;

public class MasterSideLB extends Thread {
	private ActiveMasters masters;
	private int portNumber;

	public MasterSideLB(ActiveMasters masters, int port) {
		this.masters = masters;
		portNumber = port;
	}

	public void run(){
		try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (true) {
                Socket s = serverSocket.accept();
                System.out.println("New Master Logged In!");

                masters.putSocket(s);
            }
            //serverSocket.close();
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
	}
}