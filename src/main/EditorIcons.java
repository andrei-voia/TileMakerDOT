package main;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import utils.ImageUtils;
import utils.Utils;

public class EditorIcons {
	
	private BufferedImage iconImage;
	
    private ImageIcon eraserIcon;
    private ImageIcon noteIcon;
    private BufferedImage notFoundIcon;
    private BufferedImage deleteImage;
    private BufferedImage objectPickerImage;
    
    public EditorIcons(int tileSize, int defaultSelectedTile) {
    	initializeIcons(tileSize, defaultSelectedTile);
    }
    
    //icon loading section
    private void initializeIcons(int tileSize, int defaultSelectedTile) {
        try {
            //construct the path to the icon file
            File iconFile = new File(Utils.DEFAULT_BASE_PATH, "/icons/tile_editor_icon.png"); 
            
            //load the image
            iconImage = ImageIO.read(iconFile);
            
            //load the eraser icon
            File eraserFile = new File(Utils.DEFAULT_BASE_PATH, "/icons/eraser_icon.png");
            BufferedImage eraserImage = ImageIO.read(eraserFile);
            
            deleteImage = ImageIO.read(new File(Utils.DEFAULT_BASE_PATH, "/icons/delete_icon.png"));
            objectPickerImage = ImageIO.read(new File(Utils.DEFAULT_BASE_PATH, "/icons/object_picker.png"));
            notFoundIcon = ImageIO.read(new File(Utils.DEFAULT_BASE_PATH, "/icons/not_found.png"));
            notFoundIcon = ImageUtils.resizeImage(notFoundIcon, tileSize, tileSize);
            
            //scale it to the desired preview size
            Image scaledEraser = eraserImage.getScaledInstance(defaultSelectedTile, defaultSelectedTile, Image.SCALE_SMOOTH);
            this.eraserIcon = new ImageIcon(scaledEraser);
            
            //load the entire note icon
            this.noteIcon = new ImageIcon(ImageIO.read(new File(Utils.DEFAULT_BASE_PATH, "/icons/note_icon.png"))
            		.getScaledInstance(defaultSelectedTile, defaultSelectedTile, Image.SCALE_SMOOTH));
            
        } catch (IOException e) {
            System.err.println("Could not load application icon: " + e.getMessage());
            //the application will still run without the icon
        }
    }

	public BufferedImage getIconImage() {
		return iconImage;
	}

	public ImageIcon getEraserIcon() {
		return eraserIcon;
	}
	
	public ImageIcon getNoteIcon() {
		return noteIcon;
	}

	public BufferedImage getNotFoundIcon() {
		return notFoundIcon;
	}

	public BufferedImage getDeleteImage() {
		return deleteImage;
	}

	public BufferedImage getObjectPickerImage() {
		return objectPickerImage;
	}
}
