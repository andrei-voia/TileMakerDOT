package main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import core.Tile;
import localization.LocalizationManager;
import tools.SearchFieldFunction;
import utils.ApplicationLegend;
import utils.IDLoader;
import utils.Utils;
import view.TileCanvas;

public class TileEditor {
	
    private TileCanvas canvas;
    
    private ApplicationLegend applicationLegend;    
    private EditorWindow editorWindow;
    
    private ResourceLoaderWorker resourceWorker;
    private EditorMenuBar editorMenuBar;
    private EditorIcons editorIcons;
    private LoadedSetup loadedSetup;
    private AssetPalette assetPalette;
    private StatusInfoBar statusInfoBar;
    private IDLoader idLoader = null;
    private SearchFieldFunction searchFieldFunction;
    
    //used for updateStatusUI
    private JTabbedPane tileTabs;
    private JTabbedPane objectTabs;
    private JTabbedPane npcTabs;
    
    public static void main(String[] args) {
        //ensures the GUI runs on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new TileEditor().createAndShowGUI());
    }
    
    private void createAndShowGUI() {
    	//load initial setup and input dialog
    	loadedSetup = new LoadedSetup(this);

        //create the starting window where you can edit your input before using this tool
        editorWindow = new EditorWindow(this);
        
        //get grid size and tile size
        String[] inputs = editorWindow.showGridSizeSelectionDialog(loadedSetup.getDefaultSizes(), loadedSetup.getTileSizeLoaded(), Long.toString(loadedSetup.getFrameDuration()));
        if (inputs == null || inputs.length != 4 || !inputs[0].matches("\\d+x\\d+") || !inputs[1].matches("\\d+")) {
            return;
        }
        
        if(inputs[2].equals("true")) {
        	File idLoaderPath = new File(loadedSetup.getOldResourceBasePath() + "/settings/" + "default_used_ids_list.txt");
        	if (idLoaderPath.exists() && idLoaderPath.isFile()) {
        		idLoader = new IDLoader();
        		idLoader.loadUniqueIDsList(idLoaderPath);
        	} else {
        		idLoader = null;
        	    //the file is missing
        	    System.out.println("\"default_used_ids_list.txt\" file not found. Skipping unique ID loading.");
        	}
        }
        
        long newFrameDuration = Long.parseLong(inputs[3]);
        if(newFrameDuration != loadedSetup.getFrameDuration()) {
        	loadedSetup.setFrameDuration(newFrameDuration);
        	//save the new frame duration animation inside the corresponding .txt file
        	Utils.saveInitialDefaultValue("default_frame_ms_duration.txt", String.valueOf(loadedSetup.getFrameDuration()));
        }
        
        //create the main frame
        JFrame frame = new JFrame("TileMaker DOT");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //create and show the loading screen
        JDialog loadingDialog = Utils.createLoadingDialog(frame);
        //position it in the center of the screen
        loadingDialog.setLocationRelativeTo(null);
        
        //use a timer to show the dialog slightly later if needed for now keep it 0
        Timer showDialogTimer = new Timer(0, e -> loadingDialog.setVisible(true));
        showDialogTimer.setRepeats(false);
        showDialogTimer.start();
        
        //initialize the application legend text
        applicationLegend = new ApplicationLegend();
        
        //start the work on a background thread
        resourceWorker = new ResourceLoaderWorker(frame, loadingDialog, inputs, this);
        resourceWorker.execute();
    }
    
    private void updateTabViewport(JTabbedPane mainContainer, JTabbedPane newSource) {
        //wipe the old tabs categories clean
        mainContainer.removeAll();
        //loop through the new JTabbedPane and move its tabs to the main one
        int tabCount = newSource.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            String title = newSource.getTitleAt(0);
            Component comp = newSource.getComponentAt(0);
            mainContainer.addTab(title, comp);
        }
        
        //help the GC
        newSource = null;
        
