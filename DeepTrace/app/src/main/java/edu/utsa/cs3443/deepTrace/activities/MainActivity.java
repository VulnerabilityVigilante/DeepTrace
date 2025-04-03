package edu.utsa.cs3443.deepTrace.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.models.ActivityLogger;
import edu.utsa.cs3443.deepTrace.models.FileScanner;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    FileScanner scanner;
    ActivityLogger logger;
    // Use the app's external files directory as our base folder.
    File appFilesDir;
    // We'll create (or use) a "tmp" subdirectory for storing and scanning files.
    File tmpDir;

    // Mapping from the imported file (in tmp) to its original location.
    public static HashMap<String, String> importedFileOriginalPaths = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanner = new FileScanner();
        logger = new ActivityLogger();

        if (!hasStoragePermission()) {
            requestStoragePermission();
        } else {
            setupDemoFile();
        }
    }

    /**
     * Sets up the demo environment by:
     * 1. Obtaining the app's external files directory.
     * 2. Creating (if needed) a "tmp" subdirectory.
     * 3. Creating the demo file ("crackme_demo.exe") inside the tmp folder.
     */
    private void setupDemoFile() {
        appFilesDir = getExternalFilesDir(null);
        if (appFilesDir == null) {
            Toast.makeText(this, "App files directory unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create or reuse the "tmp" subdirectory within the app's files directory.
        tmpDir = new File(appFilesDir, "tmp");
        if (!tmpDir.exists() && tmpDir.mkdirs()) {
            Toast.makeText(this, "Created tmp folder", Toast.LENGTH_SHORT).show();
        } else if (tmpDir.exists()){
            Toast.makeText(this, "tmp folder already exists", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to create tmp folder", Toast.LENGTH_SHORT).show();
        }
        // Create the demo suspicious file in the tmp directory.
        File demoFile = new File(tmpDir, "crackme_demo.exe");
        if (!demoFile.exists()) {
            try (FileOutputStream fos = new FileOutputStream(demoFile)) {
                fos.write("This is a demo suspicious file".getBytes());
                Toast.makeText(this, "Demo file created in tmp for testing", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to create demo file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Demo file already exists in tmp", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * When "Scan" is clicked:
     * - Attempts to pull suspicious files from an external directory (Downloads) into tmpDir.
     * - If external directories are inaccessible, shows an appropriate message.
     * - Then scans the tmp directory for suspicious files.
     * - Launches ResultActivity with the scan results.
     */
    public void onScanClick(View view) {
        if (tmpDir == null || !tmpDir.exists()) {
            Toast.makeText(this, "tmp directory not available", Toast.LENGTH_SHORT).show();
            return;
        }
        // Attempt to pull suspicious files from the Downloads folder into tmpDir.
        pullSuspiciousFilesFromExternalDirs(tmpDir);
        List<File> suspiciousFiles = scanner.scanFolder(tmpDir);
        logger.logScan(this, scanner.getFormattedFindings());
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("hasThreats", !suspiciousFiles.isEmpty());
        intent.putExtra("demoFolderPath", tmpDir.getAbsolutePath());
        startActivity(intent);
    }

    /**
     * Attempts to copy suspicious files from external non-root system directories
     * (e.g. the Downloads folder) into the provided destination directory.
     * Also records the original location of each imported file.
     * If the external directory is not accessible or no suspicious files are found,
     * a toast message is shown.
     */
    private void pullSuspiciousFilesFromExternalDirs(File destDir) {
        // First, check the permission explicitly.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Access to external directories denied due to permissions", Toast.LENGTH_SHORT).show();
            return;
        }

        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloads == null || !downloads.exists() || !downloads.isDirectory()) {
            Toast.makeText(this, "External directory is not accessible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to list files, catching any potential SecurityException.
        File[] files;
        try {
            files = downloads.listFiles();
        } catch (SecurityException se) {
            Toast.makeText(this, "Access to external directories denied due to permissions", Toast.LENGTH_SHORT).show();
            return;
        }

        if (files == null) {
            Toast.makeText(this, "Unable to read external directory", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean foundAny = false;
        for (File file : files) {
            if (scanner.isSuspicious(file)) {
                File destFile = new File(destDir, file.getName());
                copyFile(file, destFile);
                // Record the original location so that later deletion targets the source file.
                MainActivity.importedFileOriginalPaths.put(destFile.getAbsolutePath(), file.getAbsolutePath());
                foundAny = true;
            }
        }
        if (!foundAny) {
            Toast.makeText(this, "No suspicious files found in external directories", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Copies the source file to the destination file.
     */
    private void copyFile(File src, File dest) {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to copy file: " + src.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onSettingsClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private boolean hasStoragePermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. Setting up demo file.", Toast.LENGTH_SHORT).show();
                setupDemoFile();
            } else {
                Toast.makeText(this, "Permission denied. External directories may not be scanned.", Toast.LENGTH_LONG).show();
                // Even if denied, we still set up the demo file.
                setupDemoFile();
            }
        }
    }
}
