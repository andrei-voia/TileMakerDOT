package io;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import core.ItemType;
import core.TileObject;
import data.CanvasViewState;
import data.MapNote;
import data.MapState;
import data.NoteColor;
import data.ButtonsOptions;
import tools.BrushTool;
import tools.ChunkSelectionTool;
import tools.HistoryFunction;
import view.Camera;
import view.TileCanvas;

public class MouseController extends MouseAdapter {

	private TileCanvas tileCanvas;
	private CanvasViewState canvasViewState;
	private MapState mapState;
	private Camera camera;
	private HistoryFunction historyFunction;
	private ChunkSelectionTool chunkSelectionTool;
	private BrushTool brushTool;
	
	//this flag tracks if the mouse press was for a single Object / NPC placement / erasure
	private boolean isPlacingSingleItem = false;
	
	//mouse dragging State
	private Point dragStartPoint; 		//for panning
	private Point selectionStartPoint; 	//for rectangle selection
	
	public MouseController(TileCanvas tileCanvas, CanvasViewState canvasViewState,
			MapState mapState, Camera camera, HistoryFunction historyFunction,
			ChunkSelectionTool chunkSelectionTool, BrushTool brushTool) {
		this.tileCanvas = tileCanvas;
		this.canvasViewState = canvasViewState;
		this.mapState = mapState;
		this.camera = camera;
		this.historyFunction = historyFunction;
		this.chunkSelectionTool = chunkSelectionTool;
		this.brushTool = brushTool;
	}
	
	@Override
	public void mouseMoved(MouseEvent e){
		canvasViewState.setObjectPreviewPosition(getHoverTileRowCol());
		tileCanvas.getCanvasRenderer().repaint();
	}
	
	@Override
    public void mouseExited(MouseEvent e) {
        //when the mouse leaves the component
        if (canvasViewState.getObjectPreviewPosition() != null) {
        	//clear the preview position
        	canvasViewState.setObjectPreviewPosition(null);
        	//force a repaint to erase the preview
            tileCanvas.getCanvasRenderer().repaint();
        }
    }
	
