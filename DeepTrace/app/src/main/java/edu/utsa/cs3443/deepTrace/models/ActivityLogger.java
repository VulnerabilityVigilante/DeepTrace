package edu.utsa.cs3443.deepTrace.models;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.content.Context;

/**
 * Logs scan results to external storage as both text and CSV files.
 */
public class ActivityLogger {

    /**
     * Writes the scan data to the TXT and CSV log files.
     *
     * @param context  the context for file access
     * @param scanData the lines of output to log
     */
    public void logScan(Context context, List<String> scanData) {
        writeToTXT(context, scanData);
        writeToCSV(context, scanData);
    }


    /**
     * Writes each entry in data as a new line to the file.
     *
     * @param context the context for file access
     * @param data    the lines to write to the text log
     */
    public void writeToTXT(Context context, List<String> data) {
        try {
            File logFile = new File(context.getExternalFilesDir(null), "scan_log.txt");
            FileWriter writer = new FileWriter(logFile);
            for (String line : data) {
                writer.write(line + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes each entry in data as a line in to the file.
     *
     * @param context the context for file access
     * @param data    the lines to write to the CSV log
     */
    public void writeToCSV(Context context, List<String> data) {
        try {
            File logFile = new File(context.getExternalFilesDir(null), "scan_log.csv");
            FileWriter writer = new FileWriter(logFile);
            for (String line : data) {
                writer.write("\"" + line + "\"\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}