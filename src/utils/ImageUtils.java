package utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public final class ImageUtils {
	
    public static final String PNG_FORMAT = ".png";
    
	private ImageUtils(){}

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
    
    //read the given language flag images from the assets flag folder and scales them
    public static Icon getFlagIcon(String languageCode, int imageScale) {
        try {
			ImageIO.read(new File(Utils.DEFAULT_BASE_PATH, "/icons/localization_flags/" + languageCode.toLowerCase() + ".png"));

			return new ImageIcon(ImageIO.read(new File(Utils.DEFAULT_BASE_PATH, "/icons/localization_flags/" + languageCode.toLowerCase() + ".png"))
            		.getScaledInstance(imageScale, -1, Image.SCALE_SMOOTH));
        } catch (IOException e) {
			System.out.println("WARNING: localization flags not found for " + languageCode.toLowerCase() + ", returning null image");
	        //fallback to text only if icon is missing
	        return null; 
		}
    }
}
