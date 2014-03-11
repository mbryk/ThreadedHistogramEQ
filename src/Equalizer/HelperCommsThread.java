import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

public class HelperCommsThread extends Thread {
    private Socket socket;
    private int type;
    private BufferedImageArray array;
    private HistogramSplit histogram;
    private int index;
    //private int waiting;

    public HelperCommsThread(Socket socket,int type,BufferedImageArray array, HistogramSplit histogram, int index) {
        super("Equalizers");
        this.socket = socket;
        this.type = type;
        this.array = array;
        this.histogram = histogram;
        this.index = index;
        //this.waiting = waiting;
    }
 
    public void run() {
        BufferedImage image = array.getImage(index);
        
      try{
        OutputStream outToHelper = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outToHelper);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image,"jpg", baos);
        baos.flush();
        byte[] receivedByteImage = baos.toByteArray();
        oos.writeObject(receivedByteImage);
      } catch (IOException e){
        System.out.println("Error Sending Image: "+e);
        System.exit(-1);
      }


        if(type==1) receiveHistogram();
        else receiveImage();
        Equalizers.waiting++;
        System.out.println("Waiting: "+Equalizers.waiting);
    }

    private void receiveHistogram(){
        ArrayList<int[]> myHist = new ArrayList<int[]>();
        myHist.add(new int[256]);
        myHist.add(new int[256]);
        myHist.add(new int[256]);
      try{
        BufferedReader inFromHelper = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        
        int i;
        for(int[] intArray : myHist){
            for(i = 0; i<256; i++){
                String curVal_str = inFromHelper.readLine();
                intArray[i] = Integer.parseInt(curVal_str);
            }
        }
      } catch(IOException e){
        System.out.println("Error Reading Histogram: "+e);
        System.exit(-1);
      }

        histogram.addHist(myHist);
    }

    private void receiveImage(){
        //socket.sendStuff(histogram.getLUT());
      try{
        PrintWriter outToHelper = new PrintWriter(socket.getOutputStream(), true);
        ArrayList<int[]> myLUT = histogram.getLUT();
        int j;
        for(int[] intArray : myLUT){
            for (j = 0; j<256; j++){
                outToHelper.println(intArray[j]);
            }
        }
        InputStream inFromHelper = socket.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(inFromHelper);
        byte[] equalizedByteImage = (byte[])ois.readObject();
        ByteArrayInputStream bais = new ByteArrayInputStream(equalizedByteImage);
        BufferedImage equalized = ImageIO.read(bais);
        array.addImage(equalized,index);
      } catch (IOException e){
        System.out.println("Error Sending Image: "+e);
        System.exit(-1);
      } catch (ClassNotFoundException e){
        System.out.println("Error Sending Image: "+e);
        System.exit(-1);
      }      
    }
}