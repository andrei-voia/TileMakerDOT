package main;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import data.MapDirection;
import tools.SpritesheetImporter;
import utils.ApplicationLegend;
import utils.ImageUtils;
import utils.Utils;
import view.TileCanvas;

public class EditorMenuBar {
	
	private TileEditor tileEditor;
	private TileCanvas canvas;
	private ApplicationLegend applicationLegend;
	
    private String[] readInputs;
    public String resourceBasePath;
    
    private File loadStartingDirectory = null;
    private boolean showSpreadsheetUpdateOnce = true;
    
    private JMenuItem quickSaveItem;
    private JMenuBar menuBar = new JMenuBar();
    
    private Set<Integer> brushIds = new HashSet<>();
    private int brushSpread = 5;
    
    public EditorMenuBar(TileEditor tileEditor, TileCanvas canvas, ApplicationLegend applicationLegend, 
    		String[] readInputs, String resourceBasePath) {
    	this.tileEditor = tileEditor;
    	this.canvas = canvas;
    	this.applicationLegend = applicationLegend;
    	
    	this.readInputs = readInputs;
    	this.resourceBasePath = resourceBasePath;
    }
    
    //creates and returns the application JMenuBar and organizing existing functions
	public JMenuBar createMenuBar(JFrame frame) {
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem loadItem = new JMenuItem("Load Map...");
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        loadItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            
            //we remove the dot extension if it exists
            String extension = Utils.SAVE_MAP_EXTENSION.replace(".", "");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("TileMaker DOT Maps (*." + extension + ")", extension);
            
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);
            
