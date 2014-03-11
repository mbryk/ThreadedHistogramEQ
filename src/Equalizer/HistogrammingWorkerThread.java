import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class HistogrammingWorkerThread implements Runnable {
	private BufferedImageArray array;
    private HistogramSplit histogram;
	private int index;
    private int type;

    public HistogrammingWorkerThread(BufferedImageArray array, HistogramSplit histogram, int imageNumber, int type) {
        this.array = array;
        this.histogram = histogram;
        this.index = imageNumber;
        this.type = type;
    }
 
    public void run() {
    	BufferedImage original = array.getImage(index);
        if(type==1){ // Making the Histogram
            ArrayList<int[]> myHist = histogram.makeHist(original);
            histogram.addHist(myHist);

        } else{ // Equalizing
            BufferedImage equalized = histogram.equalize(original);
            array.addImage(equalized,index);
        }
    }
}
