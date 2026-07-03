package view;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import core.ItemType;
import core.Tile;
import core.TileObject;
import data.CanvasViewState;
import data.MapState;
import data.Selection;
import data.SelectionDragged;
import io.KeyController;
import io.MapExporter;
import io.MapLoader;
import io.MouseController;
import main.TileEditor;
import tools.AnnotatedNotesTool;
import tools.BrushTool;
import tools.ChunkSelectionTool;
import tools.ExtendMapTool;
import tools.HistoryFunction;
import utils.Utils;

public class TileCanvas {
	
	//map layers
	private MapState mapState;
	
	//view state used to toggle modes inside the tool
	private CanvasViewState canvasViewState = new CanvasViewState();
	
	//used to load and save the map
	private MapExporter mapExporter;
	private MapLoader mapLoader;
	
	//used to zoom / transform / pin the view in the canvas
	private Camera camera;
	
	//used for undo / redo actions inside tool
	private HistoryFunction historyFunction;
	
	//used for notifications inside tool view
	private ToastNotificationAbstract toastNotification;
	
	//used to copy and paste chunks of the map
	private ChunkSelectionTool chunkSelectionTool;
	
	//used to paint objects using the brush functionality tool
	private BrushTool brushTool;
	
	//used to extend or shrink the map in any of the 4 directions
	private ExtendMapTool extendMapTool;
	
	//used to add annotations to the map for the end user to take notes
	private AnnotatedNotesTool annotatedNotesTool;
	
	//current selection and index in allTiles, allObjects, allNpcs (-1: none)
	private Selection selected = new Selection(ItemType.TILE, 0);
	//used to backup the values for when you switch to erase mode and you go back to previous saved selected
	private Selection saveSelected = new Selection(ItemType.TILE, -1);
    //state for drag and drop
    private SelectionDragged draggedItem = new SelectionDragged();
    
	//used to read the input from the mouse and keyboard
	private MouseController mouseController;
	private KeyController keyController;
	
	//used to render the canvas map on the screen
	private CanvasRenderer canvasRenderer;
	
    private TileEditor tileEditor;
    
    //custom sorting formula for render order
    public static Comparator<TileObject> RENDER_COMPARATOR;

	public TileCanvas(TileEditor tileEditor, int rows, int cols, List<Tile> tiles, List<Tile> objects, List<Tile> npcs, int tileSize) {
		this.tileEditor = tileEditor;
		
		canvasRenderer = new CanvasRenderer(tileSize, canvasViewState, 
				selected, draggedItem, tileEditor.getEditorIcons().getDeleteImage(), 
				tileEditor.getEditorIcons().getObjectPickerImage(), this);
		
		initComparator(canvasRenderer.getTileSize());
		
		mapState = new MapState(tileEditor.getEditorIcons().getNotFoundIcon(), tileEditor.getLoadedSetup().getResourceBasePath(), 
				rows, cols, tiles, objects, npcs, RENDER_COMPARATOR);
		
		chunkSelectionTool = new ChunkSelectionTool(mapState);
		
		mapState.checkLoadingTexturesIntegrity(tiles, objects, npcs);
		if (!mapState.getRegistry().getAllTiles().isEmpty()) {
			selected.set(ItemType.TILE, 0);
		}
		mapState.refreshAllObjectsList();
		
		camera = new Camera();
		camera.calculateZoomValues(rows, cols, tileSize, true);
		
		mapExporter = new MapExporter(this, mapState, chunkSelectionTool, canvasViewState, canvasRenderer);
		mapLoader = new MapLoader(this, mapState, chunkSelectionTool, canvasViewState, canvasRenderer);
		
		historyFunction = new HistoryFunction(this, mapState, canvasViewState, canvasRenderer);
		historyFunction.saveState();
		
		brushTool = new BrushTool(mapState, historyFunction);
		
		extendMapTool = new ExtendMapTool(mapState, historyFunction, camera, 
				canvasViewState, canvasRenderer, keyController);
		
		annotatedNotesTool = new AnnotatedNotesTool();
		
		toastNotification = new ToastNotification(canvasRenderer);
		toastNotification.initToastNotification();
		
		//initialize the keyboard and mouse listeners
		addListeners();
		
		//repaint every 100ms to handle animations
		Timer animationTimer = new Timer(100, e -> canvasRenderer.repaint());
		animationTimer.start();
	}
	
	private void initComparator(int tileSize) {
		RENDER_COMPARATOR = Comparator
		        //sort 1 to define the three rendering layers(0, 1, 2)
				.<TileObject>comparingInt(TileObject::getRenderOrder)
		        
		        //sort 2 to apply height logic only to the normal layer(1)
			    //we only apply this depth sorting to objects that are not set above or below
			    //we only apply this logic if the layer value(0, 1, 2) is the same
		        .thenComparingInt(t -> {
		        	if (t.getRenderOrder() == 1) {
		                //apply depth sort using the computed pixel height
		                return t.getY() * tileSize + t.getComputedHeight();
		            }
			        //for above(2) or below(0) objects, their final order does not depend on height only their initial layer grouping
			        return t.getY();
		        })
		        //sort 3 in case of tie breakers, we compare the x coordinates
		        //this is executed only if both the primary and secondary comparisons resulted in a tie
		        .thenComparingInt(TileObject::getX);
	}

