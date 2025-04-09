/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This class helps you build a URL request to the MusicBrainz API.
 */

package com.CDPrintable.MusicBrainzResources;

/**
 * A class that helps you build a URL request to the MusicBrainz API.
 */
public class MusicBrainzRequest {
    private final String query;
    private final String queryType;
    private final String otherParams;

    /**
     * Constructor for MusicbrainzRequest.
     * @param query The query string.
     * @param queryType Query type. Possible values: artist, cdstub, tracks, release.
     */
    public MusicBrainzRequest(String queryType, String query) {
        this.query = query;
        this.queryType = queryType;
        otherParams = null;
    }

    /**
     * Constructor for MusicbrainzRequest with additional parameters.
     * @param queryType The query type.
     * @param query The query string.
     * @param otherParams Other parameters for the request. MUST be in the format of "&key=value&key2=value2".
     */
    public MusicBrainzRequest(String queryType, String query, String otherParams) {
        this.query = query;
        this.queryType = queryType;
        this.otherParams = otherParams;
    }

    /**
     * Builds the request URL for the Musicbrainz API.
     * @return The request URL.
     */
    public String buildRequestURL() {
        StringBuilder url = new StringBuilder("https://musicbrainz.org/ws/2/");

        switch (queryType) {
            case "artist":
                url.append("artist");
                break;
            case "cdstub":
                url.append("cdstub");
                break;
            case "tracks":
                url.append("discid");
                break;
            case "release":
                url.append("release");
                break;
            default:
                throw new IllegalArgumentException("Invalid query type: " + queryType);
        }
        url.append("?query=").append(query);
        url.append("&fmt=json");
        if (otherParams != null) {
            url.append(otherParams);
        }

        return url.toString().replace(" ", "%20");
    }

    /**
     * Gets the query string.
     * @return The query string.
     */
    @Override
    public String toString() {
        return "MusicbrainzRequest{" +
                "query='" + query + '\'' +
                ", queryType='" + queryType + '\'' +
                '}';
    }
}