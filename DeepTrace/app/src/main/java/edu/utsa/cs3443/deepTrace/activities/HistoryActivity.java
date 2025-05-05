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

        Settings.init(getApplicationContext());

        historyTitle   = findViewById(R.id.HistoryName);
        ListView lv    = findViewById(R.id.historyList);

        applySettingsToUi();

        HistoryLogger.ensureHistoryLog(this);

        historyLogger = new HistoryLogger();

        try {

            historyLogger.loadHistory(this);

        } catch (IOException e) {

            e.printStackTrace();

        }

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

        lv.setAdapter(adapter);

    }

    protected void onResume() {

        super.onResume();
        // re-apply UI settings when user alters
        applySettingsToUi();

        if (adapter != null) adapter.notifyDataSetChanged();

    }

    private void rewriteHistoryCsv() {

        try {

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
