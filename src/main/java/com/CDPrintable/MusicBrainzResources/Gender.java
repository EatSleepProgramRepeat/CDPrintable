/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This is an enum to represent genders.
 */

package com.CDPrintable.MusicBrainzResources;

public enum Gender {
    MALE,
    FEMALE,
    NON_BINARY,
    UNKNOWN;

    /**
     * Converts a string to a Gender enum.
     * @param genderString The input string.
     * @return The corresponding Gender enum, or UNKNOWN if no match is found.
     */
    public static Gender fromString(String genderString) {
        if (genderString == null) {
            return UNKNOWN;
        }
        switch (genderString.toLowerCase()) {
            case "male":
                return MALE;
            case "female":
                return FEMALE;
            case "non-binary":
            case "nonbinary":
                return NON_BINARY;
            default:
                return UNKNOWN;
        }
    }
}