package view;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import localization.LocalizationManager;
import utils.ApplicationLegend;
import utils.RecentFileManager;
import utils.Utils;

public class WelcomePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	public static final String APP_ICON_PATH = "assets/icons/tile_editor_icon.png";
	public static final String APP_ICON_DEFAULT_SAVE_PATH = "assets/icons/recent_saves/tile_editor_save1.png";
	
	private LocalizationManager loc = LocalizationManager.getInstance();

	public interface WelcomeActionListener {
		void onNewMap();
		void onOpenMap();
		void onLoadRecent(File mapFile);
	}

	private WelcomeActionListener listener;

	public WelcomePanel(WelcomeActionListener listener) {
		this.listener = listener;
		setLayout(new BorderLayout());
		
        //localize option buttons and file chooser
        Utils.initLocalizedDialogs();

		add(createLeftSidebar(), BorderLayout.WEST);
		add(createMainContent(), BorderLayout.CENTER);
	}

	private JPanel createLeftSidebar() {
		JPanel sidebar = new JPanel(new BorderLayout());
		sidebar.setPreferredSize(new Dimension(200, 0));

		Color borderColor = UIManager.getColor("Component.borderColor");
		if (borderColor == null)
			borderColor = new Color(60, 60, 60);

		sidebar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, borderColor),
				new EmptyBorder(20, 15, 20, 15)));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.setOpaque(false);

		JButton newMapBtn = new JButton(loc.getString("welcome_new_map"));
		//forces the preferred height to 48px
		newMapBtn.setPreferredSize(new Dimension(0, 48));
		newMapBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
		//ensures FlatLaf respects height
		newMapBtn.putClientProperty("JButton.minimumHeight", 48);
		newMapBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
		newMapBtn.addActionListener(e -> listener.onNewMap());

		newMapBtn.addAncestorListener(new javax.swing.event.AncestorListener() {
			@Override
			public void ancestorAdded(javax.swing.event.AncestorEvent event) {
				JRootPane root = SwingUtilities.getRootPane(newMapBtn);
				if (root != null) {
					root.setDefaultButton(newMapBtn);
				}
			}

			@Override
			public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
			}

			@Override
			public void ancestorMoved(javax.swing.event.AncestorEvent event) {
			}
		});

		JButton openMapBtn = new JButton(loc.getString("welcome_open_map"));
		//forces the preferred height to 48px
		openMapBtn.setPreferredSize(new Dimension(0, 48));
		openMapBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
		//ensures FlatLaf respects height
		openMapBtn.putClientProperty("JButton.minimumHeight", 48);
		openMapBtn.addActionListener(e -> listener.onOpenMap());

		buttonPanel.add(newMapBtn);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(openMapBtn);

		//the side bar footer displays the application version
		JLabel versionLabel = new JLabel(ApplicationLegend.APP_VERSION, SwingConstants.CENTER);
		versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
		versionLabel.setEnabled(false);

		sidebar.add(buttonPanel, BorderLayout.NORTH);
		sidebar.add(versionLabel, BorderLayout.SOUTH);

		return sidebar;
	}

	private JPanel createMainContent() {
		JPanel mainContent = new JPanel(new BorderLayout(0, 20));
		mainContent.setBorder(new EmptyBorder(30, 40, 30, 40));

		Color borderColor = UIManager.getColor("Component.borderColor");
		if (borderColor == null)
			borderColor = new Color(60, 60, 60);

		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setOpaque(false);

		//the header title with icon attached
		JLabel welcomeTitle = new JLabel(" " + loc.getString("welcome_title"));
		welcomeTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
		welcomeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

		File iconFile = new File(APP_ICON_PATH);
		if (iconFile.exists()) {
			Image img = new ImageIcon(iconFile.getAbsolutePath()).getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
			welcomeTitle.setIcon(new ImageIcon(img));
		}

		JPanel titleSeparator = new JPanel();
		titleSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		titleSeparator.setPreferredSize(new Dimension(10, 1));
		titleSeparator.setBackground(borderColor);
		titleSeparator.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel recentHeaderSubPanel = new JPanel(new BorderLayout());
		recentHeaderSubPanel.setOpaque(false);
		recentHeaderSubPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		recentHeaderSubPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

		JLabel recentLabel = new JLabel(loc.getString("welcome_recent_projects"));
		recentLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

		//add a clear history button for the recent projects section
		JButton clearHistoryBtn = new JButton(loc.getString("welcome_clear_history"));
		clearHistoryBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
		clearHistoryBtn.setPreferredSize(new Dimension(120, 32));
		clearHistoryBtn.putClientProperty("JButton.minimumHeight", 32);
		clearHistoryBtn.setFocusPainted(false);

		if (RecentFileManager.getRecentFiles().isEmpty()) {
		    clearHistoryBtn.setEnabled(false);
		}

		clearHistoryBtn.addActionListener(e -> {
		    int confirm = JOptionPane.showConfirmDialog(
		        WelcomePanel.this,
		        loc.getString("welcome_clear_history_message"),
		        loc.getString("welcome_clear_history_title"),
		        JOptionPane.YES_NO_OPTION,
		        JOptionPane.WARNING_MESSAGE
		    );
		    if (confirm == JOptionPane.YES_OPTION) {
		        RecentFileManager.clearHistory();
		        refreshPanel();
		    }
		});

		recentHeaderSubPanel.add(recentLabel, BorderLayout.WEST);
		recentHeaderSubPanel.add(clearHistoryBtn, BorderLayout.EAST);

		headerPanel.add(welcomeTitle);
		headerPanel.add(Box.createVerticalStrut(15));
		headerPanel.add(titleSeparator);
		headerPanel.add(Box.createVerticalStrut(20));
		headerPanel.add(recentHeaderSubPanel);

		mainContent.add(headerPanel, BorderLayout.NORTH);

		JPanel recentListPanel = new JPanel();
		recentListPanel.setLayout(new BoxLayout(recentListPanel, BoxLayout.Y_AXIS));
		recentListPanel.setOpaque(false);

		List<File> recentFiles = RecentFileManager.getRecentFiles();

		if (recentFiles.isEmpty()) {
			JLabel emptyLabel = new JLabel(loc.getString("welcome_recent_not_found"));
			emptyLabel.setEnabled(false);
			mainContent.add(emptyLabel, BorderLayout.CENTER);
		} else {
			for (File file : recentFiles) {
				recentListPanel.add(createRecentFileRow(file));
				recentListPanel.add(Box.createVerticalStrut(8));
			}

			JScrollPane scrollPane = new JScrollPane(recentListPanel);
			scrollPane.setOpaque(false);
			scrollPane.getViewport().setOpaque(false);
			scrollPane.setBorder(null);
			mainContent.add(scrollPane, BorderLayout.CENTER);
		}

		return mainContent;
	}

	private JPanel createRecentFileRow(File file) {
	    JPanel row = new JPanel(new BorderLayout(0, 0));
	    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
	    row.setPreferredSize(new Dimension(0, 50));

	    //the base background will match the active theme
	    Color defaultBg = UIManager.getColor("Panel.background");
	    if (defaultBg == null) {
	        defaultBg = row.getBackground();
	    }
	    row.setBackground(defaultBg);
	    row.setBorder(BorderFactory.createEtchedBorder());
	    
	    //here we set the icon of the  saved file
	    JLabel iconLabel = new JLabel() {
	        private static final long serialVersionUID = 1L;
	        private boolean hovered = false;

	        {
	            addMouseListener(new MouseAdapter() {
	                @Override
	                public void mouseEntered(MouseEvent e) {
	                    hovered = true;
	                    repaint();
	                }

	                @Override
	                public void mouseExited(MouseEvent e) {
	                    hovered = false;
	                    repaint();
	                }

	                @Override
	                public void mouseClicked(MouseEvent e) {
	                	//we can change the icon
	                    showIconSelectionDialog(file);
	                }
	            });
	        }

	        @Override
	        protected void paintComponent(Graphics g) {
	            super.paintComponent(g);
	            if (hovered) {
	                Graphics2D g2 = (Graphics2D) g.create();
	                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	                //make a dark dimming layer matching the full hit area
	                g2.setColor(new Color(0, 0, 0, 130));
	                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);

	                //draw a centered pencil icon when hovered over
	                g2.setColor(Color.WHITE);
	                g2.setFont(new Font("SansSerif", Font.BOLD, 16));
	                FontMetrics fm = g2.getFontMetrics();
	                int x = (getWidth() - fm.stringWidth("✏")) / 2;
	                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
	                g2.drawString("✏", x, y);

	                g2.dispose();
	            }
	        }
	    };

	    //set the size of the icon
	    iconLabel.setPreferredSize(new Dimension(54, 50));
	    iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    iconLabel.setVerticalAlignment(SwingConstants.CENTER);
	    iconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    iconLabel.setToolTipText(loc.getString("welcome_icon_tooltip"));

	    File iconFile = new File(getIconPathForFile(file));
	    if (iconFile.exists()) {
	        ImageIcon rawIcon = new ImageIcon(iconFile.getAbsolutePath());
	        Image scaledImg = rawIcon.getImage().getScaledInstance(42, 42, Image.SCALE_SMOOTH);
	        iconLabel.setIcon(new ImageIcon(scaledImg));
	    }

	    //right content panel, file path info and modified date
	    JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
	    contentPanel.setOpaque(false);
	    contentPanel.setBorder(new EmptyBorder(6, 10, 6, 12));
	    contentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

	    JLabel fileNameLabel = new JLabel(file.getName());
	    fileNameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

	    JLabel filePathLabel = new JLabel(file.getAbsolutePath());
	    filePathLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
	    filePathLabel.setEnabled(false);

	    JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
	    infoPanel.setOpaque(false);
	    infoPanel.add(fileNameLabel);
	    infoPanel.add(filePathLabel);

	    String modifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(file.lastModified()));
	    JLabel fileDateLabel = new JLabel(modifiedDate);
	    fileDateLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
	    fileDateLabel.setEnabled(false);

	    contentPanel.add(infoPanel, BorderLayout.CENTER);
	    contentPanel.add(fileDateLabel, BorderLayout.EAST);

	    row.add(iconLabel, BorderLayout.WEST);
	    row.add(contentPanel, BorderLayout.CENTER);

	    //the row hover state and map loading handler
	    row.addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseEntered(MouseEvent e) {
	            Color hoverBg = UIManager.getColor("List.hoverBackground");
	            if (hoverBg == null) hoverBg = UIManager.getColor("List.selectionBackground");
	            if (hoverBg == null) hoverBg = UIManager.getColor("Component.accentColor");

	            row.setBackground(hoverBg);
	        }

	        @Override
	        public void mouseExited(MouseEvent e) {
	            Color normalBg = UIManager.getColor("Panel.background");
	            if (normalBg == null) normalBg = UIManager.getColor("List.background");

	            row.setBackground(normalBg);
	        }

	        @Override
	        public void mouseClicked(MouseEvent e) {
	            listener.onLoadRecent(file);
	        }
	    });

	    return row;
	}
	
	//returns the correct saved icon image path
	private String getIconPathForFile(File file) {
	    if (file == null) {
	        return APP_ICON_DEFAULT_SAVE_PATH;
	    }

	    //check if the file has a custom user assigned icon index, -1 is default
	    int customIndex = RecentFileManager.getIconIndex(file);
	    int iconIndex;

	    if (customIndex > 0) {
	        iconIndex = customIndex;
	    } else {
	    	//fall back to use absolute hash code of the filename to get a fixed index
	        int hash = Math.abs(file.getName().toLowerCase().hashCode());
	        iconIndex = (hash % 9) + 1;
	    }
	    
	    String dynamicPath = "assets/icons/recent_saves/tile_editor_save" + iconIndex + ".png";
	    File dynamicFile = new File(dynamicPath);
	    return dynamicFile.exists() ? dynamicPath : APP_ICON_DEFAULT_SAVE_PATH;
	}
	
	private void refreshPanel() {
	    removeAll();
	    add(createLeftSidebar(), BorderLayout.WEST);
	    add(createMainContent(), BorderLayout.CENTER);
	    revalidate();
	    repaint();
	}
	
	private void showIconSelectionDialog(File file) {
	    Window parentWindow = SwingUtilities.getWindowAncestor(this);
	    JDialog dialog = new JDialog(parentWindow, loc.getString("welcome_icon_select") + " - " + file.getName(), 
	    		Dialog.ModalityType.APPLICATION_MODAL);
	    dialog.setLayout(new BorderLayout(0, 0));

	    //define a 3x3 grid panel for preset icons
	    JPanel gridPanel = new JPanel(new GridLayout(3, 3, 12, 12));
	    gridPanel.setBorder(new EmptyBorder(15, 15, 12, 15));

	    for (int i = 1; i <= 9; i++) {
	        final int iconIndex = i;
	        String path = "assets/icons/recent_saves/tile_editor_save" + iconIndex + ".png";
	        
	        JButton iconBtn = new JButton();
	        iconBtn.setPreferredSize(new Dimension(96, 96));
	        iconBtn.setFocusPainted(false);

	        File imgFile = new File(path);
	        if (imgFile.exists()) {
	            Image scaled = new ImageIcon(path).getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
	            iconBtn.setIcon(new ImageIcon(scaled));
	        } else {
	            iconBtn.setText("#" + i);
	        }

	        iconBtn.addActionListener(e -> {
	            RecentFileManager.setIconIndex(file, iconIndex);
	            dialog.dispose();
	            refreshPanel();
	        });
	        
	        gridPanel.add(iconBtn);
	    }

	    //bottom section for auto image with separator line and full width and double height button
	    JPanel bottomPanel = new JPanel(new BorderLayout(0, 12));
	    bottomPanel.setBorder(new EmptyBorder(0, 15, 15, 15));

	    JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);

	    JButton autoBtn = new JButton(loc.getString("welcome_icon_default"));
	    autoBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
	    autoBtn.setPreferredSize(new Dimension(0, 48));
	    autoBtn.putClientProperty("JButton.minimumHeight", 48);
	    autoBtn.setFocusPainted(false);
	    autoBtn.addActionListener(e -> {
	        RecentFileManager.setIconIndex(file, -1);
	        dialog.dispose();
	        refreshPanel();
	    });

	    bottomPanel.add(separator, BorderLayout.NORTH);
	    bottomPanel.add(autoBtn, BorderLayout.CENTER);

	    dialog.add(gridPanel, BorderLayout.CENTER);
	    dialog.add(bottomPanel, BorderLayout.SOUTH);
	    dialog.pack();
	    dialog.setLocationRelativeTo(this);
	    dialog.setVisible(true);
	}
}
