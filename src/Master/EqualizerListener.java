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

    public boolean ping(Socket s){
        String received = null;
        try{
            PrintWriter outToEqualizer = new PrintWriter(s.getOutputStream(), true);
            BufferedReader inFromEqualizer = new BufferedReader(new InputStreamReader(s.getInputStream()));
            outToEqualizer.println("pingTest");
            received = inFromEqualizer.readLine();
        }catch (IOException e){
            System.err.println("ping Err: "+e);
        }
        if (received == null){
            System.out.println("disconnected");
            return false;
        } else {return true;}
    }

    public void run() {

    	Data data = null;
        boolean haveData = false;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (true) {
                Socket s = serverSocket.accept();
                System.out.println("Have New Waiting Processing Server");
                
                if ( !haveData )  {data=cubbyhole.get();}// { //value = null if contents of cubbyhole have been removed
                    System.out.println("Sending New Assignment: "+data.ia);
                    System.out.println("Closed?"+s.isClosed());
                    System.out.println("Connected?"+s.isConnected());
                	if(ping(s)){
                        System.out.println("Sending New Assignment");
                        PrintWriter outToEqualizer = new PrintWriter(s.getOutputStream(), true);
                        outToEqualizer.println(data.requestType);
                        outToEqualizer.println(data.ia.getHostName());
                        outToEqualizer.println(data.p);

                		haveData = false;
                		s.close();
                        System.out.println("Sent New Assignment");
                	} else {
                        haveData = true; 
                        System.out.println("He was impatient. Let's look for another processing server.");
                    }

//            	} else {s.close(); break;}
            }
            //serverSocket.close();
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
    }
}