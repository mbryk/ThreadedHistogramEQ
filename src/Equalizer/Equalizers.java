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

    private String filename;
    private String clientHostName;
    private int returnPort;

    protected int judgeImage(int size, int workers){
        int pieceSize = 10000*2^10; // 10 MB
        int pieces = size/pieceSize + 1;
        if(pieces==1) return 0;
        else if(pieces<workerCount*2) return 1;
        else return 2;
    }
    
    protected void splitUp(BufferedImage image,int pieces){
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
        array = new BufferedImageArray();

        try (
            InputStream inFromClient = socket.getInputStream();
            ) {
            byte[] originalByteImage, receivedByteImage;
            ObjectInputStream ois = new ObjectInputStream(inFromClient);

            if((originalByteImage = (byte[])ois.readObject())==null){
                sendNullBack(); // Tell Client that something was screwed up with the image
                return;
            }
            socket.close();
             
            int sizeInBytes = originalByteImage.length;
            System.out.println("NUM BYTES: "+sizeInBytes);

            //convert byte array to BufferedImage
            ByteArrayInputStream bais = new ByteArrayInputStream(originalByteImage);
            BufferedImage image = ImageIO.read(bais);

            int workerCount = THREAD_POOL_SIZE*2;
            int judge = judgeImage(sizeInBytes,workerCount); // Return 0 if you can do it in one thread. 1 if you can do it with a pool. 2 if you need help.


            if(judge == 0){
                BufferedImage original = array.getImage(index);
                Histogram histogram = new Histogram(original);
                array.addImage(histogram.equalized,index);
            }            
            else{
                HistogramSplit histogram = new HistogramSplit();

                if(judge==1){
                    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                    splitUp(image,workerCount); // Add pieces of image to the array and return array size

                    for(i=0;i<workerCount;i++){
                        Runnable worker = new HistogrammingWorkerThread(array,histogram,i,1);    
                        executor.execute(worker);
                    }
                    executor.shutdown();
                    while (!executor.isTerminated()) {}

                    histogram.calcHistogramLUT();
                
                    for(i=0;i<workerCount;i++){
                        Runnable worker = new HistogrammingWorkerThread(array,histogram,i,2);    
                        executor.execute(worker);
                    } 
                    executor.shutdown();
                    while (!executor.isTerminated()) {}

                    System.out.println("Finished all threads");
                
                } else { // You need help.
                    for(int type=1;type<3;type++){
                        Socket reqHelpers = socket(masterHostName, masterRequestPortNumber);
                        BufferedReader inFromMReq = new BufferedReader(
                            new InputStreamReader(reqHelpers.getInputStream()));
                        PrintWriter outToMReq = new PrintWriter(reqHelpers.getOutputStream(), true);
                        outToMReq.println(imParts);
                        outToMReq.println(requestType);

                        String helpersComing_str = inFromMReq.readline();
                        int helpersComing = Integer.parseInt(helpersComing_str);

                        splitUp(image,helpersComing);

                        try(ServerSocket getHelpers = helpersComing){
                            while(int h=0;h<helpersComing;h++){
                                Socket helper = getHelpers.accept();
                                new HelperCommsThread(helper,type,array.getImage(h),histogram).start();
                            }
                            if(type==1){
                                histogram.calcHistogramLUT();
                                array.clearArray();
                            } 
                        }
                    }
                }
                BufferedImage processedImage = recombineImage(image);
            }

            Socket returnSocket = new Socket(clientHostName, returnPort);
            
            // workers are done
            OutputStream outToClient = returnSocket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(outToClient);
            PrintWriter outToClient = new PrintWriter(returnSocket.getOutputStream(), true);
            outToClient.println(filename);

            //convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processedImage,"jpg", baos);
            baos.flush();
            receivedByteImage = baos.toByteArray();
            oos.writeObject(receivedByteImage);
            returnSocket.close();
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (ClassNotFoundException e){ e.printStackTrace(); }
    }
    
    public static void processPartData(Socket socket, int assignmentType){
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        int workerCount = THREAD_POOL_SIZE*2;

        InputStream inFromClient = socket.getInputStream();
        byte[] originalByteImage;
        ObjectInputStream ois = new ObjectInputStream(inFromClient);

        originalByteImage = (byte[])ois.readObject();
        ByteArrayInputStream bais = new ByteArrayInputStream(originalByteImage);
        BufferedImage image = ImageIO.read(bais);

        splitUp(image,workerCount);

        HistogramSplit histogram = new HistogramSplit();
        ArrayList<int[]> histLUT;
        if(assignmentType==2){
            histLUT = socket.receiveLUT();
            histogram.setLUT(histLUT);
        }
        for(i=0;i<workerCount;i++){
            Runnable worker = new HistogrammingWorkerThread(array,histogram,i,assignmentType);    
            executor.execute(worker);
        } 
        executor.shutdown();
        while (!executor.isTerminated()) {}

        if(assignmentType==1){
            histogram.calcHistogramLUT();
            histLUT = histogram.getLUT();
            socket.send(histLUT);
        } else {
            BufferedImage processedImage = recombineImage(image);
            socket.send(processedImage);
        }
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
                Socket infoSocket = new Socket(masterHostName,masterPortNumber); // To EqualizerListener
                BufferedReader inFromMaster = new BufferedReader(
                    new InputStreamReader(infoSocket.getInputStream()));
                
                PrintWriter outToMaster = new PrintWriter(infoSocket.getOutputStream(), true);
                String receivedPing = inFromMaster.readLine(); //accept ping
                outToMaster.println(receivedPing);

                String assignmentType_str = inFromMaster.readLine();
                int assignmentType = Integer.parseInt(assignmentType_str);

                clientHostName = inFromMaster.readLine();
                String portString = inFromMaster.readLine();
                int receivedPort = Integer.parseInt(portString);

                Socket socket = new Socket(clientHostName,receivedPort);
                BufferedReader inFromC = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                filename = inFromC.readLine();
                String returnPort_str = inFromC.readLine();
                returnPort = Integer.parseInt(returnPort_str);

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