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
import java.lang.reflect.Array;

public class MusicBrainzJSONReader {
    private final JsonObject json;

    public MusicBrainzJSONReader(String json) {
        JsonElement jsonElement = JsonParser.parseString(json);
        this.json = jsonElement.getAsJsonObject();
    }

    @SuppressWarnings("unchecked")
    private <T> T[] parseJsonArray(String key, JsonArrayProcessor<T> processor, T[] array) {
        if (!json.has(key)) {return null;}
        JsonArray jsonArray = json.getAsJsonArray(key);
        array = (T[]) Array.newInstance(array.getClass().getComponentType(), jsonArray.size());

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
        return parseJsonArray("releases", jsonObject -> {
            String title = jsonObject.has("title") ? jsonObject.get("title").getAsString() : null;
            String date = jsonObject.has("date") ? jsonObject.get("date").getAsString() : null;
            int trackCount = jsonObject.has("count") ? jsonObject.get("count").getAsInt() : -1;
            String id = jsonObject.has("id") ? jsonObject.get("id").getAsString() : null;

            JsonArray artistsArray = jsonObject.getAsJsonArray("artist-credit");
            String[] artists = new String[artistsArray.size()];
            for (int j = 0; j < artistsArray.size(); j++) {
                JsonObject artistObject = artistsArray.get(j).getAsJsonObject();
                JsonElement artistElement = artistObject.get("name");
                artists[j] = artistElement != null ? artistElement.getAsString() : null;
            }

            return new MusicBrainzRelease(title, artists, date, trackCount, id);
        }, new MusicBrainzRelease[0]);
    }

    public MusicBrainzCDStub[] getCDStubs() {
        return parseJsonArray("cdstubs", jsonObject -> {
            String title = jsonObject.has("title") ? jsonObject.get("title").getAsString() : null;
            String id = jsonObject.has("id") ? jsonObject.get("id").getAsString() : null;
            int trackCount = jsonObject.has("count") ? jsonObject.get("count").getAsInt() : -1;
            String artist = jsonObject.has("artist") ? jsonObject.get("artist").getAsString() : null;
            return new MusicBrainzCDStub(id, title, new String[] {artist}, trackCount);
        }, new MusicBrainzCDStub[0]);
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
