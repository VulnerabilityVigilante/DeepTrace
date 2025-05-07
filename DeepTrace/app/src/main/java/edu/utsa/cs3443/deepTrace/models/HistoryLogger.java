package edu.utsa.cs3443.deepTrace.models;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

/**
 * Handles logging and persistence of scan history timestamps.
 * Maintains a list of the most recent scan times and stores them in a CSV file
 * ("historyLog.csv") in the app's internal storage.
 */

public class HistoryLogger {

    private ArrayList<String> logTimes;


    /**
     * Creates a new HistoryLogger with an empty history list.
     */
    public HistoryLogger() {

        this.logTimes = new ArrayList<>();

    }

    /**
     * Returns the list of logged scan timestamps.
     * @return A list of timestamp strings.
     */
    public ArrayList<String> getLogTimes() {

        return logTimes;

    }


    /**
     * Sets the current log history with a new list of timestamps.
     *
     * @param logTimes A list of timestamp strings to replace the current log.
     */
    public void setLogTimes(ArrayList<String> logTimes) {

        this.logTimes = logTimes;

    }

    /**
     * Returns the number of entries in the log history.
     *
     * @return The number of recorded timestamps.
     */
    public int getSizeOfLog() {

        return logTimes.size();

    }

    /**
     * Loads the history of scan timestamps from the internal file "historyLog.csv".
     *
     * @param ctx The context used to access internal storage.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    public void loadHistory(Context ctx) throws IOException {

        logTimes.clear();

        try (Scanner scan = new Scanner(ctx.openFileInput("historyLog.csv"))) {

            if (scan.hasNextLine()) scan.nextLine();

            while (scan.hasNextLine()) {

                logTimes.add(scan.nextLine().trim());

            }

        }

    }

    /**
     * Appends a new scan timestamp to the history, keeping only the five most recent entries.
     * After appending, rewrites the CSV file in internal storage.
     *
     * @param ctx       The context used to access internal storage.
     * @param timestamp The timestamp to add.
     * @throws IOException If an I/O error occurs during read/write operations.
     */
    public void appendHistory(Context ctx, String timestamp) throws IOException {

        ensureHistoryLog(ctx);

        loadHistory(ctx);

        logTimes.add(timestamp);


        if (logTimes.size() > 5) {

            ArrayList<String> lastFive = new ArrayList<>(

                    logTimes.subList(logTimes.size() - 5, logTimes.size())

            );

            logTimes.clear();
            logTimes.addAll(lastFive);

        }

        try (FileOutputStream fos = ctx.openFileOutput(

                "historyLog.csv", Context.MODE_PRIVATE)) {

            fos.write("Time\n".getBytes(StandardCharsets.UTF_8));

            for (String ts : logTimes) {

                fos.write((ts + "\n").getBytes(StandardCharsets.UTF_8));

            }

        }

    }

    /**
     * Ensures that the "historyLog.csv" file exists in internal storage.
     * If it does not, copies the default version from the app's assets folder.
     *
     * @param ctx The context used to access assets and internal files.
     */
    public static void ensureHistoryLog(Context ctx) {

        File dest = new File(ctx.getFilesDir(), "historyLog.csv");
        if (dest.exists()) return;

        try (InputStream is = ctx.getAssets().open("historyLog.csv");

             FileOutputStream out = ctx.openFileOutput("historyLog.csv", Context.MODE_PRIVATE)) {

            byte[] buf = new byte[4096];
            int r;

            while ((r = is.read(buf)) > 0) {

                out.write(buf, 0, r);

            }

        } catch (IOException e) {

            Log.e("HistoryLogger", "Failed to copy historyLog.csv", e);

        }

    }



}
