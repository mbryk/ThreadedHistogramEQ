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
    public static final int FULL_IMAGE = 0;
    public static final int HELPER_HISTOGRAM = 1;
    public static final int HELPER_SCALING = 2;

    protected static final int THREAD_POOL_SIZE = 5;
    private static String masterHostName;
    private static int masterPortNumber;
    private static int masterRequestPortNumber;
    private static BufferedImageArray array;

    protected int splitUp(BufferedImage image,int size){
        int pieceSize = 10000*2^10; // 10 MB
        int pieces = size/pieceSize + 1;
        if(pieces>1){
            int height = image.getHeight(); int width = image.getWidth();
            int interval = height/pieces;
            int ylow = 0; int y=0; int i=0;
            for(int y = 0; y<height; y+= interval){
                if(y+interval>height){ interval = height-y; }
                subImage = image.getSubImage(0,y,width,interval);
                array.addImage(subImage);
            }
        } else array.addImage(image);

        return pieces;
    }

    protected BufferedImage recombineImage(BufferedImage oldImage){
        BufferedImage image = new BufferedImage(oldImage.getWidth(), oldImage.getHeight(), oldImage.getType());
        Graphics g = image.getGraphics();
        BufferedImage subImage;
        int y = 0;

        for(i=0;i<array.size();i++){
            subImage = array.getImage(i);
            g.drawImage(subImage,0,y,null);
            y += subImage.getHeight();
        }
        return image;
    }

    public static void processFullData(Socket socket) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        array = new BufferedImageArray();
        int i = 0;

        try (
            OutputStream outToClient = socket.getOutputStream();
            InputStream inFromClient = socket.getInputStream();
            ) {
            byte[] originalByteImage, receivedByteImage;
            ObjectInputStream ois = new ObjectInputStream(inFromClient);

            if((originalByteImage = (byte[])ois.readObject())==null){
                sendNullBack(); // Tell Client that something was screwed up with the image
                return;
            }
             
            int sizeInBytes = originalByteImage.length;
            System.out.println("NUM BYTES: "+sizeInBytes);

            //convert byte array to BufferedImage
            ByteArrayInputStream bais = new ByteArrayInputStream(originalByteImage);
            BufferedImage image = ImageIO.read(bais);

            int imParts = splitUp(image,sizeInBytes); // Add pieces of image to the array and return array size
            int workerCount = THREAD_POOL_SIZE*2;

            if(imParts>workerCount*2){ //If I can't make it through the image in 2 go-rounds, then ask for help.
                // This function will use the member variables of this class of the master info.
                // It will return the amount of helpers that are being allotted.
                
                Socket reqHelpers = socket(masterHostName, masterRequestPortNumber);
                BufferedReader inFromMReq = new BufferedReader(
                    new InputStreamReader(reqHelpers.getInputStream()));
                PrintWriter outToMReq = new PrintWriter(reqHelpers.getOutputStream(), true);
                outToMReq.println(imParts);
                outToMReq.println(requestType);

                String helpersComing_str = inFromMReq.readline();
                int helpersComing = Integer.parseInt(helpersComing_str);

                try(ServerSocket getHelpers = helpersComing){
                    while(int h=0;h<helpersComing;h++){
                        Socket helper = getHelpers.accept();
                    }
                }
                
            } else{ // You can do it on your own!!
                
                if(imParts==1){
                    BufferedImage original = array.getImage(index);
                    Histogram histogram = new Histogram(original);
                    array.addImage(histogram.equalized,index);
                } else{
                    HistogramSplit histogram = new HistogramSplit();

                    for(i=0;i<imParts;i++){
                        Runnable worker = new HistogrammingWorkerThread(array,histogram,i);    
                        executor.execute(worker);
                        executor.shutdown();
                        while (!executor.isTerminated()) {}
                    }

                    histogram.calcHistogramLUT()
                
                    for(i=0;i<imParts;i++){
                        Runnable worker = new EqualizingWorkerThread(array,histogram,i);    
                        executor.execute(worker);
                        executor.shutdown();
                        while (!executor.isTerminated()) {}
                    }    
                
                    BufferedImage processedImage = recombineImage(image);                

                    System.out.println("Finished all threads");
                }

            }
            
            // workers are done
            ObjectOutputStream oos = new ObjectOutputStream(outToClient);

            //convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processedImage,"jpg", baos);
            baos.flush();
            receivedByteImage = baos.toByteArray();
            oos.writeObject(receivedByteImage);
            socket.close();
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (ClassNotFoundException e){ e.printStackTrace(); }
    }
    
    public static void main(String[] args) {
        if (args.length != 3){
            System.err.println("Usage java Equalizers <Master host name> <Master port number> <Master's Requesting Port>");
            System.exit(1);
        }        

        masterHostName = args[0];
        masterPortNumber = Integer.parseInt(args[1]);
        masterRequestPortNumber = Integer.parseInt(args[2]);
        int i;

        while(true){
            try{
                System.out.println("Waiting for Next Assignment");
                Socket infoSocket = new Socket(hostName,portNumber); // To EqualizerListener
                BufferedReader inFromMaster = new BufferedReader(
                    new InputStreamReader(infoSocket.getInputStream()));
                
                PrintWriter outToMaster = new PrintWriter(infoSocket.getOutputStream(), true);
                String receivedPing = inFromMaster.readLine(); //accept ping
                outToMaster.println(receivedPing);

                String assignmentType_str = inFromMaster.readLine();
                int assignmentType = Integer.parseInt(assignmentType_str);

                String hostName = inFromMaster.readLine();
                String portString = inFromMaster.readLine();
                int receivedPort = Integer.parseInt(portString);

                Socket socket = new Socket(hostName,receivedPort);
                BufferedReader inFromC = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                String filename = inFromC.readLine();
                String returnPort_str = inFromC.readLine();
                int returnPort = Integer.parseInt(returnPort_str);

                if(assignmentType==FULL_IMAGE){
                    // The socket you just opened is with the Client                    

                    processFullData(socket);

                    System.out.println("Processed Image and Returned to Client");

                } else{ // Your Job is to help out
                    // The socket you just opened is with the lead producer
                    // Note: This guy does NOT need to know the index of the image part he is getting.
                    processPartData(socket,assignmentType);

                    System.out.println("Processed Part of Image and Returned to Lead Producer");
                }
                
            } catch(IOException e){
                System.err.println(e);
                System.err.println("Client Gone, probably");
            }
        }
    }
}