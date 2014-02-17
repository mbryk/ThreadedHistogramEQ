import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.net.Socket;
import javax.imageio.ImageIO;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;


public class CloudImageClient {
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        if (args.length != 4){
            System.err.println("Usage java CloudImageClient <host name> <port number> <original file> <output file>");
            System.exit(1);
        }
        
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        File original_f = new File(args[2]+".jpg");
        String output_f = args[3];
        BufferedImage original, received;
        byte[] originalByteImage, receivedByteImage;

        original = ImageIO.read(original_f);

        //convert original to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(original,"jpg", baos);
        baos.flush();
        originalByteImage = baos.toByteArray();
        
        //initiate client socket
        Socket echoSocket = new Socket(hostName, portNumber);

        //get output stream and sent byte array image
        OutputStream outToServer = echoSocket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outToServer);
        oos.writeObject(originalByteImage);

        //get input stream and receive byte array image
        InputStream inFromServer = echoSocket.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(inFromServer);
        receivedByteImage = (byte[])ois.readObject();

        //convert byte array to BufferedImage
        ByteArrayInputStream bais = new ByteArrayInputStream(receivedByteImage);
        received = ImageIO.read(bais);

        //write image to file
        File file = new File(output_f);
        ImageIO.write(received,"jpg",file);
        System.out.println("Done");
    }
}
