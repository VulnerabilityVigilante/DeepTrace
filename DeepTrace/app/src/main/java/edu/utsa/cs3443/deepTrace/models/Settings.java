package edu.utsa.cs3443.deepTrace.models;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    private static SharedPreferences prefs;

    // Initialize SharedPreferences
    public static void init(Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        }
    }

    // Get a setting by key (default is false)
    public static boolean getSetting(String name) {
        if (prefs == null) {
            throw new IllegalStateException("Settings not initialized. Call Settings.init(context) first.");
        }
        return prefs.getBoolean(name, false);
    }

    // Save a setting by key
    public static void setSetting(String name, boolean value) {
        if (prefs == null) {
            throw new IllegalStateException("Settings not initialized. Call Settings.init(context) first.");
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    public static float getFontSize() {
        if (prefs == null) {
            throw new IllegalStateException("Settings not initialized. Call Settings.init(context) first.");
        }
        return prefs.getFloat("fontSize", 20f);
    }

    public static void setFontSize(float size) {
        if (prefs == null) {
            throw new IllegalStateException("Settings not initialized. Call Settings.init(context) first.");
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("fontSize", size);
        editor.apply();
    }
}