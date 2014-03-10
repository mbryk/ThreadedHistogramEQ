import java.net.*;
import java.io.*;

public class ProducerListener extends Thread {
    public static final int FULL_IMAGE = 0;
    public static final int HELPER_HISTOGRAM = 1;
    public static final int HELPER_SCALING = 2;

	private int portNumber;
	private CubbyHole cubbyhole;

    public ProducerListener(CubbyHole c, int port) {
        super("TwoListeners");
        this.cubbyhole = c;
        this.portNumber = port;
        cubbyhole.addProducer();
    }

    // Not sure this works. numWaiting tells you the amount of clients waiting in the cubbyhole. not the amount of equalizers waiting to help.
    // Need to add a member variable which is incremented inside the equalizerlistener and decremented when an assignment is given, which counts how many equalizers are available to help.
    // Unfortunately, we only connect to one equalizer at a time, so that number is apparently inaccessible.
    
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
	            if (numRequested_str == null || requestType_str == null){
	            	System.out.println("Failed Request");
	            	return;
	            }
	            int numRequested = Integer.parseInt(numRequested_str);
	            int requestType = Integer.parseInt(requestType_str);
	            if (requestType != HELPER_SCALING && requestType != HELPER_HISTOGRAM){
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