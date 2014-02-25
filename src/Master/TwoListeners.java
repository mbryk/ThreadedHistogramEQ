public class TwoListeners {
	
    public static void main(String[] args) {
    	if (args.length != 4){
            System.err.println("Usage: java TwoListeners <Client Port Number> <Equalizers Port Number> <LB Hostname> <LB Port>");
            System.exit(1);
        }

    	int clientPort = Integer.parseInt(args[0]);
    	int processingPort = Integer.parseInt(args[1]);

        int lbPort = Integer.parseInt(args[3]);


    	CubbyHole c = new CubbyHole();

        new LBChatter(c, args[2],lbPort).start();
    	new ClientListener(c, clientPort).start();
    	new EqualizerListener(c, processingPort).start();
    }
}
