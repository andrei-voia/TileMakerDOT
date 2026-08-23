package view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import core.Tile;
import data.MapState;
import localization.LocalizationManager;

public class StatisticsDashboardDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	//keep track of both sorters so the single search bar filters both tabs simultaneously!
    private TableRowSorter<DefaultTableModel> objectSorter;
    private TableRowSorter<DefaultTableModel> npcSorter;
    private TableRowSorter<DefaultTableModel> tileSorter;
    
    private MapState mapState;
	private Map<String, Integer> tileAssets = new HashMap<>();
	
	private LocalizationManager loc = LocalizationManager.getInstance();

	public StatisticsDashboardDialog(JFrame parent, MapStatistics mapStatistics, MapState mapState) {
        super(parent, LocalizationManager.getInstance().getString("menu_statistics_dashboard"), true);
        setSize(500, 620); //set the size of the dialog
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        this.mapState = mapState;
        
        //run fresh density calculations so we have the objects and NPCs densities
        mapStatistics.calculateAssetDensity();
        calculateTileAssetsDensity();
        
        //calculate grand totals for Objects and NPCs from the calculated lists
        List<Map.Entry<String, Integer>> objectList = mapStatistics.getObjectAssetsList();
        List<Map.Entry<String, Integer>> npcList = mapStatistics.getNpcAssetsList();
        List<Map.Entry<String, Integer>> tileList = getTileAssetsList();

        int totalObjectsCount = objectList.stream().mapToInt(Map.Entry::getValue).sum();
        int uniqueObjectsCount = objectList.size(); //the number of unique object types
        
        int totalNpcsCount = npcList.stream().mapToInt(Map.Entry::getValue).sum();
        int uniqueNpcsCount = npcList.size(); //the number of unique NPC types
        
        int totalTilesCount = tileList.stream().mapToInt(Map.Entry::getValue).sum();
        int uniqueTilesCount = tileList.size(); //the number of unique tiles types

        //create a clean summary panel at the top
        JPanel summaryPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("📊 " + loc.getString("dashboard_total")));
        
        JLabel tilesLabel = new JLabel("  ▧ " + loc.getString("dashboard_total_tiles") + totalTilesCount);
        JLabel uniqueTilesLabel = new JLabel("  ▧ " + loc.getString("dashboard_unique_tiles") + uniqueTilesCount);
        JLabel objectsLabel = new JLabel("  📦 " + loc.getString("dashboard_total_objects") + totalObjectsCount);
        JLabel uniqueObjectsLabel = new JLabel("  📦 " + loc.getString("dashboard_unique_objects") + uniqueObjectsCount);
        JLabel npcsLabel = new JLabel("  👥 " + loc.getString("dashboard_total_npcs") + totalNpcsCount);
        JLabel uniqueNpcsLabel = new JLabel("  👥 " + loc.getString("dashboard_unique_npcs") + uniqueNpcsCount);
        
        Font summaryFont = new Font("Monospaced", Font.BOLD, 14);
        tilesLabel.setFont(summaryFont);
        uniqueTilesLabel.setFont(summaryFont);
        objectsLabel.setFont(summaryFont);
        uniqueObjectsLabel.setFont(summaryFont);
        npcsLabel.setFont(summaryFont);
        uniqueNpcsLabel.setFont(summaryFont);

        summaryPanel.add(tilesLabel);
        summaryPanel.add(uniqueTilesLabel);
        summaryPanel.add(objectsLabel);
        summaryPanel.add(uniqueObjectsLabel);
        summaryPanel.add(npcsLabel);
        summaryPanel.add(uniqueNpcsLabel);
        
        //global real time search filter panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel searchLabel = new JLabel(loc.getString("dashboard_filter"));
        JTextField searchField = new JTextField();
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        //create the itemized data tables
        JComponent objectsTab = createStatsTableTab(objectList, loc.getString("dashboard_col_object_name"), 0);
        JComponent npcsTab = createStatsTableTab(npcList, loc.getString("dashboard_col_npc_name"), 1);
        JComponent tilesTab = createStatsTableTab(tileList, loc.getString("dashboard_col_tile_name"), 2);
        
        //wire up the search listener to filter in real-time
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTables(searchField.getText()); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTables(searchField.getText()); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTables(searchField.getText()); }
        });
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📦 " + loc.getString("dashboard_object_density"), objectsTab);
        tabbedPane.addTab("👥 " + loc.getString("dashboard_npc_density"), npcsTab);
        tabbedPane.addTab("▧ " + loc.getString("dashboard_tile_density"), tilesTab);

        //combine everything into a master layout panel
        JPanel topContainer = new JPanel(new BorderLayout());
        JLabel warningLabel = new JLabel(loc.getString("dashboard_title_review"), SwingConstants.CENTER);
        warningLabel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        warningLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        topContainer.add(warningLabel, BorderLayout.NORTH);
        topContainer.add(summaryPanel, BorderLayout.CENTER);
        topContainer.add(searchPanel, BorderLayout.SOUTH);

        //final assembly onto dialog content pane
        add(topContainer, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

	//helper method to generate tables and track their respective row sort
    private JComponent createStatsTableTab(List<Map.Entry<String, Integer>> dataList, String columnName, int tabIndex) {
        String[] columnNames = {columnName, loc.getString("dashboard_total_placed")};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
			private static final long serialVersionUID = 1L;

			@Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        JTable table = new JTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);

        //populate items
        for (Map.Entry<String, Integer> entry : dataList) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }

        //initialize and link the sorter to this table
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        //save references globally so our search listener can control them
        if (tabIndex == 0) {
            this.objectSorter = sorter;
        } else if(tabIndex == 1) {
            this.npcSorter = sorter;
        } else if(tabIndex == 2) {
        	this.tileSorter = sorter;
        }

        return new JScrollPane(table);
    }
    
    //applies a REGEX filter across all data grid sorters safely
    private void filterTables(String text) {
        //(?i) makes the search completely case insensitive
        RowFilter<DefaultTableModel, Object> rowFilter = null;
        if (text != null && text.trim().length() > 0) {
            rowFilter = RowFilter.regexFilter("(?i)" + text.trim(), 0); //0 filters by column 0 (name)
        }
        
        if (objectSorter != null) objectSorter.setRowFilter(rowFilter);
        if (npcSorter != null) npcSorter.setRowFilter(rowFilter);
        if (tileSorter != null) tileSorter.setRowFilter(rowFilter);
    }
    
    private void calculateTileAssetsDensity() {
        tileAssets.clear();
        
	    for (int i = 0; i < mapState.getData().getTileMap().length; i++) {
	        for (int j = 0; j < mapState.getData().getTileMap()[i].length; j++) {
	        	int tileId = mapState.getData().getTileMap(i, j);
	        	
	            if (tileId != -1) {
	                Tile tile = mapState.findTileById(tileId);
	                
	            	String assetName = tile.getName();
	            	//add to tiles map count
	            	tileAssets.put(assetName, tileAssets.getOrDefault(assetName, 0) + 1);
	            }
	        }
	    }
    }
    
	private List<Entry<String, Integer>> getTileAssetsList() {
        //sort the tile map by its values (counts) in descending order (highest density first)
        return tileAssets.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
	}
}
