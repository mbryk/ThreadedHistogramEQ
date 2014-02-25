import java.net.*;
import java.io.*;

public class Data {
	public InetAddress ia;
	public int p;
	
	public Data(InetAddress ia,int p){
		this.ia = ia;
		this.p = p;
	}
	
	public String toString(){
		return "ia: "+ia+" and p:"+p;
	}
}
