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

    public static void processData(Socket socket, int imageCount) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        BufferedImageArray array = new BufferedImageArray(imageCount);

        try (
            OutputStream outToClient = socket.getOutputStream();
            InputStream inFromClient = socket.getInputStream();
            ) {
            byte[] originalByteImage, receivedByteImage;


            for(i=0;i<imageCount;i++){
                //get original byte image
                ObjectInputStream ois = new ObjectInputStream(inFromClient);
                originalByteImage = (byte[])ois.readObject();

                //convert byte array to BufferedImage
                ByteArrayInputStream bais = new ByteArrayInputStream(originalByteImage);
                BufferedImage image = ImageIO.read(bais);
                array.addImage(image,i);
                Runnable worker = new ProcessingWorkerThread(array,i);
                executor.execute(worker);
            }

            executor.shutdown();
            while (!executor.isTerminated()) {}
            System.out.println("Finished all threads");
            
            // workers are done
            for(i=0;i<imageCount;i++){
                //cnvert to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(array.getImage(i),"jpg", baos);
                baos.flush();
                receivedByteImage = baos.toByteArray();
            
                //send equalized image
                ObjectOutputStream oos = new ObjectOutputStream(outToClient);
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
                String clientHostName = inFromMaster.readLine();
                String clientPortString = inFromMaster.readLine();
                String imageCountString = inFromMaster.readLine();
                System.out.println("Received Assignment");

                int clientPort = Integer.parseInt(clientPortString);
                int imageCount = Integer.parseInt(imageCountString);

                Socket socket = new Socket(clientHostName,clientPort);
                System.out.println("Connected to Client at Port "+clientPort);

                processData(socket,imageCount);
                System.out.println("Processed Images and Returned to Client");
                
            } catch(IOException e){
                System.err.println(e);
                System.err.println("Client Gone, probably");
            }
        }
    }
}