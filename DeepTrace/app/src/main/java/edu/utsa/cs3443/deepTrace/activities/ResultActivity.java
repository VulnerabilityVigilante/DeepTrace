package edu.utsa.cs3443.deepTrace.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.adapters.SuspiciousFileAdapter;
import edu.utsa.cs3443.deepTrace.models.CsvPathScanner;
import edu.utsa.cs3443.deepTrace.models.FileRemover;
import edu.utsa.cs3443.deepTrace.models.VirusDatabase;
import edu.utsa.cs3443.deepTrace.models.Settings;

public class ResultActivity extends AppCompatActivity {

    TextView resultText;
    Button deleteButton;
    ListView suspiciousListView;
    SuspiciousFileAdapter adapter;

    List<File> detectedFiles;
    FileRemover remover;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Settings.init(getApplicationContext());

        resultText = findViewById(R.id.resultText);
        resultText.setTextSize(Settings.getFontSize());
        // Get the outer layout where resultText is located
        LinearLayout outerLayout = findViewById(R.id.outerLayout);

        ImageView statusImage = findViewById(R.id.statusImage);

        deleteButton = findViewById(R.id.deleteBtn);
        suspiciousListView = findViewById(R.id.suspiciousList);
        remover = new FileRemover();
        remover = new FileRemover();

        // Retrieve the folder path from the Intent extras
        String csvPath = getIntent().getStringExtra("csvPath");
        if (csvPath == null) {
            Toast.makeText(this, "No CSV path provided", Toast.LENGTH_SHORT).show();
            return;
        }

        File csvFile = new File(csvPath);
        Log.d("ResultActivity", "Reading from CSV: " + csvFile.getAbsolutePath());
        File virusDbFile = copyVirusDBFromAssets(this);
        VirusDatabase db = new VirusDatabase(virusDbFile);

        detectedFiles = CsvPathScanner.scanFromCSV(csvFile, db);





        // Log the found files for debugging
        for (File f : detectedFiles) {
            Log.d("ResultActivity", "Detected file: " + f.getAbsolutePath());
        }

        boolean hasThreats = getIntent().getBooleanExtra("hasThreats", false);
        if (hasThreats && !detectedFiles.isEmpty()) {
            resultText.setText("⚠️ Suspicious Files Detected");
            statusImage.setImageResource(R.drawable.hazardlogo);
            deleteButton.setVisibility(View.VISIBLE);
            statusImage.setImageResource(R.drawable.hazardlogo); // Replace with your image in res/drawable
            // Set up the ListView with the custom adapter to show toggles
            adapter = new SuspiciousFileAdapter(this, detectedFiles);
            outerLayout.setBackgroundColor(Color.parseColor("#FFFF99"));
            suspiciousListView.setAdapter(adapter);
        } else {
            resultText.setText("✅ No Virus Was Found!");
            statusImage.setImageResource(R.drawable.thumbsup);
            deleteButton.setVisibility(View.GONE);
            outerLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            statusImage.setImageResource(R.drawable.thumbsup);
        }

        applyAppropriateBackground(outerLayout, hasThreats);
    }

    protected void onResume() {
        super.onResume();
        // make sure the text size updates if they changed it in Settings
        resultText.setTextSize(Settings.getFontSize());
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        LinearLayout outerLayout = findViewById(R.id.outerLayout);
        boolean hasThreats = getIntent().getBooleanExtra("hasThreats", false);
        applyAppropriateBackground(outerLayout, hasThreats);

    }

    private void applyAppropriateBackground(View root, boolean hasThreats) {
        if (Settings.getSetting("dark mode")) {
            root.setBackgroundColor(
                    getResources().getColor(android.R.color.darker_gray)
            );
        } else if (hasThreats) {
            root.setBackgroundColor(Color.parseColor("#FFFF99"));
        } else {
            root.setBackgroundColor(
                    getResources().getColor(android.R.color.holo_blue_light)
            );
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
            String csvPath = getIntent().getStringExtra("csvPath");
            if (csvPath == null) {
                Toast.makeText(this, "No CSV path provided", Toast.LENGTH_SHORT).show();
                return;
            }

            File csvFile = new File(csvPath);
            File virusDbFile = copyVirusDBFromAssets(this);
            VirusDatabase db = new VirusDatabase(virusDbFile);

            detectedFiles = CsvPathScanner.scanFromCSV(csvFile, db);
            adapter = new SuspiciousFileAdapter(this, detectedFiles);
            suspiciousListView.setAdapter(adapter);

        }
    }


    public void onBackClick(View view) {
        finish();
    }

    private File copyVirusDBFromAssets(Context context) {
        File outFile = new File(context.getFilesDir(), "virus_db.csv");

        if (outFile.exists()) {
            outFile.delete(); // 👈 force recopy every time
        }

        try (InputStream is = context.getAssets().open("virus_db.csv");
             FileOutputStream fos = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            System.out.println("✅ Copied updated virus_db.csv to: " + outFile.getAbsolutePath());
            return outFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }




}
