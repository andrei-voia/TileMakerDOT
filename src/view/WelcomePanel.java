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

public class WelcomePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	public static final String APP_ICON_PATH = "assets/icons/tile_editor_icon.png";
	
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
			Image img = new ImageIcon(iconFile.getAbsolutePath()).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
			welcomeTitle.setIcon(new ImageIcon(img));
		}

		JPanel titleSeparator = new JPanel();
		titleSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		titleSeparator.setPreferredSize(new Dimension(10, 1));
		titleSeparator.setBackground(borderColor);
		titleSeparator.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel recentLabel = new JLabel(loc.getString("welcome_recent_projects"));
		recentLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
		recentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		headerPanel.add(welcomeTitle);
		headerPanel.add(Box.createVerticalStrut(15));
		headerPanel.add(titleSeparator);
		headerPanel.add(Box.createVerticalStrut(20));
		headerPanel.add(recentLabel);

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
		JPanel row = new JPanel(new BorderLayout(15, 0));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		row.setPreferredSize(new Dimension(0, 50));

		//base background matching the active theme
		Color defaultBg = UIManager.getColor("Panel.background");
		if (defaultBg == null) {
			defaultBg = row.getBackground();
		}
		row.setBackground(defaultBg);

		row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), 
				new EmptyBorder(6, 12, 6, 12)));
		//sets a different cursor hand when hovering over recent saved files
		row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		//icon setup
		JLabel iconLabel = new JLabel();
		File iconFile = new File(APP_ICON_PATH);
		if (iconFile.exists()) {
			ImageIcon rawIcon = new ImageIcon(iconFile.getAbsolutePath());
			Image scaledImg = rawIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
			iconLabel.setIcon(new ImageIcon(scaledImg));
		}

		//saved files text labels
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

		row.add(iconLabel, BorderLayout.WEST);
		row.add(infoPanel, BorderLayout.CENTER);
		row.add(fileDateLabel, BorderLayout.EAST);

		//mouse listener querying UIManager dynamically for theme switches
		row.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				Color hoverBg = UIManager.getColor("List.hoverBackground");
				if (hoverBg == null)
					hoverBg = UIManager.getColor("List.selectionBackground");
				if (hoverBg == null)
					hoverBg = UIManager.getColor("Component.accentColor");

				row.setBackground(hoverBg);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				Color normalBg = UIManager.getColor("Panel.background");
				if (normalBg == null)
					normalBg = UIManager.getColor("List.background");

				row.setBackground(normalBg);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				listener.onLoadRecent(file);
			}
		});

		return row;
	}
}
