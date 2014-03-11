public class Master {
	
    public static void main(String[] args) {
    	if (args.length != 5){
            System.err.println("Usage: java Master <Client Port Number> <Equalizers Port Number> <LB Hostname> <LB Port> <Producer Port>");
            System.exit(1);
        }

    	int clientPort = Integer.parseInt(args[0]);
    	int processingPort = Integer.parseInt(args[1]);

        int lbPort = Integer.parseInt(args[3]);
        int producerPort = Integer.parseInt(args[4]);


    	CubbyHole c = new CubbyHole();

        new LBChatter(c, args[2],lbPort, clientPort).start();
    	new ClientListener(c, clientPort).start();
    	new EqualizerListener(c, processingPort).start();
        new ProducerListener(c,producerPort).start();
        new ClientProcessorAssigner(c).start();
    }
}
