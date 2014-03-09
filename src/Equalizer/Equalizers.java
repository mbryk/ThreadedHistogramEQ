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
    protected static final int THREAD_POOL_SIZE = 5;
    private static String masterHostName;
    private static int masterPortNumber;
    private static int masterRequestPortNumber;

    public static void processFullData(Socket socket) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        BufferedImageArray array = new BufferedImageArray();
        int i = 0;

        try (
            OutputStream outToClient = socket.getOutputStream();
            InputStream inFromClient = socket.getInputStream();
            ) {
            byte[] originalByteImage, receivedByteImage;
            ObjectInputStream ois = new ObjectInputStream(inFromClient);

            if((originalByteImage = (byte[])ois.readObject())!=null){
             
                int sizeInBytes = originalByteImage.length;
                System.out.println("NUM BYTES: "+sizeInBytes);

                //convert byte array to BufferedImage
                ByteArrayInputStream bais = new ByteArrayInputStream(originalByteImage);
                BufferedImage image = ImageIO.read(bais);

                int imParts = splitUp(array, image); // Add pieces of image to the array and return array size
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

                    //int helpersComing = askMaster(imParts); 
                    try(ServerSocket getHelpers = helpersComing){
                        while(int h=0;h<helpersComing;h++){
                            Socket helper = getHelpers.accept();
                        }
                    }
                    
                } else{
                    for(i=0;i<imParts;i++){
                        Runnable worker = new HistogrammingWorkerThread(array,histogram,i);    
                        executor.execute(worker);
                        executor.shutdown();
                        while (!executor.isTerminated()) {}
                    }
                    for(i=0;i<imParts;i++){
                        Runnable worker = new EqualizingWorkerThread(array,histogram,i);    
                        executor.execute(worker);
                        executor.shutdown();
                        while (!executor.isTerminated()) {}
                    }    
                    BufferedImage processedImage = recombineImage(array);                
                }
//
// if (imSize > thresh)
    // split up, run imageHistogram on each piece on another node
    // wait for all, as they come back add to ArrayList of ArrayLists
    // run combineHists on ArrayLists<ArrayLists<int[]>>
    // run calcScaleFactor
    // ask Master for more Equalizers again
    // run eq
//
//
            }
//
//
//is there no .join or something in order to wait for things
//rather than running a while loop??
//
//
            System.out.println("Finished all threads");
            
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
        if (args.length != 2){
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
                Socket clientInfoSocket = new Socket(hostName,portNumber); // To ConsumerListener
                BufferedReader inFromMaster = new BufferedReader(
                    new InputStreamReader(clientInfoSocket.getInputStream()));
                
                PrintWriter outToMaster = new PrintWriter(clientInfoSocket.getOutputStream(), true);
                String receivedPing = inFromMaster.readLine(); //accept ping
                outToMaster.println(receivedPing);

                String assignmentType = inFromMaster.readLine();

                String hostName = inFromMaster.readLine();
                String portString = inFromMaster.readLine();
                int receivedPort = Integer.parseInt(portString);

                Socket socket = new Socket(hostName,receivedPort);

                if(assignmentType=="FullImage"){
                    // The socket you just opened is with the Client                    

                    // This is for telling the client which image to send.
                    String imageNumString = inFromMaster.readLine();
                    int imageNum = Integer.parseInt(imageNumString);
                    
                    processFullData(socket,imageNum);
                    
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