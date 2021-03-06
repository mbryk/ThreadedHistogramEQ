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

public class CloudImageClient {

    public static String[] parseDirectory(String dir_string){
        File dir = new File(dir_string);
        if(!dir.isDirectory()){
            System.out.println("Invalid directory entered");
            System.exit(-1);
        }

        String[] dirFiles = dir.list(new FilenameFilter() {
            public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".jpg");
            }
        });

        return dirFiles;
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        if (args.length != 4){
            System.err.println("Usage java CloudImageClient <host name> <port number> <original directory> <output directory>");
            System.exit(1);
        }
        
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        String in_dir = args[2];
        String out_dir = args[3];

        String[] fileNames = parseDirectory(in_dir);
        File out_dir_f = new File(out_dir);
        if (!out_dir_f.exists()){
            if(!out_dir_f.mkdir() && !out_dir_f.mkdirs()){
                System.out.println("Could Not Create Directory");
                System.exit(-1);
            }
        } else if(!out_dir_f.isDirectory()){
            System.out.println("Please input a directory name for output");
            System.exit(-1);
        }

        //connect to Load Balancer
        System.out.println("Connecting To Load Balancer");
        Socket sLB = new Socket(hostName, portNumber);
        BufferedReader inFromLB = new BufferedReader(
            new InputStreamReader(sLB.getInputStream()));
        String masterHostName = inFromLB.readLine();
        String masterPortString = inFromLB.readLine();
        int masterPort = Integer.parseInt(masterPortString);
        sLB.close();

        //initiate client socket to Master
        System.out.println("Connecting To Master Server");
        Socket s = new Socket(masterHostName, masterPort);
        
        //tell master how many images
        PrintWriter outToM = new PrintWriter(s.getOutputStream(), true);
        outToM.println(fileNames.length);

        int originalPort = s.getLocalPort();
        int returnPort = originalPort+1;
        s.setReuseAddress(true);

        // Listening for Processors to collect images
        ServerSocket sSend = new ServerSocket(originalPort);
        System.out.println("Port: "+originalPort);

        //separate thread to send files
        new writeToProcessors(sSend, fileNames, returnPort, in_dir).start();

        //receive in this thread
        ServerSocket sRcv = new ServerSocket(returnPort);
        
        for(int i=0; i<fileNames.length; i++) {
            Socket rcvSocket = sRcv.accept();
            InputStream inFromServer = rcvSocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(inFromServer);
            BufferedReader inFromP = new BufferedReader(
                new InputStreamReader(rcvSocket.getInputStream()));

            String fileName = inFromP.readLine();

            byte[] receivedByteImage = (byte[])ois.readObject();
            ByteArrayInputStream bais = new ByteArrayInputStream(receivedByteImage);
            BufferedImage received = ImageIO.read(bais);

            //write image to file
            File ofile = new File(out_dir+"/"+fileName);
            ImageIO.write(received,"jpg",ofile);
            System.out.println("Image Received and saved at " + out_dir + "/" + fileName);
            
            rcvSocket.close();
        }

    }
}
