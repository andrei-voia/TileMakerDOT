package tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import localization.LocalizationManager;
import main.TileEditor;
import utils.ImageUtils;
import utils.Utils;

public class SpritesheetImporter extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	private static final String NOT_IN_RESOURCES = "NONE";
	private static final String PANEL_BACKGROUND = "Panel.background";
	
	private BufferedImage sheet;
    private int tileSize;
    
    //image coordinates
    private Rectangle selection = new Rectangle(0, 0, 0, 0);
    private Point startPoint; 
    private File destinationFolder;
    
    private double zoomFactor = 1.0;
    private JPanel canvas;
    
    private TileEditor tileEditor;
    
    //needed to calculate drag distance
    private Point lastMousePos;
    
    //used to get the translated strings for the tool sprite sheet
    private LocalizationManager loc;
    
    private List<Rectangle> importedAreas = new ArrayList<>();
    
    public static final Color COLOR_SELECTION_BORDER = new Color(255, 215, 0);
    public static final Color COLOR_SELECTION = new Color(255, 140, 0, 40);
    
    public SpritesheetImporter(JFrame parent, File sheetFile, int tileSize, TileEditor tileEditor) {
        super(parent, "Spritesheet Slicer", true);
        this.tileSize = tileSize;
        this.tileEditor = tileEditor;
        
		loc = LocalizationManager.getInstance();
        
        try {
            sheet = ImageIO.read(sheetFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (!chooseDestination()) {
            dispose();
            return;
        }

        setupUI();
        
        //get the screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        //set window to ~80% of the screen width and height
        int height = (int)(screenSize.height * 0.74);
        int width = (int)(height * 1.3);
        
        //pack realizes the window, making isDisplayable() true
        pack();
        setSize(width, height);
        setLocationRelativeTo(parent);
    }

    private void setupUI() {
        canvas = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
			    super.paintComponent(g);
			    Graphics2D g2 = (Graphics2D) g;
			    
			    //fetch the theme dependent background color from FlatLaf
			    //this ensures it matches the UI whether you are in Dark or Light mode
			    Color themeBackground = UIManager.getColor(PANEL_BACKGROUND);
			    
			    //fallback
			    if (themeBackground == null) themeBackground = Color.GRAY;

			    //fill the entire component area including outside the grid
			    g2.setColor(themeBackground);
			    g2.fillRect(0, 0, getWidth(), getHeight());

			    //apply zoom transformation for the actual image and grid
			    g2.scale(zoomFactor, zoomFactor);
			    
			    //paint the grid background using the same color as the map editor grid background
			    g2.setColor(Color.LIGHT_GRAY);
			    g2.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());

			    //draw Image
			    g2.drawImage(sheet, 0, 0, null);
                
			    //draw already imported areas
                g2.setColor(new Color(0, 255, 100, 80));
                for (Rectangle rect : importedAreas) {
                    g2.fill(rect);
                    g2.setColor(new Color(0, 255, 100, 150));
                    g2.setStroke(new BasicStroke((float)(1 / zoomFactor)));
                    g2.draw(rect);
                    g2.setColor(new Color(0, 255, 100, 80)); //reset fill color for loop
                }
                
                //match map editor grid
                g2.setColor(Color.BLACK);
                
                //keep grid lines 1px thin
                g2.setStroke(new BasicStroke((float)(1 / zoomFactor)));
                
                for (int x = 0; x <= sheet.getWidth(); x += tileSize) {
                    g2.drawLine(x, 0, x, sheet.getHeight());
                }
                for (int y = 0; y <= sheet.getHeight(); y += tileSize) {
                    g2.drawLine(0, y, sheet.getWidth(), y);
                }

                //draw a high contrast selection
                if (selection.width > 0 && selection.height > 0) {
                    g2.setColor(COLOR_SELECTION_BORDER);
                    g2.setStroke(new BasicStroke((float)(2 / zoomFactor)));
                    g2.draw(selection);
                    g2.setColor(COLOR_SELECTION);
                    g2.fill(selection);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension((int)(sheet.getWidth() * zoomFactor), (int)(sheet.getHeight() * zoomFactor));
            }
        };
        
        //make the area outside the image also dark
        canvas.setBackground(Color.LIGHT_GRAY);
        
        JScrollPane scrollPane = new JScrollPane(canvas);
        
        //this fixes the flicker when resizing
        scrollPane.getViewport().setBackground(UIManager.getColor(PANEL_BACKGROUND));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                //right click for panning the view
                if (SwingUtilities.isRightMouseButton(e)) {
                	//store start position
                    lastMousePos = e.getPoint();
                    canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
                //left click for selecting
                else if (SwingUtilities.isLeftMouseButton(e)) {
                    int sx = ((int) (e.getX() / zoomFactor) / tileSize) * tileSize;
                    int sy = ((int) (e.getY() / zoomFactor) / tileSize) * tileSize;
                    startPoint = new Point(sx, sy);
                    selection = new Rectangle(sx, sy, tileSize, tileSize);
                    canvas.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    canvas.setCursor(Cursor.getDefaultCursor());
                    //clear the point
                    lastMousePos = null;
                } else if (selection.width > 0 && selection.height > 0) {
                    processCrop();
                }
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                //right click panning logic
                if (SwingUtilities.isRightMouseButton(e) && lastMousePos != null) {
                    Point currentPos = e.getPoint();
                    
                    //calculate how far the mouse moved
                    int dx = lastMousePos.x - currentPos.x;
                    int dy = lastMousePos.y - currentPos.y;

                    JViewport viewport = scrollPane.getViewport();
                    Point viewPos = viewport.getViewPosition();
                    
                    //move the viewport
                    viewPos.translate(dx, dy);

                    //prevent the panning into the empty void
                    viewPos.x = Math.max(0, Math.min(viewPos.x, canvas.getWidth() - viewport.getWidth()));
                    viewPos.y = Math.max(0, Math.min(viewPos.y, canvas.getHeight() - viewport.getHeight()));

                    viewport.setViewPosition(viewPos);
                }
                //left click selection logic
                else if (SwingUtilities.isLeftMouseButton(e)) {
                    int worldX = (int) (e.getX() / zoomFactor);
                    int worldY = (int) (e.getY() / zoomFactor);

                    int x1 = startPoint.x;
                    int y1 = startPoint.y;
                    int x2 = (worldX / tileSize) * tileSize;
                    int y2 = (worldY / tileSize) * tileSize;

                    selection.x = Math.min(x1, x2);
                    selection.y = Math.min(y1, y2);
                    selection.width = Math.abs(x2 - x1) + tileSize;
                    selection.height = Math.abs(y2 - y1) + tileSize;

                    if (selection.x + selection.width > sheet.getWidth()) selection.width = sheet.getWidth() - selection.x;
                    if (selection.y + selection.height > sheet.getHeight()) selection.height = sheet.getHeight() - selection.y;

                    canvas.repaint();
                }
            }
        });
        
        canvas.addMouseWheelListener(e -> {
            //get the current mouse position relative to the canvas
            Point mousePos = e.getPoint();

            //calculate the world coordinates where the mouse is on the actual image
            double oldZoom = zoomFactor;
            double worldX = mousePos.x / oldZoom;
            double worldY = mousePos.y / oldZoom;

            //perform the zoom
            if (e.getWheelRotation() < 0) zoomFactor *= 1.1;
            else zoomFactor /= 1.1;
            
            //clamp the zoom
            zoomFactor = Math.max(0.1, Math.min(zoomFactor, 5.0));

            //validate so the canvas gets its new preferred size immediately
            canvas.revalidate();

            //calculate where that same world point is at the new zoom level
            int newMouseX = (int) (worldX * zoomFactor);
            int newMouseY = (int) (worldY * zoomFactor);

            //adjust the ScrollPane viewport to center the zoom on the mouse
            JViewport viewport = scrollPane.getViewport();
            Point viewPos = viewport.getViewPosition();

            //calculate the shift needed to keep the mouse point stable
            int scrollX = viewPos.x + (newMouseX - mousePos.x);
            int scrollY = viewPos.y + (newMouseY - mousePos.y);

            //ensure we do not scroll into negative space
            scrollX = Math.max(0, scrollX);
            scrollY = Math.max(0, scrollY);

            viewport.setViewPosition(new Point(scrollX, scrollY));
            canvas.repaint();
        });

        add(scrollPane);
        setSize(1100, 850);
        setLocationRelativeTo(null);
    }

    private boolean chooseDestination() {
    	//start in project root
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle(loc.getString("dialog_destination_folder"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int result = chooser.showOpenDialog(this.getParent()); 
        
        if (result == JFileChooser.APPROVE_OPTION) {
            destinationFolder = chooser.getSelectedFile();
            return true;
        }
        return false;
    }

    private void processCrop() {
        int nextId = getNextAvailableID(destinationFolder);
        String defaultName = nextId + "_newAsset";

        //passing this ensures it pops up in the middle of the importer window
        String input = (String) JOptionPane.showInputDialog(
                this,
                loc.getString("dialog_export_spritesheet_texture_name"), 
                loc.getString("dialog_export") + destinationFolder.getName(), 
                JOptionPane.PLAIN_MESSAGE, 
                null, 
                null, 
                defaultName);

        if (input == null || input.trim().isEmpty()) {
        	resetSelection();
        	return;
        }

        try {
            //remove existing extensions and force .png format
            String cleanName = input.replaceAll("(?i)\\.(png|jpg|jpeg|gif)$", "");
            String fileName = cleanName + ImageUtils.PNG_FORMAT; 
            
            BufferedImage cropped = sheet.getSubimage(selection.x, selection.y, selection.width, selection.height);
            File outputFile = new File(destinationFolder, fileName);
            
            ImageIO.write(cropped, "png", outputFile);
            System.out.println("Spritesheet Saved: " + outputFile.getName());
            
            //add to our visual history
            importedAreas.add(new Rectangle(selection));
            resetSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, loc.getString("dialog_save_failed") + ex.getMessage());
        }
    }

    private int getNextAvailableID(File folder) {
    	String absolutePath = folder.getAbsolutePath().toLowerCase();
    	String category = NOT_IN_RESOURCES;
    	
        //identify which pool we are in
        if (absolutePath.contains("\\tiles\\")) {
            category = Utils.TILES_NAME;
        } else if (absolutePath.contains("\\npcs\\")) {
        	category = Utils.NPCS_NAME;
        } else if (absolutePath.contains("\\objects\\")){
        	category = Utils.OBJECTS_NAME;
        }
        
        //if selected path is in the assets folder
        if(!category.equals(NOT_IN_RESOURCES)) {
	        File categoryRoot = new File(tileEditor.getLoadedSetup().getResourceBasePath(), category);
	        
	        //map out every ID currently used in this category globally
	        Set<Integer> globalUsedIDs = new HashSet<>();
	        Utils.collectExistingIDs(categoryRoot, globalUsedIDs);
	        
			//find the first ID >= searchStart that is not used anywhere in the category
			int nextAvailableID = 1;
			while (globalUsedIDs.contains(nextAvailableID)) {
				nextAvailableID++;
			}
			
			return nextAvailableID;
        }
    	
        //if the file is not in the assets folder for the game resource then compare only inside
        else {
            int maxId = -1;
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(ImageUtils.PNG_FORMAT));
            if (files != null) {
                for (File f : files) {
                    try {
                        String idPart = f.getName().split("_")[0];
                        int id = Integer.parseInt(idPart);
                        if (id > maxId) maxId = id;
                    } catch (Exception e) {}
                }
            }
            return maxId + 1;
        }
    }
    
    private void resetSelection() {
        //reset the yellow selection so it does not stay on top
        selection = new Rectangle(0, 0, 0, 0);
        canvas.repaint();
    }
}
