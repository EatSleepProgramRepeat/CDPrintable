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
    public String discID;
    public String discTitle;
    public String discArtist;
    public int trackCount;
    public int releaseYear;

    public MusicBrainzCDStub(String discID, String discTitle, String discArtist, int trackCount, int releaseYear) {
        this.discID = discID;
        this.discTitle = discTitle;
        this.discArtist = discArtist;
        this.trackCount = trackCount;
        this.releaseYear = releaseYear;
    }

    public String getDiscTitle() {
        return discTitle;
    }

    public void setDiscTitle(String discTitle) {
        this.discTitle = discTitle;
    }

    public String getDiscArtist() {
        return discArtist;
    }

    public void setDiscArtist(String discArtist) {
        this.discArtist = discArtist;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getDiscID() {
        return discID;
    }

    public void setDiscID(String discID) {
        this.discID = discID;
    }
}
