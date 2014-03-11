import java.net.*;
import java.io.*;

public class ClientProcessorAssigner extends Thread {
	private CubbyHole cubbyhole;

	public ClientProcessorAssigner(CubbyHole c){
		this.cubbyhole = c;
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

	public void run(){
		boolean havedata = false;
		try{
			while(true){
				Data curProcessor = cubbyhole.getProcessor();
				if (!havedata) Data data = cubbyhole.get();

				Socket s = new Socket(curProcessor.ia, curProcessor.p);
				if(ping(s)){
		            System.out.println("Sending New Assignment");
		            PrintWriter outToEqualizer = new PrintWriter(s.getOutputStream(), true);
		            outToEqualizer.println(data.requestType);
		            outToEqualizer.println(data.ia.getHostName());
		            outToEqualizer.println(data.p);

		    		haveData = false;
		    		s.close();
		            System.out.println("Sent New Assignment");
				} else{
					havedata = true;
					System.out.println("He was impatient. Let's look for another processing server.");
				}
			}
		} catch (IOException e){
			System.err.println("ClientProcessorAssigner Error: "+e);
			System.exit(-1);
		}
	}

}