package edu.utsa.cs3443.deepTrace.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;

import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.models.Settings;

public class SettingsActivity extends AppCompatActivity {

    CheckBox cbDeepScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        cbDeepScan = findViewById(R.id.cbDeepScan);
        cbDeepScan.setChecked(Settings.getSetting("deepScan"));
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        Settings.setSetting("deepScan", checked);
    }

    public void onBackClick(View view) {
        finish(); // return to home
    }
}

