package edu.utsa.cs3443.deepTrace.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class VirusDatabase {

    private final Map<String, String> virusMap = new HashMap<>(); // filename -> sha256

    public VirusDatabase(File csvFile) {
        loadDatabase(csvFile);
    }

    private void loadDatabase(File csv) {
        if (!csv.exists()) {
            System.out.println("🚫 virus_db.csv NOT FOUND at: " + csv.getAbsolutePath());
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header

                String[] parts = line.split(","); // ✅ Match your CSV's actual format!
                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    String hash = parts[2].trim().toLowerCase();
                    virusMap.put(name, hash);
                }
            }
            System.out.println("📦 Virus database loaded with " + virusMap.size() + " entries:");
            for (String key : virusMap.keySet()) {
                System.out.println("🦠 Virus Entry: " + key);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("✅ Final virus DB contents:");
        for (String key : virusMap.keySet()) {
            System.out.println("🧬 DB key loaded: '" + key + "'");
        }

    }

    public boolean isMatch(File file) {
        String name = file.getName().toLowerCase();
        boolean result = virusMap.containsKey(name);
        System.out.println("🧪 Matching against: '" + name + "'");
        System.out.println("🎯 DB match? " + result);
        return result;
    }


    public static String computeSHA256(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            fis.close();

            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
