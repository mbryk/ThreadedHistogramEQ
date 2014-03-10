import java.net.*;
import java.io.*;
import org.hyperic.sigar.*;

public class LBChatter extends Thread {
    private CubbyHole c;
    private String hostName;
    private int portNumber;
    private int clientPort;
    private static Sigar sigar;

    public LBChatter(CubbyHole cubbyhole, String host, int port, int clientPort) {
        super("TwoListeners");
        c = cubbyhole;
        hostName = host;
        portNumber = port;
        this.clientPort = clientPort;
        sigar = new Sigar();
    }

    public void run() {
        try{
            Socket sLB = new Socket(hostName, portNumber);
            BufferedReader inFromLB = new BufferedReader(
                new InputStreamReader(sLB.getInputStream()));
            PrintWriter outToLB = new PrintWriter(sLB.getOutputStream(), true);

            outToLB.println(clientPort);

            while (true) {
                // Hangs until asked for info
                String request = inFromLB.readLine();
                if (request == null) {
                    System.out.println("The LB has Died :(");
                    System.exit(-1);
                }

                System.out.println("LB Pinged Me");
                outToLB.println(c.getQueueRatio());
                // cf. ProducerListener. This doesn't actually tell us how many equalizers we have. Just how many clientlisteners we have (1) :(
                outToLB.println(c.getProducerCount()); 
                try{outToLB.println(sigar.getLoadAverage()[0]);}
                catch (SigarException e){
                    System.err.println("Sigar error:"+e);
                }

            }
        } catch (IOException e){
            System.err.println("LB Chatter error: "+e);
        }
    }
}