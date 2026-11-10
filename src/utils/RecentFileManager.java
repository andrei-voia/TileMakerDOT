package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RecentFileManager {

	private static final String RECENT_FILE_PATH = Utils.ASSETS_SETTINGS_PATH + "recent_files.txt";
	//max number of files that can be saved in the recent function
	private static final int MAX_RECENT_FILES = 12;

	//helper container for file path and saved icon index
	private static class RecentItem {
		File file;
		int iconIndex;

		RecentItem(File file, int iconIndex) {
			this.file = file;
			this.iconIndex = iconIndex;
		}
	}

	private static List<RecentItem> loadItems() {
		List<RecentItem> items = new ArrayList<>();
		Path path = Paths.get(RECENT_FILE_PATH);
		if (!Files.exists(path))
			return items;

		try {
			List<String> lines = Files.readAllLines(path);
			for (String line : lines) {
				if (line.trim().isEmpty())
					continue;

				String[] parts = line.split("\\|");
				File f = new File(parts[0].trim());
				int iconIndex = -1;

				if (parts.length > 1) {
					try {
						iconIndex = Integer.parseInt(parts[1].trim());
					} catch (NumberFormatException ignored) {
					}
				}

				if (f.exists() && f.isFile()) {
					items.add(new RecentItem(f, iconIndex));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return items;
	}

	private static void saveItems(List<RecentItem> items) {
		try {
			Files.createDirectories(Paths.get(Utils.ASSETS_SETTINGS_PATH));
			List<String> lines = new ArrayList<>();
			for (RecentItem item : items) {
				lines.add(item.file.getAbsolutePath() + "|" + item.iconIndex);
			}
			Files.write(Paths.get(RECENT_FILE_PATH), lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<File> getRecentFiles() {
		List<File> files = new ArrayList<>();
		for (RecentItem item : loadItems()) {
			files.add(item.file);
		}
		return files;
	}

	public static int getIconIndex(File file) {
		if (file == null)
			return -1;
		for (RecentItem item : loadItems()) {
			if (item.file.getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath())) {
				return item.iconIndex;
			}
		}
		return -1;
	}

	public static void setIconIndex(File file, int iconIndex) {
		if (file == null)
			return;
		List<RecentItem> items = loadItems();
		boolean found = false;

		for (RecentItem item : items) {
			if (item.file.getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath())) {
				item.iconIndex = iconIndex;
				found = true;
				break;
			}
		}

		if (!found && file.exists()) {
			items.add(0, new RecentItem(file, iconIndex));
		}

		saveItems(items);
	}

	public static void addRecentFile(String filePath) {
		File target = new File(filePath);
		if (!target.exists())
			return;

		List<RecentItem> items = loadItems();
		int existingIndex = -1;

		//preserve assigned icon if the file is already listed
		for (RecentItem item : items) {
			if (item.file.getAbsolutePath().equalsIgnoreCase(target.getAbsolutePath())) {
				existingIndex = item.iconIndex;
				break;
			}
		}

		//remove duplicate if already exists
		items.removeIf(item -> item.file.getAbsolutePath().equalsIgnoreCase(target.getAbsolutePath()));
		//add to top
		items.add(0, new RecentItem(target, existingIndex));

		if (items.size() > MAX_RECENT_FILES) {
			items = items.subList(0, MAX_RECENT_FILES);
		}

		saveItems(items);
	}
	
	public static void clearHistory() {
	    saveItems(new ArrayList<>());
	}
}
