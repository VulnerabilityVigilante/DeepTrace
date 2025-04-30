package edu.utsa.cs3443.deepTrace.models;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import edu.utsa.cs3443.deepTrace.activities.MainActivity;

public class LastLogger {

    private String time;

    public LastLogger(String time) {

        this.time = time;

    }

    /**
     * Sets the name of the park.
     * @param time
     */
    public void setTime(String time) {

        this.time = time;

    }

    /**
     *
     * @return String for name
     */
    public String getTime() {

        return time;

    }
    public void loadLastTime(Context ctx) throws IOException {
        // openFileInput targets /data/data/your.package/files/lastTime.csv
        try (Scanner scan = new Scanner(ctx.openFileInput("lastTime.csv"))) {
            // skip the header line
            if (scan.hasNextLine()) scan.nextLine();
            // read the actual timestamp
            if (scan.hasNextLine()) {
                this.time = scan.nextLine().trim();
            }
        } catch (IOException e) {
            throw new IOException("ERROR: Cannot open lastTime.csv in internal storage", e);
        }
    }

    public void saveData(Context ctx) {

        String filename = "lastTime.csv";
        String timestamp = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        ).format(new Date());

        setTime(timestamp);

        StringBuilder sb = new StringBuilder();
        sb.append("Time\n")
                .append(timestamp).append("\n");


        try (FileOutputStream out = ctx.openFileOutput(filename, Context.MODE_PRIVATE)) {
            out.write(sb.toString().getBytes(StandardCharsets.UTF_8));


            Log.i("LastLogger", "Wrote " + timestamp + " time to " + filename);
        } catch (IOException exc) {
            Log.e("LastLogger", "ERROR: can't save " + filename, exc);
        }
    }

    public static void ensureLastLogFile(Context ctx) {

        File dest = new File(ctx.getFilesDir(), "lastTime.csv");
        if (dest.exists()) return;

        try (InputStream is = ctx.getAssets().open("lastTime.csv");
             FileOutputStream stream = ctx.openFileOutput("lastTime.csv", Context.MODE_PRIVATE)) {
            byte[] buffer = new byte[4096];
            int i;
            while ((i = is.read(buffer)) != -1) {

                stream.write(buffer, 0, i);

            }
        } catch (IOException e) {

            Log.e("LastLogger", "Failed to copy lastTime.csv to internal storage", e);

        }

    }
}
