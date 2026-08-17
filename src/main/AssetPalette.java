package main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Scrollable;

import core.TabData;
import core.Tile;
import localization.LocalizationManager;
import utils.ImageUtils;
import utils.Utils;
import view.TileCanvas;

public class AssetPalette {

    public static final String TILE_INDEX = "tileIndex";
    public static final String OBJECT_INDEX = "objectIndex";
    public static final String NPC_INDEX = "npcIndex";
    
    private static final String HIDDEN_FOLDER = "#hidden";
    
    private List<String> hiddenFolders = new LinkedList<>();
    
    private int defaultTileSizeSelection;
    
    private List<TabData> tilesTabListClone = new ArrayList<>();
    private List<TabData> objectTabListClone = new ArrayList<>();
    private List<TabData> npcsTabListClone = new ArrayList<>();
    
    private TileCanvas canvas;
    private TileEditor tileEditor;
    
    private LocalizationManager loc = LocalizationManager.getInstance();
    
	public AssetPalette(TileEditor tileEditor, TileCanvas canvas) {
		this.tileEditor = tileEditor;
		this.canvas = canvas;
		
		this.defaultTileSizeSelection = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.042); //48
	}
	
	public JTabbedPane createTilePalette(Map<String, List<Tile>> tileCategories) {
    	JTabbedPane tileTabs = new JTabbedPane(JTabbedPane.TOP);
  	
    	//it prevents the cylinder rotation
    	tileTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    	
    	tileTabs.putClientProperty("JTabbedPane.showTabSeparators", true);
    	tileTabs.putClientProperty("JTabbedPane.maximumTabWidth", 160);
        
        boolean initialized = false;
        
        //sorting logic
        List<String> sortedKeys = new ArrayList<>(tileCategories.keySet());
        Collections.sort(sortedKeys, String.CASE_INSENSITIVE_ORDER);
        
        for (String category : tileCategories.keySet()) {
        	//do not add hidden folders in the view
            if(category.toLowerCase().endsWith(HIDDEN_FOLDER)) {
            	hiddenFolders.add(Utils.removeSuffix(category, HIDDEN_FOLDER));
            	continue;
            }
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            List<Tile> tiles = tileCategories.get(category);
            for (Tile tile : tiles) {
                int tileId = tile.getId();
                JButton btn = new JButton(new ImageIcon(tile.getImage().getScaledInstance(defaultTileSizeSelection, defaultTileSizeSelection, Image.SCALE_SMOOTH)));
                btn.setToolTipText(tile.getName() + " - ID:" + tileId);
                btn.putClientProperty(TILE_INDEX, tileId);
                btn.addActionListener(e -> canvas.setSelectedTileIndex(tileId));
                panel.add(btn);
                
                //make sure you initialize to select an existing tile before application start
                if(initialized == false) {
                	canvas.setSelectedTileIndex(tileId);
                	initialized = true;
                }
            }
            
            JScrollPane scroll = new JScrollPane(panel);
            scroll.setPreferredSize(new Dimension(800, 120));
            tileTabs.addTab(category, scroll);
            
            //populate this data to use in search by name recovery
            tilesTabListClone.add(new TabData(category, scroll));
        }
        return tileTabs;
    }
    
    //creates the object palette using the custom wrap layout panel
    public JTabbedPane createObjectPalette(Map<String, List<Tile>> objectCategories, List<Tile> allObjects) {
    	JTabbedPane objectTabs = new JTabbedPane(JTabbedPane.LEFT);
    	
        //it prevents the cylinder rotation
        objectTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        
        objectTabs.putClientProperty("JTabbedPane.tabAlignment", "leading");
        //add lines between buttons (does it by default i think, but still)
        objectTabs.putClientProperty("JTabbedPane.showTabSeparators", true);
        objectTabs.putClientProperty("JTabbedPane.maximumTabWidth", 160);
        
        //get the keys from the map and sort them A-Z
        List<String> sortedCategories = new ArrayList<>(objectCategories.keySet());
        Collections.sort(sortedCategories, String.CASE_INSENSITIVE_ORDER);
        
        for (String category : sortedCategories) {
        	
        	//do not add hidden folders in the view
            if(category.toLowerCase().endsWith(HIDDEN_FOLDER)) {
            	hiddenFolders.add(Utils.removeSuffix(category, HIDDEN_FOLDER));
            	continue;
            }            
            JPanel panel = createPalettePanel(objectCategories.get(category), allObjects, true);
            JScrollPane scroll = new JScrollPane(panel);
            scroll.setPreferredSize(new Dimension(240, 400));
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            objectTabs.addTab(category, scroll);
            
            //populate this data to use in search by name recovery
            objectTabListClone.add(new TabData(category, scroll));
        }
        return objectTabs;
    }
    
    //creates the NPC palette using the custom wrap layout panel
    public JTabbedPane createNpcPalette(Map<String, List<Tile>> npcCategories, List<Tile> allNpcs) {
    	JTabbedPane npcTabs = new JTabbedPane(JTabbedPane.LEFT);
  	
    	//it prevents the cylinder rotation
    	npcTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        
    	npcTabs.putClientProperty("JTabbedPane.tabAlignment", "leading");
    	//add lines between buttons (does it by default i think, but still)
    	npcTabs.putClientProperty("JTabbedPane.showTabSeparators", true);
    	npcTabs.putClientProperty("JTabbedPane.maximumTabWidth", 160);
    	
        //get the keys from the map and sort them A-Z
        List<String> sortedCategories = new ArrayList<>(npcCategories.keySet());
        Collections.sort(sortedCategories, String.CASE_INSENSITIVE_ORDER);
        
        for(int i = 0; i < sortedCategories.size(); i++) {
            String category = sortedCategories.get(i);
            
        	//do not add hidden folders in the view
            if(category.toLowerCase().endsWith(HIDDEN_FOLDER)) {
            	hiddenFolders.add(Utils.removeSuffix(category, HIDDEN_FOLDER));
            	continue;
            }
            
            JPanel panel = createPalettePanel(npcCategories.get(category), allNpcs, false);
            JScrollPane scroll = new JScrollPane(panel);
            scroll.setPreferredSize(new Dimension(240, 400));
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            npcTabs.addTab(category, scroll);
            
            //populate this data to use in search by name recovery
            npcsTabListClone.add(new TabData(category, scroll));
        }
        return npcTabs;
    }
    
    //this helper class correctly implements scrollable for this application
    private class ScrollableWrapPanel extends JPanel implements Scrollable {
    	
	private static final long serialVersionUID = 1L;

		public ScrollableWrapPanel() {
			//keep the null layout
            super(null);
        }
        @Override 
        public boolean getScrollableTracksViewportWidth() { return true; }
        @Override 
        public boolean getScrollableTracksViewportHeight() { return false; }
        @Override 
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
		@Override 
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {return 20;}
		@Override 
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {return 100;}
    }
    
    //creates a custom panel with wrap around layout logic for side palettes
    private JPanel createPalettePanel(List<Tile> tiles, List<Tile> allAssets, boolean isObjectPalette) {
        //custom JPanel that acts as a WrapLayout
        JPanel panel = new ScrollableWrapPanel() {
            private static final long serialVersionUID = 1L;
            private final int H_GAP = 5;
            private final int V_GAP = 5;

            @Override
            public Dimension getPreferredSize() {
                return layoutAndGetSize(false);
            }

            @Override
            public void doLayout() {
                layoutAndGetSize(true);
            }

            //core logic to calculate size and layout components
            private Dimension layoutAndGetSize(boolean doLayout) {
                Insets insets = getInsets();
                
                //always try to get the width from the parent ScrollPane's viewport
                int maxPanelWidth = 0;
                if (getParent() != null) {
                    maxPanelWidth = getParent().getWidth();
                }

                //fallback if the parent is not ready or width is 0
                if (maxPanelWidth <= 0) {
                    maxPanelWidth = getWidth() > 0 ? getWidth() : 220;
                }

                int usableWidth = maxPanelWidth - insets.left - insets.right;
                int currentX = insets.left;
                int currentY = insets.top;
                int rowHeight = 0;
                int preferredHeight = 0;

                for (Component comp : getComponents()) {
                	if (!comp.isVisible()) continue;

                    Dimension d = comp.getPreferredSize();

                    //check if the component exceeds the usable width of the viewport
                    if (currentX + d.width > usableWidth && currentX > insets.left) {
                        currentX = insets.left;
                        currentY += rowHeight + V_GAP;
                        rowHeight = 0;
                    }

                    if (doLayout) {
                        comp.setBounds(currentX, currentY, d.width, d.height);
                    }

                    currentX += d.width + H_GAP;
                    rowHeight = Math.max(rowHeight, d.height);
                    preferredHeight = currentY + rowHeight + insets.bottom;
                }

                return new Dimension(maxPanelWidth, preferredHeight);
            }
        };
        
        //determine if this is the object or NPC palette for key and action listener
        String clientPropertyKey = isObjectPalette ? OBJECT_INDEX : NPC_INDEX;

        for (Tile asset : tiles) {
        	ImageIcon icon = ImageUtils.createScaledIcon(asset.getImage(), defaultTileSizeSelection, defaultTileSizeSelection);
        	JButton btn = new JButton(icon);
            int imageTileWidth = asset.getImage().getWidth() / tileEditor.getLoadedSetup().getTileSize();
            int imageTileHeight = asset.getImage().getHeight() / tileEditor.getLoadedSetup().getTileSize();
            btn.setToolTipText(asset.getName() + " - ID:" + asset.getId() + " - " + 
            LocalizationManager.getInstance().getString("hover_object_size") + ":" + imageTileWidth + "x" + imageTileHeight);
            btn.setPreferredSize(new Dimension(defaultTileSizeSelection, defaultTileSizeSelection));
            btn.putClientProperty(clientPropertyKey, asset.getId());
            
            //listener for the brush objects selection
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    int id = asset.getId();
                    
                    boolean isCtrlDown = e.isControlDown();

                    if (isObjectPalette) {
                        if (isCtrlDown) {
                            //brush logic
                            if (tileEditor.getEditorMenuBar().getBrushIds().contains(id)) {
                            	tileEditor.getEditorMenuBar().getBrushIds().remove(id);
                            } else {
                            	tileEditor.getEditorMenuBar().getBrushIds().add(id);
                            }
                            tileEditor.getStatusInfoBar().updateStatusUI();
                            
                            if (canvas.getSelected().isBrushTool()) {
                                if (tileEditor.getEditorMenuBar().getBrushIds().isEmpty()) {
                                    canvas.setUseBrushObjectFalse();
                                } else {
                                    int[] idsArray = tileEditor.getEditorMenuBar().getBrushIds().stream().mapToInt(Integer::intValue).toArray();
                                    canvas.useBrushObject(idsArray);
                                }
                            }
                            
                            canvas.getToastNotification().showToastNotification(loc.getFormattedString("toast_brush_count", tileEditor.getEditorMenuBar().getBrushIds().size()));
                        } else {
                            //selection logic
                            canvas.setSelectedObjectIndex(id);
                        }
                    } else {
                        canvas.setSelectedNpcIndex(id);
                    }
                }
            });
            panel.add(btn);
        }
        return panel;
    }

	public List<String> getHiddenFolders() {
		return hiddenFolders;
	}

	public List<TabData> getTilesTabListClone() {
		return tilesTabListClone;
	}

	public List<TabData> getObjectTabListClone() {
		return objectTabListClone;
	}

	public List<TabData> getNpcsTabListClone() {
		return npcsTabListClone;
	}
}
