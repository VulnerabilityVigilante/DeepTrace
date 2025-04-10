package edu.utsa.cs3443.deepTrace.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvPathScanner {

    public static List<File> scanFromCSV(File csvFile, VirusDatabase db) {
        List<File> suspicious = new ArrayList<>();

        if (!csvFile.exists()) return suspicious;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                File file = new File(line.trim());
                if (!file.exists()) continue;

                if (db.isMatch(file)) {
                    suspicious.add(file);
                }
                String fileName = file.getName().toLowerCase();
                System.out.println("🔍 Checking file: " + fileName);
                System.out.println("   Is in DB? " + db.isMatch(file));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return suspicious;
    }
}
