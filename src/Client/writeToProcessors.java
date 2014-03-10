import java.net.*;
import java.io.*;

public class writeToProcessors extends Thread{
	private ServerSocket s2;
	private String[] filenames;
	private int returnPort;

	public writeToProcessors(ServerSocket s2, String[] filenames, int returnPort){
		this.s2 = s2;
		this.filenames = filenames;
		this.returnPort = returnPort;
	}

	public void run(){

		try{
			//loop through images in directory
	        for(String fileName : fileNames) {
	            Socket echoSocket = s2.accept();
	            System.out.println("Connected to Processing Server");

	            //get output stream and sent byte array image
	            PrintWriter outToP = new PrintWriter(s2.getOutputStream(), true);
	            OutputStream outToServer = echoSocket.getOutputStream();
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