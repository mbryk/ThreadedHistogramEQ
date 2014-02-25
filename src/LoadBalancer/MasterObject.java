import java.net.*;
import java.io.*;

public class MasterObject{

	Socket s;
	PrintWriter pw;
	BufferedReader br;
	InetAddress ia;
	int p;

	public MasterObject(Socket s, PrintWriter pw, BufferedReader br, InetAddress ia, int p){
		this.s = s;
		this.pw = pw;
		this.br = br;
		this.ia = ia;
		this.p = p;
	}

	@Override 
	public boolean equals(MasterObject o){
		//return ia==o.ia
		return (ia==o.ia && p==o.p);
	}

}