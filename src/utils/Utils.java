package utils;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import core.Tile;
import localization.LocalizationManager;

public final class Utils {
	
	private Utils(){}
	
    public static final String DEFAULT_BASE_PATH = "assets";
    
    public static final String TILES_NAME = "tiles";
    public static final String OBJECTS_NAME = "objects";
    public static final String NPCS_NAME = "npcs";
    
	public static final String CSV_FILE_EXTENSION = ".csv";
	public static final String JSON_FILE_EXTENSION = ".json";
	public static final String TMX_FILE_EXTENSION = ".tmx";
	public static final String TSX_FILE_EXTENSION = ".tsx";
	public static final String EXPORT_MAP_EXTENSION = ".lvl";
	public static final String TXT_FILE_EXTENSION = ".txt";
	public static final String SAVE_MAP_EXTENSION = ".tmdot";
	
	private static final String RENDER_UNDER_SEPARATOR = "b";
	private static final String RENDER_ABOVE_SEPARATOR = "a";
	private static final String NAME_SEPARATOR = "#";
	
    public static final Color COLOR_ENGINE_BLUE = new Color(0, 122, 204);
    public static final Color COLOR_SAGE_GREEN = new Color(78, 201, 176);
	
	public static int getRenderOrder(String name) {
		String[] nameParts = name.split(NAME_SEPARATOR);
		for(String value: nameParts) {
			if(RENDER_UNDER_SEPARATOR.equals(value.toLowerCase())) {
				return 0;
			}
			if(RENDER_ABOVE_SEPARATOR.equals(value.toLowerCase())) {
				return 2;
			}
		}
		return 1;
	}
	
	public static Tile findFirstDuplicateTile(List<Tile> tiles) {
		Set<Integer> seenIds = new HashSet<>();
		return tiles.stream().filter(tile -> !seenIds.add(tile.getId())).findFirst().orElse(null);
	}
	
	public static boolean tileExists(List<Tile> allTiles, int id) {
		for (Tile tile : allTiles) {
			if (tile.getId() == id) {
				return true;
			}
		}
		return false;
	}
	
	public static int countValidObjects(int[][] tileMap, int... excludedIds) {
		//convert the array to a Set for O(1) lookup time
	    Set<Integer> excludeSet = new HashSet<>();
	    for (int id : excludedIds) {
	        excludeSet.add(id);
	    }
	    
		int count = 0;
	    for (int i = 0; i < tileMap.length; i++) {
	        for (int j = 0; j < tileMap[i].length; j++) {
	            if (tileMap[i][j] != -1 && !excludeSet.contains(tileMap[i][j])) {
	                count++;
	            }
	        }
	    }
	    return count;
	}
	
	public static File addFileType(File file, String endsWithType) {
		//determine the file extension
		if (!file.getName().toLowerCase().endsWith(endsWithType)) {
			file = new File(file.getAbsolutePath() + endsWithType);
		}
		return file;
	}
	
	public static File extendFileName(File file, String extension) {
		//get the original path and name
	    String absolutePath = file.getAbsolutePath();
	    
	    //remove the extension if the user typed one and append your suffix
	    String newPath;
	    if (absolutePath.contains(".")) {
	        newPath = absolutePath.substring(0, absolutePath.lastIndexOf(".")) + extension;
	    } else {
	        newPath = absolutePath + extension;
	    }
	    
	    //reconstruct the File object
	    return new File(newPath);
	}
	
    //extracts all leading numeric IDs from a string split by any non digit characters and stops at the first non numeric part
    public static List<Integer> extractLeadingIds(String input) {
        List<Integer> ids = new ArrayList<>();

        //split by any non digit character
        String[] parts = input.split("\\D+");

        for (String part : parts) {
            if (!part.isEmpty()) {
                ids.add(Integer.parseInt(part));
            } else {
                break;
            }
        }

        return ids;
    }
    
	public static void collectExistingIDs(File folder, Set<Integer> idSet) {
	    File[] files = folder.listFiles();
	    if (files == null) return;
	    for (File f : files) {
	        if (f.isDirectory()) {
	            collectExistingIDs(f, idSet);
	        } else if (f.getName().endsWith(ImageUtils.PNG_FORMAT)) {
	            List<Integer> ids = Utils.extractLeadingIds(f.getName().replace(ImageUtils.PNG_FORMAT, ""));
	            if (!ids.isEmpty()) {
	                idSet.add(ids.get(0));
	            }
	        }
	    }
	}
	
