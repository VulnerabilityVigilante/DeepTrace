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
import java.util.ArrayList;
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
    Button backButton;
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

        LinearLayout outerLayout = findViewById(R.id.outerLayout);

        ImageView statusImage = findViewById(R.id.statusImage);

        deleteButton = findViewById(R.id.deleteBtn);
        backButton = findViewById(R.id.backBtn);
        deleteButton.setTextSize(Settings.getFontSize());
        backButton.setTextSize(Settings.getFontSize());

        suspiciousListView = findViewById(R.id.suspiciousList);
        remover = new FileRemover();
        remover = new FileRemover();

        // --- MODIFIED: Retrieve the list of suspicious file paths from the Intent extras ---
        ArrayList<String> detectedFilePaths = getIntent().getStringArrayListExtra("detectedFilePaths");
        detectedFiles = new ArrayList<>();
        if (detectedFilePaths != null) {

            for (String path : detectedFilePaths) {

                File file = new File(path);

                if (file.exists()) {

                    detectedFiles.add(file);

                } else {

                    Log.w("ResultActivity", "Detected file path does not exist: " + path);

                }

            }

        }



        for (File f : detectedFiles) {
            Log.d("ResultActivity", "Detected file: " + f.getAbsolutePath());
        }

        // Determine if threats were found based on the list size
        boolean hasThreats = !detectedFiles.isEmpty();

        if (hasThreats) {

            resultText.setText("⚠️ Suspicious Files Detected");
            statusImage.setImageResource(R.drawable.hazardlogo);
            deleteButton.setVisibility(View.VISIBLE);
            outerLayout.setBackgroundColor(Color.parseColor("#FFFF99"));
            adapter = new SuspiciousFileAdapter(this, detectedFiles);
            suspiciousListView.setAdapter(adapter);

        } else {

            resultText.setText("✅ No Virus Was Found!");
            statusImage.setImageResource(R.drawable.thumbsup);
            deleteButton.setVisibility(View.GONE);
            outerLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            // Clear the adapter or set a different view for no results if needed
            suspiciousListView.setAdapter(null);

        }

        applyAppropriateBackground(outerLayout, hasThreats);
    }

    @Override
    protected void onResume() {

        super.onResume();
        // make sure the settings update on this page
        resultText.setTextSize(Settings.getFontSize());
        deleteButton.setTextSize(Settings.getFontSize());
        backButton.setTextSize(Settings.getFontSize());

        if (adapter != null) {

            adapter.notifyDataSetChanged();

        }

        LinearLayout outerLayout = findViewById(R.id.outerLayout);
        // Recalculate hasThreats based on the current detectedFiles list
        boolean hasThreats = !detectedFiles.isEmpty();
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

    public void onDeleteClick(View view) {

        if (adapter != null) {

            List<File> filesToDelete = adapter.getSelectedFiles();

            if (filesToDelete.isEmpty()) {

                Toast.makeText(this, "No files selected for deletion", Toast.LENGTH_SHORT).show();
                return;

            }

            boolean allDeleted = true;
            for (File file : filesToDelete) {

                if (MainActivity.importedFileOriginalPaths.containsKey(file.getAbsolutePath())) {

                    String originalPath = MainActivity.importedFileOriginalPaths.get(file.getAbsolutePath());
                    boolean deletedOriginal = new File(originalPath).delete();

                    if (!deletedOriginal) {

                        allDeleted = false;
                        Log.e("ResultActivity", "Failed to delete original imported file: " + originalPath);

                    } else {

                        Log.d("ResultActivity", "Deleted original imported file: " + originalPath);
                        MainActivity.importedFileOriginalPaths.remove(file.getAbsolutePath()); // Remove from map after deletion

                    }

                } else {

                    if (!file.delete()) {

                        allDeleted = false;
                        Log.e("ResultActivity", "Failed to delete local file: " + file.getAbsolutePath());

                    } else {

                        Log.d("ResultActivity", "Deleted local file: " + file.getAbsolutePath());

                    }

                }

            }
            if (allDeleted) {

                Toast.makeText(this, "Selected files deleted successfully", Toast.LENGTH_SHORT).show();
                // After deletion, update the displayed list
                detectedFiles.removeAll(filesToDelete); // Remove deleted files from the list
                adapter.notifyDataSetChanged(); // Update the list view
                // Check if there are still threats after deletion
                boolean hasThreats = !detectedFiles.isEmpty();
                applyAppropriateBackground(findViewById(R.id.outerLayout), hasThreats);

                if (!hasThreats) {

                    resultText.setText("✅ No Virus Was Found!");
                    ImageView statusImage = findViewById(R.id.statusImage);
                    statusImage.setImageResource(R.drawable.thumbsup);
                    deleteButton.setVisibility(View.GONE);
                    suspiciousListView.setAdapter(null);

                }

            } else {

                Toast.makeText(this, "Failed to delete some files", Toast.LENGTH_SHORT).show();

            }
            // Note: A full re-scan after deletion might be more robust in a real app.

        }

    }


    public void onBackClick(View view) {
        finish();
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

            Log.d("ResultActivity", "✅ Copied virus_db.csv to: " + outFile.getAbsolutePath());

            return outFile;

        } catch (Exception e) {

            e.printStackTrace();
            Log.e("ResultActivity", "❌ Failed to copy virus_db.csv", e);
            return null;

        }

    }

}