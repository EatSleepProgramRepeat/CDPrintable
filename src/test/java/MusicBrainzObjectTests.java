import com.CDPrintable.MusicBrainzResources.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.swing.table.DefaultTableModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class MusicBrainzObjectTests {
    // Read example JSON files
    private final String releasesJson = readFile("src/test/resources/ReleaseExample.json");
    private final String cdStubsJson = readFile("src/test/resources/CDStubExample.json");
    private final String artistsJson = readFile("src/test/resources/ArtistExample.json");

    /**
     * Test that the JSON reader can handle an invalid JSON string.
     */
    @Test
    public void invalidJsonString() {
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader("<xml></xml>");
        MusicBrainzRelease[] releases = reader.getReleases();

        assertEquals("{}", reader.toString());

        assertNotNull(releases);
        assertEquals(0, releases.length);
    }

    /**
     * Test that the JSON reader can handle an empty JSON string as well as invalid JSON.
     * This might happen if the database is down.
     */
    @ParameterizedTest
    @CsvSource({
            "{\"invalidKey\": []}, releases",
            "{\"releases\": []}, releases",
            "{\"invalidKey\": []}, cdstubs",
            "{\"cdstubs\": []}, cdstubs",
            "{\"invalidKey\": []}, artists",
            "{\"artists\": []}, artists"
    })
    void testGetItemsWithInvalidJsonStructure(String jsonString, String key) {
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(jsonString);

        if (key.equals("releases")) {
            MusicBrainzRelease[] releases = reader.getReleases();
            assertNotNull(releases);
            assertEquals(0, releases.length);
        } else if (key.equals("cdstubs")) {
            MusicBrainzCDStub[] cdStubs = reader.getCDStubs();
            assertNotNull(cdStubs);
            assertEquals(0, cdStubs.length);
        }
    }

    /**
     * Test that the JSON reader can handle a JSON string with missing or null fields.
     * This is a very common occurrence, however the server typically would return an
     * empty String instead of null.
     */
    @ParameterizedTest
    @CsvSource({
            "{\"releases\": [{\"title\": null, \"date\": null, \"count\": null, \"id\": null, \"artist-credit\": []}]}, releases",
            "{\"cdstubs\": [{\"title\": null, \"id\": null, \"count\": null, \"artist\": null}]}, cdstubs",
            "{\"artists\": [{\"name\": null, \"sort-name\": null, \"country\": null, \"gender\": null, \"disambiguation\": null, \"life-span\": null}]}, artists"
    })
    void testGetItemsWithMissingOrNullFields(String jsonString, String key) {
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(jsonString);

        if (key.equals("releases")) {
            MusicBrainzRelease[] releases = reader.getReleases();
            assertNotNull(releases);
            assertEquals(1, releases.length);

            assertNull(releases[0].getTitle());
            assertNull(releases[0].getDate());
            assertEquals(-1, releases[0].getTrackCount());
            assertNull(releases[0].getId());
            assertEquals(0, releases[0].getArtists().length);
        } else if (key.equals("cdstubs")) {
            MusicBrainzCDStub[] cdStubs = reader.getCDStubs();
            assertNotNull(cdStubs);
            assertEquals(1, cdStubs.length);

            assertNull(cdStubs[0].getTitle());
            assertNull(cdStubs[0].getId());
            assertEquals(-1, cdStubs[0].getTrackCount());
            assertNull(cdStubs[0].getArtists()[0]);
        }
    }

    /**
     * Test the JSON reader against array issues. There are many cases tested here.
     * Case 1: (CDStubs and Releases) The array is null.
     * Case 2: (CDStubs and Releases) The array is empty.
     * Case 3: (CDStubs and Releases) The array is still cooked but contains a null field.
     * Case 4: (CDStubs and Releases) The array is still cooked but contains an invalid key.
     * @param key The array key to test. (releases or cdstubs) (more to come)
     * @param isNull Whether the array is null or not.
     * @param readerJson The JSON string to test.
     */
    @ParameterizedTest
    @CsvSource({
            "releases, true, null",                     // case 1
            "releases, false, {}",                      // case 2
            "cdstubs, true, null",                      // case 1
            "cdstubs, false, {}",                       // case 2
            "artists, true, null",                      // case 1
            "artists, false, {}",                       // case 2
            "releases, false, {\"releases\": null}",    // case 3
            "cdstubs, false, {\"cdstubs\": null}",      // case 3
            "artists, false, {\"artists\": null}",      // case 3
            "releases, false, {\"invalidKey\": []}",    // case 4
            "cdstubs, false, {\"invalidKey\": []}",     // case 4
            "artists, false, {\"invalidKey\": []}"      // case 4
    })
    void testGetTableModelWithArrayIssues(String key, boolean isNull, String readerJson) {
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(readerJson != null ? readerJson : "");
        if (key.equals("releases")) {
            MusicBrainzRelease[] releases = isNull ? null : new MusicBrainzRelease[0];
            DefaultTableModel tableModel = reader.getReleasesAsTableModel(releases);

            assertNotNull(tableModel);
            assertEquals(0, tableModel.getRowCount());
        } else if (key.equals("cdstubs")) {
            MusicBrainzCDStub[] cdStubs = isNull ? null : new MusicBrainzCDStub[0];
            DefaultTableModel tableModel = reader.getCDStubsAsTableModel(cdStubs);

            assertNotNull(tableModel);
            assertEquals(0, tableModel.getRowCount());
        }
    }

    /**
     * Test the JSON reader against the example JSON files. This is effectively
     * normal operation for the program.
     * This test is for Releases.
     */
    @Test
    void genericGetReleasesTest() {
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(releasesJson);
        MusicBrainzRelease[] releases = reader.getReleases();

        assertNotNull(releases);
        assertEquals(2, releases.length);

        assertEquals("Nights Like This", releases[0].getTitle());
        assertEquals("Witt Lowry", releases[0].getArtists()[0]);
        assertEquals(1, releases[0].getArtists().length);
        assertEquals("2022-10-28", releases[0].getDate());
        assertEquals(1, releases[0].getTrackCount());
        assertEquals("b47fb945-e576-4f89-bd83-8b5e809fbd0b", releases[0].getId());

        assertEquals("Nights Like This", releases[1].getTitle());
        assertEquals("Kaylee Bell", releases[1].getArtists()[0]);
        assertEquals(1, releases[1].getArtists().length);
        assertNull(releases[1].getDate());
        assertEquals(11, releases[1].getTrackCount());
        assertEquals("846ee5f9-ad18-4f3a-a883-43ec58ca0805", releases[1].getId());
    }

    /**
     * Test the JSON reader against the example JSON files. This is effectively
     * normal operation for the program.
     * This test is for CDStubs.
     */
    @SuppressWarnings("SpellCheckingInspection")
    @Test
    void genericGetCDStubsTest() {
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(cdStubsJson);
        MusicBrainzCDStub[] cdStubs = reader.getCDStubs();

        assertNotNull(cdStubs);
        assertEquals(2, cdStubs.length);

        assertEquals("Songs From The Big Chair", cdStubs[0].getTitle());
        assertEquals("Tears For Fears", cdStubs[0].getArtists()[0]);
        assertEquals(1, cdStubs[0].getArtists().length);
        assertEquals(8, cdStubs[0].getTrackCount());
        assertEquals("hU2FJcnlVtratcrOn9rAhPzzCMo-", cdStubs[0].getId());

        assertEquals("+", cdStubs[1].getTitle());
        assertEquals("Ed Sheeran", cdStubs[1].getArtists()[0]);
        assertEquals(1, cdStubs[1].getArtists().length);
        assertEquals(13, cdStubs[1].getTrackCount());
        assertEquals("KZn02eYalzdXJNbtmuz2xKDzLZU-", cdStubs[1].getId());
    }

    /**
     * Test the JSON reader against the example JSON files. This is effectively
     * normal operation for the program.
     * This test is for Artists.
     */
    @SuppressWarnings("SpellCheckingInspection")
    @Test
    void genericGetArtistsTest() {
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(artistsJson);
        MusicBrainzArtist[] artists = reader.getArtists();

        assertNotNull(artists);
        assertEquals(2, artists.length);

        assertEquals("Tears for Fears", artists[0].getName());
        assertEquals("Tears for Fears", artists[0].getSortName());
        assertEquals("1981", artists[0].getDateOrganized());
        assertNull(artists[0].getBirthDate());
        assertEquals("7c7f9c94-dee8-4903-892b-6cf44652e2de", artists[0].getId());
        assertEquals(Gender.UNKNOWN, artists[0].getGender());
        assertEquals("Group", artists[0].getType());
        assertEquals("new wave", artists[0].getDisambiguation());
        assertNull(artists[0].getLifeSpan());
        assertEquals("United Kingdom", artists[0].getCountry());

        assertEquals("The Kid LAROI", artists[1].getName());
        assertEquals("Kid Laroi, The", artists[1].getSortName());
        assertNull(artists[1].getDateOrganized());
        assertEquals("2003-08-16", artists[1].getBirthDate());
        assertEquals("80609a00-b394-4a49-975b-2db6b543fa97", artists[1].getId());
        assertEquals(Gender.MALE, artists[1].getGender());
        assertEquals("Person", artists[1].getType());
        assertEquals("hip hop", artists[1].getDisambiguation());
        assertNull(artists[1].getLifeSpan());
        assertEquals("Australia", artists[1].getCountry());

    }

    /**
     * Tests the JSON reader again, but requests a table model and tests that instead.
     * This is effectively normal operation for the program.
     * This test is for Releases.
     */
    @Test
    void genericGetReleasesAsTableModelTest() {
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(releasesJson);
        MusicBrainzRelease[] releases = reader.getReleases();
        DefaultTableModel tableModel = reader.getReleasesAsTableModel(releases);

        assertNotNull(tableModel);
        assertEquals(2, tableModel.getRowCount());
        assertEquals(5, tableModel.getColumnCount());

        assertEquals("Nights Like This", tableModel.getValueAt(0, 0));
        assertEquals("Witt Lowry", tableModel.getValueAt(0, 1));
        assertEquals("1", tableModel.getValueAt(0, 2));
        assertEquals("2022-10-28", tableModel.getValueAt(0, 3));
        assertEquals("", tableModel.getValueAt(0, 4));

        assertEquals("Nights Like This", tableModel.getValueAt(1, 0));
        assertEquals("Kaylee Bell", tableModel.getValueAt(1, 1));
        assertEquals("11", tableModel.getValueAt(1, 2));
        assertEquals("", tableModel.getValueAt(1, 3));
        assertEquals("", tableModel.getValueAt(1, 4));
    }

    /**
     * Tests the JSON reader again, but requests a table model and tests that instead.
     * This is effectively normal operation for the program.
     * This test is for CDStubs.
     */
    @Test
    void genericGetCDStubsAsTableModelTest() {
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(cdStubsJson);
        MusicBrainzCDStub[] cdStubs = reader.getCDStubs();
        DefaultTableModel tableModel = reader.getCDStubsAsTableModel(cdStubs);

        assertNotNull(tableModel);
        assertEquals(2, tableModel.getRowCount());
        assertEquals(4, tableModel.getColumnCount());

        assertEquals("Songs From The Big Chair", tableModel.getValueAt(0, 0));
        assertEquals("Tears For Fears", tableModel.getValueAt(0, 1));
        assertEquals("8", tableModel.getValueAt(0, 2));
        assertEquals("", tableModel.getValueAt(0, 3));

        assertEquals("+", tableModel.getValueAt(1, 0));
        assertEquals("Ed Sheeran", tableModel.getValueAt(1, 1));
        assertEquals("13", tableModel.getValueAt(1, 2));
        assertEquals("", tableModel.getValueAt(1, 3));
    }

    /**
     * Tests the JSON reader again, but requests a table model and tests that instead.
     * This is effectively normal operation for the program.
     * This test is for Artists.
     */
    @Test
    void genericGetArtistsAsTableModelTest() {
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(artistsJson);
        MusicBrainzArtist[] artists = reader.getArtists();
        DefaultTableModel tableModel = reader.getArtistsAsTableModel(artists);

        assertNotNull(tableModel);
        assertEquals(2, tableModel.getRowCount());
        assertEquals(10, tableModel.getColumnCount());

        assertEquals("Tears for Fears", tableModel.getValueAt(0, 0));
        assertEquals("1981", tableModel.getValueAt(0, 1));
        assertEquals("", tableModel.getValueAt(0, 2));
        assertEquals("Tears for Fears", tableModel.getValueAt(0, 3));
        assertEquals("unknown", tableModel.getValueAt(0, 4));
        assertEquals("Group", tableModel.getValueAt(0, 5));
        assertEquals("new wave", tableModel.getValueAt(0, 6));
        assertEquals("", tableModel.getValueAt(0, 7));
        assertEquals("United Kingdom", tableModel.getValueAt(0, 8));
        assertEquals("", tableModel.getValueAt(0, 9));

        assertEquals("The Kid LAROI", tableModel.getValueAt(1, 0));
        assertEquals("", tableModel.getValueAt(1, 1));
        assertEquals("2003-08-16", tableModel.getValueAt(1, 2));
        assertEquals("Kid Laroi, The", tableModel.getValueAt(1, 3));
        assertEquals("male", tableModel.getValueAt(1, 4));
        assertEquals("Person", tableModel.getValueAt(1, 5));
        assertEquals("hip hop", tableModel.getValueAt(1, 6));
        assertEquals("", tableModel.getValueAt(1, 7));
        assertEquals("Australia", tableModel.getValueAt(1, 8));
        assertEquals("", tableModel.getValueAt(1, 9));
    }

    /**
     * Read a file and return its contents as a String.
     * This is a helper method used for the example JSON files.
     * @param filePath The path to the file.
     * @return The contents of the file as a String.
     */
    private String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            System.out.println("There was an IO exception when reading the examples.");
            return "";
        }
    }
}