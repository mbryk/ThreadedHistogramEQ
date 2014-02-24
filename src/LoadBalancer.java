import java.net.*;
import java.io.*;

public class LoadBalancer {
	private ArrayList<Socket> masterSocketList = new ArrayList<Socket>();
	private ArrayList<PrintWriter> masterWriterList = new ArrayList<PrintWriter>();
	private ArrayList<BufferedReader> masterReaderList = new ArrayList<BufferedReader>();


	protected static Data getMasterInfo(){
		Socket curSocket;

		for(int i=0; i<masterSocketList.size(); i++){
			curSocket = masterSocketList.get(i);
			masterWriterList.get(i).println("Give me your stats!");
			String queueRatio_str = masterReaderList.get(i).readLine();
			String numProducers_str = masterReaderList.get(i).readLine();
			if (queueRatio_str == null || numProducers_str == null){
				masterSocketList.remove(i);
				masterWriterList.remove(i);
				masterReaderList.remove(i);
			}
		}


	}
	
	public static void main(String[] args) {



		try (ServerSocket serverSocket = new ServerSocket(3500)) { 
            while (true) {
                System.out.println("Listening for Clients");
                Socket s = serverSocket.accept();

                //Data master = getMasterInfo();

                PrintWriter outToClient = new PrintWriter(s.getOutputStream(), true);
                //outToClient.println(master.ia.getHostName());
                //outToClient.println(master.p);
                outToClient.println("127.0.0.1");
                outToClient.println("40000");
                s.close();
        
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
        
	}



}