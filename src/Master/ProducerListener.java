import java.net.*;
import java.io.*;

public class ProducerListener extends Thread {
	private int portNumber;
	private CubbyHole cubbyhole;

    public ProducerListener(CubbyHole c, int port) {
        super("TwoListeners");
        this.cubbyhole = c;
        this.portNumber = port;
        cubbyhole.addProducer();
    }

    protected int checkAvailability(int numRequested){
    	int numWaiting = cubbyhole.Qsize - cubbyhole.getQueueRatio();
    	int numGranted = numRequested - numWaiting;
    	if (numGranted < 0) numGranted = 0;

    	return numGranted;
    }

	protected void putData(Data data) {
        cubbyhole.put(data);
    }

    public void run(){
    	try (ServerSocket serverSocket = new ServerSocket(portNumber)){
    		while(true){
    			Socket s = serverSocket.accept();
    			System.out.println("Producer Called in!!");
    			int p = s.getPort();
    			InetAddress ia = s.getInetAddress();

	            BufferedReader inFromP = new BufferedReader(
	                new InputStreamReader(s.getInputStream()));
	            PrintWriter outToP = new PrintWriter(sLB.getOutputStream(), true);
	            String numRequested_str = inFromP.readline();
	            String requestType_str = inFromP.readline();
	            if (numRequested_str == null | requestType_str == null){
	            	System.out.println("Failed Request");
	            	return;
	            }
	            int numRequested = Integer.parseInt(numRequested_str);
	            int requestType = Integer.parseInt(requestType_str);
	            if (requestType != 1 & requestType != 2){
	            	System.out.println("Failed Request: Incorrect requestType");
	            	return;
	            }

	            int numGranted = checkAvailability(numRequested);
	            outToP.println(numGranted);

	            s.close();
	            for (int i = 0; i<numGranted; i++){
	            	putData(new Data(ia, p, requestType));
	            }
    		}
    	} catch (IOException e){
    		System.err.println("ProducerListener Error: "+e);
    		System.exit(-1);
    	}

    	cubbyhole.subProducer();
    }

}