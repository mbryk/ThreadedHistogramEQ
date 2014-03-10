import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.lang.Object;
import java.util.concurrent.TimeUnit;

public class CubbyHole {
    public int Qsize = 50;
    private BlockingQueue<Data> clientQueue = new ArrayBlockingQueue<>(Qsize);
    private PriotityBlockingQueue<Data> processorQueue = new PriorityBlockingQueue<>(Qsize);
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

    //for LB stats:
    public int getQueueRatio(){
        return clientQueue.remainingCapacity();
    }
    public int getProducerCount(){
        return numProds;
    }

    public Data get() {
        Data ret = null;
        try{
            if (!isDone()){
                ret = clientQueue.take();
            }
        } catch (InterruptedException e){
            System.err.println("Take error: " + e);
        }

        return ret;
    }
 
    public Data get2() {
        Data ret = null;
        try{
            if (!isDone()){
                ret = clientQueue.poll();
            }
        } catch (InterruptedException e){
            System.err.println("Take error: " + e);
        }

        return ret;
    }

    public void put(Data value) {
        try{
            clientQueue.put(value);
        } catch (InterruptedException e){
            System.err.println("Put error: " + e);
        }
    }

    public void putProcessor(Data value){
        try{
            processorQueue.put(value);
        } catch (InterruptedException e){
            System.err.println("Processor Put error: "+e);
        }
    }

    public Data getProcessor(){
        Data ret = null;
        try{
            ret = processorQueue.take();
        } catch (InterruptedException e){
            System.err.println("Processor Take error: " + e);
        }
        return ret;
    }

}