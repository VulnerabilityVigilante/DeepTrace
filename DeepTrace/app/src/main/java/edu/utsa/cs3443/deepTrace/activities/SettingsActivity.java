package edu.utsa.cs3443.deepTrace.activities;

import android.content.Intent;
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

/**
 * Manages font size, theme mode, and provides access to scan history and last scan time.
 */
public class SettingsActivity extends AppCompatActivity {

    LastLogger sameLogger = MainActivity.lastLogger;
    FrameLayout rootLayout;
    CheckBox cbFont;
    CheckBox cbDarkMode;
    Button btnLastScan;
    Button btnScanHist;
    TextView cbFontText;
    TextView cbDarkModeText;


    float fontSize = 25f;
    float otherSize = 18f;

    /**
     * Initializes the settings activity, applies stored preferences,
     * and binds UI elements to variables.
     * @param savedInstanceState The previously saved state, if any.
     */
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
        btnScanHist = findViewById(R.id.btnScanHist);
        cbFontText = findViewById(R.id.cbFontText);
        cbDarkModeText = findViewById(R.id.cbDarkModeText);
        fontSize = Settings.getFontSize();
        updateFontSizes(fontSize);

    }

    /**
     * Triggered when the font size checkbox is clicked.
     * Updates app-wide font size setting based on user selection.
     * @param view The view that was clicked.
     */
    public void onFontClick(View view) {

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

    /**
     * Triggered when the dark mode checkbox is clicked.
     * Applies dark or light background color to the settings screen.
     * @param view The view that was clicked.
     */
    public void onBackgroundClick(View view) {

        boolean checked = ((CheckBox) view).isChecked();
        Settings.setSetting("dark mode", checked);

        if (checked) {

            rootLayout.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        } else {

            rootLayout.setBackgroundColor(getResources().getColor(android.R.color.white));

        }

    }

    /**
     * Displays a toast showing the timestamp of the most recent scan.
     * @param view The view that was clicked.
     */
    public void onLastScanClick(View view) {

        String lastTime = sameLogger.getTime();
        Toast.makeText(this, "Last scanned at:\n" + lastTime, Toast.LENGTH_LONG).show();

    }

    /**
     * Opens the history screen to display past scan results.
     *
     * @param view the “Scan History” button that was clicked
     */
    public void onScanHistClick(View view) {

        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);

    }

    /**
     * Handles the back button click to return to the previous screen.
     * @param view The view that was clicked.
     */
    public void onBackClick(View view) {
        finish(); // return to home
    }

    /**
     * Applies the given font size to all relevant text and button views on this screen.
     * @param newFontSize The size to apply to all UI elements.
     */
    private void updateFontSizes(float newFontSize) {

        cbFontText.setTextSize(newFontSize);
        cbDarkModeText.setTextSize(newFontSize);
        btnLastScan.setTextSize(newFontSize);
        btnScanHist.setTextSize(newFontSize);

    }

}

