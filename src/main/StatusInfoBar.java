package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.formdev.flatlaf.ui.FlatLineBorder;

import core.Tile;
import data.Selection;
import localization.LocalizationManager;
import utils.ImageUtils;
import utils.Utils;
import view.TileCanvas;

public class StatusInfoBar {

	private TileEditor tileEditor;
	private TileCanvas canvas;
	private EditorMenuBar editorMenuBar;
	
    private JLabel selectionLabel;
    private JLabel selectionPreview;
    private JLabel statusBar;
	
	public StatusInfoBar(TileEditor tileEditor, TileCanvas canvas, EditorMenuBar editorMenuBar) {
		this.tileEditor = tileEditor;
		this.canvas = canvas;
		this.editorMenuBar = editorMenuBar;
	}

    public void updateStatusUI() {
    	if(areTabsLabelsEmpty()) {
    		return;
    	}
    	
    	LocalizationManager loc = LocalizationManager.getInstance();
        Selection selection = canvas.getSelected();
        
        Icon icon = null;
        String modeText = loc.getString("status_paint_tile");
        
        if(this.canvas.getSelected().isChunkSelectionTool()) {
        	modeText = loc.getString("status_chunk_selection");
        }
        //priority is NPC > Object > Tile
        else if(this.canvas.getSelected().isNpcWalkAreaMode()) {
        	modeText = loc.getString("status_npc_walk_area");
        }
        else if(this.canvas.getSelected().isBrushTool()) {
        	modeText = loc.getString("status_brush_mode");
        }
        else if (selection.isNpcMode()) {
            Tile npc = this.canvas.getMapState().findNpcById(selection.getIndex());
            icon = ImageUtils.createScaledIcon(npc.getImage(), tileEditor.getLoadedSetup().getDefaultSelectedTile(), tileEditor.getLoadedSetup().getDefaultSelectedTile());
            modeText = loc.getString("status_place_npc");
        }
        else if (selection.isEraseMode()) {
        	icon = tileEditor.getEditorIcons().getEraserIcon();
            modeText = loc.getString("status_erase_mode");
        }
        else if (selection.isNotesTool()) {
        	icon = tileEditor.getEditorIcons().getNoteIcon();
            modeText = loc.getString("status_notes_mode");
        }
        else if (selection.isObjectMode()) {
            Tile obj = this.canvas.getMapState().findObjectById(selection.getIndex());
        	icon = ImageUtils.createScaledIcon(obj.getImage(), tileEditor.getLoadedSetup().getDefaultSelectedTile(), tileEditor.getLoadedSetup().getDefaultSelectedTile());
        	modeText = loc.getString("status_place_object");
        }
        else if (selection.isTileMode()) {
            Tile tile = this.canvas.getMapState().findTileById(selection.getIndex());
            icon = new ImageIcon(tile.getImage().getScaledInstance(tileEditor.getLoadedSetup().getDefaultSelectedTile(), tileEditor.getLoadedSetup().getDefaultSelectedTile(), Image.SCALE_SMOOTH));
            modeText = loc.getString("status_paint_tile");
        }
        
        selectionLabel.setText(loc.getString("status_selection"));
        selectionPreview.setIcon(icon);
        
        String statusBarText = loc.getString("status_mode") + ": " + modeText;
        String stats = "";
        
        if(selection.isTileMode()) {
        	stats = loc.getString("status_tile_name") + this.canvas.getMapState().findTileById(selection.getIndex()).getName() + " | ID: " + selection.getIndex();
        }
        else if(selection.isObjectMode()) {
        	stats = loc.getString("status_object_name") + this.canvas.getMapState().findObjectById(selection.getIndex()).getName() + " | ID: " + selection.getIndex();
        }
        else if(selection.isNpcMode()) {
        	stats = loc.getString("status_npc_name") + this.canvas.getMapState().findNpcById(selection.getIndex()).getName() + " | ID: " + selection.getIndex();
        }
        
        statusBarText = "<html>" + loc.getString("status_mode") + ": " + modeText + 
                "<br>" + stats;
        
        if(canvas.getCanvasViewState().isLocateMode()) {
        	statusBarText += "<br>" + loc.getString("status_found_instances") + canvas.getLocateCounter();
        }
        
        statusBarText += "</html>";
        
        statusBar.setText(statusBarText);

        updateButtonHighlights(tileEditor.getTileTabs(), true, false);
        updateButtonHighlights(tileEditor.getObjectTabs(), false, false);
        updateButtonHighlights(tileEditor.getNpcTabs(), false, true);
        
        //update quick save
        if(canvas.getMapState().getCacheData().getCachedSavedLocation() != null && canvas.getMapState().getCacheData().isCanQuickSave()) {
        	editorMenuBar.getQuickSaveItem().setEnabled(true);
        }
        else {
        	editorMenuBar.getQuickSaveItem().setEnabled(false);
        }
    }
    