        mainContainer.revalidate();
        mainContainer.repaint();
    }
    
    //a method that goes through the list of tabs shown (tiles, objects or NPCs) and saves all the tiles from it to a new list
    public List<Tile> flattenCategoryMap(Map<String, List<Tile>> categories) {
    	List<Tile> flatList = new ArrayList<>();
        categories.values().forEach(flatList::addAll);
        return flatList;
    }
    
    public void finishGUISetup(
        JFrame frame, 
        String[] inputs, 
        Map<String, List<Tile>> tileCategories, 
        Map<String, List<Tile>> objectCategories, 
        Map<String, List<Tile>> npcCategories) {
    	
    	//this happens only when you refresh assets from the tool, does not enter on the first start of the tool
    	if (canvas != null) {
    	    //refresh the raw data lists
    		resourceWorker.setAllTiles(flattenCategoryMap(tileCategories));
    		resourceWorker.setAllObjects(flattenCategoryMap(objectCategories));
    		resourceWorker.setAllNpcs(flattenCategoryMap(npcCategories));

    	    //update canvas references
    	    canvas.getMapState().loadNewTextures(resourceWorker.getAllTiles(), resourceWorker.getAllObjects(), resourceWorker.getAllNpcs());

    	    //update the UI
    	    updateTabViewport(this.tileTabs, assetPalette.createTilePalette(tileCategories));
    	    updateTabViewport(this.objectTabs, assetPalette.createObjectPalette(objectCategories, resourceWorker.getAllObjects()));
    	    updateTabViewport(this.npcTabs, assetPalette.createNpcPalette(npcCategories, resourceWorker.getAllNpcs()));

			//if the autoTile is not active then do not reset autotileMap to save time
			if (canvas.getCanvasViewState().isShowAutotile()) {
				canvas.getMapState().refreshAutotileMap();
			}
    	    canvas.getMapState().refreshAllObjectsList();
    	    
    	    canvas.getCanvasRenderer().revalidate();
    	    canvas.getCanvasRenderer().repaint();
    	    return;
    	}

        //parse input
        String[] parts = inputs[0].split("x");
        int cols = Integer.parseInt(parts[0]);
        int rows = Integer.parseInt(parts[1]);
        loadedSetup.setTileSize(Integer.parseInt(inputs[1]));
        
        //save the new tile size inside the corresponding .txt file
        if(Integer.parseInt(loadedSetup.getTileSizeLoaded()) != loadedSetup.getTileSize()) {
        	Utils.saveInitialDefaultValue("default_tile_size.txt", String.valueOf(loadedSetup.getTileSize()));
        }
        
		//add the window listener
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				//call the custom exit confirmation method if unsaved progress
				if (canvas != null && canvas.getMapState().getCacheData().getCachedSavedLocation() == null || canvas.getMapState().getCacheData().isCanQuickSave()) {
					confirmExit(frame);
				} else {
					frame.dispose(); //clean up the window resources
					System.exit(0); //terminate the application
				}
			}
		});

        //icon loading section
        editorIcons = new EditorIcons(loadedSetup.getTileSize(), loadedSetup.getDefaultSelectedTile());
        //set the icon for the JFrame window
        frame.setIconImage(editorIcons.getIconImage());
        
        //initialize canvas and scroll pane using the loaded tile size
        canvas = new TileCanvas(this, rows, cols, resourceWorker.getAllTiles(), resourceWorker.getAllObjects(), resourceWorker.getAllNpcs(), loadedSetup.getTileSize()); 
        JScrollPane canvasScroll = new JScrollPane(canvas.getCanvasRenderer());
        canvas.getCanvasRenderer().requestFocusInWindow();
        
        assetPalette = new AssetPalette(this, canvas);
        
        editorMenuBar = new EditorMenuBar(this, canvas, applicationLegend, inputs, loadedSetup.getResourceBasePath());
        
        statusInfoBar = new StatusInfoBar(this, canvas, editorMenuBar);
        
        //attach the menu bar
        frame.setJMenuBar(editorMenuBar.createMenuBar(frame));

        //create UI palette using the loaded resources
        tileTabs = assetPalette.createTilePalette(tileCategories);
        objectTabs = assetPalette.createObjectPalette(objectCategories, resourceWorker.getAllObjects());
        npcTabs = assetPalette.createNpcPalette(npcCategories, resourceWorker.getAllNpcs());
        
        statusInfoBar.initSelections();
        frame.add(statusInfoBar.getStatusBar(), BorderLayout.SOUTH);

        //force the editor to recalculate thumbnails and button highlights immediately
        statusInfoBar.updateStatusUI();

        //create a container panel for the search bar
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        searchFieldFunction = new SearchFieldFunction(editorMenuBar, assetPalette,
        		tileTabs, objectTabs, npcTabs);

        //assemble the panel and add to the left side bar
        searchPanel.add(searchFieldFunction.getSearchField(), BorderLayout.CENTER);
        searchPanel.add(searchFieldFunction.getClearButton(), BorderLayout.EAST);
        searchPanel.add(searchFieldFunction.getSearchLabel(), BorderLayout.WEST);
        
        //add the objects and NPCs tabs view
        JTabbedPane itemTabs = new JTabbedPane();
        itemTabs.addTab(Utils.OBJECTS_NAME, objectTabs);
        itemTabs.addTab(Utils.NPCS_NAME, npcTabs);
        
        //add the status down on the left side bar
        JPanel controlPanel = createControlPanel();
        controlPanel.add(statusInfoBar.getSelectionLabel());
        controlPanel.add(statusInfoBar.getSelectionPreview());
        controlPanel.add(statusInfoBar.getStatusBar());
        
        //assemble the left side bar
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.add(searchPanel, BorderLayout.NORTH); //add a search bar at the top
        sidebar.add(itemTabs, BorderLayout.CENTER); 
        sidebar.add(controlPanel, BorderLayout.SOUTH); 
        sidebar.setPreferredSize(new Dimension(350, 600));
        sidebar.setMinimumSize(new Dimension(150, 600));

        //assemble the main window split panes
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, canvasScroll, tileTabs);
        verticalSplit.setResizeWeight(1.0);
        verticalSplit.setDividerSize(6);
        verticalSplit.setDividerLocation(640);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, verticalSplit);
        splitPane.setResizeWeight(0.0);
        splitPane.setDividerLocation(360);
        
        frame.setLayout(new BorderLayout());
        frame.add(splitPane, BorderLayout.CENTER);
        frame.setSize(1300, 800);
        
        //show the frame
        frame.setVisible(true);
        
        //show message if any hidden folders were found
        if(!assetPalette.getHiddenFolders().isEmpty()) {
        	LocalizationManager loc = LocalizationManager.getInstance();
            JOptionPane.showMessageDialog(
                    frame, loc.getFormattedString("hidden_folders_message", assetPalette.getHiddenFolders().toString()),
                    loc.getString("hidden_folders_message"), JOptionPane.INFORMATION_MESSAGE);
        }

        //forced revalidation after GUI is visible
        Timer revalidateTimer = new Timer(100, e -> {
            frame.revalidate();
            frame.repaint();
            ((Timer) e.getSource()).stop();
        });
        revalidateTimer.setRepeats(false);
        revalidateTimer.start();
        
        //save the new assets location inside the corresponding .txt file
        if(!loadedSetup.getOldResourceBasePath().equals(loadedSetup.getResourceBasePath())) {
        	Utils.saveInitialDefaultValue("default_assets_path.txt", String.valueOf(loadedSetup.getResourceBasePath()));
        }
    }
    
    //displays a confirmation dialog and exits the application if the user confirms
    private void confirmExit(JFrame frame) {
    	LocalizationManager loc = LocalizationManager.getInstance();
        int response = JOptionPane.showOptionDialog(
            frame,
            loc.getString("exit_confirm_message"),
            loc.getString("exit_confirm_title"),
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            new Object[]{loc.getString("button_yes"), loc.getString("button_no")},
            loc.getString("button_yes")
        );

        //if the user clicks yes then terminate the application
        if (response == 0) {
            frame.dispose(); //clean up the window resources
            System.exit(0);  //terminate the Java Virtual Machine
        }
        //if the user clicks no or closes the dialog nothing happens and the application remains open
    }
    
    //this panel serves as a container for the selection preview
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return controlPanel;
    }
    
	public JTabbedPane getTileTabs() {
		return tileTabs;
	}

	public JTabbedPane getObjectTabs() {
		return objectTabs;
	}

	public JTabbedPane getNpcTabs() {
		return npcTabs;
	}

	public StatusInfoBar getStatusInfoBar() {
		return statusInfoBar;
	}

	public ResourceLoaderWorker getResourceWorker() {
		return resourceWorker;
	}
	
	public IDLoader getIdLoader() {
		return idLoader;
	}

	public EditorMenuBar getEditorMenuBar() {
		return editorMenuBar;
	}

	public EditorIcons getEditorIcons() {
		return editorIcons;
	}

	public TileCanvas getCanvas() {
		return canvas;
	}

	public LoadedSetup getLoadedSetup() {
		return loadedSetup;
	}

	public SearchFieldFunction getSearchFieldFunction() {
		return searchFieldFunction;
	}
}
