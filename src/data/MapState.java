package data;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import core.Autotile;
import core.PointC;
import core.Tile;
import core.TileObject;
import localization.LocalizationManager;
import utils.Utils;

public class MapState {
	
	private final MapData data;
    private final MapRegistry registry;
    private final CacheData cacheData;
    private final Comparator<TileObject> RENDER_COMPARATOR;
    
    public MapState(BufferedImage notFoundIcon, String resourceBasePath,
    		int rows, int cols,
    		List<Tile> allTiles, List<Tile> allObjects, List<Tile> allNpcs,
    		final Comparator<TileObject> RENDER_COMPARATOR) {
    	this.data = new MapData(rows, cols);
    	this.registry = new MapRegistry(allTiles, allObjects, allNpcs);
    	this.cacheData = new CacheData(notFoundIcon, resourceBasePath);
    	this.RENDER_COMPARATOR = RENDER_COMPARATOR;
    }
    
	public MapRegistry getRegistry() {
		return registry;
	}

	public MapData getData() {
		return data;
	}
	
	public CacheData getCacheData() {
		return cacheData;
	}
	
	public void loadNewTextures(List<Tile> tiles, List<Tile> objects, List<Tile> npcs) {
		registry.clear();
		registry.setAll(tiles, objects, npcs);
		
		checkLoadingTexturesIntegrity(tiles, objects, npcs);
	}
	
