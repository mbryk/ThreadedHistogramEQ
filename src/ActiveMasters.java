public class ActiveMasters{
	private ArrayList<Socket> masterSocketList = new ArrayList<Socket>();
	private ArrayList<PrintWriter> masterWriterList = new ArrayList<PrintWriter>();
	private ArrayList<BufferedReader> masterReaderList = new ArrayList<BufferedReader>();

	public synchronized void putSocket(Socket s) {
		masterSocketList.add(s);
		masterWriterList.add(new PrintWriter(s.getOutputStream(), true));
		masterReaderList.add(new BufferedReader(new InputStreamReader(s.getInputStream())));
	}

	private synchronized void removeSocket(int i){
		masterSocketList.remove(i);
		masterWriterList.remove(i);
		masterReaderList.remove(i);
	}

	public synchronized Data requestStats(){
		int bestScore = 0; int bestIndex = null;
		for(int i=0; i<masterSocketList.size(); i++){
			masterWriterList.get(i).println("Give me your stats!");
			String queueRatio_str = masterReaderList.get(i).readLine();
			String numProducers_str = masterReaderList.get(i).readLine();
		}
	}
}