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

    public static List<File> getRecentFiles() {
        List<File> files = new ArrayList<>();
        Path path = Paths.get(RECENT_FILE_PATH);
        if (!Files.exists(path)) return files;

        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                File f = new File(line.trim());
                if (f.exists() && f.isFile()) {
                    files.add(f);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    public static void addRecentFile(String filePath) {
        List<File> current = getRecentFiles();
        File target = new File(filePath);

        //remove duplicate if already exists
        current.removeIf(f -> f.getAbsolutePath().equalsIgnoreCase(target.getAbsolutePath()));
        //add to top
        current.add(0, target);
        
        if (current.size() > MAX_RECENT_FILES) {
            current = current.subList(0, MAX_RECENT_FILES);
        }

        //save back to file
        try {
            Files.createDirectories(Paths.get(Utils.ASSETS_SETTINGS_PATH));
            List<String> paths = new ArrayList<>();
            for (File f : current) {
                paths.add(f.getAbsolutePath());
            }
            Files.write(Paths.get(RECENT_FILE_PATH), paths);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
