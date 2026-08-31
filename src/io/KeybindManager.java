package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.KeyStroke;

public class KeybindManager {

    private static KeybindManager instance;
    
    private static final String KEYBIND_FILE_PATH = "assets/settings/default_keybinds.txt";
    private static final String FACTORY_FILE_PATH = "assets/settings/factory_keybinds.txt";
    public static final String NONE = "None";
    
    private final Properties properties = new Properties();
    private final Map<String, KeyStroke> keyStrokes = new LinkedHashMap<>();

    private KeybindManager() {
        loadKeybinds();
    }

    public static KeybindManager getInstance() {
        if (instance == null) {
            instance = new KeybindManager();
        }
        return instance;
    }

    private void loadKeybinds() {
    	//keeps line by line order
        keyStrokes.clear();

        File configFile = new File(KEYBIND_FILE_PATH);
        if (!configFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                //split with limit 2 so "action.undo=" creates ["action.undo", ""]
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String actionKey = parts[0].trim();
                    String rawShortcut = parts[1].trim();
                    
                    //maps to null if empty
                    keyStrokes.put(actionKey, parseKeyStroke(rawShortcut)); 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //gets the key stroke from the string, including null value for not set shortcut keys
    private static KeyStroke parseKeyStroke(String keyString) {
        if (keyString == null || keyString.trim().isEmpty() || keyString.equalsIgnoreCase(NONE)) {
            return null;
        }
        
        String[] parts = keyString.trim().split("\\s+");
        if (parts.length > 0) {
            parts[parts.length - 1] = parts[parts.length - 1].toUpperCase();
        }
        
        return KeyStroke.getKeyStroke(String.join(" ", parts));
    }

    public KeyStroke getKeyStroke(String actionKey) {
        return keyStrokes.get(actionKey);
    }

    public void setKeybind(String actionKey, KeyStroke newKeyStroke) {
        keyStrokes.put(actionKey, newKeyStroke);
        properties.setProperty(actionKey, keyStrokeToString(newKeyStroke));
    }

    //save the key binds values
    public void saveKeybinds() {
        File configFile = new File(KEYBIND_FILE_PATH);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write("# TileMakerDOT Keybindings Configuration");
            writer.newLine();
            writer.newLine();

            writeSection(writer, "File Menu", new String[]{
                "action.load_map", "action.quick_save", "action.save_as",
                "action.import_spritesheet", "action.import_chunk", "action.export_chunk",
                "action.export_tmx", "action.export_lvl", "action.export_csv",
                "action.export_json", "action.export_ids", "action.export_png"
            });

            writeSection(writer, "Edit Menu", new String[]{
                "action.undo", "action.redo", "action.fill_map", "action.fill_empty",
                "action.extend_up", "action.extend_down", "action.extend_left", "action.extend_right",
                "action.refresh_assets", "action.auto_assign_ids", "action.keybind_settings"
            });

            writeSection(writer, "Tools Menu", new String[]{
                "action.notes_tool", "action.scatter_brush", "action.clean_brush",
                "action.chunk_selection", "action.cleanup_assets"
            });

            writeSection(writer, "Mode Menu", new String[]{
                "action.erase_mode", "action.npc_walk_area", "action.locate_item",
                "action.statistics_dashboard", "action.toggle_dark_mode"
            });

            writeSection(writer, "View Menu", new String[]{
                "action.toggle_tile_map", "action.toggle_object_map", "action.toggle_npc_map",
                "action.toggle_grid", "action.toggle_cursor", "action.toggle_placement",
                "action.toggle_autotile", "action.toggle_notes", "action.night_mode"
            });

            writeSection(writer, "Help Menu", new String[]{
                "action.legend"
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //write the values for the key strokes
    private void writeSection(BufferedWriter writer, String sectionTitle, String[] actionKeys) throws IOException {
        writer.write("# " + sectionTitle);
        writer.newLine();
        for (String actionKey : actionKeys) {
            KeyStroke ks = keyStrokes.get(actionKey);
            //if the key stroke value is null write an empty string after '='
            String shortcutStr = (ks != null) ? keyStrokeToString(ks) : ""; 
            writer.write(actionKey + "=" + shortcutStr);
            writer.newLine();
        }
        writer.newLine();
    }

    public static String keyStrokeToString(KeyStroke ks) {
    	//ensures setProperty receives "" rather than null
        if (ks == null) return "";
        
        StringBuilder sb = new StringBuilder();
        int modifiers = ks.getModifiers();
        
        if ((modifiers & java.awt.event.InputEvent.CTRL_DOWN_MASK) != 0) sb.append("ctrl ");
        if ((modifiers & java.awt.event.InputEvent.ALT_DOWN_MASK) != 0) sb.append("alt ");
        if ((modifiers & java.awt.event.InputEvent.SHIFT_DOWN_MASK) != 0) sb.append("shift ");
        if ((modifiers & java.awt.event.InputEvent.META_DOWN_MASK) != 0) sb.append("meta ");
        
        //always convert the key name to UPPERCASE for LEFT, RIGHT for example
        String keyName = java.awt.event.KeyEvent.getKeyText(ks.getKeyCode()).toUpperCase();
        sb.append(keyName);
        
        return sb.toString().trim();
    }
    
    public Set<String> getAllActionKeys() {
        return keyStrokes.keySet();
    }
    
    //reads and returns all the saved key binds from factory .txt save file
    public Map<String, KeyStroke> loadFactoryDefaults() {
        Map<String, KeyStroke> defaults = new LinkedHashMap<>();
        File factoryFile = new File(FACTORY_FILE_PATH);
        
        if (!factoryFile.exists()) {
            return defaults;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(factoryFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String actionKey = parts[0].trim();
                    String rawShortcut = parts[1].trim();
                    defaults.put(actionKey, parseKeyStroke(rawShortcut));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defaults;
    }
}
