package edu.utsa.cs3443.deepTrace.models;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.content.Context;

public class ActivityLogger {

    public void logScan(Context context, List<String> scanData) {
        writeToTXT(context, scanData);
        writeToCSV(context, scanData);
    }


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