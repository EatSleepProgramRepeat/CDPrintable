/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This is the JSON reader for this project. It reads the output from the web request.
 */

package com.CDPrintable.MusicBrainzResources;

import com.google.gson.*;

import javax.swing.table.DefaultTableModel;

public class MusicBrainzJSONReader {
    private final JsonObject json;

    public MusicBrainzJSONReader(String json) {
        JsonElement jsonElement = JsonParser.parseString(json);
        this.json = jsonElement.getAsJsonObject();
    }

    private <T> T[] parseJsonArray(String key, JsonArrayProcessor<T> processor, T[] array) {
        if (!json.has(key)) {return null;}
        JsonArray jsonArray = json.getAsJsonArray(key);

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            array[i] = processor.process(jsonObject);
        }
        return array;
    }

    /*
    * Gets releases from the JSON.
    * @return An array of the releases.
     */
    public MusicBrainzRelease[] getReleases() {
        MusicBrainzRelease[] releaseArray = null;
        if (json.has("releases")) {
            // Main array for all releases
            JsonArray releases = json.getAsJsonArray("releases");
            releaseArray = new MusicBrainzRelease[releases.size()];

            for (int i = 0; i < releases.size(); i++) {
                JsonObject release = releases.get(i).getAsJsonObject();
                JsonArray artistsJson = release.has("artist-credit") ? release.getAsJsonArray("artist-credit") : null;
                String title = release.has("title") ? release.get("title").getAsString() : null;

                // Get artists
                String[] artists = null;
                if (artistsJson != null) {
                    artists = new String[artistsJson.size()];
                    for (int j = 0; j < artistsJson.size(); j++) {
                        artists[j] = artistsJson.get(j).getAsJsonObject().get("name").getAsString();
                    }
                }

                String date = release.has("date") ? release.get("date").getAsString() : null;
                int trackCount = release.has("track_count") ? release.get("track_count").getAsInt() : 0;
                String id = release.has("id") ? release.get("id").getAsString() : null;

                releaseArray[i] = new MusicBrainzRelease(title, artists, date, trackCount, id);
            }
        }
        return releaseArray;
    }

    public MusicBrainzCDStub[] getCDStubs() {
        MusicBrainzCDStub[] cdStubArray = null;
        if (json.has("cdstubs")) {
            JsonArray cdStubs = json.getAsJsonArray("cdstubs");
            cdStubArray = new MusicBrainzCDStub[cdStubs.size()];

            for (int i = 0; i < cdStubs.size(); i++) {
                JsonObject stub = cdStubs.get(i).getAsJsonObject();
                String artist = stub.has("artist") ? stub.get("artist").getAsString() : null;
                String title = stub.has("title") ? stub.get("title").getAsString() : null;
                String id = stub.has("id") ? stub.get("id").getAsString() : null;
                int trackCount = stub.has("count") ? stub.get("count").getAsInt() : -1;

                cdStubArray[i] = new MusicBrainzCDStub(id, title, new String[] {artist}, trackCount);
            }

            return cdStubArray;
        }
        return null;
    }

    /*
    * Creates a table model from an array of items.
    * @param items The array of items. Usually a MusicBrainzRelease, MusicBrainzCDStub, etc.
    * @param columnNames The names of the columns.
    * @param extractor The extractor that extracts the data from the item.
     */
    private DefaultTableModel createTableModel(Object[] items, String[] columnNames, DataExtractor extractor) {
        String[][] data = new String[items.length][columnNames.length];
        for (int i = 0; i < items.length; i++) {
            data[i] = extractor.extractData(items[i]);
        }
        return new DefaultTableModel(data, columnNames);
    }

    public DefaultTableModel getReleasesAsTableModel(MusicBrainzRelease[] releaseArray) {
        String[] columnNames = {"Release Name", "Artist", "Track Count", "Date", ""};
        return createTableModel(releaseArray, columnNames, item -> {
            MusicBrainzRelease release = (MusicBrainzRelease) item;
            return new String[]{
                    release.getTitle(),
                    release.getArtistsAsString(),
                    String.valueOf(release.getTrackCount()),
                    release.getDate(),
                    ""
            };
        });
    }

    public DefaultTableModel getCDStubsAsTableModel(MusicBrainzCDStub[] cdStubArray) {
        String[] columnNames = {"Disc Name", "Artist", "Track Count", ""};
        return createTableModel(cdStubArray, columnNames, item -> {
            MusicBrainzCDStub cdStub = (MusicBrainzCDStub) item;
            return new String[]{
                    cdStub.getTitle(),
                    cdStub.getArtistsAsString(),
                    String.valueOf(cdStub.getTrackCount()),
                    ""
            };
        });
    }

    @FunctionalInterface
    private interface DataExtractor {
        String[] extractData(Object item);
    }

    @FunctionalInterface
    private interface JsonArrayProcessor<T> {
        T process(JsonObject jsonObject);
    }
}
