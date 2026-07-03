package utils;

import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

public class ApplicationLegend {
	
	private JScrollPane scrollPane;
	
	public ApplicationLegend() {
		initialize();
	}

	public void initialize() {
        String message = """
        	<html>
        	<body style='font-family: sans-serif; padding: 10px;'>
        	
            <h2 style='color: #2e86c1;'>Exclusive  🖱️ Mouse +  ⌨ Keyboard Shortcuts:</h2>
            <ul>
            <li><b>Shift / Alt + Left Mouse + Drag</b> → Start rectangle fill with selected tile</li>
            <li><b>Shift / Ctrl + Right Click</b> → Pick tile, object or NPC from canvas</li>
            <li><b>Right Click + Drag</b> → Pan the canvas</li>
            <li><b>Mouse Wheel</b> → Zoom in/out</li>
            <li><b>Left Click</b> → Paint tile, place object, or place NPC</li>
            <li><b>Left Click + Drag (on Object / NPC)</b> → Move object to new location</li>
            </ul>
            
            <hr>
            <h2 style='color: #27ae60;'>🎮 Engine & Tool Compatibility:</h2>
            </p><b>GameMaker:</b> Compatible via <b>.json</b> export (ideal for GML custom scripting)</p>
            <p>The <b>Export to Tiled (.tmx/.tsx)</b> feature produces industry-standard files compatible with:</p>
            <ul>
            <li><b>Tiled Map Editor:</b> Native support; files can be opened and edited directly in Tiled</li>
            <li><b>Godot Engine:</b> Compatible via the <b>'YATI (Yet Another Tiled Importer)'</b> plugin</li>
            <li><b>Unity:</b> Compatible via the <b>'SuperTiled2Unity'</b> package</li>
            <li><b>General:</b> Any game engine or framework that supports the XML-based TMX format</li>
            </ul>
            
            <hr>
            <h2 style='color: #2e86c1;'>✨  UI Features (All accessible via Menu Bar or shortcuts):</h2>
            
            <font size='5'><b>📁 File Menu:</b></font>
            <ul>
            <li><b>Load Map / Save As</b> → File operations</li>
            <li><b>Quick Save</b> → Quickly save the current map in the last saved file location</li>
            <li><b>Import from Spritesheet</b> → Slice a large image into individual tiles, objects or NPCs with auto-ID assignment</li>
            <li><b>Export / Import Chunk Selection</b> → Save a specific selected area to a file or load it into another map</li>
            <li><b>Export Map</b> → Save game-ready data in universal formats</li>
        	             Note: (*.csv, *.json, *.xml, *.tmx, *.tsx, *.tmj, *.lvl)
            <li><b>Export Used IDs List</b> → Save a list of only the used tiles, objects and NPCs on the map</li>
            <li><b>Export Image</b> → Save map as PNG image</li>
            </ul>
                        
            <font size='5'><b>🛠️ Edit Menu:</b></font>
            <ul>
            <li><b>Undo / Redo</b> → Revert/restore map changes</li>
            <li><b>Fill Entire Map</b> → Fill and replace all tiles for the entire map</li>
            <li><b>Fill Empty Tiles</b> → Fill only blank areas with selected tile</li>
            <li><b>Extend map</b> → Extends or reduces the map size in one of the four directions</li>
        	             Note: Undo (Ctrl + Z) will not work after extending the map
            <li><b>Refresh Assets</b> → Re-scans the folders and reloads textures</li>
            <li><b>Auto-Assign Missing IDs</b> → Automatically fixes files missing "ID_" prefixes by finding the next available ID</li>
            </ul>
                        
            <font size='5'><b>🖌️ Tools Menu:</b></font>
            <ul>
            <li><b>Annotated Notes Tool</b> → Place custom color coded map markers with custom labels to flag boundaries, write script reminders, or map out level design notes directly on the grid</li>
            <li><b>Custom Objects Scatter Brush</b> → Paint multiple different objects randomly at once in the selected area</li>
        	             Note: Select (Ctrl + LClick) objects in the 'Objects' palette first to add them to your brush
            <li><b>Spread</b> → Distribute objects across a wider area with variable density</li>
            <li><b>Cleanup Brush Selection</b> → Clears the current selections of the multiple objects for the brush</li>
            <li><b>Chunk Selection Tool</b> → Select a rectangular area for mass moving, copying, or exporting</li>
            <li><b>Cleanup Missing Assets</b> → Removes references on the map to files that no longer exist in the resource folders</li>
            </ul>
            
            <font size='5'><b>💡 Mode Menu:</b></font>
            <ul>
            <li><b>Erase Object / NPC Mode</b> → Remove placed items (objects and NPCs)</li>
            <li><b>Place NPC Walk Area</b> → Set a boundary where NPCs can walk</li>
        	             Note: [Shift + Left Mouse + Drag → place, ALT + Left Mouse + Draw → remove]
            <li><b>Locate Item</b> → Shows only the currently selected Tile, Object, or NPC on the map</li>
            <li><b>Scene Items Count</b> → Shows the total number of Tiles, Objects, and NPCs added on the map</li>
            <li><b>Toggle Dark Mode</b> → Switches the entire UI between Light and Dark themes for better visibility in different environments</li>
            </ul>
            
            <font size='5'><b>👁️ View Menu:</b></font>
            <ul>
            <li><b>Toggle Tile Map</b> → Show/hide tiles map</li>
            <li><b>Toggle Object Map</b> → Show/hide objects map</li>
            <li><b>Toggle NPC Map</b> → Show/hide NPCs map</li>
            <li><b>Toggle Grid</b> → Show/hide grid lines</li>
            <li><b>Toggle Cursor Position</b> → Show/hide the tile coordinates at mouse pointer</li>
            <li><b>Toggle Live Placement</b> → Show/hide the item preview before placing on the map</li>
            <li><b>Toggle Autotile</b> → Show/hide the automatic border and corner correction</li>
            <li><b>Toggle Annotated Notes</b> → Show/hide placed annotation notes and markers on the map</li>
            <li><b>Preview Night Mode</b> → Show/hide simulated night time lighting on the map</li>
            </ul>
            
            <font size='5'><b>📜 Help Menu:</b></font>
            <ul>
            <li><b>Legend</b> → Show this shortcut guide</li>
            <li><b>Video Tutorials</b> → Learn how to build maps with the official video series</li>
            <li><b>About</b> → Details about TileMaker DOT</li>
            </ul>
            
            <font size='5'><b>📺 UI information:</b></font>
            <ul>
            <li><b>Filter Assets</b> → Type here to quickly hide or show specific Tiles, Objects, or NPCs based on their name</li>
            <li><b>Selection Preview</b> → Shows thumbnail of selected Tile, Object, or NPC</li>
            <li><b>Status Bar / Mode</b> → Displays active mode, names and IDs of selected items</li>
            <li><b>Palette Highlight</b> → Colored border around selected item button for tiles, objects and NPCs</li>
            </ul>
            
            </body>
        	</html>
            """;
        
        JEditorPane editPane = new JEditorPane();
        editPane.setContentType("text/html");
        editPane.setText(message);
        editPane.setEditable(false);
        //ensure it starts at the top
        editPane.setCaretPosition(0);

        //wrap in JScrollPane
        scrollPane = new JScrollPane(editPane);
        scrollPane.setPreferredSize(new Dimension(800, 600));
	}
	
	public void showLegend(JFrame frame) {
        //show dialog
        JOptionPane.showMessageDialog(frame, scrollPane, "Application Legend", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void showAbout(JFrame frame) {
		String aboutMessage = "<html><body style='width: 260px; padding: 10px;'>" +
			    "<h1 style='color: #58a6ff; margin-bottom: 0;'>TileMaker DOT</h1>" + 
			    "<p style='margin-top: 0;'>Professional Map Engineering Tool</p>" +
			    "<p>Created by <b>Andrei Voia</b></p>" +
			    "<br><hr>" +
			    "<p style='font-size: 9px; color: gray;'>" +
			    "Dark Mode powered by <b>FlatLaf</b> technology.<br>" +
			    "Licensed under Apache License 2.0." +
			    "</p></body></html>";
			JOptionPane.showMessageDialog(frame, aboutMessage, "About TileMaker DOT", JOptionPane.PLAIN_MESSAGE);
	}
}
