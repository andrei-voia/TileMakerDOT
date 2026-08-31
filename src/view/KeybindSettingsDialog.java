package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import io.KeybindManager;
import localization.LocalizationManager;

public class KeybindSettingsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private final KeybindManager keybindManager = KeybindManager.getInstance();
	//new key binds that will be saved after saving this window
    private final Map<String, KeyStroke> pendingKeybinds = new HashMap<>();
    private final List<String> rowActionKeys = new ArrayList<>();
    
    private final DefaultTableModel tableModel;
    private final JButton saveBtn;
    
    private LocalizationManager loc = LocalizationManager.getInstance();

    public KeybindSettingsDialog(JFrame parent, Runnable onSaveCallback) {
        super(parent, LocalizationManager.getInstance().getString("menu_keybind_settings"), true);

        setSize(450, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        //table setup
        String[] columnNames = {loc.getString("keybind_action"), loc.getString("keybind_shortcut")};
        tableModel = new DefaultTableModel(columnNames, 0) {
			private static final long serialVersionUID = 1L;
			@Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        
        //this adds double click listener to table rows so we can modify the values
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1 && selectedRow < rowActionKeys.size()) {
                        String actionKey = rowActionKeys.get(selectedRow);
                        promptNewKeybind(actionKey, selectedRow);
                    }
                }
            }
        });

        loadCurrentKeybinds();
        add(new JScrollPane(table), BorderLayout.CENTER);

        //control panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton restoreBtn = new JButton(loc.getString("keybind_restore_default"));
        saveBtn = new JButton(loc.getString("keybind_Save"));
        JButton cancelBtn = new JButton(loc.getString("button_cancel"));

        saveBtn.setEnabled(false);

        saveBtn.addActionListener(e -> {
            for (Map.Entry<String, KeyStroke> entry : pendingKeybinds.entrySet()) {
                keybindManager.setKeybind(entry.getKey(), entry.getValue());
            }
            keybindManager.saveKeybinds();

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            dispose();
        });

        //set the restore button for the factory shortcut values
        restoreBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                loc.getString("keybind_restore_message"),
                loc.getString("keybind_restore_title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                //load default mapping into pending state
                Map<String, KeyStroke> defaults = keybindManager.loadFactoryDefaults();
                if (!defaults.isEmpty()) {
                    pendingKeybinds.clear();
                    pendingKeybinds.putAll(defaults);

                    //update UI table
                    for (int i = 0; i < rowActionKeys.size(); i++) {
                        String actionKey = rowActionKeys.get(i);
                        KeyStroke ks = pendingKeybinds.get(actionKey);
                        String display = (ks != null) ? KeybindManager.keyStrokeToString(ks) : KeybindManager.NONE;
                        tableModel.setValueAt(display, i, 1);
                    }

                    saveBtn.setEnabled(true);
                }
            }
        });
        
        cancelBtn.addActionListener(e -> dispose());

        bottomPanel.add(restoreBtn);
        bottomPanel.add(saveBtn);
        bottomPanel.add(cancelBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCurrentKeybinds() {
        pendingKeybinds.clear();
        rowActionKeys.clear();
        tableModel.setRowCount(0);

        for (String actionKey : keybindManager.getAllActionKeys()) {
            KeyStroke ks = keybindManager.getKeyStroke(actionKey);
            pendingKeybinds.put(actionKey, ks);
            rowActionKeys.add(actionKey);
            
            String shortcutDisplay = (ks != null) ? KeybindManager.keyStrokeToString(ks) : KeybindManager.NONE;
            tableModel.addRow(new Object[]{getActionDisplayName(actionKey), shortcutDisplay});
        }
    }

    private String getActionDisplayName(String actionKey) {
        LocalizationManager loc = LocalizationManager.getInstance();
        switch (actionKey) {
            case "action.load_map": return loc.getString("menu_load_map");
            case "action.quick_save": return loc.getString("menu_quick_save");
            case "action.save_as": return loc.getString("menu_save_as");
            case "action.import_spritesheet": return loc.getString("menu_import_spritesheet");
            case "action.import_chunk": return loc.getString("menu_import_chunk");
            case "action.export_chunk": return loc.getString("menu_export_chunk");
            case "action.export_tmx": return loc.getString("menu_export_tmx");
            case "action.export_lvl": return loc.getString("menu_export_lvl");
            case "action.export_csv": return loc.getString("menu_export_csv");
            case "action.export_json": return loc.getString("menu_export_json");
            case "action.export_ids": return loc.getString("menu_export_ids");
            case "action.export_png": return loc.getString("menu_export_png");
            
            case "action.undo": return loc.getString("menu_undo");
            case "action.redo": return loc.getString("menu_redo");
            case "action.fill_map": return loc.getString("menu_fill_map");
            case "action.fill_empty": return loc.getString("menu_fill_empty");
            case "action.extend_up": return loc.getString("menu_extend_up");
            case "action.extend_down": return loc.getString("menu_extend_down");
            case "action.extend_left": return loc.getString("menu_extend_left");
            case "action.extend_right": return loc.getString("menu_extend_right");
            case "action.refresh_assets": return loc.getString("menu_refresh_assets");
            case "action.auto_assign_ids": return loc.getString("menu_auto_assign_ids");
            case "action.keybind_settings": return loc.getString("menu_keybind_settings");
            
            case "action.notes_tool": return loc.getString("menu_notes_tool");
            case "action.scatter_brush": return loc.getString("menu_scatter_brush");
            case "action.clean_brush": return loc.getString("menu_clean_brush");
            case "action.chunk_selection": return loc.getString("menu_chunk_selection");
            case "action.cleanup_assets": return loc.getString("menu_cleanup_assets");

            case "action.erase_mode": return loc.getString("menu_erase_mode");
            case "action.npc_walk_area": return loc.getString("menu_npc_walk_area");
            case "action.locate_item": return loc.getString("menu_locate_item");
            case "action.statistics_dashboard": return loc.getString("menu_statistics_dashboard");
            case "action.toggle_dark_mode": return loc.getString("menu_toggle_dark_mode");
            
            case "action.toggle_tile_map": return loc.getString("menu_toggle_tile_map");
            case "action.toggle_object_map": return loc.getString("menu_toggle_object_map");
            case "action.toggle_npc_map": return loc.getString("menu_toggle_npc_map");
            case "action.toggle_grid": return loc.getString("menu_toggle_grid");
            case "action.toggle_cursor": return loc.getString("menu_toggle_cursor");
            case "action.toggle_placement": return loc.getString("menu_toggle_placement");
            case "action.toggle_autotile": return loc.getString("menu_toggle_autotile");
            case "action.toggle_notes": return loc.getString("menu_toggle_notes");
            case "action.night_mode": return loc.getString("menu_night_mode");
            
            case "action.legend": return loc.getString("menu_legend");
            
            default: return actionKey;
        }
    }

    //creates a window for changing the value of a key bind
    private void promptNewKeybind(String actionKey, int row) {
        JDialog captureDialog = new JDialog(this, loc.getString("keybind_new_key_title"), true);
        
        captureDialog.setLocationRelativeTo(this);
        
        JLabel label = new JLabel(loc.getString("keybind_new_key_message"), SwingConstants.CENTER);
        //add padding around the label so the text is not flush against the window borders
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 20, 30));
        captureDialog.add(label);
        
        //calculates window dimensions dynamically based on label content
        captureDialog.pack();
        

        captureDialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                
                //allow unbinding (removing) by pressing backspace or delete
                if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
                    pendingKeybinds.put(actionKey, null);
                    tableModel.setValueAt(KeybindManager.NONE, row, 1);
                    saveBtn.setEnabled(true);
                    captureDialog.dispose();
                    return;
                }

                if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_SHIFT || 
                    keyCode == KeyEvent.VK_ALT || keyCode == KeyEvent.VK_META) {
                    return;
                }

                //set the new key
                KeyStroke newKs = KeyStroke.getKeyStroke(keyCode, e.getModifiersEx());

                //check to see if it is already the same
                KeyStroke existingKs = pendingKeybinds.get(actionKey);
                if (newKs.equals(existingKs)) {
                    captureDialog.dispose();
                    return;
                }

                //check to see if the key is already used somewhere else in another key command
                String conflictingAction = null;
                for (Map.Entry<String, KeyStroke> entry : pendingKeybinds.entrySet()) {
                    if (!entry.getKey().equals(actionKey) && newKs.equals(entry.getValue())) {
                        conflictingAction = entry.getKey();
                        break;
                    }
                }

                if (conflictingAction != null) {
                    JOptionPane.showMessageDialog(
                        captureDialog,
                        loc.getFormattedString("keybind_duplicate_key_message", 
                        		KeybindManager.keyStrokeToString(newKs), 
                        		getActionDisplayName(conflictingAction)),
                        loc.getString("keybind_duplicate_key_title"),
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                pendingKeybinds.put(actionKey, newKs);
                tableModel.setValueAt(KeybindManager.keyStrokeToString(newKs), row, 1);
                saveBtn.setEnabled(true);
                
                captureDialog.dispose();
            }
        });

        captureDialog.setVisible(true);
    }
}
