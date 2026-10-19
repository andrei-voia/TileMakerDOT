package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import localization.LocalizationManager;
import utils.OSDetector;
import utils.Utils;

public class AssetPathWindow {

    private String defaultPath;
    private int mapTileSize = -1;
    private int assetTileSize = -1;
    
    private TileEditor tileEditor;

    public AssetPathWindow(String defaultPath, int mapTileSize, int assetTileSize, TileEditor tileEditor) {
        this.defaultPath = defaultPath;
        this.mapTileSize = mapTileSize;
        this.assetTileSize = assetTileSize;
        
        this.tileEditor = tileEditor;
    }

    public String[] showPathSelectionDialog() {
        LocalizationManager loc = LocalizationManager.getInstance();

        JTextField pathField = new JTextField(defaultPath != null ? defaultPath : "", 20);
        JTextField tileSizeField = new JTextField(mapTileSize > 0 ? String.valueOf(mapTileSize) : "", 5);

        //panel for tile size input
        JPanel tileSizePanel = new JPanel(new BorderLayout(5, 5));
        tileSizePanel.add(new JLabel(loc.getString("startup_tile_label")), BorderLayout.NORTH);
        tileSizePanel.add(tileSizeField, BorderLayout.CENTER);

        //panel for assets path input
        JPanel pathPanel = new JPanel(new BorderLayout(5, 5));
        String osExample = switch (OSDetector.detectOS()) {
            case "Windows" -> "C:/MyAssets";
            case "macOS" -> "/Users/name/MyAssets";
            default -> "/home/name/MyAssets";
        };
        pathPanel.add(new JLabel(loc.getFormattedString("startup_path_label", osExample)), BorderLayout.NORTH);

        //add browse button with file chooser
        JButton browseButton = new JButton(loc.getString("startup_browse"));
        browseButton.setPreferredSize(new Dimension(88, 34));
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            String currentText = pathField.getText().trim();
            if (!currentText.isEmpty()) {
                chooser.setCurrentDirectory(new File(currentText));
            }
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

        //main input container combining path and tile size inputs
        JPanel mainInputPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        mainInputPanel.add(pathPanel);
        mainInputPanel.add(tileSizePanel);

        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        //add warning message panel if tile sizes mismatch
        if (hasTileSizeMismatch()) {
            JPanel warningPanel = createWarningPanel(loc);
            dialogPanel.add(warningPanel, BorderLayout.NORTH);
        }

        dialogPanel.add(mainInputPanel, BorderLayout.CENTER);

        //localize option buttons and file chooser
        Utils.initLocalizedDialogs();

        JOptionPane optionPane = new JOptionPane(
            dialogPanel,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION
        );

        JDialog dialog = optionPane.createDialog(tileEditor.getMainFrame(), loc.getString("startup_title"));
        dialog.setVisible(true);

        int result = (optionPane.getValue() instanceof Integer) ? (int) optionPane.getValue() : JOptionPane.CLOSED_OPTION;

        if (result == JOptionPane.OK_OPTION) {
            String tileSize = tileSizeField.getText().trim();
            String assetPath = pathField.getText().trim();

            //validation for tile size
            if (!tileSize.matches("\\d+") || Integer.parseInt(tileSize) <= 0) {
                 JOptionPane.showMessageDialog(dialog, loc.getString("startup_invalid_tile"), loc.getString("startup_error_title"), JOptionPane.ERROR_MESSAGE);
                 return null;
            }

            return new String[]{ tileSize, assetPath };
        }

        return null;
    }

    private boolean hasTileSizeMismatch() {
        return mapTileSize > 0 && assetTileSize > 0 && mapTileSize != assetTileSize;
    }

	private JPanel createWarningPanel(LocalizationManager loc) {
		JPanel panel = new JPanel(new BorderLayout());
		//a yellow warning
		panel.setBackground(new Color(50, 42, 24));
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(140, 105, 35), 1),
				new EmptyBorder(10, 12, 10, 12)));

		String warningMessage = loc.getFormattedString("startup_tilesize_mismatch_warn", assetTileSize, mapTileSize);

		JLabel warningLabel = new JLabel(
				"<html><body style='width: 310px; color: #EAD08B;'>" + warningMessage + "</body></html>");
		warningLabel.setFont(warningLabel.getFont().deriveFont(12f));

		panel.add(warningLabel, BorderLayout.CENTER);
		return panel;
	}
}