	private void addListeners() {
		mouseController = new MouseController(this, canvasViewState, mapState, 
				camera, historyFunction, chunkSelectionTool, brushTool);

		keyController = new KeyController(canvasRenderer);		
		
		canvasRenderer.addMouseListener(mouseController);
		canvasRenderer.addMouseMotionListener(mouseController);
		canvasRenderer.addMouseWheelListener(mouseController);
		
		//add the new key listener
		canvasRenderer.addKeyListener(keyController);
		//this allows the panel to receive keyboard focus
		canvasRenderer.setFocusable(true);
		canvasRenderer.requestFocusInWindow();
	}

	//fill the entire map with the given tile
	public void fillMapWithTile(int tileIndex) {
		if (tileIndex < 0)
			return;
		historyFunction.saveState();
		for (int[] row : mapState.getData().getTileMap()) {
			Arrays.fill(row, tileIndex);
		}
		//if the autoTile is not active then do not reset autoTile Map to save time
		if (canvasViewState.isShowAutotile())
			mapState.refreshAutotileMap();
		canvasRenderer.repaint();
	}

	//fills only tiles that are currently empty(-1)
	public void fillEmptyTilesWithSelected() {
		if (selected.isTileMode() == false)
			return;
		historyFunction.saveState();
		for (int r = 0; r < mapState.getData().getRows(); r++) {
			for (int c = 0; c < mapState.getData().getCols(); c++) {
				if (mapState.getData().getTileMap(r, c) == -1) {
					mapState.getData().setTileMap(r, c, selected.getIndex());
				}
			}
		}
		//if the autoTile is not active then do not reset autoTile Map to save time
		if (canvasViewState.isShowAutotile())
			mapState.refreshAutotileMap();
		canvasRenderer.repaint();
	}

	public void cleanupMissingAssets() {
	    int removedTilesCount = 0;
	    int removedObjectsCount = 0;
	    int removedNPCsCount = 0;

	    //cleanup tiles
	    for (int r = 0; r < mapState.getData().getRows(); r++) {
	        for (int c = 0; c < mapState.getData().getCols(); c++) {
	            if (mapState.getData().getTileMap(r, c) >= 0 && !Utils.tileExists(mapState.getRegistry().getAllTiles(), mapState.getData().getTileMap(r, c))) {
	                mapState.getData().setTileMap(r, c, -1);
	                removedTilesCount++;
	            }
	            if (mapState.getData().getObjectMap(r, c) >= 0 && !Utils.tileExists(mapState.getRegistry().getAllObjects(), mapState.getData().getObjectMap(r, c))) {
	            	mapState.getData().setObjectMap(r, c, -1);
	                removedObjectsCount++;
	            }
	            if (mapState.getData().getNpcMap(r, c) >= 0 && !Utils.tileExists(mapState.getRegistry().getAllNpcs(), mapState.getData().getNpcMap(r, c))) {
	            	mapState.getData().setNpcMap(r, c, -1);
	                removedNPCsCount++;
	            }
	        }
	    }
	    
	    //if the autoTile is not active then do not reset autoTile Map to save time
		if (canvasViewState.isShowAutotile())
			mapState.refreshAutotileMap();
		mapState.refreshAllObjectsList();
		canvasRenderer.repaint();
	    JOptionPane.showMessageDialog(canvasRenderer, 
	        "Cleanup complete! Removed invalid references: " + removedTilesCount + " tiles, " + 
	    removedObjectsCount + " objects and " + removedNPCsCount + " NPCs.", 
	        "Map Cleanup", 
	        JOptionPane.INFORMATION_MESSAGE);
	}
	
	//count the instances that are located on the map
	public int getLocateCounter() {
		int counter = 0;
		if(selected.isTileMode()) {
	        for (int r = 0; r < mapState.getData().getRows(); r++) {
	            for (int c = 0; c < mapState.getData().getCols(); c++) {
	            	if(selected.getIndex() == mapState.getData().getTileMap(r, c)) {
	            		counter++;
	            	}
	            }
	        }
		}
		else {
			for(TileObject objects: mapState.getRegistry().getAllSortedItems()) {
				if(selected.isEraseMode() && objects.isObject() &&
						saveSelected.getType() == ItemType.OBJECT &&
						saveSelected.getIndex() == objects.getId()) {
					counter ++;
				}
				else if((objects.isObject() && selected.getIndex() == objects.getId()) ||
						(!objects.isObject() && selected.getIndex() == objects.getId())) {
					counter++;
				}
			}
		}
		return counter;
	}

