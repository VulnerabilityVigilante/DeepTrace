package edu.utsa.cs3443.deepTrace.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.models.LastLogger;
import edu.utsa.cs3443.deepTrace.models.Settings;

public class SettingsActivity extends AppCompatActivity {

    LastLogger sameLogger = MainActivity.lastLogger;
    FrameLayout rootLayout;
    CheckBox cbFont;
    CheckBox cbDarkMode;
    Button btnLastScan;
    TextView cbFontText;
    TextView cbDarkModeText;


    float fontSize = 25f;
    float otherSize = 18f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Settings.init(getApplicationContext());

        rootLayout = findViewById(R.id.settingsPageRoot);
        if (Settings.getSetting("dark mode")) {
            rootLayout.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            rootLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
        }
        cbFont = findViewById(R.id.cbFont);
        cbFont.setChecked(Settings.getSetting("font"));
        cbDarkMode = findViewById(R.id.cbDarkMode);
        cbDarkMode.setChecked(Settings.getSetting("dark mode"));
        btnLastScan = findViewById(R.id.btnLastScan);
        cbFontText = findViewById(R.id.cbFontText);
        cbDarkModeText = findViewById(R.id.cbDarkModeText);
        fontSize = Settings.getFontSize();  // Load the font size
        updateFontSizes(fontSize);
    }

    public void onCheckboxClicked(View view) {
        boolean checked = cbFont.isChecked();
        if (checked) {
            fontSize = 27f;
            otherSize = 25f;
        } else {
            fontSize = 20f;
            otherSize = 18f;
        }
        Settings.setSetting("font", checked);
        updateFontSizes(fontSize);
        Settings.setFontSize(fontSize);

    }
    public void onCheckboxClickedSec(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        Settings.setSetting("dark mode", checked);
        if (checked) {
            rootLayout.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            rootLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
        }
    }
    public void onLastScanClick(View view) {
        // sameLogger was already initialized via MainActivity.lastLogger
        String lastTime = sameLogger.getTime();
        Toast.makeText(this, "Last scanned at:\n" + lastTime, Toast.LENGTH_LONG).show();
    }

    public void onBackClick(View view) {
        finish(); // return to home
    }

    private void updateFontSizes(float newFontSize) {
        cbFontText.setTextSize(newFontSize);
        cbDarkModeText.setTextSize(newFontSize);
        btnLastScan.setTextSize(newFontSize);
    }

}

