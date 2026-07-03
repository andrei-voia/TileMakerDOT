package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import data.CanvasViewState;
import data.MapNote;
import data.MapState;
import data.NoteColor;
import tools.ChunkSelectionTool;
import utils.Utils;
import view.CanvasRenderer;
import view.TileCanvas;

public class MapLoader {

	private static final String TILE_LAYER = "# Tile Layer";
	private static final String OBJECT_LAYER = "# Object Layer";
	private static final String NPC_LAYER = "# NPC Layer";
	private static final String NPC_WALK_AREA_LAYER = "# NPC Walk Area Layer";
	
	private TileCanvas tileCanvas;
	private MapState mapState;
	private ChunkSelectionTool chunkSelectionTool;
	private CanvasViewState canvasViewState;
	private CanvasRenderer canvasRenderer;
	
	public MapLoader(TileCanvas tileCanvas, MapState mapState, 
			ChunkSelectionTool chunkSelectionTool, CanvasViewState canvasViewState,
			CanvasRenderer canvasRenderer) {
		this.tileCanvas = tileCanvas;
		this.mapState = mapState;
		this.chunkSelectionTool = chunkSelectionTool;
		this.canvasViewState = canvasViewState;
		this.canvasRenderer = canvasRenderer;
	}
	
	public void loadChunk(File file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {

			//read dimensions
			String line = br.readLine();
			if (line == null) {
				throw new IOException("Map file is empty or corrupted.");
			}
			String[] dims = line.split(",");
			if (dims.length < 2) {
				throw new IOException("Missing map dimensions in the file.");
			}

			//reading new map sizes from the loaded file
			int rows = Integer.parseInt(dims[0].trim());
			int cols = Integer.parseInt(dims[1].trim());
			
			//read the tile size
			line = br.readLine();
			line = br.readLine();
			canvasRenderer.setTileSize(Integer.parseInt(line.trim()));
			
			//initialize a new size for the map chunk where it will load the new IDs values
			chunkSelectionTool.newMapChunk(cols, rows);

			//read tile Layer
			readLayer(br, chunkSelectionTool.getMapChunk().getTiles(), rows, cols, TILE_LAYER);
			//read object Layer
			readLayer(br, chunkSelectionTool.getMapChunk().getObjects(), rows, cols, OBJECT_LAYER);
			//read NPC Layer
			readLayer(br, chunkSelectionTool.getMapChunk().getNpcs(), rows, cols, NPC_LAYER);
			//read NPC Walk Area Layer
			readLayer(br, chunkSelectionTool.getMapChunk().getWalkArea(), rows, cols, NPC_WALK_AREA_LAYER);

			tileCanvas.getSelected().setChunkSelectionTool();
			
			tileCanvas.getTileEditor().getStatusInfoBar().updateStatusUI();

			JOptionPane.showMessageDialog(null, "Chunk imported successfully from " + file.getName(), "Load Success",
					JOptionPane.INFORMATION_MESSAGE);

		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "File not found.", "Load Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error reading map data: " + e.getMessage(), "Load Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Map file contains non-numeric data.", "Load Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	public void loadMap(File file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {

			//read dimensions
			String line = br.readLine();
			if (line == null) {
				throw new IOException("Map file is empty or corrupted.");
			}
			String[] dims = line.split(",");
			if (dims.length < 2) {
				throw new IOException("Missing map dimensions in the file.");
			}

			//reading new map sizes from the loaded file
			mapState.getData().setRows(Integer.parseInt(dims[0].trim()));
			mapState.getData().setCols(Integer.parseInt(dims[1].trim()));
			
			//read the tile size
			line = br.readLine();
			line = br.readLine();
			canvasRenderer.setTileSize(Integer.parseInt(line.trim()));

			//here we reset the current maps to the new loaded file sizes
			mapState.getData().cleanMap();

			//read tile layer
			readLayer(br, mapState.getData().getTileMap(), 
					mapState.getData().getRows(), 
					mapState.getData().getCols(), TILE_LAYER);
			//read object layer
			readLayer(br, mapState.getData().getObjectMap(), 
					mapState.getData().getRows(), 
					mapState.getData().getCols(), OBJECT_LAYER);
			//read NPC layer
			readLayer(br, mapState.getData().getNpcMap(), 
					mapState.getData().getRows(), 
					mapState.getData().getCols(), NPC_LAYER);
			//read NPC walk area layer
			readLayer(br, mapState.getData().getNpcWalkAreaMap(), 
					mapState.getData().getRows(), 
					mapState.getData().getCols(), NPC_WALK_AREA_LAYER);

			//read the annotation notes layer
			readAnnotationNotes(br);
			
			//save new cached location from last loaded file
			mapState.getCacheData().setCachedSavedLocation(file);
			mapState.getCacheData().setCanQuickSave(false);
			
			tileCanvas.getTileEditor().getStatusInfoBar().updateStatusUI();

			tileCanvas.getCamera().calculateZoomValues(mapState.getData().getRows(), mapState.getData().getCols(), 
					canvasRenderer.getTileSize(), true);
			
			//if the autoTile is not active then do not reset autotileMap to save time
			if (canvasViewState.isShowAutotile())
				mapState.refreshAutotileMap();
			mapState.refreshAllObjectsList();
			tileCanvas.getHistoryFunction().resetSaveState();
			
			canvasRenderer.repaint();
			JOptionPane.showMessageDialog(null, "Map loaded successfully from " + file.getName(), "Load Success",
					JOptionPane.INFORMATION_MESSAGE);

		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "File not found.", "Load Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error reading map data: " + e.getMessage(), "Load Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Map file contains non-numeric data.", "Load Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	private void readAnnotationNotes(BufferedReader br) throws IOException {
		//clear the array of previous annotation notes if exists from previous session
		if (tileCanvas.getAnnotatedNotesTool() != null) {
			tileCanvas.getAnnotatedNotesTool().getMapNotes().clear();

			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				
				if (line.isEmpty()) {
					continue;
				}
				
				//split by comma up to 4 parts ensuring custom notes with commas inside works
				String[] tokens = line.split(",", 4);
				if (tokens.length >= 4) {
					try {
						int col = Integer.parseInt(tokens[0].trim());
						int row = Integer.parseInt(tokens[1].trim());

						//build the NoteColor enum instance safely matching by name
						NoteColor noteColor;
						try {
							noteColor = NoteColor.valueOf(tokens[2].trim());
						} catch (IllegalArgumentException e) {
							noteColor = NoteColor.YELLOW; //fallback to a default value
						}

						String textMessage = tokens[3];

						//add new notes to the list of active notes
						tileCanvas.getAnnotatedNotesTool().getMapNotes()
								.add(new MapNote(col, row, textMessage, noteColor));
					} catch (NumberFormatException e) {
						System.err.println(
								"Note parsing skipped: invalid row/col structure inside row line: " + line);
					}
				}
			}
		}
	}

