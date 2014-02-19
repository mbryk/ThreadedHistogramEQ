import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.io.*;

public class KKMultiServerThread extends Thread {
    private Socket socket = null;

    public KKMultiServerThread(Socket socket) {
        super("KKMultiServerThread");
        this.socket = socket;
    }
    
    public void run() {
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
}
