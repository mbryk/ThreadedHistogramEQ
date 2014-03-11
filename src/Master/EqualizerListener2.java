import java.net.*;
import java.io.*;

public class EqualizerListener2 extends Thread {
	private int portNumber;
	private CubbyHole cubbyhole;

    public EqualizerListener2(CubbyHole c, int port) {
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
                
                BufferedReader inFromEqualizer = 
                    new BufferedReader(new InputStreamReader(s.getInputStream()));

                String procLoad_str = inFromEqualizer.readLine();
                String procThreads_str = inFromEqualizer.readLine();

                float procLoad = Float.parseFloat(procLoad_str);
                int procThreads = Integer.parseInt(procThreads_str);

                float priority = 1/procLoad + (float)procThreads;

                int p = s.getPort();
                InetAddress ia = s.getInetAddress();

                s.close();

                cubbyhole.putProcessor(new Data(ia, p, -1, priority));
            }

        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
    }
}