import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.io.*;

public class Equalizers extends Thread {
    private int number;
    private int numConsumed;

    public static void processData(Socket socket) {
        try (
            OutputStream outToClient = socket.getOutputStream();
            InputStream inFromClient = socket.getInputStream();
            ) {
            BufferedImage original, equalized;
            byte[] originalByteImage, receivedByteImage;

            //get original byte image
            ObjectInputStream ois = new ObjectInputStream(inFromClient);
            originalByteImage = (byte[])ois.readObject();

            //convert byte array to BufferedImage
            ByteArrayInputStream bais = new ByteArrayInputStream(originalByteImage);
            original = ImageIO.read(bais);

            //run Histogram
            Histogram hist = new Histogram(original);
            equalized = hist.equalized;

            //convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(equalized,"jpg", baos);
            baos.flush();
            receivedByteImage = baos.toByteArray();
            
            //send equalized image
            ObjectOutputStream oos = new ObjectOutputStream(outToClient);
            oos.writeObject(receivedByteImage);

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

        while(true){
            try{
                System.out.println("Waiting for Next Assignment");
                Socket clientInfoSocket = new Socket(hostName,portNumber); // To ConsumerListener
                BufferedReader inFromMaster = new BufferedReader(
                    new InputStreamReader(clientInfoSocket.getInputStream()));
                String clientHostName = inFromMaster.readLine();
                String clientPortString = inFromMaster.readLine();
                System.out.println("Received Assignment");

                int clientPort = Integer.parseInt(clientPortString);

                Socket socket = new Socket(clientHostName,clientPort);
                System.out.println("Connected to Client at Port "+clientPort);

                processData(socket);
                System.out.println("Processed Image and Returned to Client");
                
            } catch(IOException e){
                System.err.println(e);
                System.err.println("Client Gone, probably");
            }
        }
        
    }
}