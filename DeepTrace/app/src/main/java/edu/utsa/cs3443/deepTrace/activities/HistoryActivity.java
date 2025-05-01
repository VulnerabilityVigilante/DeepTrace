package edu.utsa.cs3443.deepTrace.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.models.HistoryLogger;
import edu.utsa.cs3443.deepTrace.models.Settings;

public class HistoryActivity extends AppCompatActivity {
    private HistoryLogger historyLogger;
    private ArrayAdapter<String> adapter;
    private TextView historyTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // 1) Init Settings
        Settings.init(getApplicationContext());

        // 2) Wire up your header TextView & ListView
        historyTitle   = findViewById(R.id.HistoryName);
        ListView lv    = findViewById(R.id.historyList);

        // 3) Apply dark-mode and font-size immediately
        applySettingsToUi();

        // 4) Prepare your history CSV (copies from assets only once)
        HistoryLogger.ensureHistoryLog(this);

        // 5) Load the timestamps into memory
        historyLogger = new HistoryLogger();
        try {
            historyLogger.loadHistory(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 6) Create an ArrayAdapter that applies font-size (and color) per row
        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                historyLogger.getLogTimes()
        ) {
            @NonNull
            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(pos, convertView, parent);
                // set font size
                tv.setTextSize(Settings.getFontSize());

                return tv;
            }
        };

        // 7) Hook the adapter up
        lv.setAdapter(adapter);
    }

    protected void onResume() {
        super.onResume();
        // re-apply UI settings in case user changed them
        applySettingsToUi();
        // rebind the ListView so it picks up any new font-size / colors
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void rewriteHistoryCsv() {
        try {
            // Overwrite file, include header, then each line
            FileOutputStream fos = openFileOutput("historyLog.csv", Context.MODE_PRIVATE);
            fos.write("Time\n".getBytes(StandardCharsets.UTF_8));
            for (String ts : historyLogger.getLogTimes()) {
                fos.write((ts + "\n").getBytes(StandardCharsets.UTF_8));
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onBackClick (View view){

        finish();

    }

    private void applySettingsToUi() {
        // Grab it as a View, since in XML it’s a LinearLayout
        View root = findViewById(R.id.historyPageRoot);
        if (Settings.getSetting("dark mode")) {
            root.setBackgroundColor(
                    getResources().getColor(android.R.color.darker_gray)
            );
        } else {
            root.setBackgroundColor(
                    getResources().getColor(android.R.color.white)
            );
        }

    }

}
