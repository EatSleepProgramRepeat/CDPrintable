/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This class reads and writes to the config file for the program.
 */

package com.CDPrintable;

import com.google.gson.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private final static String configFilePath;
    private final static File file;
    private static String json;

    static {
        configFilePath = System.getProperty("user.home") + "/CDPrintable/config.json";
        file = new File(configFilePath);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                JOptionPane.showMessageDialog(null, "Couldn't create the parent directory", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        try {
            if (file.createNewFile()) {
                JOptionPane.showMessageDialog(null, "Config file created. Welcome to CDPrintable!", "Welcome", JOptionPane.INFORMATION_MESSAGE);
                Files.writeString(file.toPath(), "{}");
                setProperty("userAgentWebAddress", "https://github.com/EatSleepProgramRepeat/CDPrintable");
                setProperty("font", "Arial");
                setIntProperty("fontSize", 10);
                setDoubleProperty("paperWidth", 8.5);
                setDoubleProperty("paperHeight", 11);
                setDoubleProperty("labelWidth", 4);
                setDoubleProperty("labelMaxHeight", 2);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "oopsie poopsies", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reads the config file and returns the JSON string.
     * @return JSON string from the config file.
     */
    public static String getProperty(String key) {
        readConfigFile();
        try {
            JsonElement jsonElement = JsonParser.parseString(json);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.has(key) ? jsonObject.get(key).getAsString() : null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading property from config file!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Reads a property from the config file.
     * @param key The key to read.
     * @param defaultValue The default to return if it doesn't exist.
     * @return The requested property.
     */
    public static String getProperty(String key, String defaultValue) {
        readConfigFile();
        try {
            JsonElement jsonElement = JsonParser.parseString(json);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.has(key) ? jsonObject.get(key).getAsString() : defaultValue;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading property from config file!", "Error", JOptionPane.ERROR_MESSAGE);
            return defaultValue;
        }
    }

    /**
     * Reads an integer property from the config.
     * @param key The key to read.
     * @param defaultValue The default to return if it doesn't exist.
     * @return The requested int.
     */
    public static int getIntProperty(String key, int defaultValue) {
        readConfigFile();
        try {
            JsonElement jsonElement = JsonParser.parseString(json);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.has(key) ? jsonObject.get(key).getAsInt() : defaultValue;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading property from config file!", "Error", JOptionPane.ERROR_MESSAGE);
            return defaultValue;
        }
    }

    /**
     * Reads a double property from the config.
     * @param key The key to read.
     * @param defaultValue The default to return if it doesn't exist.
     * @return The requested double.
     */
    public static double getDoubleProperty(String key, double defaultValue) {
        readConfigFile();
        try {
            JsonElement jsonElement = JsonParser.parseString(json);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.has(key) ? jsonObject.get(key).getAsDouble() : defaultValue;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading property from config file!", "Error", JOptionPane.ERROR_MESSAGE);
            return defaultValue;
        }
    }

    /**
     * Sets a property in the config file.
     * @param key The key to set.
     * @param value The value to set.
     */
    public static void setProperty(String key, String value) {
        readConfigFile();
        JsonElement jsonElement = JsonParser.parseString(json);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        jsonObject.addProperty(key, value);
        writeConfigFile(jsonObject);
    }

    /**
     * Sets an int in the config file.
     * @param key The key to set.
     * @param value The value to set.
     */
    public static void setIntProperty(String key, int value) {
        readConfigFile();
        JsonElement jsonElement = JsonParser.parseString(json);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        jsonObject.addProperty(key, value);
        writeConfigFile(jsonObject);
    }

    /**
     * Sets a double in the config file.
     * @param key The key to set.
     * @param value The value to set.
     */
    public static void setDoubleProperty(String key, double value) {
        readConfigFile();
        JsonElement jsonElement = JsonParser.parseString(json);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        jsonObject.addProperty(key, value);
        writeConfigFile(jsonObject);
    }

    /**
     * Helper method to read a JSON file.
     */
    private static void readConfigFile() {
        try {
            json = Files.readString(file.toPath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not read config file!", "Error", JOptionPane.ERROR_MESSAGE);
            json = "{}";
        }
    }

    /**
     * Helper method to write a JSON file.
     */
    public static void writeConfigFile(JsonObject jsonObject) {
        Path filePath = Path.of(configFilePath);

        try {
            Files.writeString(filePath, jsonObject.toString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not write to JSON config file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}