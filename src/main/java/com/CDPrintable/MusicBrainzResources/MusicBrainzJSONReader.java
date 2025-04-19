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

import com.CDPrintable.Constants;
import com.CDPrintable.ProgramWindow;
import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Array;
import java.util.concurrent.Future;

public class MusicBrainzJSONReader {
    private final JsonObject json;

    /*
     * Creates a MusicBrainzJSONReader from a JSON string.
     * @param JSON The JSON string.
     * @throws IllegalArgumentException If the JSON is invalid.
     */
    public MusicBrainzJSONReader(String json) throws IllegalArgumentException {
        JsonObject tempJsonObject;
        try {
            JsonElement jsonElement = JsonParser.parseString(json);
            tempJsonObject = jsonElement.getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            tempJsonObject = new JsonObject();
        }
        this.json = tempJsonObject;
    }

    /**
     * This method splits a JSON array into smaller chunks for multithreading.
     * @param jsonArray The JSON array to split.
     */
    public JsonArray[] splitJsonArray(JsonArray jsonArray) {
        int chunkSize = Constants.MAX_THREADS;
        int arraySize = jsonArray.size();
        int numChunks = (int) Math.ceil((double) arraySize / chunkSize);
        JsonArray[] chunks = new JsonArray[numChunks];

        for (int i = 0; i < numChunks; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, arraySize);
            JsonArray chunk = new JsonArray();
            for (int j = start; j < end; j++) {
                chunk.add(jsonArray.get(j));
            }
            chunks[i] = chunk;
        }
        return chunks;
    }

    /**
     * Parses a JSON array and creates a new array of the same type as the provided array.
     *
     * @param <T> The type of the array elements.
     * @param key The JSON key to look for (e.g., "releases", "cdstubs").
     * @param processor A functional interface to process each {@link JsonObject} in the JSON array
     *                  and convert it into an object of type {@code T}.
     * @param array An example array of type {@code T[]} used to determine the type of the output array.
     * @return A new array of type {@code T[]} containing the processed elements from the JSON array.
     *         If the key does not exist or the JSON array is empty, an empty array is returned.
     */
    @SuppressWarnings("unchecked")
    private <T> T[] parseJsonArray(String key, JsonArrayProcessor<T> processor, T[] array) {
        if(!json.has(key)) {
            // Return an empty array if the key does not exist
            return (T[]) Array.newInstance(array.getClass().getComponentType(), 0);
        }

        JsonArray jsonArray = json.getAsJsonArray(key);
        JsonArray[] chunks = splitJsonArray(jsonArray);

        // Use multithreading to process the JSON array
        // This is a list of promises that promise to return a list
        // of whatever type T is, e.g., MusicBrainzRelease.
        List<Future<List<T>>> futures = new ArrayList<>();
        for (JsonArray chunk : chunks) {
            // Submit a task to the thread manager
            futures.add(Constants.THREAD_MANAGER.submit(() -> {
                List<T> result = new ArrayList<>();
                for (JsonElement element : chunk) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    result.add(processor.process(jsonObject));
                }
                return result;
            }));
        }

        List<T> resultList = new ArrayList<>();
        try {
            for (Future<List<T>> future : futures) {
                resultList.addAll(future.get());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        return resultList.toArray((T[]) Array.newInstance(array.getClass().getComponentType(), resultList.size()));
    }

    /**
    * Gets releases from the JSON.
    * @return An array of the releases.
     */
    public MusicBrainzRelease[] getReleases() {
        return parseJsonArray("releases", jsonObject -> {
            // Get the title, date, track count, and id from the JSON object
            // If the value does not exist in JSON, n/a will be returned
            String title = jsonHasAndIsNotNull(jsonObject, "title") ? jsonObject.get("title").getAsString() : null;
            String date = jsonHasAndIsNotNull(jsonObject, "date") ? jsonObject.get("date").getAsString() : null;
            int trackCount = jsonHasAndIsNotNull(jsonObject, "track-count") ? jsonObject.get("track-count").getAsInt() : -1;
            String id = jsonHasAndIsNotNull(jsonObject, "id") ? jsonObject.get("id").getAsString() : null;

            // Get all artists as a String[]
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

    /**
     * Gets CD stubs from the JSON.
     * @return An array of the CD stubs.
     */
    public MusicBrainzCDStub[] getCDStubs() {
        return parseJsonArray("cdstubs", jsonObject -> {
            // Read the title, track count, and id from the JSON object
            // Also applies n/a should the value not exist in JSON
            String title = jsonHasAndIsNotNull(jsonObject, "title") ? jsonObject.get("title").getAsString() : null;
            String id = jsonHasAndIsNotNull(jsonObject, "id") ? jsonObject.get("id").getAsString() : null;
            int trackCount = jsonHasAndIsNotNull(jsonObject, "count") ? jsonObject.get("count").getAsInt() : -1;
            String artist = jsonHasAndIsNotNull(jsonObject, "artist") ? jsonObject.get("artist").getAsString() : null;

            // Keep in mind that CDStubs only have one artist
            return new MusicBrainzCDStub(id, title, new String[] {artist}, trackCount);
        }, new MusicBrainzCDStub[0]);
    }

    /**
     * Gets Artists from the JSON.
     * @return An array of the artists.
     */
    public MusicBrainzArtist[] getArtists() {
        return parseJsonArray("artists", jsonObject -> {
            String name = null;
            if (jsonHasAndIsNotNull(jsonObject, "name")) {
                JsonElement nameElement = jsonObject.get("name");
                if (nameElement != null && !nameElement.isJsonNull()) {
                    name = nameElement.getAsString();
                }
            }
            String type = jsonHasAndIsNotNull(jsonObject, "type") ? jsonObject.get("type").getAsString() : null;

            // THE CODE BELOW MAY CAUSE FUTURE BUGS, THOUGH UNLIKELY.
            // Keep in mind that the life-span.begin JSON for artists is
            // either the date organized or the birthdate, depending on if
            // the artist is a group or a person.
            String birthDate;
            String organizedDate;
            if (jsonHasAndIsNotNull(jsonObject, "life-span")) {
                JsonObject lifeSpanObject = jsonObject.getAsJsonObject("life-span");
                if("Group".equals(type)) {
                    organizedDate = jsonHasAndIsNotNull(lifeSpanObject, "begin") ? lifeSpanObject.get("begin").getAsString() : null;
                    birthDate = null;
                } else if ("Person".equals(type)) {
                    birthDate = jsonHasAndIsNotNull(lifeSpanObject, "begin") ? lifeSpanObject.get("begin").getAsString() : null;
                    organizedDate = null;
                } else {
                    birthDate = null;
                    organizedDate = null;
                }
            } else {
                birthDate = null;
                organizedDate = null;
            }

            String id = jsonHasAndIsNotNull(jsonObject, "id") ? jsonObject.get("id").getAsString() : null;
            String sortName = jsonHasAndIsNotNull(jsonObject, "sort-name") ? jsonObject.get("sort-name").getAsString() : null;
            // Groups do not have genders in this API.
            Gender gender = Gender.fromString(jsonHasAndIsNotNull(jsonObject, "gender") ? jsonObject.get("gender").getAsString() : null);

            String disambiguation;
            // The code below MAY also be problematic, although VERY unlikely.
            if (jsonHasAndIsNotNull(jsonObject, "tags")) {
                JsonArray tagsArray = jsonObject.getAsJsonArray("tags");
                if (!tagsArray.isEmpty()) {
                    JsonObject firstTagObject = tagsArray.get(0).getAsJsonObject();
                    disambiguation = jsonHasAndIsNotNull(firstTagObject, "name") ? firstTagObject.get("name").getAsString() : null;
                    int highestCount = 0;
                    for (JsonElement tagElement : tagsArray) {
                        JsonObject tagObject = tagElement.getAsJsonObject();
                        int count = jsonHasAndIsNotNull(tagObject, "count") ? tagObject.get("count").getAsInt() : -1;
                        if (count > highestCount) {
                            highestCount = count;
                            disambiguation = jsonHasAndIsNotNull(tagObject, "name") ? tagObject.get("name").getAsString() : null;
                        }
                    }
                } else {
                    disambiguation = null;
                }
            } else {
                disambiguation = null;
            }

            String lifeSpan;
            if (jsonHasAndIsNotNull(jsonObject, "life-span")) {
                JsonObject lifeSpanObject = jsonObject.getAsJsonObject("life-span");
                if (jsonHasAndIsNotNull(lifeSpanObject, "begin") && jsonHasAndIsNotNull(lifeSpanObject, "end")) {
                    lifeSpan = lifeSpanObject.get("begin").getAsString() + " - " + lifeSpanObject.get("end").getAsString();
                } else {
                    lifeSpan = null;
                }
            } else {
                lifeSpan = null;
            }

            String country = jsonHasAndIsNotNull(jsonObject, "country") ? jsonObject.get("country").getAsString() : null;

            return new MusicBrainzArtist(name, organizedDate, birthDate, id, sortName, gender, type, disambiguation, lifeSpan, country);
        }, new MusicBrainzArtist[0]);
    }

    /**
     * Gets track listing from the JSON from a discID.
     */
    public MusicBrainzTrack[] getTracks() {
        return parseJsonArray("tracks", jsonObject -> {
            String title = jsonHasAndIsNotNull(jsonObject, "title") ? jsonObject.get("title").getAsString() : null;
            String artist = jsonHasAndIsNotNull(jsonObject, "artist") ? jsonObject.get("artist").getAsString() : null;
            int length = jsonHasAndIsNotNull(jsonObject, "length") ? jsonObject.get("length").getAsInt() : -1;

            return new MusicBrainzTrack(title, artist, length);
        }, new MusicBrainzTrack[0]);
    }

    /**
     * Gets track listing from a release.
     * @return An array of MusicBrainzTrack objects.
     */
    public MusicBrainzTrack[] getReleaseTracks() {
        List<MusicBrainzTrack> trackList = new ArrayList<>();

        JsonArray mediaArray = json.getAsJsonArray("media");
        if (mediaArray == null) {
            JOptionPane.showMessageDialog(null, "No media found in JSON.", "Error", JOptionPane.ERROR_MESSAGE);
            return new MusicBrainzTrack[0];
        }
        boolean vinylWarningShown = false, tnWarningShown = false;
        for (JsonElement mediaElement : mediaArray) {
            JsonObject mediaObject = mediaElement.getAsJsonObject(); // Cast each element to JsonObject
            JsonArray trackArray = mediaObject.getAsJsonArray("tracks");
            for (JsonElement trackElement : trackArray) {
                JsonObject trackObject = trackElement.getAsJsonObject();
                String title = jsonHasAndIsNotNull(trackObject, "title") ? trackObject.get("title").getAsString() : null;
                int length = jsonHasAndIsNotNull(trackObject, "length") ? trackObject.get("length").getAsInt() : -1;
                int trackNumber = -1;
                // tn represents track number

                if (trackObject.has("number")) {
                    try {
                        // I guess that some vinyl discs have track number A1 which nukes this program.
                        trackNumber = trackObject.get("number").getAsInt();
                    } catch (NumberFormatException e) {
                        if (!vinylWarningShown) {
                            JOptionPane.showMessageDialog(null, "This is most likely a vinyl release. Track numbers are different on vinyl, but this release will still be processed. \n Things might get funky from here :/");
                            vinylWarningShown = true;
                        }
                        // Try to see if the position exists instead
                        if (trackObject.has("position")) {
                            try {
                                trackNumber = trackObject.get("position").getAsInt();
                            } catch (NumberFormatException e2) {
                                if (!tnWarningShown) {
                                    JOptionPane.showMessageDialog(null, "This release doesn't have correctly formatted track numbers. \n It will still be processed, but things might get strange from here.", "Warning", JOptionPane.WARNING_MESSAGE);
                                    tnWarningShown = true;
                                }
                            }
                        }
                    }
                } else if (trackObject.has("position")) {
                    trackNumber = trackObject.get("position").getAsInt();
                }

                trackList.add(new MusicBrainzTrack(title, null, length, trackNumber));
            }
        }

        return trackList.toArray(new MusicBrainzTrack[0]);
    }

    /**
    * Creates a table model from an array of items.
    * @param items The array of items. Usually a MusicBrainzRelease, MusicBrainzCDStub, etc.
    * @param columnNames The names of the columns.
    * @param extractor The extractor that extracts the data from the item.
     */
    private DefaultTableModel createTableModel(Object[] items, String[] columnNames, DataExtractor extractor) {
        // Make sure that the array is not null or empty
        if (items == null || items.length == 0) {
            // If so, return an empty table model with column names
            return new DefaultTableModel(new String[0][0], columnNames);
        }
        String[][] data = new String[items.length][columnNames.length];
        // Use the extractor provided to extract the data from the item
        for (int i = 0; i < items.length; i++) {
            data[i] = extractor.extractData(items[i]);
        }
        return new DefaultTableModel(data, columnNames);
    }

    /**
     * Gets the releases as a table model.
     * @param releaseArray The array of releases.
     * @return The table model.
     */
    public DefaultTableModel getReleasesAsTableModel(MusicBrainzRelease[] releaseArray) {
        String[] columnNames = {"Release Name", "Artist", "Track Count", "Date"};
        return createTableModel(releaseArray, columnNames, item -> {
            MusicBrainzRelease release = (MusicBrainzRelease) item;
            ProgramWindow.addId(getOrDefault(release.getId()));
            return new String[]{
                    getOrDefault(release.getTitle()),
                    getOrDefault(release.getArtistsAsString()),
                    getOrDefault(release.getTrackCount() != -1 ? String.valueOf(release.getTrackCount()) : null),
                    getOrDefault(release.getDate())
            };
        });
    }

    /**
     * Gets the CD stubs as a table model.
     * @param cdStubArray The array of CD stubs.
     * @return The table model.
     */
    public DefaultTableModel getCDStubsAsTableModel(MusicBrainzCDStub[] cdStubArray) {
        String[] columnNames = {"Disc Name", "Artist", "Track Count"};
        return createTableModel(cdStubArray, columnNames, item -> {
            MusicBrainzCDStub cdStub = (MusicBrainzCDStub) item;
            ProgramWindow.addId(getOrDefault(cdStub.getId()));
            return new String[]{
                    getOrDefault(cdStub.getTitle()),
                    getOrDefault(cdStub.getArtistsAsString()),
                    getOrDefault(String.valueOf(cdStub.getTrackCount()))
            };
        });
    }

    /**
     * Gets the artists as a table model.
     * @param artistArray The array of artists.
     * @return The table model.
     */
    public DefaultTableModel getArtistsAsTableModel(MusicBrainzArtist[] artistArray) {
        String[] columnNames = {"Artist Name", "Date Organised", "Birthdate", "Sort Name", "Gender", "Type", "Disambiguation", "Life Span", "Country"};
        return createTableModel(artistArray, columnNames, item -> {
           MusicBrainzArtist artist = (MusicBrainzArtist) item;
           ProgramWindow.addId(getOrDefault(artist.getId()));
           return new String[] {
                   getOrDefault(artist.getName()),
                   getOrDefault(artist.getDateOrganized()),
                   getOrDefault(artist.getBirthDate()),
                   getOrDefault(artist.getSortName()),
                   getOrDefault(artist.getGender().toString().toLowerCase(Locale.ROOT)),
                   getOrDefault(artist.getType()),
                   getOrDefault(artist.getDisambiguation()),
                   getOrDefault(artist.getLifeSpan()),
                   getOrDefault(artist.getCountry())
           };
        });
    }

    /**
     * Gets the tracks as a table model.
     * @param trackArray The array of tracks.
     */
    public DefaultTableModel getTracksAsTableModel(MusicBrainzTrack[] trackArray) {
        String[] columnNames = {"#", "Track Name", "Length"};
        return createTableModel(trackArray, columnNames, item -> {
            MusicBrainzTrack track = (MusicBrainzTrack) item;
            return new String[]{
                    getOrDefault(Integer.toString(track.getTrackNumber())),
                    getOrDefault(track.getTitle()),
                    getOrDefault(track.getLength())
            };
        });
    }

    /**
     * Functional interface for extracting data from an item.
     */
    @FunctionalInterface
    private interface DataExtractor {
        String[] extractData(Object item);
    }

    /**
     * Functional interface for processing a JSON object.
     * @param <T> The type of the object to be processed.
     */
    @FunctionalInterface
    private interface JsonArrayProcessor<T> {
        T process(JsonObject jsonObject);
    }

    /**
     * Checks if a JSON object has a member and is not null.
     * @param jsonObject The JSON object.
     * @param memberName The member name to check for.
     * @return True if the member exists and is not null, false otherwise.
     */
    private boolean jsonHasAndIsNotNull(JsonObject jsonObject, String memberName) {
        return jsonObject.has(memberName) && !jsonObject.get(memberName).isJsonNull();
    }

    /**
     * Gets a value or returns "n/a" if the value is null.
     * @param value The value to check.
     * @return The value or "n/a" if the value is null.
     */
    private String getOrDefault(String value) {
        return value != null ? value : "";
    }

    @Override
    public String toString() {
        return json.toString();
    }
}