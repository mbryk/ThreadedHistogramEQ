import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.io.*;

import java.lang.Object;
import java.io.ObjectInputStream;

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
            System.out.println("In try!");
            BufferedImage original, equalized;
            System.out.println("Reading");

ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
byte[] byteImage = (byte[])ois.readObject();

ByteArrayInputStream bais = new ByteArrayInputStream(byteImage);
original = ImageIO.read(bais);

            //original = ImageIO.read(inFromClient);
            System.out.println("Making Histogram");
            Histogram hist = new Histogram(original);
            equalized = hist.equalized;
            System.out.println("Returned Equalized");

File file = new File("TestOut");

            //ImageIO.write(equalized,"jpg",outToClient);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(original,"jpg", baos);
        baos.flush();
        byte[] originalByteImage = baos.toByteArray();
        ObjectOutputStream oos = new ObjectOutputStream(outToClient);
        oos.writeObject(originalByteImage);

//ImageIO.write(equalized,"jpg",file);
System.out.println("Printed");
            //outToClient.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e){e.printStackTrace();}
    }
}
