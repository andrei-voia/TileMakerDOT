package utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class ImageUtils {
	
    public static final String PNG_FORMAT = ".png";
    private static final String ICON_PATH = "assets/icons/tile_editor_icon.png";
    
	private ImageUtils(){}

	// Loads icon
	public static Image loadIconImage() {
		try {
			File iconFile = new File(ICON_PATH);
			if (iconFile.exists()) {
				BufferedImage img = ImageIO.read(iconFile);
				return img;
			}
	} catch (IOException e) {
	}
		return null;
	}

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImage.createGraphics();
        
        //set rendering hints for better quality
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        g2.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        return resizedImage;
    }
    
    public static ImageIcon createScaledIcon(BufferedImage sourceImage, int targetWidth, int targetHeight) {
        if (sourceImage == null) {
            return null;
        }
        
        int originalWidth = sourceImage.getWidth();
        int originalHeight = sourceImage.getHeight();
        
        //calculate the scale factor needed to fit both dimensions
        double scaleFactor = Math.min(
            (double) targetWidth / originalWidth,
            (double) targetHeight / originalHeight
        );
        
        //calculate new dimensions
        int newWidth = (int) (originalWidth * scaleFactor);
        int newHeight = (int) (originalHeight * scaleFactor);
        
        //use getScaledInstance for high quality scaling
        Image scaledImage = sourceImage.getScaledInstance(
            newWidth, 
            newHeight, 
            Image.SCALE_SMOOTH //good quality scaling
        );
        
        //create the ImageIcon from the scaled Image
        return new ImageIcon(scaledImage);
    }
}
