package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import core.Autotile;
import core.Tile;
import core.TileAnimated;
import localization.LocalizationManager;
import utils.ImageUtils;
import utils.NaturalFileComparator;
import utils.Utils;

public class ResourceLoaderWorker extends SwingWorker<Void, Void> {
	
    private final JFrame frame;
    private final JDialog loadingDialog;
    private final String[] inputs;
    
    private TileEditor tileEditor;
    
    private List<Tile> allTiles;
    private List<Tile> allObjects;
    private List<Tile> allNpcs;
    
    //store loaded resources as fields to pass them to the main UI setup
    private Map<String, List<Tile>> tileCategories;
    private Map<String, List<Tile>> objectCategories;
    private Map<String, List<Tile>> npcCategories;
    
    private List<String> texturesWithoutIds = new ArrayList<>();
    private List<String> badAutotiles = new ArrayList<>();
    
    public ResourceLoaderWorker(JFrame frame, JDialog loadingDialog, String[] inputs, TileEditor tileEditor) {
        this.frame = frame;
        this.loadingDialog = loadingDialog;
        this.inputs = inputs;
        
        this.tileEditor = tileEditor;
    }

    //this method runs on a separate background thread
    @Override
    protected Void doInBackground() throws Exception {
        //load resources
        tileCategories = loadTilesByCategory(Utils.TILES_NAME);
        objectCategories = loadTilesByCategory(Utils.OBJECTS_NAME);
        npcCategories = loadTilesByCategory(Utils.NPCS_NAME);
        
        //prepare the combined lists
        this.allTiles = tileEditor.flattenCategoryMap(tileCategories);
        this.allObjects = tileEditor.flattenCategoryMap(objectCategories);
        this.allNpcs = tileEditor.flattenCategoryMap(npcCategories);

        return null;
    }

