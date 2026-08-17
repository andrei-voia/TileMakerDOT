package tools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import core.TabData;
import localization.LocalizationManager;
import main.AssetPalette;
import main.EditorMenuBar;

public class SearchFieldFunction {

    //used for the filter button to search for textures by name
    private JTextField searchField = null;
    private JButton clearButton;
    private JLabel searchLabel;
    
    private EditorMenuBar editorMenuBar;
    private AssetPalette assetPalette;
    private JTabbedPane tileTabs;
    private JTabbedPane objectTabs;
    private JTabbedPane npcTabs;
    
    public SearchFieldFunction(EditorMenuBar editorMenuBar, AssetPalette assetPalette,
    	    JTabbedPane tileTabs, JTabbedPane objectTabs, JTabbedPane npcTabs) {
    	this.editorMenuBar = editorMenuBar;
    	this.assetPalette = assetPalette;
    	
    	this.tileTabs = tileTabs;
    	this.objectTabs = objectTabs;
    	this.npcTabs = npcTabs;
    	
    	initializeSearchField();
    }
    
    public void initializeSearchField() {
        //create the clear button
        clearButton = new JButton("X");
        clearButton.setFocusable(false); //keeps focus in search bar
        clearButton.setMargin(new Insets(2, 5, 2, 5)); //keep it compact
        clearButton.setVisible(false); //initially hidden
        
        //add clear functionality
        clearButton.addActionListener(e -> {
            searchField.setText("");
            searchField.requestFocusInWindow();
        });
        
        searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(300, 30));
        
        //disable legend shortcut while typing, shortcuts triggering
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
            	editorMenuBar.getMenuBar().setEnabled(false);
            }

            @Override
            public void focusLost(FocusEvent e) {
            	editorMenuBar.getMenuBar().setEnabled(true);
            }
        });
        
        //update visibility dynamically
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void insertUpdate(DocumentEvent e) { update(); }

            private void update() {
            	clearButton.setVisible(!searchField.getText().isEmpty());
            	
                updatePanelSearchVisibility(assetPalette.getObjectTabListClone(), objectTabs, searchField.getText());
                updatePanelSearchVisibility(assetPalette.getNpcsTabListClone(), npcTabs, searchField.getText());
                updatePanelSearchVisibility(assetPalette.getTilesTabListClone(), tileTabs, searchField.getText());
            }
        });
        
        //create the label
        searchLabel = new JLabel(LocalizationManager.getInstance().getString("menu_filter"));
        searchLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
    }
    
    private void updatePanelSearchVisibility(List<TabData> clonedTabs, JTabbedPane tabs, String query) {
    	//remember the title of the tab that is currently selected
        String selectedTitle = null;
        if (tabs.getSelectedIndex() != -1) {
            selectedTitle = tabs.getTitleAt(tabs.getSelectedIndex());
        }
    	
        //clean the current shown list of objects, NPCs, tiles
    	tabs.removeAll();

    	int indexToRestore = -1;
        int currentIndex = 0;
    	
        for (TabData data : clonedTabs) {
            //check if the tab content has visible items
            boolean hasVisibleItems = false;
                JPanel grid = (JPanel) data.getComponent().getViewport().getView();
                
                for (Component c : grid.getComponents()) {
                    if (c instanceof JButton btn) {
                        String name = btn.getToolTipText();
                        boolean visible = query.isEmpty() 
                            || (name != null && name.toLowerCase().contains(query.toLowerCase()));
                        
                        c.setVisible(visible);
                        if (visible) 
                        	hasVisibleItems = true;
                    }
                }
                grid.revalidate();
                grid.repaint();

                //only add the tab if it has visible items
                if (hasVisibleItems) {
                	tabs.addTab(data.getTitle(), data.getComponent());
                
                	//if this is the tab that was selected remember its new index
                	if (data.getTitle().equals(selectedTitle)) {
                		indexToRestore = currentIndex;
                	}
                	currentIndex++;
                }
        }
        
        //restore the selection if it still exists in the filtered list
        if (indexToRestore != -1) {
            tabs.setSelectedIndex(indexToRestore);
        } else if (tabs.getTabCount() > 0) {
            //if the selected tab was removed select the first one available
            tabs.setSelectedIndex(0);
        }
    }

	public JTextField getSearchField() {
		return searchField;
	}

	public JButton getClearButton() {
		return clearButton;
	}

	public JLabel getSearchLabel() {
		return searchLabel;
	}
}
