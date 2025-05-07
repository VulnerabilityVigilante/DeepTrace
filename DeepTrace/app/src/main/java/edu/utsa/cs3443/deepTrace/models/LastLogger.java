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

/**
 * Manages logging and persistence of the last recorded timestamp.
 * Stores the timestamp in a CSV file ("lastTime.csv") within the app's internal storage.
 */
public class LastLogger {

    private String time;

    /**
     * Constructs a LastLogger with the given initial timestamp.
     *
     * @param time The initial timestamp to set.
     */
    public LastLogger(String time) {

        this.time = time;

    }

    /**
     * Sets the last recorded timestamp.
     *
     * @param time The timestamp to set.
     */
    public void setTime(String time) {

        this.time = time;

    }

    /**
     * Returns the last recorded timestamp.
     *
     * @return The timestamp as a String.
     */
    public String getTime() {

        return time;

    }

    /**
     * Loads the last recorded timestamp from the internal file "lastTime.csv".
     * If the file cannot be opened or read, an IOException is thrown.
     *
     * @param ctx The Context used to access internal storage.
     * @throws IOException If the file cannot be opened or read.
     */
    public void loadLastTime(Context ctx) throws IOException {

        try (Scanner scan = new Scanner(ctx.openFileInput("lastTime.csv"))) {

            if (scan.hasNextLine()) scan.nextLine();

            if (scan.hasNextLine()) {

                this.time = scan.nextLine().trim();

            }

        } catch (IOException e) {

            throw new IOException("ERROR: Cannot open lastTime.csv in internal storage", e);

        }

    }

    /**
     * Saves the current timestamp to the internal file "lastTime.csv".
     * Generates a timestamp in the format "yyyy-MM-dd HH:mm:ss".
     *
     * @param ctx The Context used to access internal storage.
     */
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

    /**
     * Ensures that the "lastTime.csv" file exists in internal storage.
     * If it does not exist, copies the default version from the assets folder.
     *
     * @param ctx The Context used to access assets and internal files.
     */
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
