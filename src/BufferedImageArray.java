import java.awt.image.BufferedImage;

public class BufferedImageArray {
	
	public BufferedImageArray(int imageCount){
		array = new array of size image count
	}
	
	public BufferedImage getImage(int index){
		return array[index];
	}

	public void addImage(BufferedImage image, int index){
		array[index] = image;
	}
}