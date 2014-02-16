import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.io.*;
import java.lang.Object;
import java.util.*;

public class KKMultiServerThread extends Thread {
    private Socket socket = null;

    public KKMultiServerThread(Socket socket) {
        super("KKMultiServerThread");
        this.socket = socket;
    }
    
    public void run() {
        System.out.println("starting Thread.");
        try (
            OutputStream outToClient = socket.getOutputStream();
            InputStream inFromClient = socket.getInputStream();
            ) {

            InputStream stream;
            stream = inFromClient;
            
            stream = new BufferedInputStream(stream);

            ImageInputStream imgStream;
            imgStream = ImageIO.createImageInputStream(stream);

            Iterator<ImageReader> i = 
                ImageIO.getImageReaders(imgStream);
            if (!i.hasNext()) {
                //logger.log(Level.FINE, "No ImageReaders found, exiting.");
                System.out.println("i.hasNext ERR");
            }

            ImageReader reader = i.next();
            reader.setInput(imgStream);

            BufferedImage image = reader.read(0);


            System.out.println("In try!");
            BufferedImage original, equalized;
            System.out.println("Reading");
            //original = ImageIO.read(inFromClient);
            System.out.println("Making Histogram");
            original = image;
            Histogram hist = new Histogram(original);
            equalized = hist.equalized;
            System.out.println("Returned Equalized");
            ImageIO.write(equalized,"jpg",outToClient);
            outToClient.flush();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