	//Helper method to read a single layer (tileMap, objectMap, or npcMap) from the BufferedReader
	private void readLayer(BufferedReader br, int[][] mapArray, int rows, int cols, String expectedHeader)
			throws IOException {
		String line = br.readLine();
		
		StringBuilder errorMessage = new StringBuilder("The following IDs in '" + expectedHeader + "' were not found and will be ignored:\n");
		Set<Integer> missingIDs = new HashSet<>();
		
		//skip comment lines (like the header)
		while (line != null && line.startsWith("#")) {
			line = br.readLine();
		}

		if (line == null) {
			throw new IOException("File ended unexpectedly while reading layer data.");
		}

		//read the map data row by row
		for (int r = 0; r < rows; r++) {
			//if line is null we ran out of data prematurely
			if (line == null) {
				throw new IOException("Incomplete data for map layer starting at row " + r);
			}

			//split the line by comma
			String[] indices = line.split(",");
			if (indices.length != cols) {
				throw new IOException(
						String.format("Row %d has %d indices, but expected %d.", r, indices.length, cols));
			}

			//parse each index and store it
			for (int c = 0; c < cols; c++) {
				mapArray[r][c] = Integer.parseInt(indices[c].trim());

				//check that all the IDs from the loading map actually exists in the textures folder
				if (expectedHeader == TILE_LAYER && mapArray[r][c] >= 0 
						&& !Utils.tileExists(mapState.getRegistry().getAllTiles(), mapArray[r][c])) {
					missingIDs.add(mapArray[r][c]);
				} else if (expectedHeader == OBJECT_LAYER && mapArray[r][c] >= 0
						&& !Utils.tileExists(mapState.getRegistry().getAllObjects(), mapArray[r][c])) {
					missingIDs.add(mapArray[r][c]);
				} else if (expectedHeader == NPC_LAYER && mapArray[r][c] >= 0 && !Utils.tileExists(mapState.getRegistry().getAllNpcs(), mapArray[r][c])) {
					missingIDs.add(mapArray[r][c]);
				}
			}

			//read the next line for the next iteration or next layer
			line = br.readLine();
		}
		
		//show the notification if something was missing
		if (missingIDs.size() > 0) {
			
			int count = 0;
			for (Integer id : missingIDs) {
				count ++;
				
			    errorMessage.append(id).append(",");
			    if(count % 30 == 0)
			    	errorMessage.append("\n");
			}
			
			//remove the last element
			if (errorMessage.length() > 0) {
			    errorMessage.deleteCharAt(errorMessage.length() - 1);
			}
			
			JOptionPane.showMessageDialog(null, errorMessage.toString(),
					"Missing Assets Detected", JOptionPane.WARNING_MESSAGE);
		}
	}
}
