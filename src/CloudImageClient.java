import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import java.lang.Object;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;


public class CloudImageClient {
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

        if (args.length != 4){
            System.err.println("Usage java CloudImageClient <host name> <port number> <original file> <output file>");
            System.exit(1);
        }
        
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        File original_f = new File(args[2]+".jpg");
        String output_f = args[3];
        BufferedImage original, received;

        original = ImageIO.read(original_f);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(original,"jpg", baos);
        baos.flush();
        byte[] originalByteImage = baos.toByteArray();
        
        Socket echoSocket = new Socket(hostName, portNumber);
        OutputStream outToServer = echoSocket.getOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(outToServer);

        InputStream inFromServer = echoSocket.getInputStream();

        oos.writeObject(originalByteImage);

        //boolean test = ImageIO.write(original,"jpg", outToServer);
        //outToServer.write(eof());
        //outToServer.flush();
        //outToServer.close();
        //System.out.println("Wrote"+test);        
        //received = ImageIO.read(inFromServer);
        //while(received == null){received = ImageIO.read(inFromServer);}

ObjectInputStream ois = new ObjectInputStream(inFromServer);
try{byte[] byteImage = (byte[])ois.readObject();

ByteArrayInputStream bais = new ByteArrayInputStream(byteImage);
received = ImageIO.read(bais);

        File file = new File(output_f);
        ImageIO.write(received,"jpg",file);}
        catch (ClassNotFoundException e){e.printStackTrace();}
        System.out.println("Done");
    }
}