	public boolean areTabsLabelsEmpty() {
        if(tileEditor.getTileTabs() == null || tileEditor.getObjectTabs() == null || tileEditor.getNpcTabs() == null 
        		|| tileEditor.getResourceWorker().getAllTiles() == null || tileEditor.getResourceWorker().getAllObjects() == null || tileEditor.getResourceWorker().getAllNpcs() == null 
        		|| selectionLabel == null || selectionPreview == null || statusBar == null) {
        	return true;
        }
        return false;
	}
    
    private void updateButtonHighlights(JTabbedPane tabbedPane, boolean isTilePalette, boolean isNpcPalette) {
        //use the correct client property key for lookup
        String key = isTilePalette ? AssetPalette.TILE_INDEX : (isNpcPalette ? AssetPalette.NPC_INDEX : AssetPalette.OBJECT_INDEX);
        
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(i);
            JComponent panel = (JComponent) scrollPane.getViewport().getView();

            for (Component comp : panel.getComponents()) {
            	if (comp instanceof JButton) {
            	    JButton btn = (JButton) comp;
                    Integer index = (Integer) btn.getClientProperty(key);
                    int borderThickness = isTilePalette ? 4 : 3;
                    
                    //making sure to deselect all tiles when erasing mode enabled
                    if (index != null) {
						if (index == canvas.getSelected().getIndex() && canvas.getSelected().isEraseMode() == false) {
							//highlight active single selection in green
							btn.setBorder(BorderFactory.createLineBorder(Utils.COLOR_SAGE_GREEN, borderThickness));
						} else if (!isTilePalette && !isNpcPalette 
								&& tileEditor.getEditorMenuBar().getBrushIds().contains(index)) {
							//highlight brush items in blue
							btn.setBorder(BorderFactory.createLineBorder(Utils.COLOR_ENGINE_BLUE, borderThickness + 1));
						} else {
							btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
						}
                    }
                }
            }
        }
    }
    
    public void initSelections() {
        //status bar and selection preview Setup
        selectionLabel = new JLabel("Selection Preview:");
        selectionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        selectionPreview = new JLabel();
        selectionPreview.setHorizontalAlignment(SwingConstants.CENTER);
        selectionPreview.setPreferredSize(new Dimension(tileEditor.getLoadedSetup().getDefaultSelectedTile(), tileEditor.getLoadedSetup().getDefaultSelectedTile()));
        selectionPreview.setBorder(new FlatLineBorder(
        	    new Insets(0, 0, 0, 0), 
        	    Color.GRAY, 
        	    1, //line thickness
        	    10 //rounding
        	));
        
        statusBar = new JLabel();
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        statusBar.setMinimumSize(new Dimension(10, 20));
    }

	public JLabel getSelectionLabel() {
		return selectionLabel;
	}

	public JLabel getSelectionPreview() {
		return selectionPreview;
	}

	public JLabel getStatusBar() {
		return statusBar;
	}
}
