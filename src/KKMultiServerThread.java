import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.io.*;

public class KKMultiServerThread extends Thread {
    private Socket socket = null;

    public KKMultiServerThread(Socket socket) {
        super("KKMultiServerThread");
        this.socket = socket;
    }
    
    public void run() {
        try (
            OutputStream outToClient = socket.getOutputStream();
            InputStream inFromClient = socket.getInputStream();
            ) {
            BufferedImage original, equalized;
            original = ImageIO.read(inFromClient);
            Histogram hist = new Histogram(original);
            equalized = hist.equalized;
            ImageIO.write(equalized,"jpg",outToClient);
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
