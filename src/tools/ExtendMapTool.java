package tools;

import java.util.Arrays;

import javax.swing.JOptionPane;

import data.CanvasViewState;
import data.MapDirection;
import data.MapState;
import io.KeyController;
import view.Camera;
import view.CanvasRenderer;

public class ExtendMapTool {

	private MapState mapState;
	private HistoryFunction historyFunction;
	private Camera camera;
	private CanvasViewState canvasViewState;
	private CanvasRenderer canvasRenderer;
	private KeyController keyController;
	
	public ExtendMapTool(MapState mapState, HistoryFunction historyFunction, Camera camera, 
			CanvasViewState canvasViewState, CanvasRenderer canvasRenderer, KeyController keyController) {
		this.mapState = mapState;
		this.historyFunction = historyFunction;
		this.camera = camera;
		this.canvasViewState = canvasViewState;
		this.canvasRenderer = canvasRenderer;
		this.keyController = keyController;
	}
	
	//logic for extending and shrinking the map
	public void extendOrShrinkMap(MapDirection direction, int delta) {
		if(delta == 0) {
			JOptionPane.showMessageDialog(null,
					"Error: Cannot change map size by " + delta + ". There is nothing to change.",
					"Extending Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//determine new dimensions and validate
		int newRows = mapState.getData().getRows();
		int newCols = mapState.getData().getCols();

		//flag to determine if the operation is on rows (Y-axis) or columns (X-axis)
		boolean isRowOperation = (direction == MapDirection.UP || direction == MapDirection.DOWN);

		//delta can be positive (grow) or negative (shrink)
		if (isRowOperation) {
			newRows = mapState.getData().getRows() + delta;
		} else {
			newCols = mapState.getData().getCols() + delta;
		}

		if (newRows <= 0 || newCols <= 0) {
			JOptionPane.showMessageDialog(null,
					"Error: Cannot change map size by " + delta + ". New size would be invalid: (" + newRows + " x "
							+ newCols + "). Size must be greater than 0.",
					"Extending Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		//create new maps and initialize
		int[][] newTileMap = new int[newRows][newCols];
		int[][] newObjectMap = new int[newRows][newCols];
		int[][] newNpcMap = new int[newRows][newCols];
		int[][] newNpcWalkAreaMap = new int[newRows][newCols];

		//initialize all new cells to -1 which means empty
		for (int r = 0; r < newRows; r++) {
			Arrays.fill(newTileMap[r], -1);
			Arrays.fill(newObjectMap[r], -1);
			Arrays.fill(newNpcMap[r], -1);
			Arrays.fill(newNpcWalkAreaMap[r], -1);
		}

		//determine copy offsets
		int sourceOffset = 0; //where to start reading from the old map
		int destOffset = 0; //where to start writing to the new map

		//logic for UP and LEFT: Data shifts away from [0,0] when growing, or shifts toward [0,0] when shrinking
		if (direction == MapDirection.UP || direction == MapDirection.LEFT) {
			//growing UP / LEFT need to offset new map DOWN / RIGHT by delta
			if (delta > 0) {
				destOffset = delta;
			} else {
				//shrinking UP / LEFT need to offset old map DOWN / RIGHT by positive delta
				sourceOffset = Math.abs(delta);
			}
		}
		
		//copy only the dimensions that exist in both the old and new map
		int copyRows = Math.min(mapState.getData().getRows(), newRows);
		int copyCols = Math.min(mapState.getData().getCols(), newCols);

		for (int r = 0; r < copyRows; r++) {
			for (int c = 0; c < copyCols; c++) {

				//calculate source coordinates (applies only to shrinking UP / LEFT)
				int srcR = r + (isRowOperation ? sourceOffset : 0);
				int srcC = c + (isRowOperation ? 0 : sourceOffset);

				//calculate destination coordinates (applies only to growing UP / LEFT)
				int destR = r + (isRowOperation ? destOffset : 0);
				int destC = c + (isRowOperation ? 0 : destOffset);

				newTileMap[destR][destC] = mapState.getData().getTileMap(srcR, srcC);
				newObjectMap[destR][destC] = mapState.getData().getObjectMap(srcR, srcC);
				newNpcMap[destR][destC] = mapState.getData().getNpcMap(srcR, srcC);
				newNpcWalkAreaMap[destR][destC] = mapState.getData().getNpcWalkAreaMap(srcR, srcC);

			}
		}

		//save the current state for undo / redo
		historyFunction.saveState();

		mapState.getData().setRows(newRows);
		mapState.getData().setCols(newCols);
		mapState.getData().applyState(newTileMap, newObjectMap, newNpcMap, newNpcMap);

		//recalculate zoom limits and step based on new dimensions
		camera.calculateZoomValues(mapState.getData().getRows(), mapState.getData().getCols(), canvasRenderer.getTileSize(), false);

		//if the autoTile is not active then do not reset autotileMap to save time
		if (canvasViewState.isShowAutotile()) {
			mapState.refreshAutotileMap();
		}
		mapState.refreshAllObjectsList();
		historyFunction.resetSaveState();
		if(keyController != null) {
			keyController.keyRelease();
		}
		
		canvasRenderer.repaint();
	}
}