    public static JDialog createLoadingDialog(JFrame parent) {
    	LocalizationManager loc = LocalizationManager.getInstance();
        JDialog loadingDialog = new JDialog(parent, loc.getString("loading_title"), true); //true makes it modal
        loadingDialog.setUndecorated(true); //remove borders of the tool to look better and cleaner
        
        //add a simple label or message
        JLabel message = new JLabel(loc.getString("loading_message"), SwingConstants.CENTER);
        message.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        loadingDialog.add(message);
        
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(parent); //center it relative to the main frame location
        
        return loadingDialog;
    }
    
    public static void saveInitialDefaultValue(String fileName, String value) {
        File file = new File("assets/settings/" + fileName);
        
        //ensure the directory exists so we do not get a FileNotFoundException
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print(value);
        } catch (IOException e) {
            System.err.println("Error saving to " + fileName + ": " + e.getMessage());
        }
    }
    
    public static String removeSuffix(String input, String suffix) {
    	//calculate the new length
        int newLength = input.length() - suffix.length();

        //return a new string from the beginning up to the new length
        return input.substring(0, newLength);
    }
    
    public static Tile getTileWithAutotile(Map<String, List<Tile>> categorized, int autotileId) {
        for (List<Tile> tileList : categorized.values()) {
            for(Tile tile: tileList) {
            	if(tile.getId() == autotileId) {
            		return tile;
            	}
            }
        }
        return null;
    }
    
	public static int processCategoryForIDs(File folder, Set<Integer> globalUsedIDs) {
	    File[] files = folder.listFiles();
	    if (files == null) return 0;

	    int renamedCount = 0;
	    
	    //first handle all subfolders recursively
	    for (File f : files) {
	        if (f.isDirectory()) {
	            renamedCount += processCategoryForIDs(f, globalUsedIDs);
	        }
	    }

	    //now handle files in this specific folder
	    //find the smallest ID existing in this subfolder to use as a starting point
	    int localStartID = Integer.MAX_VALUE;
	    boolean foundLocalID = false;

	    List<File> filesToRename = new LinkedList<>();

	    for (File f : files) {
	        if (f.isFile() && f.getName().endsWith(ImageUtils.PNG_FORMAT)) {
	            List<Integer> ids = Utils.extractLeadingIds(f.getName().replace(ImageUtils.PNG_FORMAT, ""));
	            if (!ids.isEmpty()) {
	                localStartID = Math.min(localStartID, ids.get(0));
	                foundLocalID = true;
	            }
	            else {
	            	filesToRename.add(f);
	            }
	        }
	    }

	    //if folder is empty of IDs, start search from 1, otherwise start from the local minimum
	    int searchStart = foundLocalID ? localStartID : 1;

		for (File f : filesToRename) {
			//make sure they are empty even if I checked already
			String name = f.getName().replace(ImageUtils.PNG_FORMAT, "");
			if (Utils.extractLeadingIds(name).isEmpty()) {
				//find the first ID >= searchStart that is not used anywhere in the category
				int nextAvailableID = searchStart;
				while (globalUsedIDs.contains(nextAvailableID)) {
					nextAvailableID++;
				}
				String newName = nextAvailableID + "_" + f.getName();
				File renamedFile = new File(f.getParent(), newName);

				if (f.renameTo(renamedFile)) {
					//important add to global set so other folders do not take it
					globalUsedIDs.add(nextAvailableID);
					renamedCount++;
				}
			}
		}
	    return renamedCount;
	}
	
    //reads the asset path from default_assets_path.txt located in the project root 
    //falling back to "assets" if the file does not exist or is empty
    public static String loadInitialDefaultValues(String fileName) {
        File file = new File("assets/settings/" + fileName);
        String defaultPath = "assets";
        
        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextLine()) {
                String path = scanner.nextLine().trim();
                if (!path.isEmpty()) {
                    return path;
                }
            }
        } catch (FileNotFoundException e) {
            //this is expected if the file has not been created yet
            System.out.println(fileName + " not found. Using default path: " + defaultPath);
        }
        
        return defaultPath;
    }
    
    public static List<String> loadDefaultGridSizes() {
        List<String> sizes = new ArrayList<>();
        File file = new File("assets/settings/default_grids.txt");

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.matches("\\d+x\\d+")) {
                    sizes.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load default grid sizes from " + file.getPath() + ". Using hardcoded defaults.");
            sizes.add("50x50");
            sizes.add("100x100");
            sizes.add("200x150");
        }
        return sizes;
    }
}
