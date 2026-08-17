package main;

import java.awt.Toolkit;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import localization.LocalizationManager;
import utils.Utils;

public class LoadedSetup {

    private int tileSize;
    private int defaultSelectedTile;
    
    //field to store the asset path
    private String oldResourceBasePath;
    private String resourceBasePath;
    private String tileSizeLoaded;
    private List<String> defaultSizes;
    private long frameDuration = 200; //200ms per frame by default
    private int darkMode = 0;
    
    private TileEditor tileEditor;
    
	public LoadedSetup(TileEditor tileEditor) {
		this.tileEditor = tileEditor;
		
		loadSetup();
	}
	
    public void loadSetup() {
        //initial setup and input dialog
    	defaultSelectedTile = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.0595); //68
    
        defaultSizes = Utils.loadDefaultGridSizes();
        resourceBasePath = Utils.loadInitialDefaultValues("default_assets_path.txt");
        tileSizeLoaded = Utils.loadInitialDefaultValues("default_tile_size.txt");
        
        //load saved language preference
        String savedLanguage = Utils.loadInitialDefaultValues("default_language.txt");
        LocalizationManager.getInstance().loadSavedLocale(savedLanguage);
        
        try {
        	frameDuration =  Long.parseLong(Utils.loadInitialDefaultValues("default_frame_ms_duration.txt"));
        	
        	darkMode = Integer.parseInt(Utils.loadInitialDefaultValues("default_dark_mode.txt"));
        	if(darkMode == 0) {
            	toggleDarkMode(false);
        	}
        	else {
        		toggleDarkMode(true);
        	}

        } catch(NumberFormatException e) {
        	LocalizationManager loc = LocalizationManager.getInstance();
        	JOptionPane.showMessageDialog(null,
        			loc.getString("dialog_invalid_load"),
        	        loc.getString("Configuration Error"),
        	        JOptionPane.WARNING_MESSAGE);
        }
	}
	
	public void toggleDarkMode(boolean darkMode) {
		try {
		    //pick the theme
		    if (darkMode) {
		        UIManager.setLookAndFeel(new FlatDarkLaf());
		    } else {
		        UIManager.setLookAndFeel(new FlatLightLaf());
		    }
		    
		    //this updates every window and component instantly
		    FlatLaf.updateUI(); 

		    if (tileEditor.getCanvas() != null) {
		    	tileEditor.getCanvas().getCanvasRenderer().repaint();
		    }
		    
		} catch (Exception ex) {
		    System.err.println("Failed to switch theme: " + ex.getMessage());
		}
	}

	public String getResourceBasePath() {
		return resourceBasePath;
	}

	public void setResourceBasePath(String resourceBasePath) {
		this.resourceBasePath = resourceBasePath;
	}

	public int getDarkMode() {
		return darkMode;
	}

	public void setDarkMode(int darkMode) {
		this.darkMode = darkMode;
	}

	public int getDefaultSelectedTile() {
		return defaultSelectedTile;
	}

	public String getOldResourceBasePath() {
		return oldResourceBasePath;
	}
	
	public void setOldResourceBasePath(String oldResourceBasePath) {
		this.oldResourceBasePath = oldResourceBasePath;
	}

	public String getTileSizeLoaded() {
		return tileSizeLoaded;
	}

	public List<String> getDefaultSizes() {
		return defaultSizes;
	}

	public long getFrameDuration() {
		return frameDuration;
	}

	public void setFrameDuration(long frameDuration) {
		this.frameDuration = frameDuration;
	}

	public int getTileSize() {
		return tileSize;
	}

	public void setTileSize(int tileSize) {
		this.tileSize = tileSize;
	}
}
