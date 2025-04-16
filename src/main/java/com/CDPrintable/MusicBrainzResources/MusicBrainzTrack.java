/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This class holds data for a track from the MusicBrainz API.
 */

package com.CDPrintable.MusicBrainzResources;

public class MusicBrainzTrack extends MusicBrainzDataObject {
    private String title;
    private String artist;
    private int length;
    private int trackNumber;

    public MusicBrainzTrack(String title, String artist, int length) {
        this.title = title;
        this.artist = artist;
        this.length = length;
        this.trackNumber = 0;
    }

    public MusicBrainzTrack(String title, String artist, int length, int trackNumber) {
        this.title = title;
        this.artist = artist;
        this.length = length;
        this.trackNumber = trackNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getLength() {
        int seconds = length / 1000;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }
}
