import java.awt.image.BufferedImage;

public class ProcessingWorkerThread implements Runnable {
    private BufferedImageArray array;
    private int index;

    public ProcessingWorkerThread(BufferedImageArray array, int imageNumber) {
        this.array = array;
        this.index = imageNumber;
    }
 
    @Override
    public void run() {
        BufferedImage original = array.getImage(index);
        Histogram hist = new Histogram(original);
        array.addImage(hist.equalized,index);
    }
}