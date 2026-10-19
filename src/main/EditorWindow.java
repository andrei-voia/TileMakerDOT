package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import localization.LocalizationManager;
import utils.OSDetector;
import utils.Utils;

public class EditorWindow {
	
	private TileEditor tileEditor;

	public EditorWindow(TileEditor tileEditor) {
		this.tileEditor = tileEditor;
	}
	
	//show a custom dialog to configure map settings and grid sizes
    public String[] showGridSizeSelectionDialog(List<String> defaultSizes, 
    		String tileSizeLoaded, String frameDuration) {
    	LocalizationManager loc = LocalizationManager.getInstance();
    	
        JTextField gridSizeField = createStyledTextField(defaultSizes.get(0));
        JTextField tileSizeField = createStyledTextField(tileSizeLoaded);
        JTextField animationDurationField = createStyledTextField(frameDuration);
        JTextField pathField = createStyledTextField(tileEditor.getLoadedSetup().getResourceBasePath());
        
        //check box for unique IDs
        JCheckBox readIdUniqueCheckBox = new JCheckBox(loc.getString("startup_load_ids"));
        readIdUniqueCheckBox.setFont(readIdUniqueCheckBox.getFont().deriveFont(13f));
        boolean initialActiveState = Utils.loadIDFilterState(tileEditor.getLoadedSetup().getResourceBasePath());
        readIdUniqueCheckBox.setSelected(initialActiveState);

        //preset card highlights configuration
        List<JButton> cardButtons = new ArrayList<>();
        Border defaultBorder = new JButton().getBorder();
        //set a custom border color for selected grid cards
        Border selectedBorder = BorderFactory.createLineBorder(new Color(64, 156, 255), 2);

        Runnable updateCardHighlights = () -> {
            String currentGrid = gridSizeField.getText().trim();
            for (JButton btn : cardButtons) {
                String sizeStr = (String) btn.getClientProperty("presetSize");
                if (currentGrid.equalsIgnoreCase(sizeStr)) {
                    btn.setBorder(selectedBorder);
                } else {
                    btn.setBorder(defaultBorder);
                }
            }
        };

        //left panel with the scroll preset grid cards
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBorder(new EmptyBorder(10, 5, 10, 5));
        leftPanel.setPreferredSize(new Dimension(300, 400));
        
        JLabel presetTitle = new JLabel(loc.getString("startup_default_grid").toUpperCase());
        presetTitle.setFont(presetTitle.getFont().deriveFont(Font.BOLD, 12f));
        presetTitle.setBorder(new EmptyBorder(0, 5, 4, 0));
        leftPanel.add(presetTitle, BorderLayout.NORTH);
        
        JPanel gridCardsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        for (String size : defaultSizes) {
            JButton cardBtn = new JButton(
                "<html><center><font size='4'><b>" + 
            size + "</b></font><br><font color='#888888' size='2'>" + 
            loc.getString("startup_grid_layout") + "</font></center></html>"
            );
            cardBtn.setPreferredSize(new Dimension(125, 85));
            cardBtn.setFocusPainted(false);
            cardBtn.putClientProperty("presetSize", size);

            //select preset without auto closing the dialog and auto complete it in size
            cardBtn.addActionListener(e -> {
                gridSizeField.setText(size);
                updateCardHighlights.run();
            });

            cardButtons.add(cardBtn);
            gridCardsPanel.add(cardBtn);
        }

        //highlight default selected card on open
        updateCardHighlights.run();

        //top align cards so vertical height does not stretch when presets are few
        JPanel scrollContent = new JPanel(new BorderLayout());
        scrollContent.add(gridCardsPanel, BorderLayout.NORTH);

        //scroll pane for endless preset items
        JScrollPane presetScrollPane = new JScrollPane(scrollContent);
        presetScrollPane.setBorder(BorderFactory.createEmptyBorder());
        presetScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        presetScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        presetScrollPane.getVerticalScrollBar().setUnitIncrement(14);

        leftPanel.add(presetScrollPane, BorderLayout.CENTER);

        //right panel is for map configuration
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(80, 80, 80, 80)),
            new EmptyBorder(10, 18, 10, 10)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        int row = 0;

        //section title
        JLabel detailsTitle = new JLabel(loc.getString("startup_preset_details").toUpperCase());
        detailsTitle.setFont(detailsTitle.getFont().deriveFont(Font.BOLD, 12f));
        gbc.insets = new Insets(0, 2, 12, 2);
        gbc.gridx = 0; gbc.gridy = row++;
        rightPanel.add(detailsTitle, gbc);