    //this method runs back on the Event Dispatch Thread (EDT) when doInBackground is finished
    @Override
    protected void done() {
        try {
            //hide the loading dialog
            loadingDialog.dispose();
            
            //finish setting up the main GUI now that resources are ready
            tileEditor.finishGUISetup(frame, inputs, tileCategories, objectCategories, npcCategories);

            showLoadingAssetsReport(frame);
            showLoadingAutotilesReport(frame);

        } catch (Exception e) {
        	LocalizationManager loc = LocalizationManager.getInstance();
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, loc.getFormattedString("resource_fatal_error", e.getMessage()), loc.getString("resource_fatal_title"), JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private void showLoadingAssetsReport(JFrame frame) {
    	LocalizationManager loc = LocalizationManager.getInstance();
        if (!texturesWithoutIds.isEmpty()) {
            StringBuilder report = new StringBuilder(loc.getFormattedString("resource_skipped_textures", texturesWithoutIds.size()));
            
            final int howManyShow = 20;
            int count = 0;
            for (String name : texturesWithoutIds) {
                report.append("- ").append(name).append("\n");
                count++;
                
                //limit the pop up size so it does not go off screen
                if (count >= howManyShow) {
                    report.append(loc.getFormattedString("resource_and_more", texturesWithoutIds.size() - howManyShow));
                    break;
                }
            }

            JOptionPane.showMessageDialog(frame, report.toString(), loc.getString("resource_texture_warning"), JOptionPane.WARNING_MESSAGE);
            
            //clear the list so it does not double up on the next F5 refresh
            texturesWithoutIds.clear();
        }
    }
    
    private void showLoadingAutotilesReport(JFrame frame) {
    	LocalizationManager loc = LocalizationManager.getInstance();
        if (!badAutotiles.isEmpty()) {
            StringBuilder report = new StringBuilder(loc.getFormattedString("resource_skipped_autotiles", badAutotiles.size()));
            
            final int howManyShow = 20;
            int count = 0;
            for (String name : badAutotiles) {
                report.append("- ").append(name).append("\n");
                count++;
                
                //limit the pop up size so it does not go off screen
                if (count >= howManyShow) {
                    report.append(loc.getFormattedString("resource_and_more", badAutotiles.size() - howManyShow));
                    break;
                }
            }

            JOptionPane.showMessageDialog(frame, report.toString(), loc.getString("resource_autotile_warning"), JOptionPane.WARNING_MESSAGE);
            
            //clear the list so it does not double up on the next F5 refresh
            badAutotiles.clear();
        }
    }
    
    private Map<String, List<Tile>> loadTilesByCategory(String relativePath) {
        Map<String, List<Tile>> categorized = new LinkedHashMap<>();
        File baseFolder = new File(tileEditor.getLoadedSetup().getResourceBasePath(), relativePath);
        File[] subfolders = baseFolder.listFiles(File::isDirectory);

        if (subfolders == null || subfolders.length == 0) {
            return categorized;
        }

        List<Autotile> autotiles = new ArrayList<>();

        for (File folder : subfolders) {
            String category = folder.getName();
            List<Tile> tiles = new ArrayList<>();
            File[] files = folder.listFiles((dir, name) -> name.endsWith(ImageUtils.PNG_FORMAT));
            
            if (files != null) {
                Arrays.sort(files, new NaturalFileComparator());

                //temporary storage to group frames [Key = BaseName (ID_BaseName), Value = List of Images]
                Map<String, List<BufferedImage>> animationGroups = new LinkedHashMap<>();
                //map to keep track of the first ID found for that base name
                Map<String, Integer> groupIds = new HashMap<>();

                for (File file : files) {
                    String fileName = file.getName().replace(ImageUtils.PNG_FORMAT, "");
                    try {
                        List<Integer> extractedIds = Utils.extractLeadingIds(fileName);

                        //skip if no ID defined except for potential autoTiles check
                        if (extractedIds.isEmpty() && !category.equalsIgnoreCase("autotiles")) {
                            texturesWithoutIds.add(relativePath + "/" + category + "/" + fileName + ImageUtils.PNG_FORMAT);
                            continue;
                        }

                        BufferedImage image = ImageIO.read(file);

                        //handle autoTiles separately
                        if (category.equalsIgnoreCase("autotiles")) {
                            if (extractedIds.size() < 3) {
                                badAutotiles.add(relativePath + "/" + category + "/" + fileName + ImageUtils.PNG_FORMAT);
                                continue;
                            }
                            String path = tileEditor.getLoadedSetup().getResourceBasePath() + "/" + relativePath + "/" + category + "/" + fileName + ImageUtils.PNG_FORMAT;
                            
                            autotiles.add(new Autotile(extractedIds.get(0), extractedIds.get(1), 
                            		extractedIds.get(2), file.getName(), image, path));
                        }
                        else {
                            //REGEX that removes "_f" followed by one or more digits at the end of the string
                            String baseName = fileName.replaceAll("_f\\d+$", "");

                            //apply idLoader filters on the baseName
                            if (tileEditor.getIdLoader() != null) {
                                boolean skip = false;
                                if (relativePath.equals(Utils.OBJECTS_NAME) && !tileEditor.getIdLoader().containsObjectId(baseName)) skip = true;
                                if (relativePath.equals(Utils.NPCS_NAME) && !tileEditor.getIdLoader().containsNPCId(baseName)) skip = true;
                                if (relativePath.equals(Utils.TILES_NAME) && !tileEditor.getIdLoader().containsTileId(baseName)) skip = true;
                                if (skip) continue;
                            }

                            //add to group
                            animationGroups.putIfAbsent(baseName, new ArrayList<>());
                            animationGroups.get(baseName).add(image);
                            if (!extractedIds.isEmpty()) {
                                groupIds.putIfAbsent(baseName, extractedIds.get(0));
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to load image: " + file.getPath());
                    }
                }

                //convert grouped frames into tile or animated tile objects
                for (String baseName : animationGroups.keySet()) {
                    List<BufferedImage> frames = animationGroups.get(baseName);
                    int id = groupIds.getOrDefault(baseName, -1);
                    
                    String path = tileEditor.getLoadedSetup().getResourceBasePath() + "/" + relativePath + "/" + category + "/" + baseName + ImageUtils.PNG_FORMAT;
                    
                    if (frames.size() > 1) {
                        //create the new animated tile class
                        tiles.add(new TileAnimated(id, baseName, frames, tileEditor.getLoadedSetup().getFrameDuration(), path));
                    } else if (frames.size() == 1) {
                        tiles.add(new Tile(id, baseName, frames.get(0), path));
                    }
                }
            }

            if (!tiles.isEmpty()) {
                categorized.put(category, tiles);
            }
        }

        //set autoTiles
        for (Autotile autotile : autotiles) {
            Tile tile1 = Utils.getTileWithAutotile(categorized, autotile.getIdTile1());
            if (tile1 != null) {
                tile1.addAutotiles(autotile);
            }
        }

        return categorized;
    }

	public List<Tile> getAllTiles() {
		return allTiles;
	}

	public List<Tile> getAllObjects() {
		return allObjects;
	}

	public List<Tile> getAllNpcs() {
		return allNpcs;
	}

	public void setAllTiles(List<Tile> allTiles) {
		this.allTiles = allTiles;
	}

	public void setAllObjects(List<Tile> allObjects) {
		this.allObjects = allObjects;
	}

	public void setAllNpcs(List<Tile> allNpcs) {
		this.allNpcs = allNpcs;
	}
}
