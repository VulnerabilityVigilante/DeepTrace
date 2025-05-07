package edu.utsa.cs3443.deepTrace.models;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages persistent app settings backed by SharedPreferences.
 * Must call {@link #init(Context)} before using any other methods.
 */
public class Settings {
    private static SharedPreferences prefs;


    /**
     * Initializes the settings by obtaining a reference to SharedPreferences.
     * This method must be called before accessing any other methods of the Settings class.
     *
     * @param context the context to use for SharedPreferences
     */
    public static void init(Context context) {

        if (prefs == null) {

            prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        }

    }

    /**
     * Retrieves a setting value by its key. If the setting is not found, the default value {@code false} is returned.
     *
     * @param name The key for the setting to retrieve.
     * @return The current value of the setting, or {@code false} if the setting is not found.
     * @throws IllegalStateException if the settings have not been initialized by calling {@code Settings.init(context)}.
     */
    // Get a setting by key (default is false)
    public static boolean getSetting(String name) {

        if (prefs == null) {

            throw new IllegalStateException("Settings not initialized. Call Settings.init(context) first.");

        }

        return prefs.getBoolean(name, false);

    }

    /**
     * Saves a setting value by its key.
     *
     * @param name  The key of the setting to save.
     * @param value The value to save for the setting.
     * @throws IllegalStateException if the settings have not been initialized by calling {@code Settings.init(context)}.
     */
    // Save a setting by key
    public static void setSetting(String name, boolean value) {

        if (prefs == null) {

            throw new IllegalStateException("Settings not initialized. Call Settings.init(context) first.");

        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(name, value);
        editor.apply();

    }

    /**
     * Retrieves the font size setting.
     * If the setting is not found, a default font size of 20.0f is returned.
     *
     * @return The current font size setting, or {@code 20.0f} if not set.
     * @throws IllegalStateException if the settings have not been initialized by calling {@code Settings.init(context)}.
     */
    public static float getFontSize() {

        if (prefs == null) {

            throw new IllegalStateException("Settings not initialized. Call Settings.init(context) first.");

        }

        return prefs.getFloat("fontSize", 20f);

    }

    /**
     * Saves a font size setting.
     *
     * @param size The font size to save.
     * @throws IllegalStateException if the settings have not been initialized by calling {@code Settings.init(context)}.
     */
    public static void setFontSize(float size) {

        if (prefs == null) {

            throw new IllegalStateException("Settings not initialized. Call Settings.init(context) first.");

        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("fontSize", size);
        editor.apply();

    }

}