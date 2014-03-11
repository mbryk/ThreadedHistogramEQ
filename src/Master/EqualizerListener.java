import java.net.*;
import java.io.*;

public class EqualizerListener extends Thread {
	private int portNumber;
	private CubbyHole cubbyhole;

    public EqualizerListener(CubbyHole c, int port) {
        cubbyhole = c;
        portNumber = port;
    }

    public void run() {

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