package tools;

import data.CanvasViewState;
import data.HistoryData;
import data.MapState;
import view.CanvasRenderer;
import view.TileCanvas;

public class HistoryFunction extends HistoryData {

	private TileCanvas tileCanvas;
	private CanvasViewState canvasViewState;
	private CanvasRenderer canvasRenderer;
	
	public HistoryFunction(TileCanvas tileCanvas, MapState mapState, 
			CanvasViewState canvasViewState, CanvasRenderer canvasRenderer) {
		super(mapState, tileCanvas.getTileEditor().getMainFrame());
		this.tileCanvas = tileCanvas;
		this.canvasViewState = canvasViewState;
		this.canvasRenderer = canvasRenderer;
	}
	
	@Override
	public void saveState() {
		super.saveState();
		
		//every time a modification is made make the quick save active
		mapState.getCacheData().setCanQuickSave(true);
		if(tileCanvas.getTileEditor().getStatusInfoBar() != null) {
			tileCanvas.getTileEditor().getStatusInfoBar().updateStatusUI();
		}
	}

	@Override
	public void undo() {
		super.undo();
		
		//if the autoTile is not active then do not reset autotileMap to save time
		if (canvasViewState.isShowAutotile())
			mapState.refreshAutotileMap();
		mapState.refreshAllObjectsList();
		canvasRenderer.repaint();
		
		//every time an undo is made make the quick save active
		mapState.getCacheData().setCanQuickSave(true);
	}

	@Override
	public void redo() {
		super.redo();
		
		//if the autoTile is not active then do not reset autotileMap to save time
		if (canvasViewState.isShowAutotile())
			mapState.refreshAutotileMap();
		mapState.refreshAllObjectsList();
		canvasRenderer.repaint();
		
		//every time an undo is made make the quick save active
		mapState.getCacheData().setCanQuickSave(true);
	}
}
