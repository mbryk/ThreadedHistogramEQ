public class ActiveMasters{
	private ArrayList<MasterObject> masterList = new ArrayList<MasterObject>();

	public void putSocket(Socket s) {
		PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		InetAddress ia = s.getInetAddress();
		int p = s.getPort();

		masterList.add(MasterObject(s, pw, br, ia, p));
	}

	private void removeSocket(int i){
		masterList.remove(i);
	}

	private float calcScore(String queueRatio_str, String numProducers_str, String loadPrevMin_str){
		return ((1/Float.parseFloat(queueRatio_str)) + (1/Float.parseFloat(loadPrevMin_str)));
	}

	public synchronized MasterObject requestStats(){
		float bestScore = 0; int bestIndex = null;
		for(int i=0; i<masterList.size(); i++){
			masterList.get(i).pw.println("Give me your stats!");
			String queueRatio_str = masterList.get(i).br.readLine();
			String numProducers_str = masterList.get(i).br.readLine();
			String loadPrevMin_str = masterList.get(i).br.readLine();
			if (queueRatio_str == null || numProducers_str == null || loadPrevMin_str == null){
				removeSocket(i);
			} else{
				float score = calcScore(queueRatio_str, numProducers_str, loadPrevMin_str);
				if (calcScore > bestScore) {
					bestScore = calcScore;
					bestIndex = i;
				}
			}
		}
		return masterList.get(i);
	}
}