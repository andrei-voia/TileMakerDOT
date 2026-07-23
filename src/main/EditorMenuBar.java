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
import localization.LocalizationManager;
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
		LocalizationManager loc = LocalizationManager.getInstance();
		
        JMenu fileMenu = new JMenu(loc.getString("file_menu"));
        
        JMenuItem loadItem = new JMenuItem(loc.getString("menu_load_map"));
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
        
        JMenuItem saveItem = new JMenuItem(loc.getString("menu_save_as"));
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
        
        quickSaveItem = new JMenuItem(loc.getString("menu_quick_save"));
        quickSaveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        quickSaveItem.addActionListener(e -> {
        	canvas.getMapExporter().saveMap(canvas.getMapState().getCacheData().getCachedSavedLocation(), false);
        	canvas.getMapState().getCacheData().setCanQuickSave(false);
        	tileEditor.getStatusInfoBar().updateStatusUI();
        	canvas.getToastNotification().showToastNotification(loc.getString("toast_map_saved"));
        });
        
        JMenuItem saveChunkSelection = new JMenuItem(loc.getString("menu_export_chunk"));
        saveChunkSelection.setToolTipText(loc.getString("menu_export_chunk_tooltip"));
        saveChunkSelection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveChunkSelection.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            	canvas.getMapExporter().saveChunk(chooser.getSelectedFile());
            }
        });
        
        JMenuItem importChunkSelection = new JMenuItem(loc.getString("menu_import_chunk"));
        importChunkSelection.setToolTipText(loc.getString("menu_import_chunk_tooltip"));
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
        
        JMenuItem exportLvlFormat = new JMenuItem(loc.getString("menu_export_lvl"));
        exportLvlFormat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportLvlFormat.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                canvas.getMapExporter().exportCustomLvlFormat(chooser.getSelectedFile());
            }
        });
        
        JMenuItem exportCsvFormat = new JMenuItem(loc.getString("menu_export_csv"));
        exportCsvFormat.setToolTipText(loc.getString("menu_export_csv_tooltip"));
        exportCsvFormat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportCsvFormat.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                canvas.getMapExporter().exportCsvFormat(chooser.getSelectedFile());
            }
        });
        
        JMenuItem exportTmxFormat = new JMenuItem(loc.getString("menu_export_tmx"));
        exportTmxFormat.setToolTipText(loc.getString("menu_export_tmx_tooltip"));
        exportTmxFormat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportTmxFormat.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                canvas.getMapExporter().exportTmxTsxFormat(chooser.getSelectedFile());
            }
        });
        
        JMenuItem exportJsonFormat = new JMenuItem(loc.getString("menu_export_json"));
        exportJsonFormat.setToolTipText(loc.getString("menu_export_json_tooltip"));
        exportJsonFormat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportJsonFormat.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                canvas.getMapExporter().exportJsonFormat(chooser.getSelectedFile());
            }
        });
        
        JMenuItem exportItem = new JMenuItem(loc.getString("menu_export_png"));
        exportItem.setToolTipText(loc.getString("menu_export_png_tooltip"));
        exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                canvas.getMapExporter().exportImage(chooser.getSelectedFile());
            }
        });
        
        JMenuItem exportIds = new JMenuItem(loc.getString("menu_export_ids"));
        exportIds.setToolTipText(loc.getString("menu_export_ids_tooltip"));
        exportIds.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportIds.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                canvas.getMapExporter().exportTileIdsList(chooser.getSelectedFile());
            }
        });
        
        JMenuItem spriteSheetImport = new JMenuItem(loc.getString("menu_import_spritesheet"));
        spriteSheetImport.setToolTipText(loc.getString("menu_import_spritesheet_tooltip"));
        spriteSheetImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        spriteSheetImport.addActionListener(e -> {
        	JFileChooser fc = new JFileChooser();
        	fc.setDialogTitle(loc.getString("dialog_select_spritesheet"));

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
                        	JOptionPane.showMessageDialog(frame, loc.getString("dialog_restart_refresh"));
                        }
        	        }
        	    } else {
        	        JOptionPane.showMessageDialog(frame, loc.getString("dialog_select_png"));
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
        JMenu editMenu = new JMenu(loc.getString("edit_menu"));
        
        JMenuItem refreshAssetsItem = new JMenuItem(loc.getString("menu_refresh_assets"));
        refreshAssetsItem.setToolTipText(loc.getString("menu_refresh_assets_tooltip"));
        refreshAssetsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        refreshAssetsItem.addActionListener(e -> refreshAssets(frame));
        
        JMenuItem undoItem = new JMenuItem(loc.getString("menu_undo"));
        undoItem.setToolTipText(loc.getString("menu_undo_tooltip"));
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> {
        	canvas.getHistoryFunction().undo();
        	tileEditor.getStatusInfoBar().updateStatusUI();
        });
        
        JMenuItem redoItem = new JMenuItem(loc.getString("menu_redo"));
        redoItem.setToolTipText(loc.getString("menu_redo_tooltip"));
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> {
            canvas.getHistoryFunction().redo();
            tileEditor.getStatusInfoBar().updateStatusUI();
        });

        JMenuItem fillItem = new JMenuItem(loc.getString("menu_fill_map"));
        fillItem.setToolTipText(loc.getString("menu_fill_map_tooltip"));
        fillItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        fillItem.addActionListener(e -> {
            if (canvas.getSelected().isTileMode()) {
                canvas.fillMapWithTile(canvas.getSelected().getIndex());
                canvas.getToastNotification().showToastNotification(loc.getString("toast_map_filled"));
            } else {
                JOptionPane.showMessageDialog(frame, loc.getString("dialog_select_tile_first"));
            }
        });
        
        JMenuItem fillEmptyItem = new JMenuItem(loc.getString("menu_fill_empty"));
        fillEmptyItem.setToolTipText(loc.getString("menu_fill_empty_tooltip"));
        fillEmptyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        fillEmptyItem.addActionListener(e -> {
            if (canvas.getSelected().isTileMode()) {
                canvas.fillEmptyTilesWithSelected();
                canvas.getToastNotification().showToastNotification(loc.getString("toast_empty_filled"));
            } else {
                JOptionPane.showMessageDialog(frame, loc.getString("dialog_select_tile_before_fill"));
            }
        });
        
        JMenu extendMapMenu = new JMenu(loc.getString("menu_extend_map"));
        extendMapMenu.setToolTipText(loc.getString("menu_extend_map_tooltip"));

     //helper method used for all four directions
     JMenuItem extendMapUp = createExtendMenuItem(
         frame, 
         loc.getString("menu_extend_up"), 
         loc.getString("menu_extend_up"), 
         KeyEvent.VK_UP, 
         MapDirection.UP
     );

     JMenuItem extendMapDown = createExtendMenuItem(
         frame, 
         loc.getString("menu_extend_down"), 
         loc.getString("menu_extend_down"), 
         KeyEvent.VK_DOWN, 
         MapDirection.DOWN
     );

     JMenuItem extendMapRight = createExtendMenuItem(
         frame, 
         loc.getString("menu_extend_right"), 
         loc.getString("menu_extend_right"), 
         KeyEvent.VK_RIGHT, 
         MapDirection.RIGHT
     );

     JMenuItem extendMapLeft = createExtendMenuItem(
         frame, 
         loc.getString("menu_extend_left"), 
         loc.getString("menu_extend_left"), 
         KeyEvent.VK_LEFT, 
         MapDirection.LEFT
     );
        
        //add the buttons (JMenuItems) to the new JMenu
        extendMapMenu.add(extendMapUp);
        extendMapMenu.add(extendMapDown);
        extendMapMenu.add(extendMapLeft);
        extendMapMenu.add(extendMapRight);
        
        JMenuItem autoIdItem = new JMenuItem(loc.getString("menu_auto_assign_ids"));
        autoIdItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        autoIdItem.setToolTipText(loc.getString("menu_auto_assign_ids_tooltip"));
        autoIdItem.addActionListener(e -> {
            int confirm = JOptionPane.showOptionDialog(frame, 
                loc.getString("menu_auto_assign_ids_confirm"), 
                loc.getString("menu_auto_assign_ids_title"), 
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new Object[]{loc.getString("button_yes"), loc.getString("button_no")}, loc.getString("button_yes"));
            
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
        
        JMenu toolsMenu = new JMenu(loc.getString("tools_menu"));
        
        JMenuItem cleanupItem = new JMenuItem(loc.getString("menu_cleanup_assets"));
        cleanupItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0)); 
        cleanupItem.setToolTipText(loc.getString("menu_cleanup_assets_tooltip"));
        cleanupItem.addActionListener(e -> {
            //confirmation dialog before proceeding
            int confirm = JOptionPane.showOptionDialog(frame, 
                loc.getString("menu_cleanup_assets_confirm"), 
                loc.getString("menu_cleanup_assets_title"), 
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, new Object[]{loc.getString("button_yes"), loc.getString("button_no")}, loc.getString("button_yes"));
                
            if (confirm == JOptionPane.YES_OPTION) {
                canvas.cleanupMissingAssets();
            }
        });
        
        JMenuItem scatterBrushItem = new JMenuItem(loc.getString("menu_scatter_brush"));
        scatterBrushItem.setToolTipText(loc.getString("menu_scatter_brush_tooltip"));
        scatterBrushItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0)); 
        scatterBrushItem.addActionListener(e -> {
        	if (this.getBrushIds().isEmpty()) {
                JOptionPane.showMessageDialog(frame, loc.getString("menu_scatter_brush_empty"));
                return;
            }
            
            //convert Set to int array
            int[] idsArray = this.getBrushIds().stream().mapToInt(Integer::intValue).toArray();
            
            canvas.setBrushSpread(this.getBrushSpread());
            canvas.toggleBrushObject(idsArray);
            
            if(canvas.getSelected().isBrushTool()) {
            	canvas.getToastNotification().showToastNotification(loc.getFormattedString("menu_scatter_brush_active", idsArray.length));
            }
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_scatter_brush_off"));
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
        JMenuItem cleanBrushItem = new JMenuItem(loc.getString("menu_clean_brush"));
        cleanBrushItem.setToolTipText(loc.getString("menu_clean_brush_tooltip"));
        cleanBrushItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0)); 
        cleanBrushItem.addActionListener(e -> {
        	this.getBrushIds().clear();
        	tileEditor.getStatusInfoBar().updateStatusUI();
        	canvas.setUseBrushObjectFalse();
        	canvas.getToastNotification().showToastNotification(loc.getString("menu_brush_cleaned"));
        });
        
        JMenuItem chunkSelectionItem = new JMenuItem(loc.getString("menu_chunk_selection"));
        chunkSelectionItem.setToolTipText(loc.getString("menu_chunk_selection_tooltip"));
        chunkSelectionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0)); 
        chunkSelectionItem.addActionListener(e -> {
        	canvas.toggleChunkSelectionMode();
        	if(canvas.getSelected().isChunkSelectionTool()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_chunk_selection_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_chunk_selection_off"));
        	}
        });
        
        JMenuItem notesToolItem = new JMenuItem(loc.getString("menu_notes_tool"));
        notesToolItem.setToolTipText(loc.getString("menu_notes_tool_tooltip"));
        notesToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        notesToolItem.addActionListener(e -> {
        	canvas.setNotesTool();
        	if(canvas.getSelected().isNotesTool()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_notes_tool_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_notes_tool_off"));
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
        JMenu modeMenu = new JMenu(loc.getString("mode_menu"));
        
        JMenuItem eraseObjectItem = new JMenuItem(loc.getString("menu_erase_mode"));
        eraseObjectItem.setToolTipText(loc.getString("menu_erase_mode_tooltip"));
        eraseObjectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0)); 
        eraseObjectItem.addActionListener(e -> {
        	canvas.setSelectedEraseMode();
        	if(canvas.getSelected().isEraseMode()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_erase_mode_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_erase_mode_off"));
        	}
        });
        
        JMenuItem objectsCount = new JMenuItem(loc.getString("menu_scene_count"));
        objectsCount.setToolTipText(loc.getString("menu_scene_count_tooltip"));
        objectsCount.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0));
        objectsCount.addActionListener(e -> showObjectsCount(frame));
        
        JMenuItem npcWalkArea = new JMenuItem(loc.getString("menu_npc_walk_area"));
        npcWalkArea.setToolTipText(loc.getString("menu_npc_walk_area_tooltip"));
        npcWalkArea.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0));
        npcWalkArea.addActionListener(e -> {
        	canvas.togglePlaceNpcWalkArea();
        	if(canvas.getSelected().isNpcWalkAreaMode()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_npc_walk_area_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_npc_walk_area_off"));
        	}
        });

        JMenuItem toggleDarkMode = new JMenuItem(loc.getString("menu_toggle_dark_mode"));
        toggleDarkMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0));
        toggleDarkMode.addActionListener(e -> {
        	if(tileEditor.getLoadedSetup().getDarkMode() == 1) {
        		tileEditor.getLoadedSetup().toggleDarkMode(false);
            	tileEditor.getLoadedSetup().setDarkMode(0);
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_dark_mode_off"));
        	}
        	else {
        		tileEditor.getLoadedSetup().toggleDarkMode(true);
            	tileEditor.getLoadedSetup().setDarkMode(1);
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_dark_mode_on"));
        	}
        	
        	//save the new dark value inside the corresponding .txt file
            Utils.saveInitialDefaultValue("default_dark_mode.txt", String.valueOf(tileEditor.getLoadedSetup().getDarkMode()));
        });
        
        JMenuItem toggleLocateMode = new JMenuItem(loc.getString("menu_locate_item"));
        toggleLocateMode.setToolTipText(loc.getString("menu_locate_item_tooltip"));
        toggleLocateMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0));
        toggleLocateMode.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleLocateMode();
        	canvas.getCanvasRenderer().repaint();
        	tileEditor.getStatusInfoBar().updateStatusUI();
        	if(canvas.getCanvasViewState().isLocateMode()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_locate_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_locate_off"));
        	}
        });
        
        modeMenu.add(eraseObjectItem);
        modeMenu.add(npcWalkArea);
        modeMenu.add(toggleLocateMode);
        modeMenu.addSeparator();
        modeMenu.add(objectsCount);
        modeMenu.add(toggleDarkMode);
        
        //view menu
        JMenu viewMenu = new JMenu(loc.getString("view_menu"));
        
        JMenuItem toggleNightMode = new JMenuItem(loc.getString("menu_night_mode"));
        toggleNightMode.setToolTipText(loc.getString("menu_night_mode_tooltip"));
        toggleNightMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0));
        toggleNightMode.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleNightMode();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isNightMode()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_night_mode_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_night_mode_off"));
        	}
        });
        
        JMenuItem toggleNotesMap = new JMenuItem(loc.getString("menu_toggle_notes"));
        toggleNotesMap.setToolTipText(loc.getString("menu_toggle_notes_tooltip"));
        toggleNotesMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
        toggleNotesMap.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleNotesTool();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isShowNotesTool()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_notes_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_notes_off"));
        	}
        });
        
        JMenuItem toggleGridItem = new JMenuItem(loc.getString("menu_toggle_grid"));
        toggleGridItem.setToolTipText(loc.getString("menu_toggle_grid_tooltip"));
        toggleGridItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0));
        toggleGridItem.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleGrid();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isShowGrid()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_grid_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_grid_off"));
        	}
        });
        
        JMenuItem toggleTilePosition = new JMenuItem(loc.getString("menu_toggle_cursor"));
        toggleTilePosition.setToolTipText(loc.getString("menu_toggle_cursor_tooltip"));
        toggleTilePosition.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0));
        toggleTilePosition.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleTilePosition();
        	if(canvas.getCanvasViewState().isShowTilePosition()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_cursor_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_cursor_off"));
        	}
        });
        
        JMenuItem toggleObjectPlacerPreview = new JMenuItem(loc.getString("menu_toggle_placement"));
        toggleObjectPlacerPreview.setToolTipText(loc.getString("menu_toggle_placement_tooltip"));
        toggleObjectPlacerPreview.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
        toggleObjectPlacerPreview.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleObjectPreview();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isShowObjectPreview()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_placement_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_placement_off"));
        	}
        });
        
        JMenuItem toggleTileMap = new JMenuItem(loc.getString("menu_toggle_tile_map"));
        toggleTileMap.setToolTipText(loc.getString("menu_toggle_tile_map_tooltip"));
        toggleTileMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0));
        toggleTileMap.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleTileMap();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isShowTileLayer()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_tile_map_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_tile_map_off"));
        	}
        });
        
        JMenuItem toggleObjectMap = new JMenuItem(loc.getString("menu_toggle_object_map"));
        toggleObjectMap.setToolTipText(loc.getString("menu_toggle_object_map_tooltip"));
        toggleObjectMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0));
        toggleObjectMap.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleObjectMap();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isShowObjectLayer()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_object_map_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_object_map_off"));
        	}
        });
        
        JMenuItem toggleNpcMap = new JMenuItem(loc.getString("menu_toggle_npc_map"));
        toggleNpcMap.setToolTipText(loc.getString("menu_toggle_npc_map_tooltip"));
        toggleNpcMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
        toggleNpcMap.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleNpcMap();
        	canvas.getCanvasRenderer().repaint();
        	if(canvas.getCanvasViewState().isShowNpcLayer()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_npc_map_on"));
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_npc_map_off"));
        	}
        });
        
        JMenuItem toggleAutotile = new JMenuItem(loc.getString("menu_toggle_autotile"));
        toggleAutotile.setToolTipText(loc.getString("menu_toggle_autotile_tooltip"));
        toggleAutotile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
        toggleAutotile.addActionListener(e -> {
        	canvas.getCanvasViewState().toggleAutotile();
        	if(canvas.getCanvasViewState().isShowAutotile()) {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_autotile_on"));
    			canvas.getMapState().refreshAutotileMap();
    			canvas.getCanvasRenderer().repaint();
        	}
        	else {
        		canvas.getToastNotification().showToastNotification(loc.getString("menu_toggle_autotile_off"));
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
        viewMenu.add(toggleNotesMap);
        viewMenu.add(toggleNightMode);
        
        //info menu
        JMenu helpMenu = new JMenu(loc.getString("help_menu"));
        
        JMenuItem legendItem = new JMenuItem(loc.getString("menu_legend"));
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
	        JMenu tutorialMenu = new JMenu(loc.getString("menu_video_tutorials"));

			BufferedImage thumbTutorialPart1 = ImageIO.read(new File(Utils.DEFAULT_BASE_PATH, "/icons/tutorial_part1.png"));
			BufferedImage thumbTutorialPart2 = ImageIO.read(new File(Utils.DEFAULT_BASE_PATH, "/icons/tutorial_part2.png"));
			BufferedImage thumbTutorialPart3 = ImageIO.read(new File(Utils.DEFAULT_BASE_PATH, "/icons/tutorial_part3.png"));
			
			ImageIcon ytIconPart1 = ImageUtils.createScaledIcon(thumbTutorialPart1, thumbSize, thumbSize);
			ImageIcon ytIconPart2 = ImageUtils.createScaledIcon(thumbTutorialPart2, thumbSize, thumbSize);
			ImageIcon ytIconPart3 = ImageUtils.createScaledIcon(thumbTutorialPart3, thumbSize, thumbSize);
			
			JMenuItem ytItemPart1 = new JMenuItem(loc.getString("menu_yt_part1"), ytIconPart1);
			JMenuItem ytItemPart2 = new JMenuItem(loc.getString("menu_yt_part2"), ytIconPart2);
			JMenuItem ytItemPart3 = new JMenuItem(loc.getString("menu_yt_part3"), ytIconPart3);
			
			ytItemPart1.addActionListener(e -> {
				try {
					Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=Y0J-ezoVUCw"));
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, loc.getFormattedString("menu_browser_error", ex.getMessage()));
				}
			});
			
			ytItemPart2.addActionListener(e -> {
				try {
					Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=atrQ6VdNxC0"));
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, loc.getFormattedString("menu_browser_error", ex.getMessage()));
				}
			});
			
			ytItemPart3.addActionListener(e -> {
				try {
					Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=3fiajGU32Jg"));
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, loc.getFormattedString("menu_browser_error", ex.getMessage()));
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
		
		helpMenu.addSeparator();
		
		//language selection submenu
		JMenu langMenu = new JMenu(loc.getString("menu_language"));
		
		//dynamically create menu items for each available locale
		java.util.Locale[] availableLocales = LocalizationManager.getAvailableLocales();
		for(java.util.Locale availableLocale : availableLocales) {
			String displayName = loc.getLanguageDisplayName(availableLocale);
			JMenuItem langItem = new JMenuItem(displayName);
			langItem.addActionListener(e -> {
				LocalizationManager.getInstance().setLocale(availableLocale);
				int response = JOptionPane.showOptionDialog(frame,
					loc.getString("language_restart"),
					loc.getString("menu_language"),
					JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
					null, new Object[]{loc.getString("button_yes"), loc.getString("button_no")}, loc.getString("button_yes"));
				if(response == JOptionPane.YES_OPTION) {
					LocalizationManager.restartApplication(availableLocale);
				}
			});
			langMenu.add(langItem);
		}
		helpMenu.add(langMenu);
		
		//add about page to the help menu
		JMenuItem aboutItem = new JMenuItem(loc.getString("menu_about"));
		aboutItem.addActionListener(e -> {
			applicationLegend.showAbout(frame);
		});
		helpMenu.add(aboutItem);

        return menuBar;
    }
	
	//create a custom extension menu that can be called for every direction extension
	private JMenuItem createExtendMenuItem(JFrame frame, String title, String dialogTitle, int keyCode, MapDirection direction) {
		LocalizationManager loc = LocalizationManager.getInstance();
		
	    JMenuItem item = new JMenuItem(title);
	    
	    item.setAccelerator(KeyStroke.getKeyStroke(
	        keyCode, 
	        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
	    ));
	    
	    item.addActionListener(e -> {
	        //prompt the user for input
	        String input = JOptionPane.showInputDialog(
	            frame, 
	            loc.getString("extend_dialog_message"),
	            loc.getFormattedString("extend_dialog_title", dialogTitle),
	            JOptionPane.QUESTION_MESSAGE
	        );
	        //validate the input
	        if (input != null && !input.trim().isEmpty()) {
	            try {
	                int delta = Integer.parseInt(input.trim());
	                //call the canvas method with the parsed number and defined direction
	                canvas.getExtendMapTool().extendOrShrinkMap(direction, delta);

	                if(delta > 0) {
		                canvas.getToastNotification().showToastNotification(loc.getString("toast_map_extended"));
	                }
	                else if(delta < 0) {
		                canvas.getToastNotification().showToastNotification(loc.getString("toast_map_reduced"));
	                }

	            } catch (NumberFormatException ex) {
	                //handle case where the user enters non numeric text
	                JOptionPane.showMessageDialog(
	                    frame,
	                    loc.getString("extend_error_message"),
	                    loc.getString("extend_error_title"),
	                    JOptionPane.ERROR_MESSAGE
	                );
	            }
	        }
	    });
	    
	    return item;
	}
	
	private void showObjectsCount(JFrame frame) {
		LocalizationManager loc = LocalizationManager.getInstance();
		String message = getCountAllTiles();
		JOptionPane.showMessageDialog(frame, message, loc.getString("scene_count_title"), JOptionPane.INFORMATION_MESSAGE);
	}
	
	public String getCountAllTiles() {
		LocalizationManager loc = LocalizationManager.getInstance();
		String text = loc.getFormattedString("scene_count_tiles", Utils.countValidObjects(canvas.getMapState().getData().getTileMap()));
		text += "\n" + loc.getFormattedString("scene_count_objects", Utils.countValidObjects(canvas.getMapState().getData().getObjectMap()));
		text += "\n" + loc.getFormattedString("scene_count_npcs", Utils.countValidObjects(canvas.getMapState().getData().getNpcMap(), new int[]{1}));
		return text;
	}
	
	private void performAutoIDAssignment(JFrame frame) {
		LocalizationManager loc = LocalizationManager.getInstance();
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
	        JOptionPane.showMessageDialog(frame, loc.getFormattedString("menu_auto_assign_ids_success", totalRenamed));
	        refreshAssets(frame); 
	    } else {
	        JOptionPane.showMessageDialog(frame, loc.getString("menu_auto_assign_ids_none"));
	    }
	}
	
    public void refreshAssets(JFrame frame) {
    	LocalizationManager loc = LocalizationManager.getInstance();
        int confirm = JOptionPane.showOptionDialog(frame, 
            loc.getString("refresh_confirm"), loc.getString("refresh_title"), 
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, new Object[]{loc.getString("button_yes"), loc.getString("button_no")}, loc.getString("button_yes"));
        
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
