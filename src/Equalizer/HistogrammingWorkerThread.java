import java.awt.image.BufferedImage;

public class HistogrammingWorkerThread implements Runnable {
	private BufferedImageArray array;
    private HistogramSplit histogram;
	private int index;

    public HistogrammingWorkerThread(BufferedImageArray array, HistogramSplit histogram, int imageNumber) {
        this.array = array;
        this.histogram = histogram;
        this.index = imageNumber;
    }
 
    @Override
    public void run() {
    	BufferedImage original = array.getImage(index);
    	ArrayList<int[]> myHist = histogram.makeHist(original);
        histogram.addHist(myHist);
    }
}
