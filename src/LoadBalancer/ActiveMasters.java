import java.lang.Object;
import java.util.*;
import java.net.*;
import java.io.*;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ActiveMasters{
	private ArrayList<MasterObject> masterList = new ArrayList<MasterObject>();

	public ActiveMasters(){}

	public void putSocket(Socket s) {
		try{
			PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			InetAddress ia = s.getInetAddress();
			String p_str = br.readLine();
			int p = Integer.parseInt(p_str);

			masterList.add(new MasterObject(s, pw, br, ia, p));
		} catch (IOException e){
			System.err.println("putSocket error: "+e);
		}
	}

	private void removeSocket(int i){
		masterList.remove(i);
	}

	private float calcScore(String queueRatio_str, String numProducers_str, String loadPrevMin_str){
		return ((1/Float.parseFloat(queueRatio_str)) + (1/Float.parseFloat(loadPrevMin_str)));
	}

	public synchronized MasterObject requestStats(){
		float bestScore = 0; int bestIndex = 0;
		for(int i=0; i<masterList.size(); i++){
			masterList.get(i).pw.println("Give me your stats!");
			try{
				String queueRatio_str = masterList.get(i).br.readLine();
				String numProducers_str = masterList.get(i).br.readLine();
				String loadPrevMin_str = masterList.get(i).br.readLine();
				if (queueRatio_str == null || numProducers_str == null || loadPrevMin_str == null){
					removeSocket(i);
				} else{
					float score = calcScore(queueRatio_str, numProducers_str, loadPrevMin_str);
					if (score > bestScore) {
						bestScore = score;
						bestIndex = i;
					}
				}
		} catch (IOException e){
			System.err.println("requestStats error: "+e);
		}
	}
		return masterList.get(bestIndex);
	}
}