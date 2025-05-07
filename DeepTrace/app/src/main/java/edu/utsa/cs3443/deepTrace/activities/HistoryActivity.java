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
/**
 * Activity that displays a list of historical timestamps logged by the application.
 * It supports dynamic UI updates based on user preferences (e.g., dark mode, font size).
 */
public class HistoryActivity extends AppCompatActivity {
    private HistoryLogger historyLogger;
    private ArrayAdapter<String> adapter;
    private TextView historyTitle;


    /**
     * Called when the activity is starting.
     * Initializes the UI components, loads user settings, and populates the history list.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the most recent data. Otherwise, it is null.
     */


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
            /**
             * Customizes each item in the history list to reflect user-defined font size.
             *
             * @param pos         Position of the item in the list.
             * @param convertView The old view to reuse, if possible.
             * @param parent      The parent that this view will eventually be attached to.
             * @return A View corresponding to the data at the specified position.
             */

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

    /**
     * Called after onCreate() or after returning to the activity
     * Reapplies UI settings in case user preferences changed
     */
    protected void onResume() {

        super.onResume();
        // re-apply UI settings when user alters
        applySettingsToUi();

        if (adapter != null) adapter.notifyDataSetChanged();

    }

    /**
     * Rewrites the history log CSV file by overwriting it with the current log times.
     * Used internally to persist updated history.
     */

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

    /**
     * Handles the event when the back button is clicked.
     * Closes the current activity and returns to the previous screen.
     *
     * @param view The button view that was clicked.
     */

    public void onBackClick (View view){

        finish();

    }

    /**
     * Applies user settings (like dark mode and font size) to the activity UI.
     */

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
