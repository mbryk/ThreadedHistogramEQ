import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.lang.Object;
import java.util.concurrent.TimeUnit;

public class CubbyHole {
    public int Qsize = 50;
    private BlockingQueue<Data> queue = new ArrayBlockingQueue<>(Qsize);
    private static int numProds = 0;

    public CubbyHole() {}
    
    public void addProducer() {
        numProds++;
    }
    public void subProducer() {
       numProds--;
    }
    public boolean isDone() {
        return numProds==0;
    }

    //for Lb stats:
    public int getQueueRatio(){
        return queue.remainingCapacity();
    }
    public int getProducerCount(){
        return numProds;
    }

    public Data get() {

        Data ret = null;
        try{
            if (!isDone()){
                ret = queue.take();
            }
        } catch (InterruptedException e){
            System.err.println("Take error: " + e);
        }

        return ret;
    }
 
    public void put(Data value) {
        try{
            queue.put(value);
        } catch (InterruptedException e){
            System.err.println("Put error: " + e);
        }
    }
}