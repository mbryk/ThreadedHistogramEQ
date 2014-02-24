import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.lang.Object;
import java.util.concurrent.TimeUnit;

public class CubbyHole {
    private Data contents;
    private BlockingQueue<Data> queue = new ArrayBlockingQueue<>(10);
    private boolean available = false;
    private static int numProds = 0;

    public CubbyHole() {}
    
    public synchronized void addProducer() {
        numProds++;
       notifyAll();
    }

    public synchronized void subProducer() {
       numProds--;
       notifyAll();
    }
    
    public synchronized boolean isDone() {
        return numProds==0;
    }
    
    public synchronized Data get() {

        Data ret = null;
        try{
            if (!isDone()){
                System.out.println("Giving Data");
                ret = queue.take();
                //ret = queue.poll(2, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e){
            System.err.println("Take error: " + e);
        }

        return ret;
    }
 
    public synchronized void put(Data value) {

        try{
            queue.put(value);
        } catch (InterruptedException e){
            System.err.println("Put error: " + e);
        }
    }
}