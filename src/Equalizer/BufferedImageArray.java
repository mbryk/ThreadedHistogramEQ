import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class BufferedImageArray {
	private static ArrayList<BufferedImage> array;

	public BufferedImageArray(){
		array = new ArrayList<BufferedImage>();
	}
	
	public BufferedImage getImage(int index){
		return array.get(index);
	}

	public int getSize(){
		return array.size();
	}

	public void addImage(BufferedImage image, int index){
		if(index>=array.size())
			array.add(image);
		else 
			array.add(index,image);
	}

	public void clearArray(){
		array.clear();
	}
}