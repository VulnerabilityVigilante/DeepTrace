package edu.utsa.cs3443.deepTrace.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.models.FileRemover;
import edu.utsa.cs3443.deepTrace.models.FileScanner;

import java.io.File;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    TextView resultText;
    Button deleteButton;

    List<File> detectedFiles;
    FileRemover remover;
    FileScanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultText = findViewById(R.id.resultText);
        deleteButton = findViewById(R.id.deleteBtn);
        remover = new FileRemover();
        scanner = new FileScanner();

        boolean hasThreats = getIntent().getBooleanExtra("hasThreats", false);
        if (hasThreats) {
            resultText.setText("⚠️ Suspicious Files Detected");
            deleteButton.setVisibility(View.VISIBLE);
            detectedFiles = scanner.scanFiles(); // optional: pass real list via intent/singleton
        } else {
            resultText.setText("✅ No Virus Was Found!");
            deleteButton.setVisibility(View.GONE);
        }
    }
    public void onBackClick(View view) {
        finish(); // Closes the settings screen
    }

}