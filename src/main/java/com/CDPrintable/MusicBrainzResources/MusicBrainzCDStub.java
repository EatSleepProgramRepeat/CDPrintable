/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This is a class that stores data from the JSON reader.
 */

package com.CDPrintable.MusicBrainzResources;

public class MusicBrainzCDStub {
    private String id;
    private String title;
    private String[] artists;
    private int trackCount;

    public MusicBrainzCDStub(String id, String title, String[] artists, int trackCount) {
        this.id = id;
        this.title = title;
        this.artists = artists;
        this.trackCount = trackCount;
    }

    public String getArtistsAsString() {
        StringBuilder sb = new StringBuilder();
        for (String artist : artists) {
            sb.append(artist).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getArtists() {
        return artists;
    }

    public void setArtists(String[] artists) {
        this.artists = artists;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }
}
