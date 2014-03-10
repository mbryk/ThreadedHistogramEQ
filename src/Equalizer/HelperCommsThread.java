import java.awt.image.BufferedImage;

public class HelperCommsThread implements Runnable {
    private Socket socket;
    private int type;
    private BufferedImageArray array;
    private HistogramSplit histogram;
    private int index;

    public HelperCommsThread(Socket socket,int type,BufferedImageArray array, HistogramSplit histogram, int index) {
        this.socket = socket;
        this.type = type;
        this.array = array;
        this.histogram = histogram;
        this.index = index;
    }
 
    @Override
    public void run() {
        BufferedImage image = array.getImage(index);
        socket.send(image);
        if(type==1) receiveHistogram();
        else receiveImage();
    }

    private void receiveHistogram(){
        ArrayList<int[]> myHist = socket.readStuff();
        histogram.addHist(myHist);
    }

    private void receiveImage(){
        socket.send(histogram.getLUT());
        BufferedImage equalized = socket.readImage();
        array.addImage(equalized,index);
    }
}