import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.io.*;

public class ProcessingServers extends Thread {
    private CubbyHole cubbyhole;
    private int number;
    private int numConsumed;
    private Socket socket;

    public ProcessingServers(CubbyHole c) {
        cubbyhole = c;
    }
 
    public void processData() {
        System.out.println("Started Server Thread.");
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

            System.out.println("Finished Server Thread.");
            socket.close();
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (ClassNotFoundException e){ e.printStackTrace(); }
    }
    
    public void run() {
        Data data = null;

        while (!cubbyhole.isDone()) {
            data = cubbyhole.get();
    
            if(data != null) { //value = null if contents of cubbyhole have been removed
                
                try{
                socket = new Socket(data.ia, data.p);

                System.out.println("Got new S");

                numConsumed++;
                processData();
                } catch(IOException e){System.out.println(e);}   
                
                System.out.println("Consumer #" + this.number
                                 + " got: " + data);
                try {
                    Thread.sleep((int)(Math.random() * 100));
                } catch (InterruptedException e) {};
            }
        }
        System.out.println("Consumer #" + this.number
                        + " consumed: " + numConsumed);

    }
}