package edu.utsa.cs3443.deepTrace.models;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileScanner {

    private final List<File> suspiciousFiles = new ArrayList<>();

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
     * Checks if a file is suspicious based on its name.
     */
    public boolean isSuspicious(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".exe") || name.endsWith(".bat") ||
                name.startsWith(".") || isGibberish(name) ||
                (name.equals("systemupdate.apk") && !file.getAbsolutePath().contains("system")) ||
                (name.endsWith(".apk") && !file.getAbsolutePath().contains("app"));
    }

    private boolean isGibberish(String name) {
        String clean = name.replaceAll("[^a-zA-Z0-9]", "");
        return clean.length() > 12 && !Pattern.compile("[aeiou]").matcher(clean).find();
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