        //asset path input
        String osExample = switch (OSDetector.detectOS()) {
            case "Windows" -> "C:/MyAssets";
            case "macOS" -> "/Users/name/MyAssets";
            default -> "/home/name/MyAssets";
        };
        
        addFormLabel(rightPanel, loc.getFormattedString("startup_path_label", osExample), gbc, row++);
        
        JButton browseButton = new JButton(loc.getString("startup_browse"));
        browseButton.setPreferredSize(new Dimension(95, 34));
        browseButton.setFocusPainted(false);
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                pathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        
        JPanel pathGroup = new JPanel(new BorderLayout(8, 0));
        pathGroup.add(pathField, BorderLayout.CENTER);
        pathGroup.add(browseButton, BorderLayout.EAST);
        
        gbc.insets = new Insets(0, 2, 10, 2);
        gbc.gridx = 0; gbc.gridy = row++;
        rightPanel.add(pathGroup, gbc);

        //tile size field
        addFormLabel(rightPanel, loc.getString("startup_tile_label"), gbc, row++);
        gbc.insets = new Insets(0, 2, 10, 2);
        gbc.gridx = 0; gbc.gridy = row++;
        rightPanel.add(tileSizeField, gbc);

        //animation duration field
        addFormLabel(rightPanel, loc.getString("startup_anim_label"), gbc, row++);
        gbc.insets = new Insets(0, 2, 10, 2);
        gbc.gridx = 0; gbc.gridy = row++;
        rightPanel.add(animationDurationField, gbc);

        //custom grid size field
        addFormLabel(rightPanel, loc.getString("startup_grid_label"), gbc, row++);
        gbc.insets = new Insets(0, 2, 12, 2);
        gbc.gridx = 0; gbc.gridy = row++;
        rightPanel.add(gridSizeField, gbc);

        //load id field
        gbc.insets = new Insets(4, 2, 10, 2);
        gbc.gridx = 0; gbc.gridy = row++;
        rightPanel.add(readIdUniqueCheckBox, gbc);

        //main dialog wrapper for left and right
        JPanel mainContainer = new JPanel(new BorderLayout(15, 0));
        mainContainer.setBorder(new EmptyBorder(12, 12, 12, 12));
        mainContainer.setPreferredSize(new Dimension(740, 440));
        mainContainer.add(leftPanel, BorderLayout.WEST);
        mainContainer.add(rightPanel, BorderLayout.CENTER);
        
        Utils.initLocalizedDialogs();
        
        JOptionPane optionPane = new JOptionPane(
            mainContainer,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION
        );
        
        JDialog dialog = optionPane.createDialog(tileEditor.getMainFrame(), loc.getString("startup_title"));
        dialog.setResizable(false);
        dialog.pack();
        dialog.setVisible(true);

        int result = (optionPane.getValue() instanceof Integer) ? (int) optionPane.getValue() : JOptionPane.CLOSED_OPTION;

        if (result == JOptionPane.OK_OPTION) {
            String gridSize = gridSizeField.getText().trim();
            String tileSize = tileSizeField.getText().trim();
            String assetPath = pathField.getText().trim();
            String animationDuration = animationDurationField.getText().trim();
            String readIdFlag = String.valueOf(readIdUniqueCheckBox.isSelected());
            
            tileEditor.getLoadedSetup().setOldResourceBasePath(tileEditor.getLoadedSetup().getResourceBasePath());
            tileEditor.getLoadedSetup().setResourceBasePath(assetPath);
            
            if (!tileSize.matches("\\d+") || Integer.parseInt(tileSize) <= 0) {
                 JOptionPane.showMessageDialog(dialog, loc.getString("startup_invalid_tile"), loc.getString("startup_error_title"), JOptionPane.ERROR_MESSAGE);
                 return null;
            }
            
            if (!animationDuration.matches("\\d+") || Integer.parseInt(animationDuration) <= 0) {
                 JOptionPane.showMessageDialog(dialog, loc.getString("startup_invalid_anim"), loc.getString("startup_error_title"), JOptionPane.ERROR_MESSAGE);
                 return null;
            }
            
            //save the state of the loading IDs file
            Utils.saveIDFilterState(assetPath, readIdUniqueCheckBox.isSelected());
            
            return new String[]{gridSize, tileSize, readIdFlag, animationDuration};
        } else {
            return null;
        }
    }

    private JTextField createStyledTextField(String initialText) {
        JTextField field = new JTextField(initialText);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 34));
        field.setFont(field.getFont().deriveFont(13f));
        return field;
    }

    private void addFormLabel(JPanel panel, String text, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(12f));
        gbc.insets = new Insets(4, 2, 2, 2);
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(label, gbc);
    }
}
