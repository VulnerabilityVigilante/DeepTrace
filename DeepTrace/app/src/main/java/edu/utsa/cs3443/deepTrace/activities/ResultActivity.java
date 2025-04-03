package edu.utsa.cs3443.deepTrace.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.List;

import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.adapters.SuspiciousFileAdapter;
import edu.utsa.cs3443.deepTrace.models.FileScanner;
import edu.utsa.cs3443.deepTrace.models.FileRemover;

public class ResultActivity extends AppCompatActivity {

    TextView resultText;
    Button deleteButton;
    ListView suspiciousListView;
    SuspiciousFileAdapter adapter;

    List<File> detectedFiles;
    FileRemover remover;
    FileScanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultText = findViewById(R.id.resultText);
        deleteButton = findViewById(R.id.deleteBtn);
        suspiciousListView = findViewById(R.id.suspiciousList);
        remover = new FileRemover();
        scanner = new FileScanner();

        // Retrieve the folder path from the Intent extras
        String folderPath = getIntent().getStringExtra("demoFolderPath");
        if (folderPath == null) {
            Toast.makeText(this, "No folder path provided", Toast.LENGTH_SHORT).show();
            return;
        }

        File tmpDir = new File(folderPath);
        Log.d("ResultActivity", "Scanning folder: " + tmpDir.getAbsolutePath());
        detectedFiles = scanner.scanFolder(tmpDir);

        // Log the found files for debugging
        for (File f : detectedFiles) {
            Log.d("ResultActivity", "Detected file: " + f.getAbsolutePath());
        }

        boolean hasThreats = getIntent().getBooleanExtra("hasThreats", false);
        if (hasThreats && !detectedFiles.isEmpty()) {
            resultText.setText("⚠️ Suspicious Files Detected");
            deleteButton.setVisibility(View.VISIBLE);
            // Set up the ListView with the custom adapter to show toggles
            adapter = new SuspiciousFileAdapter(this, detectedFiles);
            suspiciousListView.setAdapter(adapter);
        } else {
            resultText.setText("✅ No Virus Was Found!");
            deleteButton.setVisibility(View.GONE);
        }
    }

    // Called when the delete button is tapped
    public void onDeleteClick(View view) {
        if (adapter != null) {
            List<File> filesToDelete = adapter.getSelectedFiles();
            if (filesToDelete.isEmpty()) {
                Toast.makeText(this, "No files selected for deletion", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean allDeleted = true;
            for (File file : filesToDelete) {
                // Check if this file was imported from an external directory.
                if (MainActivity.importedFileOriginalPaths.containsKey(file.getAbsolutePath())) {
                    String originalPath = MainActivity.importedFileOriginalPaths.get(file.getAbsolutePath());
                    boolean deletedOriginal = new File(originalPath).delete();
                    if (!deletedOriginal) {
                        allDeleted = false;
                    }
                } else {
                    // Delete local file.
                    if (!file.delete()) {
                        allDeleted = false;
                    }
                }
            }
            if (allDeleted) {
                Toast.makeText(this, "Selected files deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete some files", Toast.LENGTH_SHORT).show();
            }
            // Refresh the list after deletion.
            String folderPath = getIntent().getStringExtra("demoFolderPath");
            if (folderPath != null) {
                File tmpDir = new File(folderPath);
                detectedFiles = scanner.scanFolder(tmpDir);
                adapter = new SuspiciousFileAdapter(this, detectedFiles);
                suspiciousListView.setAdapter(adapter);
            }
        }
    }


    public void onBackClick(View view) {
        finish();
    }
}
