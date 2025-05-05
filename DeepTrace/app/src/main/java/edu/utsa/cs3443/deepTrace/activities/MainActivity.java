package edu.utsa.cs3443.deepTrace.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log; // Import Log
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet; // Import HashSet
import java.util.Set; // Import Set


import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.models.ActivityLogger;
import edu.utsa.cs3443.deepTrace.models.CsvPathScanner; // Keep import in case needed elsewhere
import edu.utsa.cs3443.deepTrace.models.FileScanner;
import edu.utsa.cs3443.deepTrace.models.HistoryLogger;
import edu.utsa.cs3443.deepTrace.models.LastLogger;
import edu.utsa.cs3443.deepTrace.models.Settings;
import edu.utsa.cs3443.deepTrace.models.VirusDatabase;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    public static LastLogger lastLogger;
    private HistoryLogger historyLogger;
    FileScanner scanner;
    ActivityLogger logger;
    File appFilesDir;
    File tmpDir;
    MediaPlayer mediaPlayer;

    public static HashMap<String, String> importedFileOriginalPaths = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Settings.init(getApplicationContext());

        scanner = new FileScanner();
        logger = new ActivityLogger();

        if (!hasStoragePermission()) {
            requestStoragePermission();
        } else {
            setupDemoFiles(); // Renamed the method
        }

        lastLogger = new LastLogger("no scans have been done");
        LastLogger.ensureLastLogFile(this);
        try {
            lastLogger.loadLastTime(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        historyLogger = new HistoryLogger();
        HistoryLogger.ensureHistoryLog(this);

        float fontSizeSp = Settings.getFontSize();

        TextView homeTitle = findViewById(R.id.homeTitle);
        Button scanBtn = findViewById(R.id.scanBtn);
        Button settingsBtn = findViewById(R.id.settingsBtn);

        homeTitle.setTextSize(fontSizeSp);
        scanBtn.setTextSize(fontSizeSp);
        settingsBtn.setTextSize(fontSizeSp);

        applyFontSizes(Settings.getFontSize());
        applyDarkModeBackground();
        loadGifBackground();
        // Call setupBackgroundMusic here, but start it in onResume
        setupBackgroundMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MusicDebug", "onResume called");
        applyFontSizes(Settings.getFontSize());
        applyDarkModeBackground();
        loadGifBackground();

        boolean isDarkMode = Settings.getSetting("dark mode");
        Log.d("MusicDebug", "Dark mode setting in onResume: " + isDarkMode);

        // Start music here if it's not null and not already playing, and not in dark mode
        if (!isDarkMode) {
            if (mediaPlayer != null) {
                Log.d("MusicDebug", "mediaPlayer is not null in onResume. isPlaying: " + mediaPlayer.isPlaying());
                if (!mediaPlayer.isPlaying()) {
                    Log.d("MusicDebug", "Starting music in onResume.");
                    mediaPlayer.start();
                } else {
                    Log.d("MusicDebug", "Music is already playing in onResume.");
                }
            } else {
                Log.d("MusicDebug", "mediaPlayer is null in onResume.");
                // Attempt to set up music again if null (e.g., activity recreated)
                setupBackgroundMusic();
                if (mediaPlayer != null) {
                    Log.d("MusicDebug", "mediaPlayer created in onResume after being null. Starting music.");
                    mediaPlayer.start();
                } else {
                    Log.d("MusicDebug", "Failed to create mediaPlayer in onResume.");
                }
            }
        } else {
            Log.d("MusicDebug", "Music not started in onResume because dark mode is enabled.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MusicDebug", "onPause called");
        // Pause music when the activity is not visible
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.d("MusicDebug", "Pausing music in onPause.");
            mediaPlayer.pause();
        } else if (mediaPlayer != null) {
            Log.d("MusicDebug", "mediaPlayer not playing in onPause. State: " + (mediaPlayer.isPlaying() ? "playing" : "paused/stopped/error"));
        } else {
            Log.d("MusicDebug", "mediaPlayer is null in onPause.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MusicDebug", "onDestroy called");
        // Release MediaPlayer resources when the activity is destroyed
        if (mediaPlayer != null) {
            Log.d("MusicDebug", "Releasing mediaPlayer in onDestroy.");
            mediaPlayer.release();
            mediaPlayer = null;
        } else {
            Log.d("MusicDebug", "mediaPlayer is null in onDestroy.");
        }
    }

    private void applyFontSizes(float sp) {
        TextView homeTitle = findViewById(R.id.homeTitle);
        Button scanBtn = findViewById(R.id.scanBtn);
        Button settingsBtn = findViewById(R.id.settingsBtn);

        homeTitle.setTextSize(sp);
        scanBtn.setTextSize(sp);
        settingsBtn.setTextSize(sp);
    }

    private void applyDarkModeBackground() {
        View root = findViewById(R.id.mainLayout);

        if (Settings.getSetting("dark mode")) {
            root.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            root.setBackgroundColor(getResources().getColor(R.color.background));
        }
    }

    private void loadGifBackground() {
        ImageView gifBg = findViewById(R.id.gifBackground);
        if (gifBg != null) {
            if (Settings.getSetting("dark mode")) {
                gifBg.setVisibility(View.GONE);
            } else {
                gifBg.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .asGif()
                        .load(R.drawable.matrix_background)
                        .into(gifBg);
            }
        }
    }

    // Renamed and adjusted to only set up the MediaPlayer
    private void setupBackgroundMusic() {
        Log.d("MusicDebug", "setupBackgroundMusic called");
        boolean isDarkMode = Settings.getSetting("dark mode");
        Log.d("MusicDebug", "Dark mode setting in setupBackgroundMusic: " + isDarkMode);

        // Create the MediaPlayer instance if it's null and not in dark mode
        if (!isDarkMode) {
            if (mediaPlayer == null) {
                try {
                    mediaPlayer = MediaPlayer.create(this, R.raw.matrix_theme);
                    if (mediaPlayer != null) {
                        mediaPlayer.setLooping(true); // Set looping here
                        Log.d("MusicDebug", "mediaPlayer successfully created.");

                        // --- Added Listeners ---
                        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                Log.e("MusicDebug", "MediaPlayer error: what=" + what + ", extra=" + extra);
                                // Return true to indicate the error was handled
                                return true;
                            }
                        });

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                Log.d("MusicDebug", "MediaPlayer completed playback.");
                                // Music is looping, so completion shouldn't happen normally unless there's an issue
                            }
                        });
                        // --- End Added Listeners ---

                    } else {
                        Log.e("MusicDebug", "mediaPlayer creation returned null.");
                    }
                } catch (Exception e) {
                    Log.e("MusicDebug", "Error creating mediaPlayer: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                Log.d("MusicDebug", "mediaPlayer is not null in setupBackgroundMusic.");
            }
        } else {
            // If dark mode is enabled and mediaPlayer exists, release it
            if (mediaPlayer != null) {
                Log.d("MusicDebug", "Dark mode enabled, releasing mediaPlayer in setupBackgroundMusic.");
                mediaPlayer.release();
                mediaPlayer = null;
            } else {
                Log.d("MusicDebug", "Dark mode enabled and mediaPlayer is already null.");
            }
        }
    }


    public void onScanClick(View view) {
        if (tmpDir == null || !tmpDir.exists()) {
            Toast.makeText(this, "tmp directory not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pause music during scan (optional but good for performance/UX)
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.d("MusicDebug", "Pausing music for scan.");
            mediaPlayer.pause();
        }


        // Copy the virus database from assets
        File virusDbFile = copyVirusDBFromAssets(this);

        if (virusDbFile == null || !virusDbFile.exists()) {
            Toast.makeText(this, "Failed to load virus definitions", Toast.LENGTH_SHORT).show();
            // Resume music if it was playing before the scan (and not in dark mode)
            if (mediaPlayer != null && !mediaPlayer.isPlaying() && !Settings.getSetting("dark mode")) {
                Log.d("MusicDebug", "Resuming music after scan error.");
                mediaPlayer.start();
            }
            return;
        }

        // Initialize the VirusDatabase
        VirusDatabase db = new VirusDatabase(virusDbFile);

        // List to hold all detected suspicious files (from heuristics or CSV)
        Set<File> suspiciousFiles = new HashSet<>();

        // --- Perform Heuristic Scan and CSV Scan ---

        // Directories to scan
        List<File> directoriesToScan = new ArrayList<>();

        // Add tmp directory
        if (tmpDir != null && tmpDir.exists() && tmpDir.isDirectory()) {
            directoriesToScan.add(tmpDir);
        }

        // Add common external storage directories
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadsDir != null && downloadsDir.exists() && downloadsDir.isDirectory()) {
            directoriesToScan.add(downloadsDir);
        }

        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (documentsDir != null && documentsDir.exists() && documentsDir.isDirectory()) {
            directoriesToScan.add(documentsDir);
        }

        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (picturesDir != null && picturesDir.exists() && picturesDir.isDirectory()) {
            directoriesToScan.add(picturesDir);
        }

        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        if (moviesDir != null && moviesDir.exists() && moviesDir.isDirectory()) {
            directoriesToScan.add(moviesDir);
        }

        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        if (musicDir != null && musicDir.exists() && musicDir.isDirectory()) {
            directoriesToScan.add(musicDir);
        }

        // You can add more directories here if needed, e.g., Environment.getExternalStorageDirectory()
        // but be mindful of deprecation and permission requirements (MANAGE_APP_ALL_FILES_ACCESS_PERMISSION is crucial).


        // Traverse directories and apply both checks
        for (File directory : directoriesToScan) {
            traverseAndScanDirectory(directory, suspiciousFiles, db, scanner);
        }


        // Log scan time and history
        lastLogger.saveData(this);
        try {
            historyLogger.appendHistory(this, lastLogger.getTime());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Failed to record scan history",
                    Toast.LENGTH_SHORT
            ).show();
        }

        // Prepare list of detected suspicious files (from heuristics or CSV) to pass to ResultActivity
        ArrayList<String> detectedFilePaths = new ArrayList<>();
        for (File file : suspiciousFiles) {
            detectedFilePaths.add(file.getAbsolutePath());
            Log.d("MainActivity", "Detected: " + file.getAbsolutePath()); // Log detected files
        }


        // Log findings (optional, you can refine this to log based on the combined list)
        // Note: scanner.getFormattedFindings() will only include files detected by the scanner's internal isSuspicious method
        // You might want to create a new logging method that takes the combined list.
        // logger.logScan(this, scanner.getFormattedFindings());


        Toast.makeText(this, "Scan completed. Results in Result Activity.", Toast.LENGTH_SHORT).show();


        // Start ResultActivity with the combined list of suspicious file paths
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putStringArrayListExtra("detectedFilePaths", detectedFilePaths);
        // The "hasThreats" flag should be based on whether any files were detected
        intent.putExtra("hasThreats", !suspiciousFiles.isEmpty());
        // Removed csvPath extra as ResultActivity now gets the full list directly
        startActivity(intent);
        // Music will be paused by onPause when the new activity starts
    }

    // Helper method to traverse directories and apply both heuristic and CSV checks
    private void traverseAndScanDirectory(File dir, Set<File> suspiciousFiles, VirusDatabase db, FileScanner scanner) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // Recursively scan subdirectories
                        traverseAndScanDirectory(file, suspiciousFiles, db, scanner);
                    } else {
                        // Add a check to exclude .nomedia, .database_uuid, and other hidden files
                        if (file.getName().startsWith(".") || file.getName().equals(".nomedia") || file.getName().equals(".database_uuid")) {
                            Log.d("ScanProcess", "Skipping hidden or system file: " + file.getAbsolutePath());
                            continue; // Skip this file
                        }

                        // Apply heuristic check
                        if (scanner.isSuspicious(file)) {
                            suspiciousFiles.add(file);
                            Log.d("ScanProcess", "Heuristic detected: " + file.getAbsolutePath());
                        }
                        // Apply CSV database check
                        if (db.isMatch(file)) {
                            // Add only if not already added by heuristic scan
                            if (suspiciousFiles.add(file)) { // Set.add() returns true if the element was added (i.e., not a duplicate)
                                Log.d("ScanProcess", "CSV DB detected: " + file.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }
    }


    private File copyVirusDBFromAssets(Context context) {
        File outFile = new File(context.getFilesDir(), "virus_db.csv");

        // Keep this to ensure the latest DB from assets is always used for now
        if (outFile.exists()) {
            outFile.delete();
        }

        try (InputStream is = context.getAssets().open("virus_db.csv");
             FileOutputStream fos = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            Log.d("MainActivity", "✅ Copied virus_db.csv to: " + outFile.getAbsolutePath());

            return outFile;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainActivity", "❌ Failed to copy virus_db.csv", e);
            return null;
        }
    }

    // Renamed the method to better reflect its purpose
    private void setupDemoFiles() {
        appFilesDir = getExternalFilesDir(null);
        if (appFilesDir == null) {
            Toast.makeText(this, "App files directory unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create tmp directory within app-specific storage
        tmpDir = new File(appFilesDir, "tmp");
        if (!tmpDir.exists() && tmpDir.mkdirs()) {
            Toast.makeText(this, "Created tmp folder in app storage", Toast.LENGTH_SHORT).show();
        }

        // Define demo files and their target directories
        HashMap<File, String> demoFilesToCreate = new HashMap<>();

        // Demo file in tmp (app-specific external storage)
        if (tmpDir != null && tmpDir.exists()) {
            demoFilesToCreate.put(tmpDir, "Setup_Worm.exe"); // Heuristic
            demoFilesToCreate.put(tmpDir, "my_document.pdf.exe"); // Heuristic
            demoFilesToCreate.put(tmpDir, "csv_test_virus.bin"); // CSV
        }

        // Demo file in Downloads
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadsDir != null && downloadsDir.exists()) {
            demoFilesToCreate.put(downloadsDir, "downloaded_malware.apk.exe"); // Heuristic
        }

        // Demo file in Documents
        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (documentsDir != null && documentsDir.exists()) {
            demoFilesToCreate.put(documentsDir, "important_doc.docx.vbs"); // Heuristic
        }

        // Demo file in Pictures
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (picturesDir != null && picturesDir.exists()) {
            demoFilesToCreate.put(picturesDir, "family_photo.jpg.sh"); // Heuristic
        }

        // Demo file in Movies
        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        if (moviesDir != null && moviesDir.exists()) {
            demoFilesToCreate.put(moviesDir, "free_movie.mp4.bat"); // Heuristic
        }

        // Demo file in Music
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        if (musicDir != null && musicDir.exists()) {
            demoFilesToCreate.put(musicDir, "latest_hit.mp3.js"); // Heuristic
        }

        // Create the demo files
        for (File directory : demoFilesToCreate.keySet()) {
            String fileName = demoFilesToCreate.get(directory);
            File demoFile = new File(directory, fileName);
            if (!demoFile.exists()) {
                try (FileOutputStream fos = new FileOutputStream(demoFile)) {
                    fos.write(("This is a demo suspicious file named " + fileName).getBytes());
                    Log.d("MainActivity", "Created demo file: " + demoFile.getAbsolutePath());
                } catch (IOException e) {
                    Log.e("MainActivity", "Failed to create demo file: " + demoFile.getAbsolutePath(), e);
                    // Optionally show a toast, but might be too many if multiple fail
                }
            } else {
                Log.d("MainActivity", "Demo file already exists: " + demoFile.getAbsolutePath());
            }
        }

        Toast.makeText(this, "Attempted to setup demo files in various directories.", Toast.LENGTH_SHORT).show();
    }


    public void onSettingsClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private boolean hasStoragePermission() {
        // On Android R and above, manage external storage permission is needed for broad access.
        // For simpler cases targeting specific directories like getExternalFilesDir or Downloads,
        // READ_EXTERNAL_STORAGE might be sufficient depending on scoped storage rules.
        // This check might need refinement based on your target SDK and exact storage needs.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Direct user to the "Manage External Storage" settings screen
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(android.net.Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            }
        } else {
            // Request READ_EXTERNAL_STORAGE for older Android versions
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Manage External Storage Permission granted. Setting up demo files.", Toast.LENGTH_SHORT).show();
                    setupDemoFiles();
                } else {
                    Toast.makeText(this, "Manage External Storage Permission denied. External directories may not be scanned.", Toast.LENGTH_LONG).show();
                    // You might still want to setup demo files in app-specific storage
                    setupDemoFiles(); // Attempt to setup demo files in app storage anyway
                }
            }
            // For older Android versions, the result is handled in onRequestPermissionsResult
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission granted. Setting up demo files.", Toast.LENGTH_SHORT).show();
                setupDemoFiles();
            } else {
                Toast.makeText(this, "Storage Permission denied. External directories may not be scanned.", Toast.LENGTH_LONG).show();
                // You might still want to setup demo files in app-specific storage
                setupDemoFiles(); // Attempt to setup demo files in app storage anyway
            }
        }
    }
}