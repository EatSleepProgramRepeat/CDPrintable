/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This class represents the final album data that will be used to print the CD label.
 */

package com.CDPrintable.MusicBrainzResources;

public class MusicBrainzFinalizedRelease {
    private String title;
    private String artist;
    private MusicBrainzTrack[] tracks;

    public MusicBrainzFinalizedRelease(String title, String artist, MusicBrainzTrack[] tracks) {
        this.title = title;
        this.artist = artist;
        this.tracks = tracks;
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

    public MusicBrainzTrack[] getTracks() {
        return tracks;
    }

    public void setTracks(MusicBrainzTrack[] tracks) {
        this.tracks = tracks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(title).append("\n");
        sb.append("Artist: ").append(artist).append("\n");
        sb.append("Tracks: \n");
        for (MusicBrainzTrack track : tracks) {
            sb.append(track.getTrackNumber()).append(" ").append(track.getTitle()).append("\n");
        }
        return sb.toString();
    }
}
