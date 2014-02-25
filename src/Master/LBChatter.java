import java.net.*;
import java.io.*;
import org.hyperic.sigar.*;

public class LBChatter extends Thread {
    private CubbyHole c;
	private String hostName;
    private int portNumber;
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
        Socket sLB = new Socket(hostName, portNumber);
        BufferedReader inFromLB = new BufferedReader(
            new InputStreamReader(sLB.getInputStream()));
        PrintWriter outToLB = new PrintWriter(sLB.getOutputStream(), true);

        outToLB.println(clientPort);

        while (true) {
            // Hangs until asked for info
            inFromLB.readLine();

            System.out.println("LB Pinged Me");
            outToLB.println(c.getQueueRatio());
            outToLB.println(c.getProducerCount());
            outToLB.println(sigar.getLoadAverage()[0]);

        }
    }
}