	@Override
	public void mousePressed(MouseEvent e) {
		//focus on the canvas area every time you click it there
		//i moved it from the paintComponent method because i need to be able to focus on search input when clicked
		tileCanvas.getCanvasRenderer().requestFocusInWindow();
		
		//tile picking
		if (SwingUtilities.isRightMouseButton(e) && (e.isShiftDown() || e.isControlDown()) &&
				!tileCanvas.getSelected().isNotesTool()) {
			pickTileOrObject(e.getX(), e.getY());
			//execute picking and stop processing the click
			return;
		}

		//panning view
		if (SwingUtilities.isRightMouseButton(e)) {
			dragStartPoint = e.getPoint();
			tileCanvas.getCanvasRenderer().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			return;
		}

		//left click / placement Logic
		if (SwingUtilities.isLeftMouseButton(e)) {
			int col = camera.posX(e.getX(), tileCanvas.getCanvasRenderer().getTileSize());
			int row = camera.posY(e.getY(), tileCanvas.getCanvasRenderer().getTileSize());
			
			if (row < 0 || row >= mapState.getData().getRows() || col < 0 || col >= mapState.getData().getCols())
				return;
			
			//add annotations to the map by left clicking it when active
			if(tileCanvas.getSelected().isNotesTool()) {
				updateAnnotatedNotes(e, col, row);
				tileCanvas.getCanvasRenderer().repaint();
				return;
			}
			
		    //drag and drop and grab if clicking on an existing NPC or Object while in the correct mode
		    if (!tileCanvas.getSelected().isChunkSelectionTool() && !e.isShiftDown() && !e.isAltDown()) {
		        if (mapState.getData().getNpcMap(row, col) != -1 && (tileCanvas.getSelected().isNpcMode() || tileCanvas.getSelected().isEraseMode())) {
		        	tileCanvas.getDraggedItem().setItemSource(new Point(row, col));
		        	tileCanvas.getDraggedItem().setIndex(mapState.getData().getNpcMap(row, col));
		        	tileCanvas.getDraggedItem().setType(ItemType.NPC);
		        }
		        else if (mapState.getData().getObjectMap(row, col) != -1 && (tileCanvas.getSelected().isObjectMode() || tileCanvas.getSelected().isEraseMode())) {
		        	tileCanvas.getDraggedItem().setItemSource(new Point(row, col));
		        	tileCanvas.getDraggedItem().setIndex(mapState.getData().getObjectMap(row, col));
		        	tileCanvas.getDraggedItem().setType(ItemType.OBJECT);
		        }
		    }
			
			//used to copy paste zones in other parts
			if (tileCanvas.getSelected().isChunkSelectionTool()) {
				if ((e.isShiftDown() || e.isAltDown()) && selectionStartPoint == null) {
					//start rectangle selection
					selectionStartPoint = e.getPoint();
					//set flag to prevent single tile painting during drag
					isPlacingSingleItem = true;
				}
				//if you are in paste mode and the user let go of the mouse
				else if (chunkSelectionTool != null && chunkSelectionTool.getMapChunk() != null) {
					historyFunction.saveState();
					chunkSelectionTool.pasteChunk(row, col, canvasViewState.isShowAutotile());
					tileCanvas.getCanvasRenderer().repaint();
					isPlacingSingleItem = true;
				}
				return;
			}
		    
			//check for single item mode like Object / NPC placement or erasure
			if (tileCanvas.getSelected().isNpcMode() || tileCanvas.getSelected().isObjectMode() || tileCanvas.getSelected().isEraseMode()) {
				isPlacingSingleItem = true;
				performPlacementAction(row, col);
			} else {
				//tile mode logic
				if ((e.isShiftDown() || e.isAltDown()) && selectionStartPoint == null) {
					//start rectangle selection
					selectionStartPoint = e.getPoint();
					//set flag to prevent single tile painting during drag
					isPlacingSingleItem = true;
				} else if (!e.isShiftDown() && !e.isAltDown()) {
					//single tile paint for tile mode only
					isPlacingSingleItem = false;
					historyFunction.saveState();
					paintTile(row, col);
				}
			}
			
			//set to delete objects AOE
			if(tileCanvas.getSelected().isEraseMode()) {
				if ((e.isShiftDown() || e.isAltDown()) && selectionStartPoint == null) {
					//start rectangle selection
					selectionStartPoint = e.getPoint();
					//set flag to prevent single tile painting during drag
					isPlacingSingleItem = true;
				}
			}
			
			tileCanvas.getCanvasRenderer().repaint();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	    if (tileCanvas.getDraggedItem().getItemSource() != null) {
	    	canvasViewState.setObjectPreviewPosition(getHoverTileRowCol());
	    	//force the ghost to follow the mouse
	    	tileCanvas.getCanvasRenderer().repaint();
	    	//do not allow panning / selection while moving an object
	        return;
	    }
	    
		//panning with right click drag
		if (dragStartPoint != null) {
			camera.transformView(e.getX() - dragStartPoint.x, e.getY() - dragStartPoint.y);
			dragStartPoint = e.getPoint();
			tileCanvas.getCanvasRenderer().repaint();
			return;
		}

		//tile painting or rectangle selection drag
		if (SwingUtilities.isLeftMouseButton(e)) {
			
			//if we started a single item placement or a rectangle selection
			if (isPlacingSingleItem) {
				if(e.isShiftDown() || e.isAltDown()) {
					if (selectionStartPoint != null) {
						//this forces the rectangle outline to update during drag
						tileCanvas.getCanvasRenderer().repaint();
					}
				}
				//stop the rectangle paint if you release the shift button before placing
				else {
					isPlacingSingleItem = false;
					selectionStartPoint = null;
					tileCanvas.getCanvasRenderer().repaint();
				}
				return;
			}
			
			//skip placing new tiles if chunk selection is active
			if(tileCanvas.getSelected().isChunkSelectionTool()) return;
			
			//if isPlacingSingleItem is false we are in continuous tile paint mode
			int col = camera.posX(e.getX(), tileCanvas.getCanvasRenderer().getTileSize());
			int row = camera.posY(e.getY(), tileCanvas.getCanvasRenderer().getTileSize());
			
			if (row >= 0 && row < mapState.getData().getRows() && col >= 0 && col < mapState.getData().getCols()) {
				
				//if the walk area is on then paint it first
				if(tileCanvas.getSelected().isNpcWalkAreaMode()) {
					mapState.getData().setNpcWalkAreaMap(row, col, tileCanvas.getSelected().getIndex());
				}
				//paint the normal tiles in this case
				else {
					paintTile(row, col);
				}
				tileCanvas.getCanvasRenderer().repaint();
			}
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		//drag and drop finish
		if (tileCanvas.getDraggedItem().getItemSource() != null) {
	        int col = camera.posX(e.getX(), tileCanvas.getCanvasRenderer().getTileSize());
	        int row = camera.posY(e.getY(), tileCanvas.getCanvasRenderer().getTileSize());

	        if (row >= 0 && row < mapState.getData().getRows() && col >= 0 && col < mapState.getData().getCols()) {
	            //check if the spot is already taken by the same type of item 
	            boolean isOccupied = (tileCanvas.getDraggedItem().getType() == ItemType.NPC && mapState.getData().getNpcMap(row, col) != -1) ||
	                                 (tileCanvas.getDraggedItem().getType() == ItemType.OBJECT && mapState.getData().getObjectMap(row, col) != -1);
	            //do not count the items own original spot as occupied
	                isOccupied = false; 
	                if (row == tileCanvas.getDraggedItem().getItemSource().x && col == tileCanvas.getDraggedItem().getItemSource().y) {
	            }
	            if (isOccupied) {
	            	tileCanvas.getToastNotification().showToastNotification("Cannot move: Object already exists here!");
	            }
	            else if (row != tileCanvas.getDraggedItem().getItemSource().x || col != tileCanvas.getDraggedItem().getItemSource().y) {
	                //proceed with the move as normal
	            	historyFunction.saveState();
	                //remove old
	                if (tileCanvas.getDraggedItem().getType() == ItemType.NPC) {
	                	mapState.getData().setNpcMap(tileCanvas.getDraggedItem().getItemSource().x, tileCanvas.getDraggedItem().getItemSource().y, -1);
	                }
	                else {
	                	mapState.getData().setObjectMap(tileCanvas.getDraggedItem().getItemSource().x, tileCanvas.getDraggedItem().getItemSource().y, -1);
	                }
	                tileCanvas.getDraggedItem().setDone(true);
	                removeSortedObject(tileCanvas.getDraggedItem().getItemSource().x, tileCanvas.getDraggedItem().getItemSource().y);
	                //place new
	                if (tileCanvas.getDraggedItem().getType() == ItemType.NPC) {
	                	mapState.getData().setNpcMap(row, col, tileCanvas.getDraggedItem().getIndex());
	                	mapState.addNewSortedObject(row, col, false);
	                } else {
	                	mapState.getData().setObjectMap(row, col, tileCanvas.getDraggedItem().getIndex());
	                	mapState.addNewSortedObject(row, col, true);
	                }
	                tileCanvas.getToastNotification().showToastNotification("Moved " + tileCanvas.getDraggedItem().getType());
	            }
	        }
	        //reset state
	        tileCanvas.getDraggedItem().reset();
	        tileCanvas.getCanvasRenderer().repaint();
	        return;
	    }
		
		dragStartPoint = null;
		tileCanvas.getCanvasRenderer().setCursor(Cursor.getDefaultCursor());
		
		//end rectangle selection (release shift + left click)
		if (SwingUtilities.isLeftMouseButton(e) && e.isShiftDown() && selectionStartPoint != null) {
			fillAOEZones(e, tileCanvas.getSelected().getIndex());
		}
		
		//end rectangle selection (release alt + left click)
		if (SwingUtilities.isLeftMouseButton(e) && e.isAltDown() && selectionStartPoint != null) {
			fillAOEZones(e, -1);
		}

		//reset the flag for the next action
		isPlacingSingleItem = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		camera.zoom(e.getWheelRotation(), e.getPoint());
		
		tileCanvas.getCanvasRenderer().revalidate();
		tileCanvas.getCanvasRenderer().repaint();
	}
	
	
	//helper method to consolidate placement / erasure logic
	private void performPlacementAction(int row, int col) {
		boolean changed = false;
		boolean isObject = false;

		if (tileCanvas.getSelected().isEraseMode()) {
			//try to erase NPC
			if (tileCanvas.getSelected().isEraseMode() && mapState.getData().getNpcMap(row, col) != -1 && canvasViewState.isShowNpcLayer()) {
				mapState.getData().setNpcMap(row, col, -1);
				changed = true;
			}

			//try to erase Object
			if (tileCanvas.getSelected().isEraseMode() && mapState.getData().getObjectMap(row, col) != -1 && canvasViewState.isShowObjectLayer()) {
				mapState.getData().setObjectMap(row, col, -1);
				changed = true;
			}

			//if we performed any erasure save state and exit
			if (changed) {
				removeSortedObject(row, col);
				historyFunction.saveState();
				return;
			}
		}

		//NPC lacement Logic
		//NPC takes priority when placing an object and an NPC on the same click
		else if (tileCanvas.getSelected().isNpcMode()) {
			if (mapState.getData().getNpcMap(row, col) == -1) {
				mapState.getData().setNpcMap(row, col, tileCanvas.getSelected().getIndex());
				changed = true;
				isObject = false;
			}
		}

		//object placement Logic
		else if (tileCanvas.getSelected().isObjectMode()) {
			if (mapState.getData().getObjectMap(row, col) == -1) {
				mapState.getData().setObjectMap(row, col, tileCanvas.getSelected().getIndex());
				changed = true;
				isObject = true;
			}
		}

		if (changed) {
			mapState.addNewSortedObject(row, col, isObject);
			//save state if placement occurred
			historyFunction.saveState();
		}
	}

	private void paintTile(int row, int col) {
		//only paint tiles if not in object or NPC placement / erase mode
		if (tileCanvas.getSelected().isTileMode()) {
			mapState.getData().setTileMap(row, col, tileCanvas.getSelected().getIndex());

			//if the autoTile is not active then do not reset autotileMap to save time
			if (canvasViewState.isShowAutotile())
				mapState.refreshAutotileMap(row - 2, col - 2, row + 2, col + 2);
		}
	}

	private void pickTileOrObject(int x, int y) {
		int col = camera.posX(x, tileCanvas.getCanvasRenderer().getTileSize());
		int row = camera.posY(y, tileCanvas.getCanvasRenderer().getTileSize());

		if (row < 0 || row >= mapState.getData().getRows() || col < 0 || col >= mapState.getData().getCols())
			return;

		//priority for pick is NPC > Object > Tile
		if (mapState.getData().getNpcMap(row, col) != -1) {
			tileCanvas.setSelectedNpcIndex(mapState.getData().getNpcMap(row, col));
		} else if (mapState.getData().getObjectMap(row, col) != -1) {
			tileCanvas.setSelectedObjectIndex(mapState.getData().getObjectMap(row, col));
		} else if (mapState.getData().getTileMap(row, col) != -1) {
			tileCanvas.setSelectedTileIndex(mapState.getData().getTileMap(row, col));
		}
	}
	
	private void fillAOEZones(MouseEvent e, int npcWalkAreaValue) {
		int endCol = camera.posX(e.getX(), tileCanvas.getCanvasRenderer().getTileSize());
		int endRow = camera.posY(e.getY(), tileCanvas.getCanvasRenderer().getTileSize());

		int startCol = camera.posX(selectionStartPoint.x, tileCanvas.getCanvasRenderer().getTileSize());
		int startRow = camera.posY(selectionStartPoint.y, tileCanvas.getCanvasRenderer().getTileSize());
		
		if (tileCanvas.getSelected().isChunkSelectionTool()) {
			chunkSelectionTool.copyChunkSelection(startRow, startCol, endRow, endCol);
		}
		
		else if(tileCanvas.getSelected().isNpcWalkAreaMode()) {
			fillNpcWalkArea(startRow, startCol, endRow, endCol, npcWalkAreaValue);
		}
		//used to paint in a rectangle like a brush the given objects
		else if(tileCanvas.getSelected().isBrushTool()) {
			brushTool.brushRectObjects(startRow, startCol, endRow, endCol);
		}
		//here we remove all objects and NPCs
		else if(tileCanvas.getSelected().isEraseMode()) {
			removeRectObjects(startRow, startCol, endRow, endCol);
		}
		else if(tileCanvas.getSelected().isTileMode()) {
			fillRectangle(startRow, startCol, endRow, endCol);
		}
		selectionStartPoint = null;
		tileCanvas.getCanvasRenderer().repaint();
	}
	
	private void fillRectangle(int r1, int c1, int r2, int c2) {
		r1 = Math.max(0, Math.min(r1, mapState.getData().getRows() - 1));
		r2 = Math.max(0, Math.min(r2, mapState.getData().getRows() - 1));
		c1 = Math.max(0, Math.min(c1, mapState.getData().getCols() - 1));
		c2 = Math.max(0, Math.min(c2, mapState.getData().getCols() - 1));

		int startRow = Math.min(r1, r2);
		int endRow = Math.max(r1, r2);
		int startCol = Math.min(c1, c2);
		int endCol = Math.max(c1, c2);

		historyFunction.saveState();

		for (int r = startRow; r <= endRow; r++) {
			for (int c = startCol; c <= endCol; c++) {
				if (tileCanvas.getSelected().isTileMode()) {
					//this line performs the fill
					mapState.getData().setTileMap(r, c, tileCanvas.getSelected().getIndex());
				}
			}
		}

		//if the autoTile is not active then do not reset autotileMap to save time
		if (canvasViewState.isShowAutotile())
			mapState.refreshAutotileMap(startRow - 2, startCol - 2, endRow + 2, endCol + 2);
	}
	
	private void fillNpcWalkArea(int r1, int c1, int r2, int c2, int npcWalkAreaValue) {
		r1 = Math.max(0, Math.min(r1, mapState.getData().getRows() - 1));
		r2 = Math.max(0, Math.min(r2, mapState.getData().getRows() - 1));
		c1 = Math.max(0, Math.min(c1, mapState.getData().getCols() - 1));
		c2 = Math.max(0, Math.min(c2, mapState.getData().getCols() - 1));

		int startRow = Math.min(r1, r2);
		int endRow = Math.max(r1, r2);
		int startCol = Math.min(c1, c2);
		int endCol = Math.max(c1, c2);

		historyFunction.saveState();

		for (int r = startRow; r <= endRow; r++) {
			for (int c = startCol; c <= endCol; c++) {
				//this line performs the fill
				mapState.getData().setNpcWalkAreaMap(r, c, npcWalkAreaValue);
			}
		}
	}
	
	private void removeRectObjects(int r1, int c1, int r2, int c2) {
		r1 = Math.max(0, Math.min(r1, mapState.getData().getRows() - 1));
		r2 = Math.max(0, Math.min(r2, mapState.getData().getRows() - 1));
		c1 = Math.max(0, Math.min(c1, mapState.getData().getCols() - 1));
		c2 = Math.max(0, Math.min(c2, mapState.getData().getCols() - 1));

		int startRow = Math.min(r1, r2);
		int endRow = Math.max(r1, r2);
		int startCol = Math.min(c1, c2);
		int endCol = Math.max(c1, c2);

		historyFunction.saveState();

		for (int r = startRow; r <= endRow; r++) {
			for (int c = startCol; c <= endCol; c++) {
				if(mapState.getData().getObjectMap(r, c) != -1 || mapState.getData().getNpcMap(r, c) != -1) {
					mapState.getData().setObjectMap(r, c, -1);
					mapState.getData().setNpcMap(r, c, -1);
					removeSortedObject(r, c);
				}
			}
		}
	}
	
	private Point getHoverTileRowCol() {
        Point mouseScreenPos = tileCanvas.getCanvasRenderer().getMousePosition();
        if (mouseScreenPos != null) {
            //convert mouse screen position to map position
            int mapX = camera.posX(mouseScreenPos.x, 1);
            int mapY = camera.posY(mouseScreenPos.y, 1);
            
            //calculate the current tile grid position
            int hoverCol = mapX / tileCanvas.getCanvasRenderer().getTileSize();
            int hoverRow = mapY / tileCanvas.getCanvasRenderer().getTileSize();

            //check bounds and active mode
            if (hoverRow >= 0 && hoverRow < mapState.getData().getRows() && hoverCol >= 0 && hoverCol < mapState.getData().getCols()) {
                return new Point(hoverRow, hoverCol);
            }
        }
        return null;
	}
	
	private boolean removeSortedObject(int r, int c) {
		//use an iterator for safe removal while looping
	    Iterator<TileObject> iterator = mapState.getRegistry().getAllSortedItems().iterator();
	    
	    while (iterator.hasNext()) {
	        TileObject object = iterator.next();
	        //check if the coordinates match
	        if (object.getX() == c && object.getY() == r) {

	        	//only applicable when moving objects around
	        	//we do this so we do not remove both object and NPC if they overlap
	        	if(tileCanvas.getDraggedItem().isDone()) {
	        		if((tileCanvas.getDraggedItem().getType() == ItemType.OBJECT && object.isObject())
	        				|| (tileCanvas.getDraggedItem().getType() == ItemType.NPC && !object.isObject())) {
	    	        	//remove the object safely using the iterator
	    	            iterator.remove();
	    	            return true;
	        		}
	        		continue;
	        	}
	        	//remove the object safely using the iterator
	            iterator.remove();
	            //we do not return after found because sometimes NPCs and Objects overlap and we need to remove both
	        }
	    }
	    
	    //no object found at the specified coordinates
	    return false;
	}
	
	private void updateAnnotatedNotes(MouseEvent e, int col, int row) {
		//search first to see if a note already exists in the given position
		MapNote existingNote = tileCanvas.getAnnotatedNotesTool().findNoteAt(col, row);

		String defaultText = (existingNote != null) ? existingNote.getText() : "";
		NoteColor defaultColor = (existingNote != null) ? existingNote.getColor() : null;

		//instantiate the UI configuration window panel
		NoteConfigPanel configPanel = new NoteConfigPanel(defaultText, defaultColor);

		//define dynamic button options based on context
		Object[] options;
		if (existingNote != null) {
			options = new Object[] {ButtonsOptions.Save, ButtonsOptions.Remove};
		} else {
			options = new Object[] {ButtonsOptions.Save, ButtonsOptions.Cancel};
		}

	    int result = JOptionPane.showOptionDialog(
	    	SwingUtilities.getWindowAncestor(e.getComponent()),
	      	configPanel,
	      	"Configure Editor Pin",
	      	JOptionPane.DEFAULT_OPTION,
	       	JOptionPane.PLAIN_MESSAGE,
	       	null,
	       	options,
	       	options[0]); //default focused button

		//process the new notes list according to the options selected in the window panel
	 	if (existingNote != null) {
	      	if (result == 0 && !configPanel.getNoteText().isEmpty()) { //save clicked
	          	existingNote.setText(configPanel.getNoteText());
	           	existingNote.setColor(configPanel.getSelectedColor());
	          	tileCanvas.getToastNotification().showToastNotification("Note saved successfully!");
	          	enableQuickSave();
	       	} else if (result == 1) { //remove clicked
	          	tileCanvas.getAnnotatedNotesTool().removeMapNote(existingNote);
	           	tileCanvas.getToastNotification().showToastNotification("Note removed.");
	          	enableQuickSave();
	      	}
		} else if (result == 0 && !configPanel.getNoteText().isEmpty()) { //save clicked for new note
			tileCanvas.getAnnotatedNotesTool().addNote(col, row, configPanel.getNoteText(), configPanel.getSelectedColor());
	      	tileCanvas.getToastNotification().showToastNotification("Note saved successfully!");
	    	enableQuickSave();
	  	}
	}
	
	//set the quick save button to be available every time you modify the notes list
	private void enableQuickSave() {
        mapState.getCacheData().setCanQuickSave(true);
		if(tileCanvas.getTileEditor().getStatusInfoBar() != null) {
			tileCanvas.getTileEditor().getStatusInfoBar().updateStatusUI();
		}
	}

	public Point getDragStartPoint() {
		return dragStartPoint;
	}

	public Point getSelectionStartPoint() {
		return selectionStartPoint;
	}
}
