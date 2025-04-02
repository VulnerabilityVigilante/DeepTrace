package edu.utsa.cs3443.deepTrace.models;

import java.util.HashMap;

public class Settings {
    private static final HashMap<String, Boolean> settingsMap = new HashMap<>();

    public static boolean getSetting(String name) {
        return settingsMap.getOrDefault(name, false);
    }

    public static void setSetting(String name, boolean value) {
        settingsMap.put(name, value);
    }
}