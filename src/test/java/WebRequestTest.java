import com.CDPrintable.Constants;
import com.CDPrintable.MusicBrainzRequest;
import com.CDPrintable.UserAgent;
import com.CDPrintable.WebRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.URISyntaxException;

import com.google.gson.*;

public class WebRequestTest {
    @Test
    void testBuildRequestURL() {
        MusicBrainzRequest request = new MusicBrainzRequest("artist", "The Beatles");
        String expectedURL = "https://musicbrainz.org/ws/2/artist?query=The%20Beatles&fmt=json";

        assertEquals(expectedURL, request.buildRequestURL());
    }

    @Test
    void sendRequestTest() throws IOException, URISyntaxException {
        MusicBrainzRequest musicBrainzRequest = new MusicBrainzRequest("artist", "The Beatles");
        UserAgent userAgent = new UserAgent("CDPrintable/"+Constants.VERSION, "example@example.com");
        String webRequest = new WebRequest(musicBrainzRequest, userAgent).sendRequest();

        assertNotNull(webRequest);
        assertTrue(webRequest.contains("artist"));
        assertTrue(webRequest.contains("The Beatles"));
        assertTrue(isValidJSON(webRequest));
    }

    @Test
    void invalidMusicBrainzQueryTypeTest() {
        MusicBrainzRequest request = new MusicBrainzRequest("invalid", "The Beatles");
        assertThrows(IllegalArgumentException.class, request::buildRequestURL);
    }

    @Test
    void testHttpErrorResponse() throws IOException, URISyntaxException {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setResponseCode(400));
            server.start();

            String url = server.url("/").toString();
            WebRequest webRequest = new WebRequest(url, "user-agent");

            assertThrows(IOException.class, webRequest::sendRequest);

            server.shutdown();
        }
    }

    public static boolean isValidJSON(String json) {
        try {
            JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            return false;
        }
        return true;
    }
}