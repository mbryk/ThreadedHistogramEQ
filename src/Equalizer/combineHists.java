import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class combineHists {
    public ArrayList<int[]> combined;
    private ArrayList<int[]> hist1;

    public combineHists(ArrayList<ArrayList<int[]>> histList) {
		int[] rhistogram = new int[256];
        int[] ghistogram = new int[256];
        int[] bhistogram = new int[256];

        for(int i=0; i<rhistogram.length; i++) rhistogram[i] = 0;
        for(int i=0; i<ghistogram.length; i++) ghistogram[i] = 0;
        for(int i=0; i<bhistogram.length; i++) bhistogram[i] = 0;

        for(int j = 0; j<histList.size(); j++){
        	hist1 = histList.get(j);
	        int[] rh1 = hist1.get(0);
	        int[] gh1 = hist1.get(1);
	        int[] bh1 = hist1.get(2);

	        for(int i=0; i<rhistogram.length; i++)
	        	rhistogram[i] += rh1[i];
	        for(int i=0; i<ghistogram.length; i++)
	        	ghistogram[i] += gh1[i];
	        for(int i=0; i<bhistogram.length; i++)
	        	bhistogram[i] += bh1[i];
	    }

        combined = new ArrayList<int[]>();
        combined.add(rhistogram);
        combined.add(ghistogram);
        combined.add(bhistogram);
    }



}