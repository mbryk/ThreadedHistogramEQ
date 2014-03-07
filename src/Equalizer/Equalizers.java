import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.*;
import java.io.*;

public class Equalizers{
    protected static final int THREAD_POOL_SIZE = 5;

    public static void processData(Socket socket) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        BufferedImageArray array = new BufferedImageArray();
        int i = 0;

        try (
            OutputStream outToClient = socket.getOutputStream();
            InputStream inFromClient = socket.getInputStream();
            ) {
            byte[] originalByteImage, receivedByteImage;
            ObjectInputStream ois = new ObjectInputStream(inFromClient);

            while((originalByteImage = (byte[])ois.readObject())!=null){
             
                int sizeInBytes = originalByteImage.length;
                System.out.println("NUM BYTES: "+sizeInBytes);

                //convert byte array to BufferedImage
                ByteArrayInputStream bais = new ByteArrayInputStream(originalByteImage);
                BufferedImage image = ImageIO.read(bais);
                array.addImage(image,i);

//
//
// if (imSize > thresh)
    // split up, run imageHistogram on each piece on another node
    // wait for all, as they come back add to ArrayList of ArrayLists
    // run combineHists on ArrayLists<ArrayLists<int[]>>
    // run calcScaleFactor
    // ask Master for more Equalizers again
    // run eq
//
//


                Runnable worker = new ProcessingWorkerThread(array,i);
                executor.execute(worker);

                i++;
            }
            int imageCount = i;

            executor.shutdown();


//
//
//is there no .join or something in order to wait for things
//rather than running a while loop??
//
//

            while (!executor.isTerminated()) {}
            System.out.println("Finished all threads");
            
            // workers are done
            ObjectOutputStream oos = new ObjectOutputStream(outToClient);

            for(i=0;i<imageCount;i++){
                //convert to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(array.getImage(i),"jpg", baos);
                baos.flush();
                receivedByteImage = baos.toByteArray();
            
                //send equalized image
                
                oos.writeObject(receivedByteImage);
            }
            socket.close();
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (ClassNotFoundException e){ e.printStackTrace(); }
    }
    
    public static void main(String[] args) {
        if (args.length != 2){
            System.err.println("Usage java Equalizers <Master host name> <Master port number>");
            System.exit(1);
        }        

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        int i;

        while(true){
            try{
                System.out.println("Waiting for Next Assignment");
                Socket clientInfoSocket = new Socket(hostName,portNumber); // To ConsumerListener
                BufferedReader inFromMaster = new BufferedReader(
                    new InputStreamReader(clientInfoSocket.getInputStream()));
                
                PrintWriter outToMaster = new PrintWriter(clientInfoSocket.getOutputStream(), true);
                String receivedPing = inFromMaster.readLine(); //accept ping
                outToMaster.println(receivedPing);

                String clientHostName = inFromMaster.readLine();
                String clientPortString = inFromMaster.readLine();
                //String imageCountString = inFromMaster.readLine();
                System.out.println("Received Assignment");

                int clientPort = Integer.parseInt(clientPortString);
                //int imageCount = Integer.parseInt(imageCountString);

                Socket socket = new Socket(clientHostName,clientPort);
                System.out.println("Connected to Client at Port "+clientPort);

                //processData(socket,imageCount);
                processData(socket);
                System.out.println("Processed Images and Returned to Client");
                
            } catch(IOException e){
                System.err.println(e);
                System.err.println("Client Gone, probably");
            }
        }
    }
}