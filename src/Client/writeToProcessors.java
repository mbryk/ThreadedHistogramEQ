import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.net.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

public class writeToProcessors extends Thread{
	private ServerSocket s;
	private String[] fileNames;
	private int returnPort;
	private String in_dir;

	public writeToProcessors(ServerSocket s, String[] fileNames, int returnPort, String in_dir){
		this.s = s;
		this.fileNames = fileNames;
		this.returnPort = returnPort;
		this.in_dir = in_dir;
	}

	public void run(){

		try{
			//loop through images in directory
	        for(String fileName : fileNames) {
	            Socket echoSocket = s.accept();
	            System.out.println("Connected to Processing Server");

	            //get output stream and sent byte array image
	            OutputStream outToServer = echoSocket.getOutputStream();
	            PrintWriter outToP = new PrintWriter(outToServer, true);
	            ObjectOutputStream oos = new ObjectOutputStream(outToServer);

	            //read in file
	            System.out.println(fileName);
	            File ifile = new File(in_dir+"/"+fileName);
	            BufferedImage original = ImageIO.read(ifile);
	            
	            //convert original to byte array
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            ImageIO.write(original,"jpg", baos);
	            baos.flush();
	            byte[] originalByteImage = baos.toByteArray();

	            //give Processor info to return image
	            outToP.println(fileName);
	            outToP.println(returnPort);

	            //write image
	            oos.writeObject(originalByteImage);
	            // Tell Equalizer that you are done:
	            oos.writeObject(null);

	            echoSocket.close();
	        }
	    } catch (IOException e){
	    	System.err.println("writeToProcessors Error: "+e);
	    	System.exit(-1);
	    }
	}

}