	public void useBrushObject(int[] brushObjectIds) {
		selected.setBrushTool();
		brushTool.setBrushObjectIds(brushObjectIds);
		
		tileEditor.getStatusInfoBar().updateStatusUI();
		canvasRenderer.repaint();
	}
	
	public void toggleBrushObject(int[] brushObjectIds) {
		if(selected.isBrushTool()) {
			backToSavedSelected();
		}
		else {
			selected.setBrushTool();
			brushTool.setBrushObjectIds(brushObjectIds);
		}
		
		tileEditor.getStatusInfoBar().updateStatusUI();
		canvasRenderer.repaint();
	}
	
	public void setUseBrushObjectFalse() {
		if(selected.isBrushTool()) {
			//exit brush mode when clear and in brush mode
			backToSavedSelected();
		}
	}
	
	public void setBrushSpread(int brushSpread) {
		brushTool.setBrushSpread(brushSpread);
	}

	public void togglePlaceNpcWalkArea() {
		if(selected.isNpcWalkAreaMode()) {
			backToSavedSelected();
		}
		else {
			selected.setNpcWalkAreaMode();
		}
		
		tileEditor.getStatusInfoBar().updateStatusUI();
		canvasRenderer.repaint();
	}
	
	public void setSelectedTileIndex(int index) {
		selected.set(ItemType.TILE, index);
		
		//save last item state for when you come back to last state from brush tool or other states
		saveSelected.set(ItemType.TILE, index);
		
		tileEditor.getStatusInfoBar().updateStatusUI();
		canvasRenderer.repaint();
	}

	public void setSelectedObjectIndex(int index) {
		selected.set(ItemType.OBJECT, index);
		
		//save last item state for when you come back to last state
		saveSelected.set(ItemType.OBJECT, index);
		
		tileEditor.getStatusInfoBar().updateStatusUI();
		canvasRenderer.repaint();
	}
	
	public void setSelectedNpcIndex(int index) {
		selected.set(ItemType.NPC, index);
		
		//save last item state for when you come back to last state
		saveSelected.set(ItemType.NPC, index);
		
		tileEditor.getStatusInfoBar().updateStatusUI();
		canvasRenderer.repaint();
	}
	
	public void setSelectedEraseMode() {
		//exit erase mode when you press again
		if(selected.isEraseMode()) {
			backToSavedSelected();
		}
		//if not erase mode already, then set to erase mode
		else {
			selected.setEraseMode();
		}
		
		tileEditor.getStatusInfoBar().updateStatusUI();
		canvasRenderer.repaint();
	}
	
	public void setNotesTool() {
		//exit notes tool when you press again
		if(selected.isNotesTool()) {
			backToSavedSelected();
		}
		//if not notes tool already, then set to notes tool
		else {
			selected.setNotesTool();
		}
		
		tileEditor.getStatusInfoBar().updateStatusUI();
		canvasRenderer.repaint();
	}
	
	//used to go back to the saved selected value before switching to something else
	private void backToSavedSelected() {
		if(saveSelected.getType() == ItemType.TILE) {
			setSelectedTileIndex(saveSelected.getIndex());
		}
		else if(saveSelected.getType() == ItemType.OBJECT) {
			setSelectedObjectIndex(saveSelected.getIndex());
		} 
		else if(saveSelected.getType() == ItemType.NPC) {
			setSelectedNpcIndex(saveSelected.getIndex());
		}
	}
	
	public void toggleChunkSelectionMode() {
		if(selected.isChunkSelectionTool()) {
			backToSavedSelected();
		}
		else {
			selected.setChunkSelectionTool();
		}
		
		tileEditor.getStatusInfoBar().updateStatusUI();
	}
	
	public Selection getSelected() {
		return selected;
	}
	
	public SelectionDragged getDraggedItem() {
		return draggedItem;
	}

	public MapState getMapState() {
		return mapState;
	}

	public TileEditor getTileEditor() {
		return tileEditor;
	}

	public MapExporter getMapExporter() {
		return mapExporter;
	}

	public MapLoader getMapLoader() {
		return mapLoader;
	}

	public HistoryFunction getHistoryFunction() {
		return historyFunction;
	}

	public ToastNotificationAbstract getToastNotification() {
		return toastNotification;
	}

	public Camera getCamera() {
		return camera;
	}

	public ExtendMapTool getExtendMapTool() {
		return extendMapTool;
	}

	public CanvasViewState getCanvasViewState() {
		return canvasViewState;
	}

	public KeyController getKeyController() {
		return keyController;
	}
	
	public MouseController getMouseController() {
		return mouseController;
	}

	public CanvasRenderer getCanvasRenderer() {
		return canvasRenderer;
	}

	public ChunkSelectionTool getChunkSelectionTool() {
		return chunkSelectionTool;
	}

	public AnnotatedNotesTool getAnnotatedNotesTool() {
		return annotatedNotesTool;
	}
}
