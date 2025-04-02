package edu.utsa.cs3443.deepTrace.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.List;

import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.models.FileScanner;
import edu.utsa.cs3443.deepTrace.models.ActivityLogger;
import edu.utsa.cs3443.deepTrace.activities.ResultActivity;


import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    FileScanner scanner;
    ActivityLogger logger;
    List<File> suspiciousFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanner = new FileScanner();
        logger = new ActivityLogger();
    }

    public void onScanClick(View view) {
        suspiciousFiles = scanner.scanFiles();
        logger.logScan(scanner.getFormattedFindings());

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("hasThreats", !suspiciousFiles.isEmpty());
        startActivity(intent);
    }

    public void onSettingsClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}