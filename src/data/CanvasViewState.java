package data;

import java.awt.Point;

public class CanvasViewState {
	
	private boolean showGrid = true;
	private boolean showTilePosition = false;
	private boolean showTileLayer = true;
	private boolean showObjectLayer = true;
	private boolean showNpcLayer = true;
	private boolean showAutotile = true;
	private boolean showObjectPreview = true;
	private Point objectPreviewPosition = null;
	private boolean locateMode = false;
	private boolean nightMode = false;
	private boolean showNotesTool = true;
	
	public void toggleNightMode() {
		nightMode = !nightMode;
	}
	
	public void toggleGrid() {
		showGrid = !showGrid;
	}
	
	public void toggleLocateMode() {
		locateMode = !locateMode;
	}
	
	public void toggleTilePosition() {
		showTilePosition = !showTilePosition;
	}
	
	public void toggleObjectPreview() {
		showObjectPreview = !showObjectPreview;
	}
	
	public void toggleTileMap() {
		showTileLayer = !showTileLayer;
	}
	
	public void toggleObjectMap() {
		showObjectLayer = !showObjectLayer;
	}

	public void toggleNpcMap() {
		showNpcLayer = !showNpcLayer;
	}

	public void toggleAutotile() {
		showAutotile = !showAutotile;
	}
	
	public void toggleNotesTool() {
		showNotesTool = !showNotesTool;
	}
	
	public boolean isShowGrid() {
		return showGrid;
	}
	
	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}
	
	public boolean isShowTilePosition() {
		return showTilePosition;
	}
	
	public void setShowTilePosition(boolean showTilePosition) {
		this.showTilePosition = showTilePosition;
	}
	
	public boolean isShowTileLayer() {
		return showTileLayer;
	}
	
	public void setShowTileLayer(boolean showTileLayer) {
		this.showTileLayer = showTileLayer;
	}
	
	public boolean isShowObjectLayer() {
		return showObjectLayer;
	}
	
	public void setShowObjectLayer(boolean showObjectLayer) {
		this.showObjectLayer = showObjectLayer;
	}
	
	public boolean isShowNpcLayer() {
		return showNpcLayer;
	}
	
	public void setShowNpcLayer(boolean showNpcLayer) {
		this.showNpcLayer = showNpcLayer;
	}
	
	public boolean isShowAutotile() {
		return showAutotile;
	}
	
	public void setShowAutotile(boolean showAutotile) {
		this.showAutotile = showAutotile;
	}
	
	public boolean isShowObjectPreview() {
		return showObjectPreview;
	}
	
	public void setShowObjectPreview(boolean showObjectPreview) {
		this.showObjectPreview = showObjectPreview;
	}
	
	public Point getObjectPreviewPosition() {
		return objectPreviewPosition;
	}
	
	public void setObjectPreviewPosition(Point objectPreviewPosition) {
		this.objectPreviewPosition = objectPreviewPosition;
	}
	
	public boolean isLocateMode() {
		return locateMode;
	}
	
	public void setLocateMode(boolean locateMode) {
		this.locateMode = locateMode;
	}
	
	public boolean isNightMode() {
		return nightMode;
	}
	
	public void setNightMode(boolean nightMode) {
		this.nightMode = nightMode;
	}

	public boolean isShowNotesTool() {
		return showNotesTool;
	}

	public void setShowNotesTool(boolean showNotesTool) {
		this.showNotesTool = showNotesTool;
	}
}
