import java.net.*;
import java.io.*;

public class LoadBalancer {
	
	public static void main(String[] args) {
    	if (args.length != 2){
            System.err.println("Usage java LoadBalancer <Client Port Number> <Master Port Number>");
            System.exit(1);
        }

    	int clientPort = Integer.parseInt(args[0]);
    	int masterPort = Integer.parseInt(args[1]);

    	new clientSideLB(c, clientPort).start();
    	new masterSideLB(c, masterPort).start();        
	}
}