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

public class HistoryLogger {

    private ArrayList<String> logTimes;


    public HistoryLogger() {

        this.logTimes = new ArrayList<>();

    }

    public ArrayList<String> getLogTimes() {

        return logTimes;

    }


    public void setLogTimes(ArrayList<String> logTimes) {

        this.logTimes = logTimes;

    }

    public int getSizeOfLog() {

        return logTimes.size();

    }

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
     * Add one timestamp, trim to the 5 most recent, and rewrite the CSV.
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

    /** Copy the CSV from assets into internal storage just once*/
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
