/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This class sends web requests and returns the result.
 */

package com.CDPrintable;

import com.CDPrintable.MusicBrainzResources.MusicBrainzRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class WebRequest {
    private final String url;
    private final String userAgent;

    /**
     * Constructor for WebRequest. Makes a web request to the MusicBrainz API according to the given MusicBrainzRequest.
     * @param musicBrainzRequest The MusicBrainzRequest to use.
     * @param userAgent The user agent to use.
     */
    public WebRequest(MusicBrainzRequest musicBrainzRequest, UserAgent userAgent) {
        this.url = musicBrainzRequest.buildRequestURL();
        this.userAgent = userAgent.toString();
    }

    /**
     * Constructor for WebRequest. Makes a web request to the MusicBrainz API according to the given URL.
     * @param url The URL to use.
     * @param userAgent The user agent to use.
     */
    public WebRequest(String url, String userAgent) {
        this.url = url;
        this.userAgent = userAgent;
    }

    /**
     * Sends the request to the MusicBrainz API and returns the response.
     * @return The response from the MusicBrainz API.
     * @throws IOException If an I/O error occurs. Usually happens when the HTTP status code is not 200.
     * @throws URISyntaxException If the URL is invalid. HINT: Check for spaces in the URL.
     */
    public String sendRequest() throws IOException, URISyntaxException {
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        }
    }
}
