package main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import core.Tile;
import localization.LocalizationManager;
import tools.SearchFieldFunction;
import utils.ApplicationLegend;
import utils.IDLoader;
import utils.RecentFileManager;
import utils.Utils;
import view.TileCanvas;
import view.WelcomePanel;

public class TileEditor {

    private CardLayout rootCardLayout;
    private JPanel rootPanel;
    private JPanel editorContainer;
    private File pendingFileToLoad;
    private EditorWindow editorWindow;

    private TileCanvas canvas;
    private ApplicationLegend applicationLegend;    
    
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
    
    //used to read the parsed tool parameters for loading saved files
    private JFrame mainFrame;
    private static String[] PARAM_ARGS;

    public static void main(String[] args) {
    	//ensures the GUI runs on the Event Dispatch Thread
        PARAM_ARGS = args;
        SwingUtilities.invokeLater(() -> new TileEditor().createAndShowGUI());
    }

    private void createAndShowGUI() {
    	//create the main frame
        mainFrame = new JFrame("TileMaker DOT");
        
        //sets the icon on the window title bar and task bar
        File iconFile = new File(WelcomePanel.APP_ICON_PATH);
        if (iconFile.exists()) {
            mainFrame.setIconImage(new ImageIcon(iconFile.getAbsolutePath()).getImage());
        }
        
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.setSize(1200, 750);
        mainFrame.setLocationRelativeTo(null); //centers the window on screen

        //close application when window X is clicked on Welcome Screen
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (canvas == null) {
                    System.exit(0);
                } else {
                    if (canvas.getMapState().getCacheData().getCachedSavedLocation() == null || canvas.getMapState().getCacheData().isCanQuickSave()) {
                        confirmExit(mainFrame);
                    } else {
                        mainFrame.dispose();
                        System.exit(0);
                    }
                }
            }
        });

        rootCardLayout = new CardLayout();
        rootPanel = new JPanel(rootCardLayout);

        //load initial setup and input dialog
        loadedSetup = new LoadedSetup(this);
        //initialize the application legend text
        applicationLegend = new ApplicationLegend();

        WelcomePanel welcomePanel = new WelcomePanel(new WelcomePanel.WelcomeActionListener() {
            @Override
            public void onNewMap() {
                handleNewMap();
            }

            @Override
            public void onOpenMap() {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("TileMaker DOT Map (*.tmdot)", "tmdot"));
                if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                    handleLoadMap(chooser.getSelectedFile());
                }
            }

            @Override
            public void onLoadRecent(File mapFile) {
                handleLoadMap(mapFile);
            }
        });

        rootPanel.add(welcomePanel, "WELCOME_SCREEN");
        mainFrame.add(rootPanel);
        mainFrame.setVisible(true);

        //normal application start
        if (appParamsExist()) {
            File fileToLoad = extractFileFromParamArgs();
            handleLoadMap(fileToLoad);
        }
    }

    private void handleNewMap() {
        editorWindow = new EditorWindow(this);
        String[] inputs = editorWindow.showGridSizeSelectionDialog(
            loadedSetup.getDefaultSizes(), 
            loadedSetup.getTileSizeLoaded(), 
            Long.toString(loadedSetup.getFrameDuration())
        );

        if (inputs != null && inputs.length == 4) {
            launchEditorWorker(inputs, null);
        }
    }

    private void handleLoadMap(File fileToLoad) {
        RecentFileManager.addRecentFile(fileToLoad.getAbsolutePath());

        loadedSetup.setOldResourceBasePath(loadedSetup.getResourceBasePath());
        loadedSetup.setResourceBasePath(loadedSetup.getResourceBasePath());

        int loadedMapTileSize = Utils.loadMapTileSize(fileToLoad, mainFrame);
        boolean savedIDFilterState = Utils.loadIDFilterState(loadedSetup.getResourceBasePath());

        String[] inputs = new String[]{
            loadedSetup.getDefaultSizes().get(0), 
            loadedSetup.getTileSizeLoaded(), 
            String.valueOf(savedIDFilterState), 
            Long.toString(loadedSetup.getFrameDuration())
        };

        if (Integer.parseInt(loadedSetup.getTileSizeLoaded()) != loadedMapTileSize) {
            AssetPathWindow pathWindow = new AssetPathWindow(
                loadedSetup.getResourceBasePath(), 
                loadedMapTileSize, 
                Integer.parseInt(loadedSetup.getTileSizeLoaded()),
                this
            );
            String[] result = pathWindow.showPathSelectionDialog();

            if (result != null) {
                if (result[1] != null) {
                    loadedSetup.setOldResourceBasePath(loadedSetup.getResourceBasePath());
                    loadedSetup.setResourceBasePath(result[1]);
                }
                inputs[1] = result[0];
            } else {
                return;
            }
        }

        launchEditorWorker(inputs, fileToLoad);
    }

    private void launchEditorWorker(String[] inputs, File fileToLoad) {
        if (inputs[2].equals("true")) {
            File idLoaderPath = new File(loadedSetup.getOldResourceBasePath() + "/settings/" + "default_used_ids_list.txt");
            if (idLoaderPath.exists() && idLoaderPath.isFile()) {
                idLoader = new IDLoader();
                idLoader.loadUniqueIDsList(idLoaderPath);
            } else {
                idLoader = null;
            }
        }

        long newFrameDuration = Long.parseLong(inputs[3]);
        if (newFrameDuration != loadedSetup.getFrameDuration()) {
            loadedSetup.setFrameDuration(newFrameDuration);
            Utils.saveInitialDefaultValue("default_frame_ms_duration.txt", String.valueOf(loadedSetup.getFrameDuration()));
        }

        this.pendingFileToLoad = fileToLoad;

        //create and show the loading screen
        JDialog loadingDialog = Utils.createLoadingDialog(mainFrame);
        loadingDialog.setLocationRelativeTo(mainFrame);

        //use a timer to show the dialog slightly later if needed for now keep it 0
        Timer showDialogTimer = new Timer(0, e -> loadingDialog.setVisible(true));
        showDialogTimer.setRepeats(false);
        showDialogTimer.start();

        //start the work on a background thread
        resourceWorker = new ResourceLoaderWorker(mainFrame, loadingDialog, inputs, this);
        resourceWorker.execute();
    }

    public void finishGUISetup(
        JFrame frame, 
        String[] inputs, 
        Map<String, List<Tile>> tileCategories, 
        Map<String, List<Tile>> objectCategories, 
        Map<String, List<Tile>> npcCategories) {
    	
    	//this happens only when you refresh assets from the tool, does not enter on the first start of the tool
        if (canvas != null) {
            resourceWorker.setAllTiles(flattenCategoryMap(tileCategories));
            resourceWorker.setAllObjects(flattenCategoryMap(objectCategories));
            resourceWorker.setAllNpcs(flattenCategoryMap(npcCategories));

            canvas.getMapState().loadNewTextures(resourceWorker.getAllTiles(), resourceWorker.getAllObjects(), resourceWorker.getAllNpcs());

            updateTabViewport(this.tileTabs, assetPalette.createTilePalette(tileCategories));
            updateTabViewport(this.objectTabs, assetPalette.createObjectPalette(objectCategories, resourceWorker.getAllObjects()));
            updateTabViewport(this.npcTabs, assetPalette.createNpcPalette(npcCategories, resourceWorker.getAllNpcs()));

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
        if (Integer.parseInt(loadedSetup.getTileSizeLoaded()) != loadedSetup.getTileSize()) {
            Utils.saveInitialDefaultValue("default_tile_size.txt", String.valueOf(loadedSetup.getTileSize()));
        }

        //icon loading section
        editorIcons = new EditorIcons(loadedSetup.getTileSize(), loadedSetup.getDefaultSelectedTile());
        //set the icon for the JFrame window
        frame.setIconImage(editorIcons.getIconImage());

        //initialize canvas and scroll pane using the loaded tile size
        canvas = new TileCanvas(this, rows, cols, resourceWorker.getAllTiles(), resourceWorker.getAllObjects(), resourceWorker.getAllNpcs(), loadedSetup.getTileSize()); 
        JScrollPane canvasScroll = new JScrollPane(canvas.getCanvasRenderer());
        canvas.getCanvasRenderer().requestFocusInWindow();

        assetPalette = new AssetPalette(this, canvas);
        editorMenuBar = new EditorMenuBar(this, canvas, applicationLegend, inputs, loadedSetup);
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
        searchFieldFunction = new SearchFieldFunction(editorMenuBar, assetPalette, tileTabs, objectTabs, npcTabs);

        //assemble the panel and add to the left side bar
        searchPanel.add(searchFieldFunction.getSearchField(), BorderLayout.CENTER);
        searchPanel.add(searchFieldFunction.getClearButton(), BorderLayout.EAST);
        searchPanel.add(searchFieldFunction.getSearchLabel(), BorderLayout.WEST);

        LocalizationManager loc = LocalizationManager.getInstance();

        //add the objects and NPCs tabs view
        JTabbedPane itemTabs = new JTabbedPane();
        itemTabs.addTab(loc.getString("objects_tab"), objectTabs);
        itemTabs.addTab(loc.getString("npcs_tab"), npcTabs);

        //add the status down on the left side bar
        JPanel controlPanel = createControlPanel();
        controlPanel.add(statusInfoBar.getSelectionLabel());
        controlPanel.add(statusInfoBar.getSelectionPreview());
        controlPanel.add(statusInfoBar.getStatusBar());

        //assemble the left side bar
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.add(searchPanel, BorderLayout.NORTH);
        sidebar.add(itemTabs, BorderLayout.CENTER); 
        sidebar.add(controlPanel, BorderLayout.SOUTH); 
        sidebar.setPreferredSize(new Dimension(350, 600));
        sidebar.setMinimumSize(new Dimension(150, 600));

        //assemble the main window split panes
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, canvasScroll, tileTabs);
        verticalSplit.setResizeWeight(1.0);
        verticalSplit.setDividerSize(6);
        //we also set it dynamically inside revalidateTimer, but it will be a split second before update
        verticalSplit.setDividerLocation(616);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, verticalSplit);
        splitPane.setResizeWeight(0.0);
        splitPane.setDividerLocation(350);

        editorContainer = new JPanel(new BorderLayout());
        editorContainer.add(splitPane, BorderLayout.CENTER);

        rootPanel.add(editorContainer, "EDITOR_SCREEN");
        rootCardLayout.show(rootPanel, "EDITOR_SCREEN");

        if (!assetPalette.getHiddenFolders().isEmpty()) {
            JOptionPane.showMessageDialog(
                frame, loc.getFormattedString("hidden_folders_message", assetPalette.getHiddenFolders().toString()),
                loc.getString("hidden_folders_detected"), JOptionPane.INFORMATION_MESSAGE);
        }

        //forced revalidation after GUI is visible
        Timer revalidateTimer = new Timer(100, e -> {
        	int totalHeight = verticalSplit.getHeight();
            if (totalHeight > 0) {
            	//set the vertical height for the tiles panel to always be around 711 - 616 = 95
                verticalSplit.setDividerLocation(totalHeight - 120);
            }
        	
            frame.revalidate();
            frame.repaint();
            ((Timer) e.getSource()).stop();
        });
        revalidateTimer.setRepeats(false);
        revalidateTimer.start();

        //save the new assets location inside the corresponding .txt file
        if (!loadedSetup.getOldResourceBasePath().equals(loadedSetup.getResourceBasePath())) {
            Utils.saveInitialDefaultValue("default_assets_path.txt", String.valueOf(loadedSetup.getResourceBasePath()));
        }

        if (pendingFileToLoad != null && pendingFileToLoad.exists() && pendingFileToLoad.isFile()) {
            canvas.getMapLoader().loadMap(pendingFileToLoad);
            pendingFileToLoad = null;
        }
    }

    private void updateTabViewport(JTabbedPane mainContainer, JTabbedPane newSource) {
        mainContainer.removeAll();
        int tabCount = newSource.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            String title = newSource.getTitleAt(0);
            Component comp = newSource.getComponentAt(0);
            mainContainer.addTab(title, comp);
        }
        newSource = null;
        mainContainer.revalidate();
        mainContainer.repaint();
    }

    public List<Tile> flattenCategoryMap(Map<String, List<Tile>> categories) {
        List<Tile> flatList = new ArrayList<>();
        categories.values().forEach(flatList::addAll);
        return flatList;
    }

    private boolean appParamsExist() {
        return PARAM_ARGS != null && PARAM_ARGS.length > 0;
    }

    private File extractFileFromParamArgs() {
        String fullPath = String.join(" ", PARAM_ARGS).replace("\"", ""); 
        return new File(fullPath);
    }

    //displays a confirmation dialog and exits the application if the user confirms
    private void confirmExit(JFrame frame) {
        LocalizationManager loc = LocalizationManager.getInstance();
        int response = JOptionPane.showConfirmDialog(
            frame,
            loc.getString("exit_confirm_message"),
            loc.getString("exit_confirm_title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        //if the user clicks yes then terminate the application
        if (response == JOptionPane.YES_OPTION) {
        	//clean up the window resources
            frame.dispose();
            //terminate the Java Virtual Machine
            System.exit(0);
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

	public JFrame getMainFrame() {
		return mainFrame;
	}
}
