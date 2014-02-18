public class CubbyHole {
    private Data contents;
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
        while (!isDone() && available == false) {
            try {
                wait();
            } catch (InterruptedException e) { }
        }
        available = false;
        notifyAll();
        Data ret = contents;
        contents = null;
        return ret;
    }
 
    public synchronized void put(Data value) {
        while (available == true) {
            try {
                wait();
            } catch (InterruptedException e) { }
        }
        contents = value;
        available = true;
        notifyAll();
    }
}