	public void checkLoadingTexturesIntegrity(List<Tile> tiles, List<Tile> objects, List<Tile> npcs) {
		LocalizationManager loc = LocalizationManager.getInstance();
		
		//check if there are duplicate IDs in the file manager where the textures are
		Tile tileCheck = Utils.findFirstDuplicateTile(tiles);
		if (tileCheck != null) {
			JOptionPane
					.showMessageDialog(null,
							loc.getFormattedString("dialog_error_load_duplicate_texture", tileCheck.getName(), tileCheck.getId()),
							loc.getString("dialog_load_error"), JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		Tile objectCheck = Utils.findFirstDuplicateTile(objects);
		if (objectCheck != null) {
			JOptionPane
					.showMessageDialog(null,
							loc.getFormattedString("dialog_error_load_duplicate_texture", objectCheck.getName(), objectCheck.getId()),
							loc.getString("dialog_load_error"), JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		Tile npcCheck = Utils.findFirstDuplicateTile(npcs);
		if (npcCheck != null) {
			JOptionPane
					.showMessageDialog(null,
							loc.getFormattedString("dialog_error_load_duplicate_texture", npcCheck.getName(), npcCheck.getId()),
							loc.getString("dialog_load_error"), JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	public Tile findTileById(int id) {
		//optimized to skip every update call if the last tile is already saved
		if (id == cacheData.getCachedTile().getId()) {
			return cacheData.getCachedTile();
		}

		for (Tile tile : registry.getAllTiles()) {
			if (tile.getId() == id) {
				cacheData.setCachedTile(tile);
				return tile;
			}
		}

		//return default value
		cacheData.setCachedTile(new Tile(id, CacheData.NOT_FOUND_NAME, cacheData.getNotFoundIcon(), cacheData.getResourceBasePath()));
		return cacheData.getCachedTile();
	}

	public Tile findObjectById(int id) {
		//optimized to skip every update call if the last tile is already saved
		if (id == cacheData.getCachedObject().getId()) {
			return cacheData.getCachedObject();
		}

		for (Tile tile : registry.getAllObjects()) {
			if (tile.getId() == id) {
				cacheData.setCachedObject(tile);
				return tile;
			}
		}

		//return default value
		cacheData.setCachedTile(new Tile(id, CacheData.NOT_FOUND_NAME, cacheData.getNotFoundIcon(), cacheData.getResourceBasePath()));
		return cacheData.getCachedTile();
	}
    
	public Tile findNpcById(int id) {
		//optimized to skip every update call if the last tile is already saved
		if (id == cacheData.getCachedNPC().getId()) {
			return cacheData.getCachedNPC();
		}

		for (Tile tile : registry.getAllNpcs()) {
			if (tile.getId() == id) {
				cacheData.setCachedNPC(tile);
				return tile;
			}
		}
		
		//return default value
		cacheData.setCachedTile(new Tile(id, CacheData.NOT_FOUND_NAME, cacheData.getNotFoundIcon(), cacheData.getResourceBasePath()));
		return cacheData.getCachedTile();
	}
	
	public void addNewSortedObject(int r, int c, boolean isObject) {
		int objId;
		Tile newObject;
		TileObject newTile;
		
		if(isObject) {
			objId = data.getObjectMap(r, c);
			newObject = findObjectById(objId);
			newTile = new TileObject(newObject, c, r, true, Utils.getRenderOrder(newObject.getName()));
		}
		else {
			objId = data.getNpcMap(r, c);
			newObject = findNpcById(objId);
			newTile = new TileObject(newObject, c, r, false, Utils.getRenderOrder(newObject.getName()));
		}
		
		addNewSortedObject(newTile, registry.getAllSortedItems());
	}
	
	//adds a new TileObject to the globally sorted list in O(log N) time which must already be sorted
	public void addNewSortedObject(TileObject newObject, List<TileObject> allSortedItems) {
	    //find the correct insertion point using the binary search algorithm
	    int insertionIndex = Collections.binarySearch(allSortedItems, newObject, RENDER_COMPARATOR);

	    //if the result is negative, calculate the correct insertion index
	    //the bitwise NOT operator (~) is equivalent to -insertionIndex - 1
	    if (insertionIndex < 0) {
	        insertionIndex = ~insertionIndex;
	    }
	    
	    //insert the object at the found index maintaining the list order
	    allSortedItems.add(insertionIndex, newObject);
	}
	
	public void refreshAllObjectsList() {
        //sort every object and NPC together
        registry.setAllSortedItems(sortAllObjectsList(0, 0, data.getRows(), data.getCols(), 0));
	}
	
	private List<TileObject> sortAllObjectsList(int startRow, int startCol, int endRow, int endCol, int offsetTiles) {
		List<TileObject> tiles = new ArrayList<>();

		for (int r = Math.max(0, startRow - offsetTiles); r < endRow; r++) {
			for (int c = Math.max(0, startCol - offsetTiles); c < endCol; c++) {
				int objId = data.getObjectMap(r, c);
				if (objId >= 0) {
					Tile object = findObjectById(objId);
					tiles.add(new TileObject(object, c, r, true, Utils.getRenderOrder(object.getName())));
				}
				int npcId = data.getNpcMap(r, c);
				if (npcId >= 0) {
					Tile npc = findNpcById(npcId);
					tiles.add(new TileObject(npc, c, r, false, Utils.getRenderOrder(npc.getName())));
				}
			}
		}
		
		tiles.sort(RENDER_COMPARATOR);
		
		return tiles;
	}
	
	public void refreshAutotileMap() {
		refreshAutotileMap(0, 0, data.getRows(), data.getCols());
	}

	public void refreshAutotileMap(int rowStart, int colStart, int rowEnd, int colEnd) {
		//if the map size changed then change the size of autoTile map
		if (data.getAutotileMap().length != data.getRows() || data.getAutotileMap()[0].length != data.getCols()) {
			data.initAutotileMap();
		}

		//make sure that the given values are within the map size
		int normRowStart = Math.max(0, rowStart);
		int normColStart = Math.max(0, colStart);
		int normRowEnd = Math.min(data.getRows(), rowEnd);
		int normColEnd = Math.min(data.getCols(), colEnd);

		for (int r = normRowStart; r < normRowEnd; r++) {
			for (int c = normColStart; c < normColEnd; c++) {
				int tileId = data.getTileMap(r, c);
				if (tileId >= 0) {
					Tile tile = findTileById(tileId);
					//draw normally
					if (tile.getAutotiles() == null) {
						data.setAutotileMap(r, c, tile);
					}
					//draw autoTiles
					else {
						BufferedImage image = getAutoTiledImage(tile, r, c);
						//if no autoTile found save the current texture
						if (image == null) {
							data.setAutotileMap(r, c, tile);
						} else {
							data.setAutotileMap(r, c, new Tile(tile.getAutotiles().get(0).getId(),
									tile.getAutotiles().get(0).getName(), image, tile.getPath()));
						}
					}
				} else if (tileId == -1) {
					//empty tile map
					data.setAutotileMap(r, c, null);
				}
			}
		}
	}
	
	public BufferedImage getAutoTiledImage(Tile currentTile, int gridX, int gridY) {
		PointC point = getAutoTiledPoint(currentTile, gridX, gridY);
		if (point != null) {
			return currentTile.getAutotiles().get(point.getId()).getSection(point.getY(), point.getX());
		}
		return null;
	}
	
	public int getAutoTiledSubimageId(Tile currentTile, int gridX, int gridY) {
		PointC point = getAutoTiledPoint(currentTile, gridX, gridY);
		if (point != null) {
			return currentTile.getAutotiles().get(point.getId()).getSectionId(point.getY(), point.getX());
		}
		return -1;
	}

	private PointC getAutoTiledPoint(Tile currentTile, int gridX, int gridY) {
		for (int i = 0; i < currentTile.getAutotiles().size(); i++) {
			Autotile autotile = currentTile.getAutotiles().get(i);

			boolean surrounded = checkIfSurrounded(gridX, gridY, autotile);
			if (surrounded) {
				String neighborTiles = getNeighborTiles(gridX, gridY);
				if (neighborTiles.equals("000110110") || neighborTiles.equals("001110110")
						|| neighborTiles.equals("100110110") || neighborTiles.equals("000110111")
						|| neighborTiles.equals("100110111")) {
					return new PointC(i, 0, 0);
				} else if (neighborTiles.equals("011011011") || neighborTiles.equals("111011011")
						|| neighborTiles.equals("011011111") || neighborTiles.equals("111011111")) {
					return new PointC(i, 0, 1);
				} else if (neighborTiles.equals("110111111")) {
					return new PointC(i, 0, 2);
				} else if (neighborTiles.equals("000111111") || neighborTiles.equals("100111111")
						|| neighborTiles.equals("001111111") || neighborTiles.equals("101111111")) {
					return new PointC(i, 0, 3);
//				} else if (neighborTiles.equals("110111011")) {
//					return new PointC(i, 1, 0);
				} else if (neighborTiles.equals("011111111")) {
					return new PointC(i, 1, 1);
				} else if (neighborTiles.equals("111111111")) {
//					return this so we can add the original texture not the subSection from the
//					autoTile
					return null;
				} else if (neighborTiles.equals("111111110")) {
					return new PointC(i, 1, 3);
				} else if (neighborTiles.equals("011011000") || neighborTiles.equals("011011100")
						|| neighborTiles.equals("111011000") || neighborTiles.equals("011011001")
						|| neighborTiles.equals("111011001")) {
					return new PointC(i, 2, 0);
				} else if (neighborTiles.equals("111111000") || neighborTiles.equals("111111100")
						|| neighborTiles.equals("111111001") || neighborTiles.equals("111111101")) {
					return new PointC(i, 2, 1);
				} else if (neighborTiles.equals("111111011")) {
					return new PointC(i, 2, 2);
				} else if (neighborTiles.equals("110110110") || neighborTiles.equals("111110110")
						|| neighborTiles.equals("110110111") || neighborTiles.equals("111110111")) {
					return new PointC(i, 2, 3);
				} else if (neighborTiles.equals("000011011") || neighborTiles.equals("100011011")
						|| neighborTiles.equals("001011011") || neighborTiles.equals("000011111")
						|| neighborTiles.equals("001011111")) {
					return new PointC(i, 3, 1);
//				} else if (neighborTiles.equals("011111110")) {
//					return new PointC(i, 3, 2);
				} else if (neighborTiles.equals("110110000") || neighborTiles.equals("110110001")
						|| neighborTiles.equals("111110000") || neighborTiles.equals("110110100")
						|| neighborTiles.equals("111110100")) {
					return new PointC(i, 3, 3);
				}
//				return default values, null will ensure default value rendered
				return null;
			}
		}
		return null;
	}
	
	//return the currently used autoTile in the current tile position if any
	public Autotile getCurrentAutotile(Tile currentTile, int gridX, int gridY) {
		if(currentTile.getAutotiles() == null)
			return null;
		
		for (int i = 0; i < currentTile.getAutotiles().size(); i++) {
			Autotile autotile = currentTile.getAutotiles().get(i);

			boolean surrounded = checkIfSurrounded(gridX, gridY, autotile);
			if (surrounded) {
				return autotile;
			}
		}
		return null;
	}
	
	private String getNeighborTiles(int gridX, int gridY) {
		StringBuilder stringBuilder = new StringBuilder();
		Tile currentTile = findTileById(data.getTileMap(gridX, gridY));
		
		for (int x = gridX - 1; x < gridX + 2; x++) {
			for (int y = gridY - 1; y < gridY + 2; y++) {
				if (!gridWithinBounds(x, y)) {
					stringBuilder.append(1);
					continue;
				}
				
				if (findTileById(data.getTileMap(x, y)).getId() == currentTile.getId()) {
					stringBuilder.append(1);
				} else {
					stringBuilder.append(0);
				}
			}
		}

		return stringBuilder.toString();
	}

	private boolean checkIfSurrounded(int gridX, int gridY, Autotile autotile) {
		for (int x = gridX - 1; x < gridX + 2; x++) {
			for (int y = gridY - 1; y < gridY + 2; y++) {
				if (gridWithinBounds(x, y)) {
					if (!autotile.containsIds(data.getTileMap(x, y)))
						return false;
				}
			}
		}
		return true;
	}

	private boolean gridWithinBounds(int x, int y) {
		if (x >= 0 && y >= 0 && x < data.getRows() && y < data.getCols())
			return true;
		return false;
	}
	
	public Map<Integer, Tile> getUniqueSortedTiles() {
		List<Integer> IDs = getUniqueSortedTileIds(data.getTileMap());
		Map<Integer, Tile> map = new HashMap<>();
		for(int id : IDs) {
			map.put(id, findTileById(id));
		}
		return map;
	}
	
	public Map<Integer, Tile> getUniqueSortedObjects() {
		List<Integer> IDs = getUniqueSortedTileIds(data.getObjectMap());
		Map<Integer, Tile> map = new HashMap<>();
		for(int id : IDs) {
			map.put(id, findObjectById(id));
		}
		return map;
	}
	
	public Map<Integer, Tile> getUniqueSortedNpcs() {
		List<Integer> IDs = getUniqueSortedTileIds(data.getNpcMap());
		Map<Integer, Tile> map = new HashMap<>();
		for(int id : IDs) {
			map.put(id, findNpcById(id));
		}
		return map;
	}
	
	public Map<Integer, Autotile> getUniqueSortedAutotiles() {
		Map<Integer, Autotile> originalAutotiles = new HashMap<>();
		
		for (int r = 0; r < data.getRows(); r++) {
			for (int c = 0; c < data.getCols(); c++) {
				int tileId = data.getTileMap(r, c);
				Tile tile = findTileById(tileId);

				Autotile autotile = getCurrentAutotile(tile, r, c);
				
				if (autotile != null) {
					originalAutotiles.put(autotile.getIdStartSubTile(), autotile);
				}
			}
		}
		return originalAutotiles;
	}
	
	public List<Integer> getUniqueSortedTileIds(int[][] tileMap) {
	    //treeSet automatically keeps elements in ascending order
	    Set<Integer> sortedSet = new TreeSet<>();

	    for (int[] row : tileMap) {
	        for (int id : row) {
	        	//skip the -1 numbers because it represents empty space
	        	if(id >= 0) {
	        		sortedSet.add(id);
	        	}
	        }
	    }

	    return new ArrayList<>(sortedSet);
	}
}
