package edu.utsa.cs3443.deepTrace.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;



import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.io.InputStream;
import java.util.Locale;

import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.models.ActivityLogger;
import edu.utsa.cs3443.deepTrace.models.FileScanner;
import edu.utsa.cs3443.deepTrace.models.CsvPathScanner;
import edu.utsa.cs3443.deepTrace.models.LastLogger;
import edu.utsa.cs3443.deepTrace.models.VirusDatabase;
import edu.utsa.cs3443.deepTrace.models.Settings;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    public static LastLogger lastLogger;
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
        //jians addition
        Settings.init(getApplicationContext());

        scanner = new FileScanner();
        logger = new ActivityLogger();

        if (!hasStoragePermission()) {
            requestStoragePermission();
        } else {
            setupDemoFile();
        }

        lastLogger = new LastLogger("no scans have been done");
        LastLogger.ensureLastLogFile(this);
        try {
            lastLogger.loadLastTime(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        float fontSizeSp = Settings.getFontSize();

        // ② find each View you want to resize…
        TextView homeTitle   = findViewById(R.id.homeTitle);
        Button scanBtn     = findViewById(R.id.scanBtn);
        Button   settingsBtn = findViewById(R.id.settingsBtn);

        // ③ and apply it (in SP)
        homeTitle .setTextSize(fontSizeSp);
        scanBtn   .setTextSize(fontSizeSp);
        settingsBtn.setTextSize(fontSizeSp);
        applyFontSizes(Settings.getFontSize());

        applyDarkModeBackground();

    }

    protected void onResume() {
        super.onResume();
        // Re-apply in case the user changed it in Settings:
        applyFontSizes(Settings.getFontSize());
        applyDarkModeBackground();
    }

    private void applyDarkModeBackground() {
        // Change `ConstraintLayout` to whatever your root is:
        View root = findViewById(R.id.mainLayout);
        if (Settings.getSetting("dark mode")) {
            root.setBackgroundColor(
                    getResources().getColor(android.R.color.darker_gray)
            );
        } else {
            // restore your original background color
            root.setBackgroundColor(
                    getResources().getColor(R.color.background)
            );
        }
    }

    private void applyFontSizes(float sp) {
        TextView homeTitle   = findViewById(R.id.homeTitle);
        Button   scanBtn     = findViewById(R.id.scanBtn);
        Button   settingsBtn = findViewById(R.id.settingsBtn);

        homeTitle.  setTextSize(sp);
        scanBtn.    setTextSize(sp);
        settingsBtn.setTextSize(sp);
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

        File csvFile = new File(tmpDir, "tmp.csv");
        logSuspiciousFilesToCSV(csvFile);

        File virusDbFile = copyVirusDBFromAssets(this);

        if (virusDbFile == null || !virusDbFile.exists()) {
            Toast.makeText(this, "Failed to load virus definitions", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Confirm file was successfully written before scanning
        VirusDatabase db = new VirusDatabase(virusDbFile);
        List<File> suspicious = CsvPathScanner.scanFromCSV(csvFile, db);

        lastLogger.saveData(this);

        Toast.makeText(
                this,
                "Scan started at: " + lastLogger.getTime(),
                Toast.LENGTH_SHORT
        ).show();

        logger.logScan(this, scanner.getFormattedFindings());

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("hasThreats", !suspicious.isEmpty());
        intent.putExtra("csvPath", csvFile.getAbsolutePath());
        startActivity(intent);
    }


    /**
     * Attempts to copy suspicious files from external non-root system directories
     * (e.g. the Downloads folder) into the provided destination directory.
     * Also records the original location of each imported file.
     * If the external directory is not accessible or no suspicious files are found,
     * a toast message is shown.
     */
    private void logSuspiciousFilesToCSV(File csvFile) {
        boolean foundAny = false;

        try (FileOutputStream fos = new FileOutputStream(csvFile, false)) {

            // ✅ Try Downloads folder, but don't depend on it
            File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (downloads != null && downloads.exists() && downloads.isDirectory()) {
                File[] downloadFiles = downloads.listFiles();
                if (downloadFiles != null) {
                    for (File file : downloadFiles) {
                        if (scanner.isSuspicious(file)) {
                            fos.write((file.getAbsolutePath() + "\n").getBytes());
                            foundAny = true;
                        }
                    }
                }
            } else {
                // Just inform, don't block
                Toast.makeText(this, "Downloads folder unavailable, skipping it", Toast.LENGTH_SHORT).show();
            }

            // ✅ Always scan tmp folder (demo file lives here)
            if (tmpDir != null && tmpDir.exists()) {
                File[] tmpFiles = tmpDir.listFiles();
                if (tmpFiles != null) {
                    for (File file : tmpFiles) {
                        if (scanner.isSuspicious(file)) {
                            fos.write((file.getAbsolutePath() + "\n").getBytes());
                            foundAny = true;
                        }
                    }
                }
            }

            if (!foundAny) {
                Toast.makeText(this, "No suspicious files found in scanned directories", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to write to CSV file", Toast.LENGTH_SHORT).show();
        }
    }


    private File copyVirusDBFromAssets(Context context) {
        File outFile = new File(context.getFilesDir(), "virus_db.csv");

        if (outFile.exists()) {
            outFile.delete(); // Always refresh
        }

        try (InputStream is = context.getAssets().open("virus_db.csv");
             FileOutputStream fos = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            System.out.println("✅ Copied virus_db.csv to: " + outFile.getAbsolutePath());
            return outFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
