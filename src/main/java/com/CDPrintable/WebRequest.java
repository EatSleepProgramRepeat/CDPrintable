package com.CDPrintable;

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
     * @throws IOException
     * @throws URISyntaxException
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
