package edu.utsa.cs3443.deepTrace.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.models.Settings;

public class SettingsActivity extends AppCompatActivity {

    FrameLayout rootLayout;
    CheckBox cbFont;
    CheckBox cbDarkMode;
    CheckBox cbNotification;
    TextView cbFontText;
    TextView cbDarkModeText;
    TextView cbNotificationText;

    float fontSize = 25f;
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
        cbNotification = findViewById(R.id.cbNotification);
        cbNotification.setChecked(Settings.getSetting("notification"));
        cbFontText = findViewById(R.id.cbFontText);
        cbDarkModeText = findViewById(R.id.cbDarkModeText);
        cbNotificationText = findViewById(R.id.cbNotificationText);
        fontSize = Settings.getFontSize();  // Load the font size
        updateFontSizes(fontSize);
    }

    public void onCheckboxClicked(View view) {
        boolean checked = cbFont.isChecked();
        if (checked) {
            fontSize = 30f;  // Increase font size by 2sp when checked
        } else {
            fontSize = 25f;  // Decrease font size by 2sp when unchecked
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
    public void onCheckboxClickedThi(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        Settings.setSetting("notification", checked);
        if (checked) {
            Toast.makeText(this, "Last Scan was April 18th 4:05 pm", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackClick(View view) {
        finish(); // return to home
    }

    private void updateFontSizes(float newFontSize) {
        cbFontText.setTextSize(newFontSize);
        cbDarkModeText.setTextSize(newFontSize);
        cbNotificationText.setTextSize(newFontSize);
    }

}

