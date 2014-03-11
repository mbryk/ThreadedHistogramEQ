import java.net.*;
import java.io.*;

public class Data implements comparable<Data>{
	public InetAddress ia;
	public int p;
	public int requestType; // Relevant for clientQueue objects
	public float priority; // Relevant for processorQueue objects
	// 0: Client request to proc images
	// 1: Producer request to calc histogram
	// 2: Producer request to rescale image
	
	public Data(InetAddress ia,int p, int requestType){
		this.ia = ia;
		this.p = p;
		this.requestType = requestType;
	}
	
	public String toString(){
		return "ia: "+ia+" and p:"+p;
	}

	public int compareTo(Data compareData){
		int compareQuantity = ((Data) compareData).priority;

		return -(this.priority - compareQuantity);
	}
}
