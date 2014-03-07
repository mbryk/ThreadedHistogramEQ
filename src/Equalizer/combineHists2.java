import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class combineHists2 {
    public ArrayList<int[]> combined;

    public combineHists2(ArrayList<int[]> hist1, ArrayList<int[]> hist2) {
		int[] rhistogram = new int[256];
        int[] ghistogram = new int[256];
        int[] bhistogram = new int[256];

        int[] rh1 = hist1.get(0);
        int[] gh1 = hist1.get(1);
        int[] bh1 = hist1.get(2);
        int[] rh2 = hist2.get(0);
        int[] gh2 = hist2.get(1);
        int[] bh2 = hist2.get(2);

        for(int i=0; i<rhistogram.length; i++)
        	rhistogram[i] = rh1[i] + rh2[i];
        for(int i=0; i<ghistogram.length; i++)
        	ghistogram[i] = gh1[i] + gh2[i];
        for(int i=0; i<bhistogram.length; i++)
        	bhistogram[i] = bh1[i] + bh2[i];

        combined = new ArrayList<int[]>();
        combined.add(rhistogram);
        combined.add(ghistogram);
        combined.add(bhistogram);
    }



}