package edu.utsa.cs3443.deepTrace.models;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ActivityLogger {

    public void logScan(List<String> scanData) {
        writeToTXT(scanData);
        writeToCSV(scanData);
    }

    public void writeToTXT(List<String> data) {
        File file = new File(Environment.getExternalStorageDirectory(), "scan_log.txt");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            for (String line : data) {
                writer.write(line + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToCSV(List<String> data) {
        File file = new File(Environment.getExternalStorageDirectory(), "scan_log.csv");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            for (String line : data) {
                writer.write("\"" + line.replace("\"", "") + "\",\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}