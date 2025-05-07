package edu.utsa.cs3443.deepTrace.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads a CSV virus database, checks files for matches, and computes SHA-256 hashes.
 */
public class VirusDatabase {

    private final Map<String, String> virusMap = new HashMap<>(); // filename -> sha256

    /**
     * Constructs a VirusDatabase by loading the database from the specified CSV file.
     *
     * @param csvFile The CSV file containing virus entries (filename, hash).
     */
    public VirusDatabase(File csvFile) {
        loadDatabase(csvFile);
    }

    /**
     * Loads the virus database from a CSV file. The file must have a header, and the entries
     * should have the format: filename, (optional columns), hash. Populates the {@link #virusMap}
     * with these entries.
     *
     * @param csv The CSV file containing the virus database entries.
     */
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

    /**
     * Checks if the provided file matches a virus entry in the database.
     * The check is done based on the filename (case-insensitive).
     *
     * @param file The file to check against the virus database.
     * @return {@code true} if the file's name matches an entry in the virus database;
     *         {@code false} otherwise.
     */
    public boolean isMatch(File file) {
        String name = file.getName().toLowerCase();
        boolean result = virusMap.containsKey(name);
        System.out.println("🧪 Matching against: '" + name + "'");
        System.out.println("🎯 DB match? " + result);
        return result;
    }


    /**
     * Computes the SHA-256 hash of a given file. This method reads the file in chunks
     * and uses a MessageDigest to calculate the hash.
     *
     * @param file The file whose SHA-256 hash is to be computed.
     * @return The computed SHA-256 hash of the file as a hexadecimal string.
     */
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
