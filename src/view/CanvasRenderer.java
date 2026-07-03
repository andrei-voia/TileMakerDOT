package view;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import core.ItemType;
import core.Tile;
import core.TileObject;
import data.CanvasViewState;
import data.MapNote;
import data.NoteColor;
import data.Selection;
import data.SelectionDragged;
import utils.Utils;

public class CanvasRenderer extends JPanel {

	private static final long serialVersionUID = 1L;
	//define how many tiles are rendered outside the canvas screen for objects and NPCs
	private static final int OFFSET_RENDER_OBJECTS = 10;
	
	//the size of a single square tile
	private int tileSize;
	
	private final Color nightColor = new Color(0, 0, 10);
	private final float nightTransparency = 0.70f;
	private final float npcWalkAreaTransparency = 0.4f;
	private final float locateTransparency = 0.20f;

	private boolean requestCanvasFocusOnce = false;
	
	private CanvasViewState canvasViewState;
	private Selection selected;
	private SelectionDragged draggedItem;
	private BufferedImage deleteImage;
	private BufferedImage objectPickerImage;
	
	private TileCanvas tileCanvas;
	
	public CanvasRenderer(int tileSize, CanvasViewState canvasViewState, 
			Selection selected, SelectionDragged draggedItem,
			BufferedImage deleteImage, BufferedImage objectPickerImage, 
			TileCanvas tileCanvas) {
		this.tileSize = tileSize;
		
		this.canvasViewState = canvasViewState;
		this.selected = selected;
		this.draggedItem = draggedItem;
		
		this.deleteImage = deleteImage;
		this.objectPickerImage = objectPickerImage;
		
		this.tileCanvas = tileCanvas;
	}
	
	//used to render the canvas view in the tool
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        //we do this here to ensure the canvas is on focus when the application starts
        if(!requestCanvasFocusOnce) {
        	requestCanvasFocusOnce = true;
            requestFocusInWindow();
        }
        
        //used for toast notification position
        //we save the initial state position using the viewport transform
        //this transform already accounts for the ScrollPane position.
        AffineTransform viewportTransform = g2d.getTransform();
        
