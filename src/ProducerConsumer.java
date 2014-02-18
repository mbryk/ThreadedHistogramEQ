public class ProducerConsumer {
	
    public static void main(String[] args) {
		if (args.length != 1) {
            System.err.println("Usage: java ProducerConsumer <port number>");
            System.exit(1);
        }

		int cores = Runtime.getRuntime().availableProcessors();
		int numProds = 1; //cores / 2;
		int numCons = cores - numProds;
		System.out.println("Starting " + numProds + " Producers and " + numCons + " Consumers.");
		CubbyHole c = new CubbyHole();
		int portNumber = Integer.parseInt(args[0]);
		for(int i=0;i<numProds;i++) {
			MasterServer cp = new MasterServer(c,portNumber);
			cp.start();
		}
		for(int i=0;i<numCons;i++) {
			ProcessingServers cc = new ProcessingServers(c);
			cc.start();
		}
    }
}
