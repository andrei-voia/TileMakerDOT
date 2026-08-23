package core;

import java.awt.image.BufferedImage;

public class TileObject extends Tile {

	private int x;
	private int y;
	private int width;
	private int height;
	private boolean isObject;
	private int renderOrder;	//0 under everything, 1 normal position, 2 above everything
	
	//keep a reference to the source tile which might be an AnimatedTile
    private Tile sourceTile;

    public TileObject(Tile tile, int x, int y, boolean isObject, int renderOrder) {
        //we still call super to keep the ID and Name but the image field in the parent will now just be a fallback
        super(tile.getId(), tile.getName(), tile.getImage(), tile.getPath());
        
        this.sourceTile = tile; //store the reference
        this.x = x;
        this.y = y;
        this.width = tile.getImage().getWidth();
        this.height = tile.getImage().getHeight();
        this.isObject = isObject;
        this.renderOrder = renderOrder;
    }

    //override getImage to delegate to the source tile
    @Override
    public BufferedImage getImage() {
        //if sourceTile is an AnimatedTile, this will call its overridden getImage() 
    	//and return the correct frame for the current time
        return sourceTile.getImage();
    }
	
	//this should run once when the image is loaded into your Tile / TileObject
	public static int calculateOpaqueHeight(BufferedImage image) {
	    if (image == null) return 0;
	    
	    int width = image.getWidth();
	    int height = image.getHeight();
	    
	    //start from the most bottom row and work up
	    for (int y = height - 1; y >= 0; y--) {
	        for (int x = 0; x < width; x++) {
	            //get the pixel ARGB value
	            int pixel = image.getRGB(x, y);
	            
	            //extract the alpha component (8 most significant bits)
	            int alpha = (pixel >> 24) & 0xFF;
	            
	            //check if the pixel is non transparent (alpha > 0)
	            if (alpha > 0) {
	                //since we start from the bottom the moment we find an opaque pixel 
	            	//we have found the true height
	                return y + 1;
	            }
	        }
	    }
	    //if the image is completely transparent return 0
	    return 0; 
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public int getComputedHeight() {
		return calculateOpaqueHeight(image);
	}

	public boolean isObject() {
		return isObject;
	}

	public int getRenderOrder() {
		return renderOrder;
	}
	
	public int getId()
	{
		return sourceTile.getId();
	}
}
