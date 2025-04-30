package edu.utsa.cs3443.deepTrace.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.models.ActivityLogger;
import edu.utsa.cs3443.deepTrace.models.CsvPathScanner;
import edu.utsa.cs3443.deepTrace.models.FileScanner;
import edu.utsa.cs3443.deepTrace.models.LastLogger;
import edu.utsa.cs3443.deepTrace.models.Settings;
import edu.utsa.cs3443.deepTrace.models.VirusDatabase;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    public static LastLogger lastLogger;
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

        TextView homeTitle = findViewById(R.id.homeTitle);
        Button scanBtn = findViewById(R.id.scanBtn);
        Button settingsBtn = findViewById(R.id.settingsBtn);

        homeTitle.setTextSize(fontSizeSp);
        scanBtn.setTextSize(fontSizeSp);
        settingsBtn.setTextSize(fontSizeSp);

        applyFontSizes(Settings.getFontSize());
        applyDarkModeBackground();
        loadGifBackground();
        startBackgroundMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFontSizes(Settings.getFontSize());
        applyDarkModeBackground();
        loadGifBackground();

        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
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

    private void startBackgroundMusic() {
        if (!Settings.getSetting("dark mode")) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.matrix_theme);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        }
    }

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

        VirusDatabase db = new VirusDatabase(virusDbFile);
        List<File> suspicious = CsvPathScanner.scanFromCSV(csvFile, db);

        lastLogger.saveData(this);

        Toast.makeText(this, "Scan started at: " + lastLogger.getTime(), Toast.LENGTH_SHORT).show();

        logger.logScan(this, scanner.getFormattedFindings());

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("hasThreats", !suspicious.isEmpty());
        intent.putExtra("csvPath", csvFile.getAbsolutePath());
        startActivity(intent);
    }

    private void logSuspiciousFilesToCSV(File csvFile) {
        boolean foundAny = false;

        try (FileOutputStream fos = new FileOutputStream(csvFile, false)) {
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
            }

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
            outFile.delete();
        }

        try (InputStream is = context.getAssets().open("virus_db.csv");
             FileOutputStream fos = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            return outFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupDemoFile() {
        appFilesDir = getExternalFilesDir(null);
        if (appFilesDir == null) {
            Toast.makeText(this, "App files directory unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        tmpDir = new File(appFilesDir, "tmp");
        if (!tmpDir.exists() && tmpDir.mkdirs()) {
            Toast.makeText(this, "Created tmp folder", Toast.LENGTH_SHORT).show();
        }

        File demoFile = new File(tmpDir, "crackme_demo.exe");
        if (!demoFile.exists()) {
            try (FileOutputStream fos = new FileOutputStream(demoFile)) {
                fos.write("This is a demo suspicious file".getBytes());
                Toast.makeText(this, "Demo file created in tmp for testing", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to create demo file", Toast.LENGTH_SHORT).show();
            }
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
                setupDemoFile();
            }
        }
    }
}