            //set the current file loading location if already saved once
            if(loadStartingDirectory != null) {
                chooser.setCurrentDirectory(loadStartingDirectory);
            }
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            	//set the last loaded location as the load
            	loadStartingDirectory = Utils.addFileType(chooser.getSelectedFile(), Utils.SAVE_MAP_EXTENSION);
                canvas.getMapLoader().loadMap(chooser.getSelectedFile());
            }
        });
        
        JMenuItem saveItem = new JMenuItem("Save As...");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(
        	    KeyEvent.VK_S,
        	    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK
        	));
        saveItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            //set the current file loading location if already saved once
            if(loadStartingDirectory != null) {
                chooser.setCurrentDirectory(loadStartingDirectory);
            }
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            	//set the last loaded location as the load
            	loadStartingDirectory = Utils.addFileType(chooser.getSelectedFile(), Utils.SAVE_MAP_EXTENSION);
                canvas.getMapExporter().saveMap(chooser.getSelectedFile(), true);
            }
        });
        
        quickSaveItem = new JMenuItem("Quick Save...");
        quickSaveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        quickSaveItem.addActionListener(e -> {
        	canvas.getMapExporter().saveMap(canvas.getMapState().getCacheData().getCachedSavedLocation(), false);
        	canvas.getMapState().getCacheData().setCanQuickSave(false);
        	tileEditor.getStatusInfoBar().updateStatusUI();
        	canvas.getToastNotification().showToastNotification("Map saved..");
        });
        
        JMenuItem saveChunkSelection = new JMenuItem("Export Chunk Selection...");
        saveChunkSelection.setToolTipText("Export only the currently selected area of the map");
        saveChunkSelection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveChunkSelection.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            	canvas.getMapExporter().saveChunk(chooser.getSelectedFile());
            }
        });
        
        JMenuItem importChunkSelection = new JMenuItem("Import Chunk Selection...");
        importChunkSelection.setToolTipText("Import a previously exported map chunk into the current map");
        importChunkSelection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        importChunkSelection.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            
            String extension = Utils.SAVE_MAP_EXTENSION.replace(".", "");
            FileNameExtensionFilter filter = 
                new FileNameExtensionFilter("TileMaker DOT Maps (*." + extension + ")", extension);
            
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);
            
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            	canvas.getMapLoader().loadChunk(chooser.getSelectedFile());
            }
        });
        
        JMenuItem exportLvlFormat = new JMenuItem("Export *.lvl...");
        exportLvlFormat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportLvlFormat.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                canvas.getMapExporter().exportCustomLvlFormat(chooser.getSelectedFile());
            }
        });
        
        JMenuItem exportCsvFormat = new JMenuItem("Export *.csv...");
        exportCsvFormat.setToolTipText("Saves your map as a plain text grid of IDs, being perfect for retro engines, spreadsheet analysis, or the simplest possible custom data parsing");
        exportCsvFormat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportCsvFormat.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                canvas.getMapExporter().exportCsvFormat(chooser.getSelectedFile());
            }
        });
        
        JMenuItem exportTmxFormat = new JMenuItem("Export to Tiled (.tmx/.tsx)...");
        exportTmxFormat.setToolTipText("Generates a Tiled Map (.tmx) and Tileset (.tsx), use to import your map into Godot Unity or Tiled with full layer and object support");
        exportTmxFormat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportTmxFormat.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                canvas.getMapExporter().exportTmxTsxFormat(chooser.getSelectedFile());
            }
        });
        
        JMenuItem exportJsonFormat = new JMenuItem("Export *.json *.tmj...");
        exportJsonFormat.setToolTipText("Generates a lightweight data structure ideal for GameMaker (GML) or custom web engines, providing a clean array of texture IDs for total logic control");
        exportJsonFormat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportJsonFormat.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                canvas.getMapExporter().exportJsonFormat(chooser.getSelectedFile());
            }
        });
        
        JMenuItem exportItem = new JMenuItem("Export Image (PNG)...");
        exportItem.setToolTipText("Save the entire map as a single PNG image");
        exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                canvas.getMapExporter().exportImage(chooser.getSelectedFile());
            }
        });
        
        JMenuItem exportIds = new JMenuItem("Export Used IDs List...");
        exportIds.setToolTipText("Export a text file listing all unique Tile IDs used in this map");
        exportIds.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportIds.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                canvas.getMapExporter().exportTileIdsList(chooser.getSelectedFile());
            }
        });
        
        JMenuItem spriteSheetImport = new JMenuItem("Import Spritesheet...");
        spriteSheetImport.setToolTipText("Slice a spritesheet into individual objects/NPCs/tiles");
        spriteSheetImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        spriteSheetImport.addActionListener(e -> {
        	JFileChooser fc = new JFileChooser();
        	fc.setDialogTitle("Select Spritesheet Image");

        	//create the filter
        	FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
        	fc.setFileFilter(filter);
        	fc.setAcceptAllFileFilterUsed(false); //disables all files option

        	if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
        	    File selectedFile = fc.getSelectedFile();
        	    //double check extension just in case
        	    if (selectedFile.getName().toLowerCase().endsWith(ImageUtils.PNG_FORMAT)) {
        	        SpritesheetImporter importer = new SpritesheetImporter(frame, fc.getSelectedFile(), tileEditor.getLoadedSetup().getTileSize(), tileEditor);
        	        
        	        //only show the window if the user did not cancel the folder selection
        	        if (importer.isDisplayable()) { 
        	            importer.setVisible(true);
        	            
        	            //refresh your assets after importing message
                        if(showSpreadsheetUpdateOnce) {
                        	showSpreadsheetUpdateOnce = false;
                        	JOptionPane.showMessageDialog(frame, "Please restart or refresh assets to see new items after all Sprite Sheets are imported.");
                        }
        	        }
        	    } else {
        	        JOptionPane.showMessageDialog(frame, "Please select a valid .png file.");
        	    }
        	}
        });
        
        fileMenu.add(loadItem);
        fileMenu.add(quickSaveItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(spriteSheetImport);
        fileMenu.add(importChunkSelection);
        fileMenu.add(saveChunkSelection);
        fileMenu.addSeparator();
        fileMenu.add(exportTmxFormat);
        fileMenu.add(exportLvlFormat);
        fileMenu.add(exportCsvFormat);
        fileMenu.add(exportJsonFormat);
        fileMenu.addSeparator();
        fileMenu.add(exportIds);
        fileMenu.add(exportItem);
        
        //edit menu
        JMenu editMenu = new JMenu("Edit");
        
        JMenuItem refreshAssetsItem = new JMenuItem("Refresh Assets");
        refreshAssetsItem.setToolTipText("Re-scan the assets folders for new or modified images");
        refreshAssetsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        refreshAssetsItem.addActionListener(e -> refreshAssets(frame));
        
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setToolTipText("Reverse the last action");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> {
        	canvas.getHistoryFunction().undo();
        	tileEditor.getStatusInfoBar().updateStatusUI();
        });
        
        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setToolTipText("Reapply the last undone action");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> {
            canvas.getHistoryFunction().redo();
            tileEditor.getStatusInfoBar().updateStatusUI();
        });

        JMenuItem fillItem = new JMenuItem("Fill Entire Map");
        fillItem.setToolTipText("Replace every tile on the map with the currently selected tile");
        fillItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        fillItem.addActionListener(e -> {
            if (canvas.getSelected().isTileMode()) {
                canvas.fillMapWithTile(canvas.getSelected().getIndex());
                canvas.getToastNotification().showToastNotification("Map filled..");
            } else {
                JOptionPane.showMessageDialog(frame, "Select a tile first.");
            }
        });
        
        JMenuItem fillEmptyItem = new JMenuItem("Fill Empty Tiles");
        fillEmptyItem.setToolTipText("Fill only the transparent/empty spots on the map with the selected tile");
        fillEmptyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        fillEmptyItem.addActionListener(e -> {
            if (canvas.getSelected().isTileMode()) {
                canvas.fillEmptyTilesWithSelected();
                canvas.getToastNotification().showToastNotification("Empty tiles filled..");
            } else {
                JOptionPane.showMessageDialog(frame, "Select a tile first before filling empty areas.");
            }
        });
        
        JMenu extendMapMenu = new JMenu("Extend Map");
        extendMapMenu.setToolTipText("Add a new row/column of tiles in the selected direction on the map");

     //helper method used for all four directions
     JMenuItem extendMapUp = createExtendMenuItem(
         frame, 
         "Extend Up       \u2B06", 
         "Extend Map Up", 
         KeyEvent.VK_UP, 
         MapDirection.UP
     );

     JMenuItem extendMapDown = createExtendMenuItem(
         frame, 
         "Extend Down  \u2B07", 
         "Extend Map Down", 
         KeyEvent.VK_DOWN, 
         MapDirection.DOWN
     );

     JMenuItem extendMapRight = createExtendMenuItem(
         frame, 
         "Extend Right  \u27A1", 
         "Extend Map Right", 
         KeyEvent.VK_RIGHT, 
         MapDirection.RIGHT
     );

     JMenuItem extendMapLeft = createExtendMenuItem(
         frame, 
         "Extend Left    \u2B05", 
         "Extend Map Left", 
         KeyEvent.VK_LEFT, 
         MapDirection.LEFT
     );
        
        //add the buttons (JMenuItems) to the new JMenu
        extendMapMenu.add(extendMapUp);
        extendMapMenu.add(extendMapDown);
        extendMapMenu.add(extendMapLeft);
        extendMapMenu.add(extendMapRight);
        
        JMenuItem autoIdItem = new JMenuItem("Auto-Assign Missing IDs");
        autoIdItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        autoIdItem.setToolTipText("Finds textures without ID_ prefixes and assigns them unique IDs");
        autoIdItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, 
                "This will rename files in your assets folder to include unique IDs. Proceed?", 
                "Confirm Bulk Rename", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                performAutoIDAssignment(frame);
            }
        });
        
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(fillItem);
        editMenu.add(fillEmptyItem);
        editMenu.addSeparator();
        editMenu.add(extendMapMenu);
        editMenu.addSeparator();
        editMenu.add(refreshAssetsItem);
        editMenu.add(autoIdItem);
        
        JMenu toolsMenu = new JMenu("Tools");
        
        JMenuItem cleanupItem = new JMenuItem("Cleanup Missing Assets");
        cleanupItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0)); 
        cleanupItem.setToolTipText("Removes all IDs from the map that don't exist in the textures folder");
        cleanupItem.addActionListener(e -> {
            //confirmation dialog before proceeding
            int confirm = JOptionPane.showConfirmDialog(frame, 
                "This will permanently remove missing textures from the map. Proceed?", 
                "Confirm Cleanup", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                canvas.cleanupMissingAssets();
            }
        });
        
        JMenuItem scatterBrushItem = new JMenuItem("Custom Objects Scatter Brush");
        scatterBrushItem.setToolTipText("Enable/Disable Random Brush mode for painting multiple IDs at once");
        scatterBrushItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0)); 
        scatterBrushItem.addActionListener(e -> {
        	if (this.getBrushIds().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Ctrl+LClick objects in the 'Objects' palette first to add them to your brush!");
                return;
            }
            
            //convert Set to int array
            int[] idsArray = this.getBrushIds().stream().mapToInt(Integer::intValue).toArray();
            
            canvas.setBrushSpread(this.getBrushSpread());
            canvas.toggleBrushObject(idsArray);
            
            if(canvas.getSelected().isBrushTool()) {
            	canvas.getToastNotification().showToastNotification("Scatter brush active with " + idsArray.length + " items!");
            }
        	else {
        		canvas.getToastNotification().showToastNotification("Scatter brush tool Off..");
        	}
        });
        
        //create a container with zero vertical gap
        JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        sliderPanel.setOpaque(false); 

        //small and clean label
        JLabel spreadLabel = new JLabel("Spread: " + this.getBrushSpread());
        spreadLabel.setFont(new Font("Arial", Font.PLAIN, 12)); 

        //compact slider
        JSlider spreadSlider = new JSlider(JSlider.HORIZONTAL, 1, 60, this.getBrushSpread());
        spreadSlider.setPreferredSize(new Dimension(110, 22));
        spreadSlider.setOpaque(false);

        spreadSlider.addChangeListener(e -> {
        	this.setBrushSpread(spreadSlider.getValue());
            spreadLabel.setText("Spread: " + this.getBrushSpread());
            canvas.setBrushSpread(this.getBrushSpread());
        });
        sliderPanel.add(spreadSlider);
        sliderPanel.add(spreadLabel);
        
        //clean the brush selections
        JMenuItem cleanBrushItem = new JMenuItem("Cleanup Brush Selection");
        cleanBrushItem.setToolTipText("Clear the list of selected tiles for your brush tool");
        cleanBrushItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0)); 
        cleanBrushItem.addActionListener(e -> {
        	this.getBrushIds().clear();
        	tileEditor.getStatusInfoBar().updateStatusUI();
        	canvas.setUseBrushObjectFalse();
        	canvas.getToastNotification().showToastNotification("Brush selection cleaned..");
        });
        
        JMenuItem chunkSelectionItem = new JMenuItem("Chunk Selection Tool");
        chunkSelectionItem.setToolTipText("Drag to select a rectangular area for copying or exporting");
        chunkSelectionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0)); 
        chunkSelectionItem.addActionListener(e -> {
        	canvas.toggleChunkSelectionMode();
        	if(canvas.getSelected().isChunkSelectionTool()) {
        		canvas.getToastNotification().showToastNotification("Chunk selection tool On..");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Chunk selection tool Off..");
        	}
        });
        
        JMenuItem notesToolItem = new JMenuItem("Annotated Notes Tool");
        notesToolItem.setToolTipText("Click on the map grid to add or edit visual text reminders");
        notesToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        notesToolItem.addActionListener(e -> {
        	canvas.setNotesTool();
        	if(canvas.getSelected().isNotesTool()) {
        		canvas.getToastNotification().showToastNotification("Notes Tool Active!");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Notes tool Off..");
        	}
        });
        
        toolsMenu.add(notesToolItem);
        toolsMenu.add(scatterBrushItem);
        toolsMenu.add(sliderPanel);
        toolsMenu.add(cleanBrushItem);
        toolsMenu.addSeparator();
        toolsMenu.add(chunkSelectionItem);
        toolsMenu.add(cleanupItem);

        //mode menu
        JMenu modeMenu = new JMenu("Mode");
        
        JMenuItem eraseObjectItem = new JMenuItem("Erase Object / NPC Mode");
        eraseObjectItem.setToolTipText("Switch to Eraser mode to remove items from the map");
        eraseObjectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0)); 
        eraseObjectItem.addActionListener(e -> {
        	canvas.setSelectedEraseMode();
        	if(canvas.getSelected().isEraseMode()) {
        		canvas.getToastNotification().showToastNotification("Erasing mode On..");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Erasing mode Off..");
        	}
        });
        
        JMenuItem objectsCount = new JMenuItem("Scene Items Count");
        objectsCount.setToolTipText("Count and display the total number of items currently on the map");
        objectsCount.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0));
        objectsCount.addActionListener(e -> showObjectsCount(frame));
        
        JMenuItem npcWalkArea = new JMenuItem("Place NPC Walk Area");
        npcWalkArea.setToolTipText("Paint areas where NPCs are allowed to wander");
        npcWalkArea.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0));
        npcWalkArea.addActionListener(e -> {
        	canvas.togglePlaceNpcWalkArea();
        	if(canvas.getSelected().isNpcWalkAreaMode()) {
        		canvas.getToastNotification().showToastNotification("Placing NPC Walk Area On..");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Placing NPC Walk Area Off..");
        	}
        });

        JMenuItem toggleDarkMode = new JMenuItem("Toggle Dark Mode");
        toggleDarkMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0));
        toggleDarkMode.addActionListener(e -> {
        	if(tileEditor.getLoadedSetup().getDarkMode() == 1) {
        		tileEditor.getLoadedSetup().toggleDarkMode(false);
            	tileEditor.getLoadedSetup().setDarkMode(0);
        		canvas.getToastNotification().showToastNotification("Switching dark mode Off..");
        	}
        	else {
        		tileEditor.getLoadedSetup().toggleDarkMode(true);
            	tileEditor.getLoadedSetup().setDarkMode(1);
        		canvas.getToastNotification().showToastNotification("Switching dark mode On..");
        	}
        	
        	//save the new dark value inside the corresponding .txt file
            Utils.saveInitialDefaultValue("default_dark_mode.txt", String.valueOf(tileEditor.getLoadedSetup().getDarkMode()));
        });
        
        JMenuItem toggleLocateMode = new JMenuItem("Locate Item");
        toggleLocateMode.setToolTipText("Shows only the currently selected Tile / Object / NPC on the map");
        toggleLocateMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0));
        toggleLocateMode.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleLocateMode();
        	canvas.getCanvasRenderer().repaint();
        	tileEditor.getStatusInfoBar().updateStatusUI();
        	if(canvas.getCanvasViewState().isLocateMode()) {
        		canvas.getToastNotification().showToastNotification("Locating Item On..");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Locating Item Off..");
        	}
        });
        
        modeMenu.add(eraseObjectItem);
        modeMenu.add(npcWalkArea);
        modeMenu.add(toggleLocateMode);
        modeMenu.addSeparator();
        modeMenu.add(objectsCount);
        modeMenu.add(toggleDarkMode);
        
        //view menu
        JMenu viewMenu = new JMenu("View");
        
        JMenuItem toggleNightMode = new JMenuItem("Preview Night Mode");
        toggleNightMode.setToolTipText("Preview the map with a dark blue overlay to simulate night time");
        toggleNightMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0));
        toggleNightMode.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleNightMode();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isNightMode()) {
        		canvas.getToastNotification().showToastNotification("Preview night mode On..");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Preview night mode Off..");
        	}
        });
        
        JMenuItem toggleNotesMap = new JMenuItem("Toggle Annotated Notes");
        toggleNotesMap.setToolTipText("Show the notes added on the map");
        toggleNotesMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
        toggleNotesMap.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleNotesTool();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isShowNotesTool()) {
        		canvas.getToastNotification().showToastNotification("Preview annotated notes On..");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Preview annotated notes Off..");
        	}
        });
        
        JMenuItem toggleGridItem = new JMenuItem("Toggle Grid");
        toggleGridItem.setToolTipText("Show or hide the grid lines on the canvas");
        toggleGridItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0));
        toggleGridItem.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleGrid();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isShowGrid()) {
        		canvas.getToastNotification().showToastNotification("Toggled grid On..");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Toggled grid Off..");
        	}
        });
        
        JMenuItem toggleTilePosition = new JMenuItem("Toggle Cursor Position");
        toggleTilePosition.setToolTipText("Show the X,Y coordinates of the tile under the mouse");
        toggleTilePosition.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0));
        toggleTilePosition.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleTilePosition();
        	if(canvas.getCanvasViewState().isShowTilePosition()) {
        		canvas.getToastNotification().showToastNotification("Toggled cursor position On..");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Toggled cursor position Off..");
        	}
        });
        
        JMenuItem toggleObjectPlacerPreview = new JMenuItem("Toggle Live Placement");
        toggleObjectPlacerPreview.setToolTipText("Show a preview of the selected item attached to your cursor");
        toggleObjectPlacerPreview.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
        toggleObjectPlacerPreview.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleObjectPreview();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isShowObjectPreview()) {
        		canvas.getToastNotification().showToastNotification("Toggled live placement On..");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Toggled live placement Off..");
        	}
        });
        
        JMenuItem toggleTileMap = new JMenuItem("Toggle Tile Map");
        toggleTileMap.setToolTipText("Show or hide the background tile layer");
        toggleTileMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0));
        toggleTileMap.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleTileMap();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isShowTileLayer()) {
        		canvas.getToastNotification().showToastNotification("Toggled tiles On..");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Toggled tiles Off..");
        	}
        });
        
        JMenuItem toggleObjectMap = new JMenuItem("Toggle Object Map");
        toggleObjectMap.setToolTipText("Show or hide the object and decoration layer");
        toggleObjectMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0));
        toggleObjectMap.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleObjectMap();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isShowObjectLayer()) {
        		canvas.getToastNotification().showToastNotification("Toggled objects On..");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Toggled objects Off..");
        	}
        });
        
        JMenuItem toggleNpcMap = new JMenuItem("Toggle NPC Map");
        toggleNpcMap.setToolTipText("Show or hide the NPC layer");
        toggleNpcMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
        toggleNpcMap.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleNpcMap();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isShowNpcLayer()) {
        		canvas.getToastNotification().showToastNotification("Toggled NPCs On..");
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Toggled NPCs Off..");
        	}
        });
        
        JMenuItem toggleAutotile = new JMenuItem("Toggle Autotile");
        toggleAutotile.setToolTipText("Enable/Disable automatic tile corner and edge transitions");
        toggleAutotile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
        toggleAutotile.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleAutotile();
        	if(canvas.getCanvasViewState().isShowAutotile()) {
        		canvas.getToastNotification().showToastNotification("Toggled autotiles On..");
    			canvas.getMapState().refreshAutotileMap();
    			canvas.getCanvasRenderer().repaint();
        	}
        	else {
        		canvas.getToastNotification().showToastNotification("Toggled autotiles Off..");
        	}
        });
        
        viewMenu.add(toggleTileMap);
        viewMenu.add(toggleObjectMap);
        viewMenu.add(toggleNpcMap);
        viewMenu.addSeparator();
        viewMenu.add(toggleGridItem);
        viewMenu.add(toggleTilePosition);
        viewMenu.add(toggleObjectPlacerPreview);
        viewMenu.add(toggleAutotile);
        viewMenu.addSeparator();
        viewMenu.add(toggleNightMode);
        viewMenu.add(toggleNotesMap);
        
        //info menu
        JMenu helpMenu = new JMenu("Info");
        
        JMenuItem legendItem = new JMenuItem("Application Legend");
        legendItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0));
        legendItem.addActionListener(e -> {
            if (!tileEditor.getSearchFieldFunction().getSearchField().hasFocus()) {
            	applicationLegend.showLegend(frame);
            }
        });

        helpMenu.add(legendItem);
        
        //assemble the entire menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(toolsMenu);
        menuBar.add(modeMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
		//YouTube tutorial video links
        int thumbSize = 50;
		try {
	        JMenu tutorialMenu = new JMenu("Video Tutorials");

			BufferedImage thumbTutorialPart1 = ImageIO.read(new File(Utils.DEFAULT_BASE_PATH, "/icons/tutorial_part1.png"));
			BufferedImage thumbTutorialPart2 = ImageIO.read(new File(Utils.DEFAULT_BASE_PATH, "/icons/tutorial_part2.png"));
			BufferedImage thumbTutorialPart3 = ImageIO.read(new File(Utils.DEFAULT_BASE_PATH, "/icons/tutorial_part3.png"));
			
			ImageIcon ytIconPart1 = ImageUtils.createScaledIcon(thumbTutorialPart1, thumbSize, thumbSize);
			ImageIcon ytIconPart2 = ImageUtils.createScaledIcon(thumbTutorialPart2, thumbSize, thumbSize);
			ImageIcon ytIconPart3 = ImageUtils.createScaledIcon(thumbTutorialPart3, thumbSize, thumbSize);
			
			JMenuItem ytItemPart1 = new JMenuItem("Watch Video Tutorial - Part 1: Full Features & Asset Workflow", ytIconPart1);
			JMenuItem ytItemPart2 = new JMenuItem("Watch Video Tutorial - Part 2: World Building & Performance", ytIconPart2);
			JMenuItem ytItemPart3 = new JMenuItem("Watch Video Tutorial - Part 3: DarkMode & Workflow Upgrades", ytIconPart3);
			
			ytItemPart1.addActionListener(e -> {
				try {
					Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=Y0J-ezoVUCw"));
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, "Could not open browser: " + ex.getMessage());
				}
			});
			
			ytItemPart2.addActionListener(e -> {
				try {
					Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=atrQ6VdNxC0"));
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, "Could not open browser: " + ex.getMessage());
				}
			});
			
			ytItemPart3.addActionListener(e -> {
				try {
					Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=3fiajGU32Jg"));
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, "Could not open browser: " + ex.getMessage());
				}
			});
			
			tutorialMenu.add(ytItemPart1);
			tutorialMenu.add(ytItemPart2);
			tutorialMenu.add(ytItemPart3);
			
			//add as a sub menu
			helpMenu.add(tutorialMenu);
	        
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//add about page to the help menu
		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(e -> {
			applicationLegend.showAbout(frame);
		});
		helpMenu.add(aboutItem);

        return menuBar;
    }
	
	//create a custom extension menu that can be called for every direction extension
	private JMenuItem createExtendMenuItem(JFrame frame, String title, String dialogTitle, int keyCode, MapDirection direction) {
	    JMenuItem item = new JMenuItem(title);
	    
	    item.setAccelerator(KeyStroke.getKeyStroke(
	        keyCode, 
	        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
	    ));
	    
	    item.addActionListener(e -> {
	        //prompt the user for input
	        String input = JOptionPane.showInputDialog(
	            frame, 
	            "Enter the number of tiles to extend:",
	            dialogTitle,
	            JOptionPane.QUESTION_MESSAGE
	        );
	        //validate the input
	        if (input != null && !input.trim().isEmpty()) {
	            try {
	                int delta = Integer.parseInt(input.trim());
	                //call the canvas method with the parsed number and defined direction
	                canvas.getExtendMapTool().extendOrShrinkMap(direction, delta);

	                if(delta > 0) {
		                canvas.getToastNotification().showToastNotification("Map extended..");
	                }
	                else if(delta < 0) {
		                canvas.getToastNotification().showToastNotification("Map reduced..");
	                }

	            } catch (NumberFormatException ex) {
	                //handle case where the user enters non numeric text
	                JOptionPane.showMessageDialog(
	                    frame,
	                    "Invalid input. Please enter a valid number.",
	                    "Input Error",
	                    JOptionPane.ERROR_MESSAGE
	                );
	            }
	        }
	    });
	    
	    return item;
	}
	
	private void showObjectsCount(JFrame frame) {
		String message = getCountAllTiles();
		JOptionPane.showMessageDialog(frame, message, "Total Number of Items in The Scene", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public String getCountAllTiles() {
		String text = "Tiles Count:       " + Utils.countValidObjects(canvas.getMapState().getData().getTileMap());
		text += "\nObjects Count:  " + Utils.countValidObjects(canvas.getMapState().getData().getObjectMap());
		text += "\nNPCs Count:      " + Utils.countValidObjects(canvas.getMapState().getData().getNpcMap(), new int[]{1});
		return text;
	}
	
	private void performAutoIDAssignment(JFrame frame) {
	    String[] categories = {Utils.TILES_NAME, Utils.OBJECTS_NAME, Utils.NPCS_NAME};
	    int totalRenamed = 0;

	    for (String category : categories) {
	        File categoryRoot = new File(resourceBasePath, category);
	        if (!categoryRoot.exists()) continue;

	        //map out every ID currently used in this category globally
	        Set<Integer> globalUsedIDs = new HashSet<>();
	        Utils.collectExistingIDs(categoryRoot, globalUsedIDs);

	        //process subfolders
	        totalRenamed += Utils.processCategoryForIDs(categoryRoot, globalUsedIDs);
	    }

	    if (totalRenamed > 0) {
	        JOptionPane.showMessageDialog(frame, "Successfully renamed " + totalRenamed + " files.\nRefreshing palettes...");
	        refreshAssets(frame); 
	    } else {
	        JOptionPane.showMessageDialog(frame, "No files needed renaming.");
	    }
	}
	
    public void refreshAssets(JFrame frame) {
        int confirm = JOptionPane.showConfirmDialog(frame, 
            "This will re-scan the asset folders. Proceed?", "Refresh Assets", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
        	loadNewResources();
        }
    } 
    
    //refresh resources
    private void loadNewResources() {
        //create a temporary loading dialog
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(canvas.getCanvasRenderer());
        JDialog loadingDialog = Utils.createLoadingDialog(frame);
        loadingDialog.setLocationRelativeTo(frame);
        
        //start the background thread first
        ResourceLoaderWorker worker = new ResourceLoaderWorker(frame, loadingDialog, readInputs, tileEditor);
        worker.execute();
        
        //now show the dialog
        loadingDialog.setVisible(true);
    }

	public JMenuItem getQuickSaveItem() {
		return quickSaveItem;
	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}
	
	public int getBrushSpread() {
		return brushSpread;
	}

	public void setBrushSpread(int brushSpread) {
		this.brushSpread = brushSpread;
	}

	public Set<Integer> getBrushIds() {
		return brushIds;
	}
}
