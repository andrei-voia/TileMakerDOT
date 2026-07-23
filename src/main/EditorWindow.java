package main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import localization.LocalizationManager;
import utils.OSDetector;

public class EditorWindow {
	
	private TileEditor tileEditor;

	public EditorWindow(TileEditor tileEditor) {
		this.tileEditor = tileEditor;
	}
	
	//shows a custom dialog window to select grid size and input tile size at the beginning of the application
    public String[] showGridSizeSelectionDialog(List<String> defaultSizes, 
    		String tileSizeLoaded, String frameDuration) {
    	LocalizationManager loc = LocalizationManager.getInstance();
    	
        JTextField gridSizeField = new JTextField(defaultSizes.get(0), 10);
        JTextField tileSizeField = new JTextField(tileSizeLoaded + "", 5);
        JTextField animationDurationField = new JTextField(frameDuration + "", 5);
        
        //create the check box
        JCheckBox readIdUniqueCheckBox = new JCheckBox(loc.getString("startup_load_ids"));
        readIdUniqueCheckBox.setSelected(false);
        
        JTextField pathField = new JTextField(tileEditor.getLoadedSetup().getResourceBasePath(), 20);
        
        final AtomicBoolean buttonPressed = new AtomicBoolean(false);

        //panel having predefined grid size buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        for (String size : defaultSizes) {
            JButton btn = new JButton(size);
            btn.addActionListener(e -> {
                gridSizeField.setText(size);
                buttonPressed.set(true);
                //trigger closing the dialog
                Window window = SwingUtilities.getWindowAncestor(btn);
                if (window instanceof JDialog) {
                    JDialog dialog = (JDialog) window;
                    dialog.dispose();
                }
            });
            buttonPanel.add(btn);
        }

        //panel for manual grid size input
        JPanel gridSizePanel = new JPanel(new BorderLayout(5, 5));
        gridSizePanel.add(new JLabel(loc.getString("startup_grid_label")), BorderLayout.NORTH);
        gridSizePanel.add(gridSizeField, BorderLayout.CENTER);
        
        //panel for tile size input
        JPanel tileSizePanel = new JPanel(new BorderLayout(5, 5));
        tileSizePanel.add(new JLabel(loc.getString("startup_tile_label")), BorderLayout.NORTH);
        tileSizePanel.add(tileSizeField, BorderLayout.CENTER);
        
        //panel for the duration of the animation input
        JPanel animationDurationPanel = new JPanel(new BorderLayout(5, 5));
        animationDurationPanel.add(new JLabel(loc.getString("startup_anim_label")), BorderLayout.NORTH);
        animationDurationPanel.add(animationDurationField, BorderLayout.CENTER);

        //panel for assets path input
        JPanel pathPanel = new JPanel(new BorderLayout(5, 5));
        String osExample = switch (OSDetector.detectOS()) {
            case "Windows" -> "C:/MyAssets";
            case "macOS" -> "/Users/name/MyAssets";
            default -> "/home/name/MyAssets";
        };
        pathPanel.add(new JLabel(loc.getFormattedString("startup_path_label", osExample)), BorderLayout.NORTH);
        
        //add a button to open a JFileChooser for the directory
        JButton browseButton = new JButton(loc.getString("startup_browse"));
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                pathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        
        JPanel pathInputGroup = new JPanel(new BorderLayout(5, 5));
        pathInputGroup.add(pathField, BorderLayout.CENTER);
        pathInputGroup.add(browseButton, BorderLayout.EAST);
        pathPanel.add(pathInputGroup, BorderLayout.CENTER);
        
        //panel to combine all inputs
        JPanel mainInputPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        mainInputPanel.add(pathPanel);
        mainInputPanel.add(tileSizePanel);
        mainInputPanel.add(animationDurationPanel);
        mainInputPanel.add(gridSizePanel);
        mainInputPanel.add(readIdUniqueCheckBox);

        //panel for default size buttons
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.add(new JSeparator(), BorderLayout.NORTH);
        optionsPanel.add(new JLabel(loc.getString("startup_default_grid")), BorderLayout.CENTER);
        optionsPanel.add(buttonPanel, BorderLayout.SOUTH);

        //main dialog content panel
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        dialogPanel.add(mainInputPanel, BorderLayout.NORTH);
        dialogPanel.add(optionsPanel, BorderLayout.CENTER);

        JOptionPane optionPane = new JOptionPane(
            dialogPanel,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION
        );
        JDialog dialog = optionPane.createDialog(null, loc.getString("startup_title"));
        dialog.setVisible(true);

        int result = (optionPane.getValue() instanceof Integer) ? (int) optionPane.getValue() : JOptionPane.CLOSED_OPTION;

        if (result == JOptionPane.OK_OPTION || buttonPressed.get()) {
            //validate and return both inputs
            String gridSize = gridSizeField.getText().trim();
            String tileSize = tileSizeField.getText().trim();
            String assetPath = pathField.getText().trim(); //capture the new path
            String animationDuration = animationDurationField.getText().trim();
            
            //capture the check box state
            String readIdFlag = String.valueOf(readIdUniqueCheckBox.isSelected());
            
            tileEditor.getLoadedSetup().setOldResourceBasePath(tileEditor.getLoadedSetup().getResourceBasePath());
            //store the path for later use in loading methods
            tileEditor.getLoadedSetup().setResourceBasePath(assetPath);
            
            //basic validation for tile size
            if (!tileSize.matches("\\d+") || Integer.parseInt(tileSize) <= 0) {
                 JOptionPane.showMessageDialog(null, loc.getString("startup_invalid_tile"), loc.getString("startup_error_title"), JOptionPane.ERROR_MESSAGE);
                 return null;
            }
            
            //basic validation for animation speed
            if (!animationDuration.matches("\\d+") || Integer.parseInt(animationDuration) <= 0) {
                 JOptionPane.showMessageDialog(null, loc.getString("startup_invalid_anim"), loc.getString("startup_error_title"), JOptionPane.ERROR_MESSAGE);
                 return null;
            }
            
            return new String[]{gridSize, tileSize, readIdFlag, animationDuration};
        } else {
            return null;
        }
    }
}