        //use Nearest Neighbor render which is fast and prevents bleeding when scaling / panning
        g2d.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );
        
        //apply transform for panning and zooming
        g2d.translate(tileCanvas.getCamera().getViewOffset().x, tileCanvas.getCamera().getViewOffset().y);
        g2d.scale(tileCanvas.getCamera().getZoomLevel(), tileCanvas.getCamera().getZoomLevel());
        
        //calculate optimized only visible map bounds
        int visibleWidth = getWidth();
        int visibleHeight = getHeight();

        //calculate the column and row indices that are currently visible on screen
        int startCol = (int) Math.max(0, tileCanvas.getCamera().posX(0, tileSize));
        int startRow = (int) Math.max(0, tileCanvas.getCamera().posY(0, tileSize));
        
        int endCol = (int) Math.min(tileCanvas.getMapState().getData().getCols(), tileCanvas.getCamera().posX(visibleWidth, tileSize) + 1);
        int endRow = (int) Math.min(tileCanvas.getMapState().getData().getRows(), tileCanvas.getCamera().posY(visibleHeight, tileSize) + 1);

        //draw tiles optimized
        if(canvasViewState.isShowTileLayer()) {
        	//set that everything is transparent except selected item if locate mode is active
        	if(canvasViewState.isLocateMode()) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, locateTransparency));
        	}
        	
            for (int r = startRow; r < endRow; r++) {
                for (int c = startCol; c < endCol; c++) {
                	//draw autoTile tiles
					if (canvasViewState.isShowAutotile()) {
						if (tileCanvas.getMapState().getData().getAutotileMap(r, c) == null) {
							g2d.setColor(Color.LIGHT_GRAY);
							g2d.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);
						} else {
							boolean drawTransparent = canvasViewState.isLocateMode() && selected.isTileMode() && selected.getIndex() == tileCanvas.getMapState().getData().getAutotileMap(r, c).getId();
			            	if(drawTransparent) {
			            		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			            	}
			            	
							g2d.drawImage(tileCanvas.getMapState().getData().getAutotileMap(r, c).getImage(), 
									c * tileSize, r * tileSize, tileSize, tileSize, this);
							
			            	if(drawTransparent) {
			            		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, locateTransparency));
			            	}
						}
					}
					//draw normal tiles
					else {
	                    int tileId = tileCanvas.getMapState().getData().getTileMap(r, c);
	                    if (tileId >= 0) {
	                        Tile tile = tileCanvas.getMapState().findTileById(tileId);
	                        
	                        boolean drawTransparent = canvasViewState.isLocateMode() && selected.isTileMode() && selected.getIndex() == tileId;
			            	if(drawTransparent) {
			            		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			            	}
	                        
	                        g2d.drawImage(tile.getImage(), 
	                        		c * tileSize, r * tileSize, tileSize, tileSize, this);
	                        
			            	if(drawTransparent) {
			            		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, locateTransparency));
			            	}
	                        
	                    } else if (tileId == -1) {
	                        //draw a placeholder for truly empty tiles
	                        g2d.setColor(Color.LIGHT_GRAY);
	                        g2d.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);
	                    }
					}
				}
            }
            
            //set everything back to normal transparency
            if(canvasViewState.isLocateMode()) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        	}
        }
        
        //here we draw the walk area for the NPCs
        if(selected.isNpcWalkAreaMode()) {
        	g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, npcWalkAreaTransparency));
        	
        	for (int r = startRow; r < endRow; r++) {
                for (int c = startCol; c < endCol; c++) {
					if (tileCanvas.getMapState().getData().getNpcWalkAreaMap(r, c) >= 0) {
						g2d.setColor(Color.PINK);
						g2d.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);
					}
				}
            }
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        //here we draw objects and NPCs together
        if(canvasViewState.isShowObjectLayer() || canvasViewState.isShowNpcLayer()) {
        	
        	//set that everything is transparent except selected item if locate mode is active
        	if(canvasViewState.isLocateMode() && selected.isEraseMode() == false) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, locateTransparency));
        	}
        	
            for(TileObject objects: tileCanvas.getMapState().getRegistry().getAllSortedItems()) {
            	int r = objects.getY();
            	int c = objects.getX();
            	
            	if(canvasViewState.isLocateMode() && ((selected.isObjectMode() && objects.isObject() && selected.getIndex() == objects.getId()) ||
            			(selected.isNpcMode() && !objects.isObject() && selected.getIndex() == objects.getId()))) {
            		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            	}
            	
            	if(startRow - OFFSET_RENDER_OBJECTS <= r && r <= endRow && startCol - OFFSET_RENDER_OBJECTS <= c && c <= endCol) {
        			if (objects.isObject() && canvasViewState.isShowObjectLayer()) {
                		g2d.drawImage(objects.getImage(), 
                				objects.getX() * tileSize, 
                				objects.getY() * tileSize, 
                				objects.getWidth(), objects.getHeight(), this);
        			}
        			else if (!objects.isObject() && canvasViewState.isShowNpcLayer()) {
                		g2d.drawImage(objects.getImage(), 
                				objects.getX() * tileSize, 
                				objects.getY() * tileSize, 
                				objects.getWidth(), objects.getHeight(), this);
        			}
            	}
            	
            	//draw the erasing mode on the screen
        		if(selected.isEraseMode()) {
        			boolean eraseVisible = false;
        			if (objects.isObject() && canvasViewState.isShowObjectLayer()) {
        				eraseVisible = true;
        			}
        			else if (!objects.isObject() && canvasViewState.isShowNpcLayer()) {
        				eraseVisible = true;
        			}
        			
        			if(eraseVisible) {
        				float alpha = 0.5f; //0.0f = fully transparent, 1.0f = fully opaque
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        g2d.setColor(Color.RED);
                        
                        //calculate stroke percentage of the current tile size
                        float strokeWidth = tileSize * 0.075f;
                        
                        //ensure it is at least 1px so it doesn't disappear on tiny tiles
                        strokeWidth = Math.max(strokeWidth, 1.0f);
                        
                        g2d.setStroke(new BasicStroke(strokeWidth));
                        g2d.drawRect(objects.getX() * tileSize, 
                				objects.getY() * tileSize, 
                				objects.getWidth(), objects.getHeight());

                		g2d.drawImage(deleteImage, 
                				objects.getX() * tileSize, 
                				objects.getY() * tileSize,
                				tileSize, tileSize, this);
                		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        			}
        		}
        		
        		//draw the object picker on the screen
        		if((tileCanvas.getKeyController().isShiftPressed()) && selected.isChunkSelectionTool() == false && selected.isEraseMode() == false) {
            		float alpha = 0.7f; //0.0f = fully transparent, 1.0f = fully opaque
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            		g2d.drawImage(objectPickerImage, 
            				objects.getX() * tileSize, 
            				objects.getY() * tileSize,
            				tileSize, tileSize, this);
                  g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        		}
        		
            	if(canvasViewState.isLocateMode() && ((selected.isObjectMode() && objects.isObject() && selected.getIndex() == objects.getId()) ||
            			(selected.isNpcMode() && !objects.isObject() && selected.getIndex() == objects.getId()))) {
            		//draw rectangle around the selected object with locateMode
                    g2d.setColor(Color.RED);
                    float strokeWidth = tileSize * 0.25f;
                    strokeWidth = Math.max(strokeWidth, 1.0f);
                    g2d.setStroke(new BasicStroke(strokeWidth));
                    g2d.drawRect(objects.getX() * tileSize - (int)strokeWidth, 
            				objects.getY() * tileSize - (int)strokeWidth, 
            				objects.getWidth() + (int)strokeWidth * 2, 
            				objects.getHeight() + (int)strokeWidth * 2);
                    
            		//turn the transparency back on
            		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, locateTransparency));
            	}
            }
            
            //set everything back to normal transparency
            if(canvasViewState.isLocateMode()) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        	}
        }
        
        //show preview of object / NPC / tile you want to paint on screen
        if(canvasViewState.isShowObjectPreview() && canvasViewState.getObjectPreviewPosition() != null 
        		&& tileCanvas.getMouseController().getDragStartPoint() == null && (!tileCanvas.getKeyController().isShiftPressed() && !tileCanvas.getKeyController().isCtrlPressed())
        		&& tileCanvas.getMouseController().getSelectionStartPoint() == null && selected.isEraseMode() == false 
        		&& selected.isChunkSelectionTool() == false) {
        	
        	int posY = (int) canvasViewState.getObjectPreviewPosition().getY();
        	int posX = (int) canvasViewState.getObjectPreviewPosition().getX();
        	
        	float alpha = 0.5f; //0.0f = fully transparent, 1.0f = fully opaque
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            //show moving preview
            if (draggedItem.getIndex() != -1 && draggedItem.getType() != ItemType.NONE) {
            	Tile object;
            	if(draggedItem.getType() == ItemType.OBJECT) {
            		object = tileCanvas.getMapState().findObjectById(draggedItem.getIndex());
            	}
            	else {
            		object = tileCanvas.getMapState().findNpcById(draggedItem.getIndex());
            	}
	            BufferedImage objectImage = object.getImage();
	            
	            //if the object can not be placed, then preview it red
				if(draggedItem.getType() == ItemType.OBJECT && tileCanvas.getMapState().getData().getObjectMap(posX, posY) != -1 
						|| draggedItem.getType() == ItemType.NPC && tileCanvas.getMapState().getData().getNpcMap(posX, posY) != -1) {
					objectImage = getTintedImage(objectImage, Color.RED, alpha);
        		}
				g2d.drawImage(objectImage, 
        				posY * tileSize, 
        				posX * tileSize, 
        				object.getImage().getWidth(), object.getImage().getHeight(), this);
            }
            //show objects preview
        	else if(selected.isObjectMode()) {
				Tile object = tileCanvas.getMapState().findObjectById(selected.getIndex());
	            BufferedImage objectImage = object.getImage();
	            
	            //if the object can not be placed then preview it red
				if(tileCanvas.getMapState().getData().getObjectMap(posX, posY) != -1) {
					objectImage = getTintedImage(objectImage, Color.RED, alpha);
        		}
				g2d.drawImage(objectImage, 
        				posY * tileSize, 
        				posX * tileSize, 
        				object.getImage().getWidth(), object.getImage().getHeight(), this);
        	}
        	//show NPCs preview
        	else if(selected.isNpcMode()) {
				Tile npc = tileCanvas.getMapState().findNpcById(selected.getIndex());
	            BufferedImage npcImage = npc.getImage();
	            
	            //if the NPC can not be placed then preview it red
				if(tileCanvas.getMapState().getData().getNpcMap(posX, posY) != -1) {
					npcImage = getTintedImage(npcImage, Color.RED, alpha);
        		}
        		g2d.drawImage(npcImage, 
        				posY * tileSize, 
        				posX * tileSize, 
        				npc.getImage().getWidth(), npc.getImage().getHeight(), this);
        	}
        	//show tiles preview
        	else if(selected.isTileMode()) {
				Tile tile = tileCanvas.getMapState().findTileById(selected.getIndex());
        		g2d.drawImage(tile.getImage(), 
        				posY * tileSize, 
        				posX * tileSize, 
        				tile.getImage().getWidth(), tile.getImage().getHeight(), this);
        	}
            //show notes preview
        	else if(selected.isNotesTool()) {
            	//temporarily reset transform to switch into viewport screen space
            	AffineTransform mapTransform = g2d.getTransform();
            	g2d.setTransform(viewportTransform);
            	
            	//calculate the raw map center coordinates of this tile, not zoomed and not panned
            	int mapTileCenterX = (posY * tileSize) + (tileSize / 2);
            	int mapTileCenterY = (posX * tileSize) + (tileSize / 2);

            	//convert map coordinates to exact screen pixel coordinates
            	double zoom = tileCanvas.getCamera().getZoomLevel();
            	Point offset = tileCanvas.getCamera().getViewOffset();
            	final int fixedPinSize = 25;
            	
            	int screenX = (int) (mapTileCenterX * zoom) + offset.x;
            	int screenY = (int) (mapTileCenterY * zoom) + offset.y;

                int offsetX = screenX - (fixedPinSize / 2);
                int offsetY = screenY - (fixedPinSize / 2);
                
                drawNoteColorCircle(g2d, offsetX, offsetY, NoteColor.YELLOW, fixedPinSize);

                //restore matrix state for the items that are next to render
                g2d.setTransform(mapTransform);
        	}
        	
        	g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        //draw the preview for chunk selection
        if(selected.isChunkSelectionTool() && tileCanvas.getChunkSelectionTool() != null && tileCanvas.getChunkSelectionTool().getMapChunk() != null
        		&& canvasViewState.getObjectPreviewPosition() != null && canvasViewState.isShowObjectPreview()
        		&& !tileCanvas.getKeyController().isShiftPressed() && !tileCanvas.getKeyController().isAltPressed()) {
        	
        	float alpha = 0.3f; //0.0f = fully transparent, 1.0f = fully opaque
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        	int posY = (int) canvasViewState.getObjectPreviewPosition().getY();
        	int posX = (int) canvasViewState.getObjectPreviewPosition().getX();
        	
        	//calculate how many tiles we can actually fit before hitting the edge
            int drawWidth = Math.min(tileCanvas.getChunkSelectionTool().getMapChunk().getCols(), tileCanvas.getMapState().getData().getCols() - posY);
            int drawHeight = Math.min(tileCanvas.getChunkSelectionTool().getMapChunk().getRows(), tileCanvas.getMapState().getData().getRows() - posX);
    	    
            g2d.setColor(Utils.COLOR_SAGE_GREEN);
            
            //multiply by tileSize to convert grid units back to pixels for drawing
            g2d.fillRect(posY * tileSize, posX * tileSize,
                         drawWidth * tileSize, 
                         drawHeight * tileSize);

			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        //if night mode active then draw in night mode overlay over everything
        if(canvasViewState.isNightMode()) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, nightTransparency));
            g2d.setColor(nightColor); //very dark blue
            g2d.fillRect(startCol * tileSize, 
            		startRow * tileSize, 
            		(endCol - startCol) * tileSize, 
            		(endRow - startRow) * tileSize);
        }
        
        //draw rectangle selection (if active)
        if (tileCanvas.getMouseController().getSelectionStartPoint() != null) {
            //calculate selection start point relative to the non scaled map before pan / zoom
            int startX = tileCanvas.getCamera().posX(tileCanvas.getMouseController().getSelectionStartPoint().x, 1);
            int startY = tileCanvas.getCamera().posY(tileCanvas.getMouseController().getSelectionStartPoint().y, 1);
            
            Point current = getMousePosition();
            if (current != null) {
                //calculate current mouse point relative to the non scaled map
                int endX = tileCanvas.getCamera().posX(current.x, 1);
                int endY = tileCanvas.getCamera().posY(current.y, 1);

                int x = Math.min(startX, endX);
                int y = Math.min(startY, endY);
                int w = Math.abs(startX - endX);
                int h = Math.abs(startY - endY);
                
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            	
                g2d.setColor(Utils.COLOR_SAGE_GREEN);
                g2d.fillRect(x, y, w, h);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            	            
                //set stroke width to be x pixels regardless of zoom
                float strokeWidth = (float) (1.5f / tileCanvas.getCamera().getZoomLevel());
                g2d.setStroke(new BasicStroke(strokeWidth));
                g2d.drawRect(x, y, w, h);
            }
            g2d.setPaintMode();
        }
        
        //draw Grid
        if (canvasViewState.isShowGrid()) {
            Stroke oldStroke = g2d.getStroke();
            
            //set stroke width to be 1 pixel regardless of zoom
            float strokeWidth = (float) (1.0f / tileCanvas.getCamera().getZoomLevel());
            g2d.setStroke(new BasicStroke(strokeWidth));
            
            g2d.setColor(Color.BLACK);
            
            //draw horizontal grid lines
            for (int r = startRow; r <= endRow; r++) {
                if (r <= tileCanvas.getMapState().getData().getRows()) {
                    g2d.drawLine(startCol * tileSize, r * tileSize, endCol * tileSize, r * tileSize);
                }
            }
            //draw vertical grid lines
            for (int c = startCol; c <= endCol; c++) {
                if (c <= tileCanvas.getMapState().getData().getCols()) {
                    g2d.drawLine(c * tileSize, startRow * tileSize, c * tileSize, endRow * tileSize);
                }
            }
            
            //restore the original stroke
            g2d.setStroke(oldStroke);
        }
        
        //here we draw the annotation notes on the screen
        if (canvasViewState.isShowNotesTool()) {
        	renderAnnotatedNotes(g2d, viewportTransform, startCol, startRow, endCol, endRow);
        }
        
        //render the toast message notifications
        if (tileCanvas.getToastNotification().getCurrentToastMessage() != null) {
            //save the current transformation state which includes pan / zoom
            AffineTransform originalTransform = g2d.getTransform();
            
            //reset to the viewport state, not an empty Identity
            g2d.setTransform(viewportTransform);
            
            //setup text rendering
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
            		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            Font font = new Font("SansSerif", Font.BOLD, 14);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics(font);
            
            int textWidth = fm.stringWidth(tileCanvas.getToastNotification().getCurrentToastMessage());
            int textHeight = fm.getHeight();
            int padding = 15;
            int boxX = 15;
            int boxY = 15;
            
            //draw transparent background box
            g2d.setColor(new Color(0, 0, 0, 180)); 
            g2d.fillRoundRect(boxX, boxY, textWidth + 2 * padding, textHeight + 2 * padding, 10, 10);

            //draw text
            g2d.setColor(Color.WHITE);
            
            //the fm.getAscent() correctly aligns the text vertically within the box
            g2d.drawString(tileCanvas.getToastNotification().getCurrentToastMessage(), boxX + padding, boxY + padding + fm.getAscent());
            
            //restore the original transformation state for any subsequent drawing
            g2d.setTransform(originalTransform);
        }
        
        if(canvasViewState.isShowTilePosition()) {
        	drawMouseCoordinates(g2d, viewportTransform);
        }
	}
	
	private void drawMouseCoordinates(Graphics2D g2d, AffineTransform viewportTransform) {
	    Point mousePos = getMousePosition();
	    if (mousePos != null) {
	        //calculate tile coordinates next to the mouse cursor
	        int tileX = (int) Math.floor(tileCanvas.getCamera().posX(mousePos.x, tileSize));
	        int tileY = (int) Math.floor(tileCanvas.getCamera().posY(mousePos.y, tileSize));
	        
	        if(tileX < 0 || tileY < 0 || tileX >= tileCanvas.getMapState().getData().getTileMap()[0].length || tileY >= tileCanvas.getMapState().getData().getTileMap().length) 
	            return;
	        
	        //switch to viewport screen space
	        AffineTransform mapTransform = g2d.getTransform();
	        g2d.setTransform(viewportTransform);

	        //setup drawing
	        String coordText = String.format("[X=%d; Y=%d]", tileX, tileY);
	        g2d.setFont(new Font("Monospaced", Font.BOLD, 15));
	        FontMetrics fm = g2d.getFontMetrics();
	        
	        int tw = fm.stringWidth(coordText);
	        int th = fm.getHeight();
	        int pad = 4;
	        
	        int drawX = mousePos.x + 30;
	        int drawY = mousePos.y + 20;

	        //draw background
	        g2d.setColor(new Color(0, 0, 0, 160));
	        g2d.fillRoundRect(drawX, drawY, tw + (pad * 2), th + (pad * 2), 5, 5);

	        //draw text
	        g2d.setColor(Color.CYAN);
	        g2d.drawString(coordText, drawX + pad, drawY + pad + fm.getAscent());

	        //restore
	        g2d.setTransform(mapTransform);
	    }
	}
	
	private void renderAnnotatedNotes(Graphics2D g2d, AffineTransform viewportTransform,
			int startCol, int startRow, int endCol, int endRow) {
		List<MapNote> mapNotes = tileCanvas.getAnnotatedNotesTool().getMapNotes();
        
		//make the circle line not pixel like
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        //size of the circle note
        final int fixedPinSize = 25;
        final int offsetRenderAreaTile = 10;

        for (MapNote note : mapNotes) {
            if (note.getCol() >= startCol - offsetRenderAreaTile && note.getCol() < endCol + offsetRenderAreaTile &&
                note.getRow() >= startRow - offsetRenderAreaTile && note.getRow() < endRow + offsetRenderAreaTile) {
                
            	//temporarily reset transform to switch into viewport screen space
            	AffineTransform mapTransform = g2d.getTransform();
            	g2d.setTransform(viewportTransform);
            	
            	//calculate the raw map center coordinates of this tile, not zoomed and not panned
            	int mapTileCenterX = (note.getCol() * tileSize) + (tileSize / 2);
            	int mapTileCenterY = (note.getRow() * tileSize) + (tileSize / 2);

            	//convert map coordinates to exact screen pixel coordinates
            	double zoom = tileCanvas.getCamera().getZoomLevel();
            	Point offset = tileCanvas.getCamera().getViewOffset();

            	int screenX = (int) (mapTileCenterX * zoom) + offset.x;
            	int screenY = (int) (mapTileCenterY * zoom) + offset.y;
            	
                int offsetX = screenX - (fixedPinSize / 2);
                int offsetY = screenY - (fixedPinSize / 2);
                
                NoteColor noteColor = drawNoteColorCircle(g2d, offsetX, offsetY, note.getColor(), fixedPinSize);

                //draw note text description
                String textMessage = note.getText();
                if (textMessage != null && !textMessage.trim().isEmpty()) {
                	//make the description capped shorter for preview
                    if (textMessage.length() > 22) {
                        textMessage = textMessage.substring(0, 19) + "...";
                    }
                    
                    g2d.setFont(new Font("Arial", Font.PLAIN, 14));
                    FontMetrics textFm = g2d.getFontMetrics();
                    int textWidth = textFm.stringWidth(textMessage);
                    int textHeight = textFm.getHeight();
                    
                    int rectX = screenX - (textWidth / 2) - 5;
                    int rectY = offsetY + fixedPinSize + 4;
                    
                    g2d.setColor(new Color(25, 25, 25, 210)); 
                    g2d.fillRoundRect(rectX, rectY, textWidth + 10, textHeight + 3, 5, 5);
                    
                    g2d.setColor(noteColor.getColorRGBA());
                    g2d.drawRoundRect(rectX, rectY, textWidth + 10, textHeight + 3, 5, 5);
                    
                    g2d.setColor(new Color(240, 240, 240));
                    g2d.drawString(textMessage, rectX + 5, rectY + textFm.getAscent() + 1);
                }
                
                //restore matrix state for the items that are next to render
                g2d.setTransform(mapTransform);
            }
        }
	}
	
	private NoteColor drawNoteColorCircle(Graphics2D g2d, int offsetX, int offsetY, NoteColor color, int fixedPinSize) {
        //draw margin shadow around the circle
        g2d.setColor(new Color(20, 20, 20, 160));
        g2d.fillOval(offsetX - 2, offsetY - 2, fixedPinSize + 4, fixedPinSize + 4);
        
        //draw note pin circle
        NoteColor noteColor = color;
        if (noteColor == null) {
            noteColor = NoteColor.YELLOW; 
        }
        g2d.setColor(noteColor.getColorRGBA());
        g2d.fillOval(offsetX, offsetY, fixedPinSize, fixedPinSize);
        
        //center the N identifier letter from notes
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics badgeFm = g2d.getFontMetrics();
        
        if (noteColor.getColorRGBA().getRed() > 200 && 
        		noteColor.getColorRGBA().getGreen() > 200 && 
        		noteColor.getColorRGBA().getBlue() > 200) {
            g2d.setColor(new Color(30, 30, 30));
        } else {
            g2d.setColor(Color.WHITE);
        }
        
        String noteLabel = "N";
        int labelX = offsetX + (fixedPinSize - badgeFm.stringWidth(noteLabel)) / 2;
        int labelY = offsetY + ((fixedPinSize - badgeFm.getHeight()) / 2) + badgeFm.getAscent();
        g2d.drawString(noteLabel, labelX, labelY);
        
        return noteColor;
	}
	
	private BufferedImage getTintedImage(BufferedImage originalImage, Color tintColor, float alpha) {
	    if (originalImage == null) {
	        return null;
	    }
	    
	    //create a new transparent buffer image with the same dimensions
	    BufferedImage tintedImage = new BufferedImage(
	        originalImage.getWidth(), 
	        originalImage.getHeight(), 
	        BufferedImage.TYPE_INT_ARGB
	    );
	    
	    Graphics2D g2d = tintedImage.createGraphics();
	    
	    //draw the original image onto the buffer to set the shape / pixels
	    g2d.drawImage(originalImage, 0, 0, null);
	    
	    //set the blend mode to apply the source (color) on top of the destination (image)
	    //SRC_ATOP ensures the fill only happens over the images non transparent pixels
	    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
	    
	    //set the tint color and fill the entire buffer
	    g2d.setColor(tintColor);
	    g2d.fillRect(0, 0, originalImage.getWidth(), originalImage.getHeight());
	    
	    g2d.dispose();
	    
	    return tintedImage;
	}
	
	public Color getNightColor() {
		return nightColor;
	}
	
	public float getNightTransparency() {
		return nightTransparency;
	}

	public int getTileSize() {
		return tileSize;
	}

	public void setTileSize(int tileSize) {
		this.tileSize = tileSize;
	}
}