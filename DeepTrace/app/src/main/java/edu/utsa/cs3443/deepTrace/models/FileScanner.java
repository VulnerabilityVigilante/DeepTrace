package edu.utsa.cs3443.deepTrace.models;

import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileScanner {

    private final List<File> suspiciousFiles = new ArrayList<>();

    public List<File> scanFiles() {
        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        scanDirectory(downloads);
        return suspiciousFiles;
    }

    private void scanDirectory(File dir) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else if (isSuspicious(file)) {
                        suspiciousFiles.add(file);
                    }
                }
            }
        }
    }

    private boolean isSuspicious(File file) {
        String name = file.getName().toLowerCase();
        return name.contains("crack") || name.contains("keygen") || name.endsWith(".exe") || name.endsWith(".bat");
    }

    public List<String> getFormattedFindings() {
        List<String> findings = new ArrayList<>();
        for (File file : suspiciousFiles) {
            findings.add(file.getAbsolutePath());
        }
        return findings;
    }

    public String getMalwareDetails() {
        return "Found " + suspiciousFiles.size() + " suspicious files.";
    }
}