import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class BufferedImageArray {
	private static ArrayList<BufferedImage> array;

	public BufferedImageArray(int imageCount){
		array = new ArrayList<BufferedImage>();
		array.ensureCapacity(imageCount);
	}
	
	public BufferedImage getImage(int index){
		return array.get(index);
	}

	public void addImage(BufferedImage image, int index){
		array.add(index,image);
	}
}