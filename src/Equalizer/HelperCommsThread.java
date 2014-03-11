import java.awt.image.BufferedImage;

public class HelperCommsThread implements Runnable {
    private Socket socket;
    private int type;
    private BufferedImageArray array;
    private HistogramSplit histogram;
    private int index;

    public HelperCommsThread(Socket socket,int type,BufferedImageArray array, HistogramSplit histogram, int index, int waiting) {
        this.socket = socket;
        this.type = type;
        this.array = array;
        this.histogram = histogram;
        this.index = index;
    }
 
    public void run() {
        BufferedImage image = array.getImage(index);
        
        //socket.send(image);
        OutputStream outToHelper = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outToHelper);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image,"jpg", baos);
        baos.flush();
        byte[] receivedByteImage = baos.toByteArray();
        oos.writeObject(receivedByteImage);


        if(type==1) receiveHistogram();
        else receiveImage();
        waiting++;
    }

    private void receiveHistogram(){
        //ArrayList<int[]> myHist = socket.readStuff();
        BufferedReader inFromHelper = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));

        ArrayList<int[]> myHist = new ArrayList<int[]>();
        myHist.add(new int[256]);
        myHist.add(new int[256]);
        myHist.add(new int[256]);
        
        int i;
        for(int[] intArray : myHist){
            for(i = 0; i<256; i++){
                String curVal_str = inFromHelper.readLine();
                intArray[i] = Integer.parseInt(curVal_str);
            }
        }

        histogram.addHist(myHist);
    }

    private void receiveImage(){
        //socket.sendStuff(histogram.getLUT());
        PrintWriter outToHelper = new PrintWriter(socket.getOutputStream(), true);
        ArrayList<int[]> myLUT = histogram.getLUT();
        int j;
        for(int[] intArray : myLUT){
            for (j = 0; j<256; j++){
                outToHelper.println(intArray[j]);
            }
        }
        
        //BufferedImage equalized = socket.readImage();
        InputStream inFromHelper = socket.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(inFromHelper);
        byte[] equalizedByteImage = (byte[])ois.readObject();
        ByteArrayInputStream bais = new ByteArrayInputStream(equalizedByteImage);
        BufferedImage equalized = ImageIO.read(bais);

        array.addImage(equalized,index);
    }
}