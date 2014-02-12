import java.net.*;
import java.io.*;

public class KKMultiServerThread extends Thread {
    private Socket socket = null;

    public KKMultiServerThread(Socket socket) {
        super("KKMultiServerThread");
        this.socket = socket;
    }
    
    public void run() {
        try (
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()));
        ) {
            String inputLine, outputLine;
            inputLine = in.readLine();
            Hist hist = new Hist(inputLine);
            outputLine = hist.writeImage();
            out.println(outputLine);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
