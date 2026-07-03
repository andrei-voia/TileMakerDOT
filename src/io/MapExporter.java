package io;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import core.Tile;
import core.TileObject;
import data.CanvasViewState;
import data.MapNote;
import data.MapState;
import data.NoteColor;
import tools.ChunkSelectionTool;
import utils.ImageUtils;
import utils.Utils;
import view.CanvasRenderer;
import view.TileCanvas;

public class MapExporter {
	
	private TileCanvas tileCanvas;
	private MapState mapState;
	private ChunkSelectionTool chunkSelectionTool;
	private CanvasViewState canvasViewState;
	private CanvasRenderer canvasRenderer;
	
	public MapExporter(TileCanvas tileCanvas, MapState mapState, 
			ChunkSelectionTool chunkSelectionTool, CanvasViewState canvasViewState,
			CanvasRenderer canvasRenderer) {
		this.tileCanvas = tileCanvas;
		this.mapState = mapState;
		this.chunkSelectionTool = chunkSelectionTool;
		this.canvasViewState = canvasViewState;
		this.canvasRenderer = canvasRenderer;
	}
	
	public void saveChunk(File file) {
		if(chunkSelectionTool != null) {
			saveMap(file, true, chunkSelectionTool.getMapChunk().getRows(), 
					chunkSelectionTool.getMapChunk().getCols(), 
					chunkSelectionTool.getMapChunk().getTiles(),
					chunkSelectionTool.getMapChunk().getObjects(),
					chunkSelectionTool.getMapChunk().getNpcs(),
					chunkSelectionTool.getMapChunk().getWalkArea());
		}
		else {
			JOptionPane.showMessageDialog(canvasRenderer, "Select a chunk before saving!", "Message",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public void saveMap(File file, boolean showMessage) {
		saveMap(file, showMessage, mapState.getData().getRows(), 
				mapState.getData().getCols(), 
				mapState.getData().getTileMap(), 
				mapState.getData().getObjectMap(), 
				mapState.getData().getNpcMap(), 
				mapState.getData().getNpcWalkAreaMap());
	}
	
	private void saveMap(File file, boolean showMessage, 
			int rows, int cols, 
			int[][] tileMap, int[][] objectMap, int[][] npcMap,
			int[][] npcWalkAreaMap) {
		//because of the shortcut of shift it stays stuck in paint mode so we remove it when we save
		tileCanvas.getKeyController().keyRelease();
		canvasRenderer.repaint();
		
		if (file == null) {
			return;
		}
		
		//determine the file extension
		file = Utils.addFileType(file, Utils.SAVE_MAP_EXTENSION);
		
		//use PrintWriter to write human readable text
		try (PrintWriter pw = new PrintWriter(new FileOutputStream(file))) {
			//write dimensions
			pw.println(rows + "," + cols);
			
			//write the tile size of the map
			pw.println("# Tile Size");
			pw.println(canvasRenderer.getTileSize());
			
			//write tile layer and a header for clarity
			pw.println("# Tile Layer");
			for (int r = 0; r < rows; r++) {
				StringBuilder sb = new StringBuilder();
				for (int c = 0; c < cols; c++) {
					sb.append(tileMap[r][c]);
					if (c < cols - 1) {
						sb.append(",");
					}
				}
				pw.println(sb.toString());
			}

			//write object layer
			pw.println("# Object Layer");
			for (int r = 0; r < rows; r++) {
				StringBuilder sb = new StringBuilder();
				for (int c = 0; c < cols; c++) {
					sb.append(objectMap[r][c]);
					if (c < cols - 1) {
						sb.append(",");
					}
				}
				pw.println(sb.toString());
			}

			//write NPC layer
			pw.println("# NPC Layer");
			for (int r = 0; r < rows; r++) {
				StringBuilder sb = new StringBuilder();
				for (int c = 0; c < cols; c++) {
					sb.append(npcMap[r][c]);
					if (c < cols - 1) {
						sb.append(",");
					}
				}
				pw.println(sb.toString());
			}
			
			//write NPC walk area layer
			pw.println("# NPC Walk Area Layer");
			for (int r = 0; r < rows; r++) {
				StringBuilder sb = new StringBuilder();
				for (int c = 0; c < cols; c++) {
					sb.append(npcWalkAreaMap[r][c]);
					if (c < cols - 1) {
						sb.append(",");
					}
				}
				pw.println(sb.toString());
			}
			
			//write the annotated notes if they exist
	        if (tileCanvas.getAnnotatedNotesTool() != null) {
	            List<MapNote> mapNotes = tileCanvas.getAnnotatedNotesTool().getMapNotes();
	            if (mapNotes != null && !mapNotes.isEmpty()) {
	                pw.println("# Annotated Notes");
	                for (MapNote note : mapNotes) {
	                    //safe string representation fallback for the color enum structure
	                    String colorEnumName = (note.getColor() != null) ? note.getColor().name() : NoteColor.YELLOW.name();
	                    
	                    //format output line is: col,row,color,text
	                    pw.println(String.format("%d,%d,%s,%s", 
	                        note.getCol(), 
	                        note.getRow(), 
	                        colorEnumName, 
	                        note.getText()
	                    ));
	                }
	            }
	        }
	        
			//save new cached location from last loaded file
			mapState.getCacheData().setCachedSavedLocation(file);
			mapState.getCacheData().setCanQuickSave(false);
			
			if (showMessage) {
				JOptionPane.showMessageDialog(canvasRenderer, "Map saved successfully to " + file.getName(), "Save Success",
						JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(canvasRenderer, "Error saving map: " + e.getMessage(), "Save Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private float calculateExportScaleFactor() {
		int width = mapState.getData().getCols() * canvasRenderer.getTileSize();
		int height = mapState.getData().getRows() * canvasRenderer.getTileSize();
		long calculatedTotalSize = (long)width * (long)height;
		//we add a buffer to the max value because if it reaches it it will still be in error
		final int INT_MAX_VALUE = (int) (Integer.MAX_VALUE * 0.93f);
		
		if(calculatedTotalSize < INT_MAX_VALUE) {
			return 1;
		}
		
		float scaleFactor = (float)INT_MAX_VALUE / (float)calculatedTotalSize;
		return scaleFactor;
	}
	
	//save methods
	private StringBuilder generateTileLayer(int defaultNoTile, String space) {
		StringBuilder sb = new StringBuilder();
		//write tile layer
		for (int r = 0; r < mapState.getData().getRows(); r++) {
			for (int c = 0; c < mapState.getData().getCols(); c++) {
				//convert from -1 to 0 when there is no tile
				if (mapState.getData().getTileMap(r, c) == -1) {
					sb.append(defaultNoTile + "," + space);
				} else if (canvasViewState.isShowAutotile()) {
					int tileId = mapState.getData().getTileMap(r, c);
					Tile tile = mapState.findTileById(tileId);

					if (tile.getAutotiles() != null) {
						int autotiledId = mapState.getAutoTiledSubimageId(tile, r, c);
						//if not autoTiled id found then just add normal id
						if (autotiledId == -1) {
							sb.append(tileId + "," + space);
						}
						//else add autoTiled generated id
						else {
							sb.append(autotiledId + "," + space);
						}
					}
					//else add autoTiled generated id
					else {
						sb.append(tileId + "," + space);
					}
				} else {
					sb.append(mapState.getData().getTileMap(r, c) + "," + space);
				}
			}
			if(!(r == mapState.getData().getRows() - 1))
				sb.append("\n");
		}
		//remove the last "," at the end
		if (sb.length() > 0) {
		    sb.setLength(sb.length() - 1);
		    //extra remove last element depending on the extra spaces
		    sb.setLength(sb.length() - space.length());
		}
		return sb;
	}
	
	private StringBuilder generateObjectLayer(int defaultNoTile, String space) {
		StringBuilder sb = new StringBuilder();
		//write object layer
		for (int r = 0; r < mapState.getData().getRows(); r++) {
			for (int c = 0; c < mapState.getData().getCols(); c++) {
				//convert from -1 to 0 when there is no object
				if (mapState.getData().getObjectMap(r, c) == -1) {
					sb.append(defaultNoTile + "," + space);
				} else {
					sb.append(mapState.getData().getObjectMap(r, c) + "," + space);
				}
			}
			if(!(r == mapState.getData().getRows() - 1))
				sb.append("\n");
		}
		//remove the last "," at the end
		if (sb.length() > 0) {
		    sb.setLength(sb.length() - 1);
		    //extra remove last element depending on the extra spaces
		    sb.setLength(sb.length() - space.length());
		}
		return sb;
	}
	
	private StringBuilder generateNpcLayer(int defaultNoTile, String space) {
		StringBuilder sb = new StringBuilder();
		//write NPC layer
		for (int r = 0; r < mapState.getData().getRows(); r++) {
			for (int c = 0; c < mapState.getData().getCols(); c++) {
				if(mapState.getData().getNpcMap(r, c) >= 0) {
					sb.append(mapState.getData().getNpcMap(r, c) + "," + space);
				}
				else if(mapState.getData().getNpcWalkAreaMap(r, c) == tileCanvas.getSelected().getIndex()) {
					sb.append(mapState.getData().getNpcWalkAreaMap(r, c) + "," + space);
				}
				//convert from -1 to 0 when there is no NPC
				else {
					sb.append(defaultNoTile + "," + space);
				}
			}
			if(!(r == mapState.getData().getRows() - 1))
				sb.append("\n");
		}
		//remove the last "," at the end
		if (sb.length() > 0) {
		    sb.setLength(sb.length() - 1);
		    //extra remove last element depending on the extra spaces
		    sb.setLength(sb.length() - space.length());
		}
		
		return sb;
	}
	
	public void exportJsonFormat(File file) {
		//determine the file extension
		file = Utils.addFileType(file, Utils.JSON_FILE_EXTENSION);

		//use PrintWriter to write human readable text
		try (PrintWriter pw = new PrintWriter(new FileOutputStream(file))) {
			pw.println(
					"{ \"compressionlevel\":-1,\r\n"
					+ " \"height\":" + mapState.getData().getRows() + ",\r\n"
					+ " \"infinite\":false,\r\n"
					+ " \"layers\":[\r\n"
					+ "        {\r\n"
					+ "         \"data\":[");
			pw.println(this.generateTileLayer(0, " "));
			
			pw.println(
					  "],\r\n"
					+ "         \"height\":" + mapState.getData().getRows() + ",\r\n"
					+ "         \"id\":1,\r\n"
					+ "         \"name\":\"tiles_layer\",\r\n"
					+ "         \"opacity\":1,\r\n"
					+ "         \"type\":\"tilelayer\",\r\n"
					+ "         \"visible\":true,\r\n"
					+ "         \"width\":" + mapState.getData().getCols() + ",\r\n"
					+ "         \"x\":0,\r\n"
					+ "         \"y\":0\r\n"
					+ "        }, \r\n"
					+ "        {\r\n"
					+ "         \"data\":[");
			pw.println(this.generateObjectLayer(0, " "));
			
			pw.println("],\r\n"
					+ "         \"height\":" + mapState.getData().getRows() + ",\r\n"
					+ "         \"id\":2,\r\n"
					+ "         \"name\":\"objects_layer\",\r\n"
					+ "         \"opacity\":1,\r\n"
					+ "         \"type\":\"tilelayer\",\r\n"
					+ "         \"visible\":true,\r\n"
					+ "         \"width\":" + mapState.getData().getCols() + ",\r\n"
					+ "         \"x\":0,\r\n"
					+ "         \"y\":0\r\n"
					+ "        }, \r\n"
					+ "        {\r\n"
					+ "         \"data\":[");
			pw.println(this.generateNpcLayer(0, " "));
			pw.println(
					"],\r\n"
					+ "         \"height\":" + mapState.getData().getRows() + ",\r\n"
					+ "         \"id\":3,\r\n"
					+ "         \"name\":\"npcs_layer\",\r\n"
					+ "         \"opacity\":1,\r\n"
					+ "         \"type\":\"tilelayer\",\r\n"
					+ "         \"visible\":true,\r\n"
					+ "         \"width\":" + mapState.getData().getCols() + ",\r\n"
					+ "         \"x\":0,\r\n"
					+ "         \"y\":0\r\n"
					+ "        }],\r\n"
					+ " \"nextlayerid\":4,\r\n"
					+ " \"nextobjectid\":1,\r\n"
					+ " \"orientation\":\"orthogonal\",\r\n"
					+ " \"renderorder\":\"right-down\",\r\n"
					+ " \"tiledversion\":\"1.11.2\",\r\n"
					+ " \"tileheight\":" + canvasRenderer.getTileSize() + ",\r\n"
					+ " \"tilesets\":[\r\n"
					+ "        {\r\n"
					+ "         \"firstgid\":1,\r\n"
					+ "         \"source\":\"..\\/TileMakerDOT\"\r\n"
					+ "        }],\r\n"
					+ " \"tilewidth\":" + canvasRenderer.getTileSize() + ",\r\n"
					+ " \"type\":\"map\",\r\n"
					+ " \"version\":\"1.10\",\r\n"
					+ " \"width\":" + mapState.getData().getCols() + "\r\n"
					+ "}");

			JOptionPane.showMessageDialog(canvasRenderer, "Tile map exported successfully to " + file.getName(), "Save Success",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(canvasRenderer, "Error exporting map: " + e.getMessage(), "Save Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	public void exportTmxTsxFormat(File file) {
	    File tsxFile = Utils.addFileType(file, Utils.TSX_FILE_EXTENSION);
	    File tmxFile = Utils.addFileType(file, Utils.TMX_FILE_EXTENSION);
	    
	    TmxTsxExporter exporter = new TmxTsxExporter(tileCanvas, mapState.getData().getRows(), mapState.getData().getCols(), canvasRenderer.getTileSize());
	    
	    //export the tileSet
	    exporter.exportTSX(tsxFile, mapState.getRegistry().getAllTiles());
	    
	    //export the map linking it to the TSX
	    exporter.exportTMX(tmxFile, tsxFile.getName(), mapState.getData().getTileMap(), mapState.getRegistry().getAllSortedItems());
	    
	    tileCanvas.getToastNotification().showToastNotification("Godot files (.tmx/.tsx) exported!");
	}
	
	public void exportCsvFormat(File file) {
	    //reconstruct the File object
	    File tilesFile = Utils.extendFileName(file, "_tiles");
	    File objectsFile = Utils.extendFileName(file, "_objects");
	    File npcsFile = Utils.extendFileName(file, "_npcs");
		//determine the file extension
	    tilesFile = Utils.addFileType(tilesFile, Utils.CSV_FILE_EXTENSION);
	    objectsFile = Utils.addFileType(objectsFile, Utils.CSV_FILE_EXTENSION);
	    npcsFile = Utils.addFileType(npcsFile, Utils.CSV_FILE_EXTENSION);
	    
		//use PrintWriter to write human readable text
	    try (PrintWriter pwt = new PrintWriter(new FileOutputStream(tilesFile))) {
			pwt.println(this.generateTileLayer(-1, ""));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(canvasRenderer, "Error exporting map: " + e.getMessage(), "Save Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	    
	    try (PrintWriter pwo = new PrintWriter(new FileOutputStream(objectsFile))) {
			pwo.println(this.generateObjectLayer(-1, ""));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(canvasRenderer, "Error exporting map: " + e.getMessage(), "Save Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	    
	    try (PrintWriter pwn = new PrintWriter(new FileOutputStream(npcsFile))) {
			pwn.println(this.generateNpcLayer(-1, ""));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(canvasRenderer, "Error exporting map: " + e.getMessage(), "Save Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	    
		JOptionPane.showMessageDialog(canvasRenderer, "Tile map exported successfully to " + file.getName(), "Save Success",
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void exportCustomLvlFormat(File file) {
	    //reconstruct the File object
	    File tilesFile = Utils.extendFileName(file, "_tiles");
	    File objectsFile = Utils.extendFileName(file, "_objects");
	    File npcsFile = Utils.extendFileName(file, "_npcs");
		//determine the file extension
	    tilesFile = Utils.addFileType(tilesFile, Utils.EXPORT_MAP_EXTENSION);
	    objectsFile = Utils.addFileType(objectsFile, Utils.EXPORT_MAP_EXTENSION);
	    npcsFile = Utils.addFileType(npcsFile, Utils.EXPORT_MAP_EXTENSION);

		//use PrintWriter to write human readable text
	    try (PrintWriter pwt = new PrintWriter(new FileOutputStream(tilesFile))) {
			pwt.println(this.generateLvlTileMap());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(canvasRenderer, "Error exporting map: " + e.getMessage(), "Save Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	    
	    try (PrintWriter pwo = new PrintWriter(new FileOutputStream(objectsFile))) {
			pwo.println(this.generateLvlObjectMap());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(canvasRenderer, "Error exporting map: " + e.getMessage(), "Save Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	    
	    try (PrintWriter pwn = new PrintWriter(new FileOutputStream(npcsFile))) {
			pwn.println(this.generateLvlNpcMap());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(canvasRenderer, "Error exporting map: " + e.getMessage(), "Save Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	    
		JOptionPane.showMessageDialog(canvasRenderer, "Tile map exported successfully to " + file.getName(), "Save Success",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private String generateLvlTileMap() {
		StringBuilder sb = new StringBuilder();
		sb.append(mapState.getData().getCols() + " " + mapState.getData().getRows() + "\n");

		//write tile layer
		for (int r = 0; r < mapState.getData().getRows(); r++) {
			for (int c = 0; c < mapState.getData().getCols(); c++) {
				//convert from -1 to 0 when there is no tile
				if (mapState.getData().getTileMap(r, c) == -1) {
					sb.append("0");
				} else if (canvasViewState.isShowAutotile()) {
					int tileId = mapState.getData().getTileMap(r, c);
					Tile tile = mapState.findTileById(tileId);

					if (tile.getAutotiles() != null) {
						int autotiledId = mapState.getAutoTiledSubimageId(tile, r, c);
						//if not autoTiled id found then just add normal id
						if (autotiledId == -1) {
							sb.append(tileId);
						}
						//else add autoTiled generated id
						else {
							sb.append(autotiledId);
						}
					}
					//else add autoTiled generated id
					else {
						sb.append(tileId);
					}
				} else {
					sb.append(mapState.getData().getTileMap(r, c));
				}
				if (c < mapState.getData().getCols() - 1) {
					sb.append(" ");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private String generateLvlObjectMap() {
		StringBuilder sb = new StringBuilder();
		sb.append(mapState.getData().getCols() + " " + mapState.getData().getRows() + "\n");

		//write object layer
		for (int r = 0; r < mapState.getData().getRows(); r++) {
			for (int c = 0; c < mapState.getData().getCols(); c++) {
				//convert from -1 to 0 when there is no object
				if (mapState.getData().getObjectMap(r, c) == -1) {
					sb.append("0");
				} else {
					sb.append(mapState.getData().getObjectMap(r, c));
				}
				sb.append(" -1 ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private String generateLvlNpcMap() {
		StringBuilder sb = new StringBuilder();
		sb.append(mapState.getData().getCols() + " " + mapState.getData().getRows() + "\n");

		//write NPC layer
		for (int r = 0; r < mapState.getData().getRows(); r++) {
			for (int c = 0; c < mapState.getData().getCols(); c++) {
				if (mapState.getData().getNpcMap(r, c) >= 0) {
					sb.append(mapState.getData().getNpcMap(r, c));
				} else if (mapState.getData().getNpcWalkAreaMap(r, c) == tileCanvas.getSelected().getIndex()) {
					sb.append(mapState.getData().getNpcWalkAreaMap(r, c));
				}
				//convert from -1 to 0 when there is no NPC
				else {
					sb.append("0");
				}
				if (c < mapState.getData().getCols() - 1) {
					sb.append(" ");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	//export all unique IDs list in a file
	public void exportTileIdsList(File file) {
		List<Integer> tiles = mapState.getUniqueSortedTileIds(mapState.getData().getTileMap());
		List<Integer> objects = mapState.getUniqueSortedTileIds(mapState.getData().getObjectMap());
		List<Integer> npcs = mapState.getUniqueSortedTileIds(mapState.getData().getNpcMap());
		
		if (file == null)
			return;

		//determine the file extension
		file = Utils.addFileType(file, Utils.TXT_FILE_EXTENSION);

		//use PrintWriter to write human readable text
		try (PrintWriter pw = new PrintWriter(new FileOutputStream(file))) {
			//write tile IDs
			pw.println("# Note: Existing Autotile IDs are not saved in this file... Keep in mind.\n");
			pw.println("# Tiles IDs (" + tiles.size() + "):");
			
			for (int r = 0; r < tiles.size(); r++) {
				Tile tile = mapState.findTileById(tiles.get(r));
				pw.println((r + 1) + ". " + tiles.get(r) + " (Name: " + tile.getName() + ")");
			}

			//write object IDs
			pw.println("\n# Objects IDs (" + objects.size() + "):");
			
			for (int r = 0; r < objects.size(); r++) {
				Tile tile = mapState.findObjectById(objects.get(r));
				pw.println((r + 1) + ". " + objects.get(r) + " (Name: " + tile.getName() + ")");
			}

			//write NPCs IDs
			pw.println("\n# NPCs IDs (" + npcs.size() + "):");
			
			for (int r = 0; r < npcs.size(); r++) {
				Tile tile = mapState.findNpcById(npcs.get(r));
				pw.println((r + 1) + ". " + npcs.get(r) + " (Name: " + tile.getName() + ")");
			}
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(canvasRenderer, "Error saving map: " + e.getMessage(), "Save Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	public void exportImage(File file) {
	    //define a smaller tile size for export
	    final float SCALE_FACTOR = calculateExportScaleFactor();
	    final int exportTileSize = (int)(canvasRenderer.getTileSize() * SCALE_FACTOR);
	    
	    System.out.println("Notification: Exporting image with the final tile size of " + exportTileSize);
	    
	    //calculate new total dimensions using the smaller exportTileSize
	    int calculatedWidth = mapState.getData().getCols() * exportTileSize;
	    int calculatedHeight = mapState.getData().getRows() * exportTileSize;
	    
	    //check if the reduced image is still too large just as a safety check
	    if (calculatedWidth > Integer.MAX_VALUE || calculatedHeight > Integer.MAX_VALUE || calculatedWidth * calculatedHeight > Integer.MAX_VALUE) {
	        JOptionPane.showMessageDialog(canvasRenderer, "Error: Map size is too large even after scaling down. COLS/ROWS must be reduced.", "Export Error", JOptionPane.ERROR_MESSAGE);
	        return; 
	    }
	    
	    BufferedImage exportImage = new BufferedImage(calculatedWidth, calculatedHeight, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2d = exportImage.createGraphics();
	    
	    //temporarily disable grid / zoom for the export process
	    boolean gridState = canvasViewState.isShowGrid();
	    canvasViewState.setShowGrid(false);

	    try {
	        //draw tiles
	        if (canvasViewState.isShowTileLayer()) {
	            for (int r = 0; r < mapState.getData().getRows(); r++) {
	                for (int c = 0; c < mapState.getData().getCols(); c++) {
	                    int id = mapState.getData().getTileMap(r, c);
	                    if (id >= 0) {
	                        Tile tile = mapState.findTileById(id);

	                        BufferedImage sourceImage = null;
	                        
	                        //decide on the source image (AutoTile or normal tile)
	                        if (tile.getAutotiles() != null && canvasViewState.isShowAutotile()) {
	                            sourceImage = mapState.getAutoTiledImage(tile, r, c);
	                        }
	                        if (sourceImage == null) {
	                            sourceImage = tile.getImage();
	                        }
	                        
	                        //scale the image and draw using the exportTileSize
	                        g2d.drawImage(
	                            sourceImage, 
	                            c * exportTileSize, 
	                            r * exportTileSize, 
	                            exportTileSize, //scaled width
	                            exportTileSize, //scaled height
	                            canvasRenderer
	                        );
	                    }
	                }
	            }
	        }

	        //new rendering method
	        mapState.refreshAllObjectsList();
	        
	        //draw objects and NPCs
	        if (canvasViewState.isShowObjectLayer() || canvasViewState.isShowNpcLayer()) {
	            for (TileObject objects : mapState.getRegistry().getAllSortedItems()) {
	                //calculate position and size based on the exportTileSize and scale factor
	                int drawX = objects.getX() * exportTileSize;
	                int drawY = objects.getY() * exportTileSize;
	                
	                //scale the object original pixel dimensions
	                //the object size must be proportional to the tile size change
	                int drawWidth = (int)(objects.getWidth() * SCALE_FACTOR); 
	                int drawHeight = (int)(objects.getHeight() * SCALE_FACTOR);

	                if (objects.isObject() && canvasViewState.isShowObjectLayer()) {
	                    g2d.drawImage(objects.getImage(), drawX, drawY, drawWidth, drawHeight, canvasRenderer);
	                } else if (!objects.isObject() && canvasViewState.isShowNpcLayer()) {
	                    g2d.drawImage(objects.getImage(), drawX, drawY, drawWidth, drawHeight, canvasRenderer);
	                }
	            }
	        }
	        
	        //draw night mode overlay which must also use the scaled dimensions
	        if(canvasViewState.isNightMode()) {
	            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, canvasRenderer.getNightTransparency()));
	            g2d.setColor(canvasRenderer.getNightColor());
	          	//width and height are the new scaled dimensions
	            g2d.fillRect(0, 0, calculatedWidth, calculatedHeight);
	        }

	        String format = "png";
	        if (!file.getName().toLowerCase().endsWith(ImageUtils.PNG_FORMAT)) {
	            file = new File(file.getAbsolutePath() + "." + format);
	        }

	        ImageIO.write(exportImage, format, file);
	        JOptionPane.showMessageDialog(canvasRenderer, "Image exported successfully as " + file.getName(), "Export Success",
	                JOptionPane.INFORMATION_MESSAGE);

	    } catch (IOException e) {
	        JOptionPane.showMessageDialog(canvasRenderer, "Error exporting image: " + e.getMessage(), "Export Error",
	                JOptionPane.ERROR_MESSAGE);
	    } finally {
	        g2d.dispose();
	        canvasViewState.setShowGrid(gridState);
	        canvasRenderer.repaint();
	    }
	}
}
