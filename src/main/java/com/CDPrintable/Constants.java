/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This class defines program constants.
 */

package com.CDPrintable;

import java.io.InputStream;
import java.util.Properties;

public class Constants {
    // MAJOR MINOR PATCH
    public static final String VERSION;
    static {
        Properties prop = new Properties();
        String tempVersion = "0.0.0"; // Default version
        try (InputStream input = Constants.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input != null) {
                prop.load(input);
                tempVersion = prop.getProperty("version");
            } else {
                System.err.println("version.properties not found in resources.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load version.properties: " + e.getMessage());
        }
        VERSION = tempVersion;
    }

    public static final int MAX_THREADS = 4;
    public static final ThreadManager THREAD_MANAGER = new ThreadManager();
}