package edu.utsa.cs3443.deepTrace.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvPathScanner {

    public static List<File> scanFromCSV(File csvFile, FileScanner scanner) {
        List<File> suspicious = new ArrayList<>();

        if (!csvFile.exists()) return suspicious;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                File file = new File(line.trim());
                if (file.exists() && scanner.isSuspicious(file)) {
                    suspicious.add(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return suspicious;
    }
}
