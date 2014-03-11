import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.*;
import java.io.*;
import org.hyperic.sigar.*;
import java.awt.Graphics;
import java.lang.Object;
import java.lang.Runtime;
import java.util.ArrayList;

public class Equalizers{
    public static final int FULL_IMAGE = 0;
    public static final int HELPER_HISTOGRAM = 1;
    public static final int HELPER_SCALING = 2;

    private static int THREAD_POOL_SIZE;
    private static String masterHostName;
    private static int masterPortNumber;
    private static int masterRequestPortNumber;
    private static BufferedImageArray array;

    private static String filename;
    private static String hostName;
    private static int returnPort;

    private static Sigar sigar;

    protected static int waiting;

    private static int judgeImage(int size, int workers){
        int pieceSize = 10000*2^10; // 10 MB
        int pieces = size/pieceSize + 1;
        if(pieces==1) return 0;
        else if(pieces<workers) return 1;
        else return pieces;
    }
    
    private static void splitUp(BufferedImage image,int pieces){
        int height = image.getHeight(); int width = image.getWidth();
        int interval = height/pieces;

        System.out.println("P: "+pieces);
        System.out.println("Height: "+height+"; width: "+width);
        System.out.println("Interval: "+interval);

        BufferedImage subImage;
        int i = 0;
        for(int y = 0; y<height; y+= interval){
            System.out.println("y: "+y+"; ");
            if(y+interval>height){ interval = height-y; }
            subImage = image.getSubimage(0,y,width,interval);
            array.addImage(subImage,i++);
        }
    }

    private static BufferedImage recombineImage(BufferedImage oldImage){
        BufferedImage image = new BufferedImage(oldImage.getWidth(), oldImage.getHeight(), oldImage.getType());
        Graphics g = image.getGraphics();
        BufferedImage subImage;
        int y = 0;

        for(int i=0;i<array.getSize();i++){
            subImage = array.getImage(i);
            g.drawImage(subImage,0,y,null);
            y += subImage.getHeight();
        }
        return image;
    }

    public static void processFullData(Socket socket) {
        try (
            InputStream inFromClient = socket.getInputStream();
            ) {
            //BufferedReader readFromClient = new BufferedReader(new InputStreamReader(inFromClient));
            //filename = readFromClient.readLine();
            //String returnPort_str = readFromClient.readLine();
            //returnPort = Integer.parseInt(returnPort_str);

            ObjectInputStream ois = new ObjectInputStream(inFromClient);
            filename = (String) ois.readObject();
            returnPort = ois.readInt();
            byte[] originalByteImage = (byte[])ois.readObject();

            socket.close();
             
            int sizeInBytes = originalByteImage.length;
            System.out.println("NUM BYTES: "+sizeInBytes);

            //convert byte array to BufferedImage
            ByteArrayInputStream bais = new ByteArrayInputStream(originalByteImage);
            BufferedImage image = ImageIO.read(bais);

            int workerCount = THREAD_POOL_SIZE;
            int imParts = judgeImage(sizeInBytes,workerCount); // Return 0 if you can do it in one thread. 1 if you can do it with a pool. 2 if you need help.
            BufferedImage processedImage;
            if(imParts == 0){
                Histogram histogram = new Histogram(image);
                processedImage = histogram.equalized;
            }            
            else{
                array = new BufferedImageArray();
                HistogramSplit histogram = new HistogramSplit();
                boolean onYourOwn = (imParts==1);
                boolean doPart1 = true;
                if(!onYourOwn){
                    for(int type=1;type<3;type++){
                        Socket reqHelpers = new Socket(masterHostName, masterRequestPortNumber);
                        BufferedReader inFromMReq = new BufferedReader(
                            new InputStreamReader(reqHelpers.getInputStream()));
                        PrintWriter outToMReq = new PrintWriter(reqHelpers.getOutputStream(), true);
                        outToMReq.println(imParts);
                        outToMReq.println(type);
                        int helperPort = reqHelpers.getLocalPort();
                        reqHelpers.setReuseAddress(true);

                        String helpersComing_str = inFromMReq.readLine();
                        int helpersComing = Integer.parseInt(helpersComing_str);
                        if(helpersComing == 0){
                            onYourOwn = true;
                            if(type==2) doPart1 = false;
                            break;
                        }

                        System.out.println("HelpersComing: "+helpersComing);
                        splitUp(image,helpersComing);

                        try(ServerSocket getHelpers = new ServerSocket(helperPort)){
                            waiting = 0;
                            for(int h=0;h<helpersComing;h++){
                                Socket helper = getHelpers.accept();
                                new HelperCommsThread(helper,type,array,histogram,h).start();
                            }
                            System.out.println("Waiting for waiting");
                            while(waiting<helpersComing){System.out.println(waiting);}
                            System.out.println("Waited for waiting");
                            //Make sure all those guys come back (.join())
                            if(type==1){
                                histogram.calcHistogramLUT(image.getHeight()*image.getWidth());
                                array.clearArray();
                            } 
                        }
                    }
                }
                if(onYourOwn){
                    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                    splitUp(image,workerCount); // Add pieces of image to the array and return array size

                    if(doPart1){
                        for(int i=0;i<workerCount;i++){
                            Runnable worker = new HistogrammingWorkerThread(array,histogram,i,HELPER_HISTOGRAM);    
                            executor.execute(worker);
                        }
                        executor.shutdown();
                        while (!executor.isTerminated()) {}

                        histogram.calcHistogramLUT(image.getHeight()*image.getWidth());
                    }
                    executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                
                    for(int i=0;i<workerCount;i++){
                        Runnable worker = new HistogrammingWorkerThread(array,histogram,i,HELPER_SCALING);    
                        executor.execute(worker);
                    } 
                    executor.shutdown();
                    while (!executor.isTerminated()) {}

                    System.out.println("Finished all threads");
                }
                processedImage = recombineImage(image);
            }

            Socket returnSocket = new Socket(hostName, returnPort);
            
            // workers are done
            OutputStream outToClient = returnSocket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(outToClient);
            PrintWriter printToClient = new PrintWriter(outToClient, true);
            printToClient.println(filename);

            //convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processedImage,"jpg", baos);
            baos.flush();
            byte[] receivedByteImage = baos.toByteArray();
            oos.writeObject(receivedByteImage);
            returnSocket.close();
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (ClassNotFoundException e){ e.printStackTrace(); }
    }
    
    public static void processPartData(Socket socket, int assignmentType){
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        int workerCount = THREAD_POOL_SIZE;
      try{
        InputStream inFromLeader = socket.getInputStream();
        byte[] originalByteImage;
        ObjectInputStream ois = new ObjectInputStream(inFromLeader);

        originalByteImage = (byte[])ois.readObject();
        ByteArrayInputStream bais = new ByteArrayInputStream(originalByteImage);
        BufferedImage image = ImageIO.read(bais);
        
        array = new BufferedImageArray();

        splitUp(image,workerCount);

        HistogramSplit histogram = new HistogramSplit();
        
        ArrayList<int[]> histVar;

        if(assignmentType==2){
            //histVar = socket.receiveStuff();
            BufferedReader inFromLP = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

            histVar = new ArrayList<int[]>();
            histVar.add(new int[256]);
            histVar.add(new int[256]);
            histVar.add(new int[256]);
            
            int i;
            for(int[] intArray : histVar){
                for(i = 0; i<256; i++){
                    String curVal_str = inFromLP.readLine();
                    intArray[i] = Integer.parseInt(curVal_str);
                }
            }
            histogram.setLUT(histVar);
        }
        for(int i=0;i<workerCount;i++){
            Runnable worker = new HistogrammingWorkerThread(array,histogram,i,assignmentType);    
            executor.execute(worker);
        } 
        executor.shutdown();
        while (!executor.isTerminated()) {}

        if(assignmentType==1){
            histVar = histogram.getHist();
            //socket.sendStuff(histVar);
            PrintWriter outToLP = new PrintWriter(socket.getOutputStream(), true);
            int j;
            for(int[] intArray : histVar){
                for (j = 0; j<256; j++){
                    outToLP.println(intArray[j]);
                }
            }

        } else {
            BufferedImage processedImage = recombineImage(image);
            //socket.send(processedImage);
            OutputStream outToLP = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(outToLP);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processedImage,"jpg", baos);
            baos.flush();
            byte[] receivedByteImage = baos.toByteArray();
            oos.writeObject(receivedByteImage);
        }
      } catch(IOException e){
        System.out.println("Error Helping Out: " + e);
        System.exit(-1);
      } catch(ClassNotFoundException e){
        System.out.println("Error Helping Out: " + e);
        System.exit(-1);
      }
    }

