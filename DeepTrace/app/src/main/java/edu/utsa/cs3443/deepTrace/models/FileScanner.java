package edu.utsa.cs3443.deepTrace.models;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileScanner {

    private final List<File> suspiciousFiles = new ArrayList<>();

    // List of common malware-related keywords for heuristic detection
    private static final String[] MALWARE_KEYWORDS = {
            "trojan", "worm", "spyware", "adware", "backdoor", "ransomware", "exploit", "malware", "heur"
    };

    // List of suspicious file extensions for heuristic detection
    private static final String[] SUSPICIOUS_EXTENSIONS = {
            ".exe", ".bat", ".js", ".vbs", ".hta", ".lnk", ".scr", ".zip", ".iso" // Added more extensions
    };


    /**
     * Recursively scans the given folder and returns a list of suspicious files.
     */
    public List<File> scanFolder(File folder) {
        suspiciousFiles.clear();
        scanDirectory(folder);
        return suspiciousFiles;
    }

    private void scanDirectory(File dir) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            Log.d("FileScanner", "Scanning directory: " + dir.getAbsolutePath());
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    Log.d("FileScanner", "Found: " + file.getName());
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else if (isSuspicious(file)) {
                        Log.w("FileScanner", "⚠️ SUSPICIOUS FILE DETECTED: " + file.getAbsolutePath());
                        suspiciousFiles.add(file);
                    }
                }
            }
        }
    }

    /**
     * Checks if a file is suspicious based on its name and heuristic patterns.
     */
    public boolean isSuspicious(File file) {
        String name = file.getName().toLowerCase();
        String absolutePath = file.getAbsolutePath().toLowerCase();

        // Existing checks
        if (name.endsWith(".exe") || name.endsWith(".bat") || name.startsWith(".")) {
            return true;
        }

        // Check for gibberish names
        if (isGibberish(name)) {
            return true;
        }

        // Check for specific hardcoded names in suspicious locations
        if (name.equals("systemupdate.apk") && !absolutePath.contains("system")) {
            return true;
        }
        if (name.endsWith(".apk") && !absolutePath.contains("app")) {
            return true;
        }


        // --- New Heuristic Checks ---

        // 1. Check for common malware keywords in the filename
        for (String keyword : MALWARE_KEYWORDS) {
            if (name.contains(keyword)) {
                Log.d("FileScanner", "Heuristic match: Keyword '" + keyword + "' found in filename.");
                return true;
            }
        }

        // 2. Check for suspicious file extensions
        for (String extension : SUSPICIOUS_EXTENSIONS) {
            if (name.endsWith(extension)) {
                // Additional check for .zip and .iso - could be expanded to look inside if possible
                if (extension.equals(".zip") || extension.equals(".iso")) {
                    // Basic check: just flag the archive. Advanced: attempt to look inside.
                    Log.d("FileScanner", "Heuristic match: Suspicious archive extension '" + extension + "'");
                    return true;
                } else {
                    Log.d("FileScanner", "Heuristic match: Suspicious extension '" + extension + "'");
                    return true;
                }
            }
        }

        // 3. Check for multiple extensions (e.g., ".txt.exe")
        if (hasMultipleExtensions(name)) {
            Log.d("FileScanner", "Heuristic match: Multiple extensions found.");
            return true;
        }

        // 4. Check for common lure names combined with suspicious extensions
        if (isLureNameWithSuspiciousExtension(name)) {
            Log.d("FileScanner", "Heuristic match: Lure name combined with suspicious extension.");
            return true;
        }


        // If none of the checks match, the file is not considered suspicious by this method
        return false;
    }

    private boolean isGibberish(String name) {
        String clean = name.replaceAll("[^a-zA-Z0-9]", "");
        // Refined gibberish check: longer names with few vowels
        if (clean.length() > 15) { // Increased length threshold
            int vowelCount = 0;
            for (char c : clean.toCharArray()) {
                if ("aeiou".contains(String.valueOf(c))) {
                    vowelCount++;
                }
            }
            // If very few vowels relative to length
            if (vowelCount < (clean.length() / 5)) { // Example: less than 20% vowels
                Log.d("FileScanner", "Heuristic match: Gibberish name pattern.");
                return true;
            }
        }
        return false;
    }

    // Helper to check for multiple extensions
    private boolean hasMultipleExtensions(String name) {
        // Check if the name contains more than one dot, and the last dot is not at the beginning
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            String potentialExtensions = name.substring(lastDot);
            int firstDot = name.indexOf('.');
            // If there's another dot before the last one
            if (firstDot != lastDot) {
                return true;
            }
        }
        return false;
    }

    // Helper to check for common lure names with suspicious extensions
    private boolean isLureNameWithSuspiciousExtension(String name) {
        String lowerName = name.toLowerCase();
        // Common lure words
        String[] lureWords = {"invoice", "payment", "report", "document", "cv", "update"};

        boolean containsLureWord = false;
        for (String word : lureWords) {
            if (lowerName.contains(word)) {
                containsLureWord = true;
                break;
            }
        }

        if (containsLureWord) {
            // Check if it also ends with a suspicious executable/script extension
            for (String extension : new String[]{".exe", ".bat", ".js", ".vbs", ".hta", ".scr"}) {
                if (lowerName.endsWith(extension)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Returns a formatted list of findings (file paths) for logging.
     */
    public List<String> getFormattedFindings() {
        List<String> findings = new ArrayList<>();
        for (File file : suspiciousFiles) {
            findings.add(file.getAbsolutePath());
        }
        return findings;
    }
}