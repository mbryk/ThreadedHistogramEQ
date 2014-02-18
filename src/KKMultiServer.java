import java.net.*;
import java.io.*;

public class KKMultiServer {
    public static void main(String[] args) throws IOException {

    if (args.length != 1) {
        System.err.println("Usage: java KKMultiServer <port number>");
        System.exit(1);
    }

        int portNumber = Integer.parseInt(args[0]);
        boolean listening = true;
        
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (listening) {
                
                Socket s = serverSocket.accept();
                int p = s.getPort();
                System.out.println("Port: "+p);
                InetAddress ia = s.getInetAddress();
                s.close();

                System.out.println("HERE");
                Socket newSocket = new Socket(ia, p);
	            System.out.println("Got new S");

                new KKMultiServerThread(newSocket).start();
	        }
	    } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
    }
}