    public static void main(String[] args) {
        if (args.length != 3){
            System.err.println("Usage java Equalizers <Master host name> <Master port number> <Master's Requesting Port>");
            System.exit(1);
        }
        THREAD_POOL_SIZE =  Runtime.getRuntime().availableProcessors();   

        masterHostName = args[0];
        masterPortNumber = Integer.parseInt(args[1]);
        masterRequestPortNumber = Integer.parseInt(args[2]); // If you need backup

        sigar = new Sigar();

        while(true){
            try{
                System.out.println("Waiting for Next Assignment");

                //attach to Master to introduce
                Socket s = new Socket(masterHostName,masterPortNumber); // To EqualizerListener 
                int originalPort = s.getLocalPort();
                s.setReuseAddress(true);
                System.out.println("Introduced to Master");           

                //tell Master your details
                double loadAvg = sigar.getLoadAverage()[0];
                PrintWriter outToMaster = new PrintWriter(s.getOutputStream(), true);
                outToMaster.println(loadAvg);
                outToMaster.println(THREAD_POOL_SIZE);
                ServerSocket sRcv = new ServerSocket(originalPort);

                System.out.println("ServerSocket waiting for ClientInfo");
                //New Server socket to wait for client info
                
                Socket infoSocket = sRcv.accept();
                BufferedReader inFromMaster = new BufferedReader(
                    new InputStreamReader(infoSocket.getInputStream()));
                outToMaster = new PrintWriter(infoSocket.getOutputStream(),true);

                String receivedPing = inFromMaster.readLine(); //accept ping
                outToMaster.println(receivedPing);

                String assignmentType_str = inFromMaster.readLine();
                int assignmentType = Integer.parseInt(assignmentType_str);

                hostName = inFromMaster.readLine();
                String portString = inFromMaster.readLine();
                int receivedPort = Integer.parseInt(portString);

                sRcv.close();
                infoSocket.close();

                System.out.println("Connecting to Client");
                Socket socket = new Socket(hostName,receivedPort);
                System.out.println("Connected to Client");

                if(assignmentType==FULL_IMAGE){
                    // The socket just opened is with the Client
                    processFullData(socket);
                    System.out.println("Processed Image and Returned to Client");

                } else{ // Your Job is to help out
                    // The socket you just opened is with the lead producer
                    // Note: This guy does NOT need to know the index of the image part he is getting.
                    processPartData(socket,assignmentType);
                    System.out.println("Processed Part of Image and Returned to Lead Producer");
                }
                
            } catch(IOException|SigarException e){
                System.err.println(e);
                System.err.println("Client Gone, probably");
            }
        }
    }
}