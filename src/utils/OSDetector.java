package utils;

import java.util.Locale;


//Detects OS
public final class OSDetector {
    
    private OSDetector() {}
    
    //Returns OS name
        public static String detectOS() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            return "Windows";
        } else if (osName.contains("mac")) {
            return "macOS";
        } else if (osName.contains("linux")) {
            return "Linux";
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return "Unix";
        } else {
            return "Unknown OS";
        }
    }
}