import java.awt.image.BufferedImage;

public class EqualizingWorkerThread implements Runnable {
    private BufferedImageArray array;
    private HistogramSplit histogram;
    private int index;

    public EqualizingWorkerThread(BufferedImageArray array, HistogramSplit histogram, int imageNumber) {
        this.array = array;
        this.histogram = histogram;
        this.index = imageNumber;
    }
 
    @Override
    public void run() {
        BufferedImage original = array.getImage(index);
        BufferedImage equalized = histogram.equalize(original);
        array.addImage(equalized,index);
    }
}
