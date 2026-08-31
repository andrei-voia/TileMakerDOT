package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IDLoader {

	//maps to store Name -> ID for fast lookup
    private Map<String, Integer> tileIds = new HashMap<>();
    private Map<String, Integer> objectIds = new HashMap<>();
    private Map<String, Integer> npcIds = new HashMap<>();

    public void loadUniqueIDsList(File file) {
        //reset maps in case this is called multiple times
        tileIds.clear();
        objectIds.clear();
        npcIds.clear();

        //REGEX explanation
        //\d+\.         -> matches the line number (e.g. "1.")
        //\s+           -> matches spaces
        //(\d+)         -> group 1: Captures the ID number
        //\s+\(Name:\s+ -> matches the " (Name: " part
        //([^)]+)       -> group 2: Captures everything until the closing ")"
        Pattern pattern = Pattern.compile("\\d+\\.\\s+(\\d+)\\s+\\(Name:\\s+([^)]+)\\)");

        //track which section we are currently reading
        int currentSection = 0; //1 = Tiles, 2 = Objects, 3 = NPCs

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                //identify sections
                if (line.startsWith("# Tiles IDs")) {
                    currentSection = 1;
                    continue;
                } else if (line.startsWith("# Objects IDs")) {
                    currentSection = 2;
                    continue;
                } else if (line.startsWith("# NPCs IDs")) {
                    currentSection = 3;
                    continue;
                }

                //parse data lines
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    int id = Integer.parseInt(matcher.group(1));
                    String name = matcher.group(2);

                    switch (currentSection) {
                        case 1 -> tileIds.put(name, id);
                        case 2 -> objectIds.put(name, id);
                        case 3 -> npcIds.put(name, id);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //helper method to get an ID by name
    public boolean containsTileId(String name) {
        return tileIds.containsKey(name); 
    }
    
    public boolean containsObjectId(String name) {
        return objectIds.containsKey(name);
    }
    
    public boolean containsNPCId(String name) {
        return npcIds.containsKey(name); 
    }
}
