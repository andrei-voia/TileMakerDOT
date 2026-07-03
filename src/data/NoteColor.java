package data;

import java.awt.Color;

public enum NoteColor {
	
	YELLOW("Yellow", new Color(241, 196, 15)),
    RED("Red",       new Color(231, 76, 60)),
    BLUE("Blue",     new Color(52, 152, 219)),
    GREEN("Green",   new Color(46, 204, 113)),
    WHITE("White",   new Color(236, 240, 241));

    private final String name;
    private final Color colorRGB;   //normal color
    private final Color colorRGBA;  //transparent color with alpha for map overlays

    NoteColor(String name, Color baseColor) {
        this.name = name;
        this.colorRGB = baseColor;
        
        //automatically create a transparent version for rendering on the map
        this.colorRGBA = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 200);
    }
    
    public String getName() {
        return name;
    }

    public Color getColorRGB() {
        return colorRGB;
    }

    public Color getColorRGBA() {
        return colorRGBA;
    }

    @Override
    public String toString() {
        return name;
    }
}
