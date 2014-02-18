public class TwoListeners {
	
    public static void main(String[] args) {
    	if (args.length != 2){
            System.err.println("Usage java TwoListeners <Client Port Number> <Equalizers Port Number>");
            System.exit(1);
        }

    	int clientPort = Integer.parseInt(args[0]);
    	int processingPort = Integer.parseInt(args[1]);

    	CubbyHole c = new CubbyHole();

    	new ClientListener(c, clientPort).start();
    	new EqualizerListener(c, processingPort).start();
    }
}
