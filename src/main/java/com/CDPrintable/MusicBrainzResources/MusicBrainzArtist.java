/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This is a class that stores info about an artist from the MusicBrainz API.
 */

package com.CDPrintable.MusicBrainzResources;

public class MusicBrainzArtist {
    private String name;
    private String dateOrganized;
    private String id;
    private String sortName;
    private Boolean gender;         // True for male, false for female, null for others.
    private String type;            // Band, Person, etc.
    private String disambiguation;
    private String lifeSpan;
}
