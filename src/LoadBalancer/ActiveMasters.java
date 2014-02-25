public class ActiveMasters{
	private ArrayList<MasterObject> masterList = new ArrayList<MasterObject>();

	public synchronized void putSocket(Socket s) {
		PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		InetAddress ia = s.getInetAddress();
		int p = s.getPort();

		masterList.add(MasterObject(s, pw, br, ia, p));
	}

	private synchronized void removeSocket(int i){
		masterList.remove(i);
	}

	public synchronized Data requestStats(){
		int bestScore = 0; int bestIndex = null;
		for(int i=0; i<masterSocketList.size(); i++){
			masterWriterList.get(i).println("Give me your stats!");
			String queueRatio_str = masterReaderList.get(i).readLine();
			String numProducers_str = masterReaderList.get(i).readLine();
			String SigarInfo
			calcScore
			if calcScore > bestScore{
				bestScore = calcScore;
				bestIndex = i;
			}
		}
	}
}