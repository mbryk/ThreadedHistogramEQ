public class LoadBalancer {



	protected static Data getMasterInfo(){


	}
	
	public static void main(String[] args) {



		try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (true) {
                System.out.println("Listening for Clients");
                Socket s = serverSocket.accept();

                Data master = getMasterInfo();

                PrintWriter outToClient = new PrintWriter(s.getOutputStream(), true);
                outToClient.println(master.ia.getHostName());
                outToClient.println(master.p);
                s.close();
        

                /*int p = s.getPort();
                System.out.println("New Client at Port "+p);
                InetAddress ia = s.getInetAddress();
                s.close();
                
                putData(new Data(ia,p));*/
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
        
        cubbyhole.subProducer();


	}



}