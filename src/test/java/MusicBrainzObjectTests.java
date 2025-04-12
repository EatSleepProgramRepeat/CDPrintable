import com.CDPrintable.MusicBrainzResources.MusicBrainzCDStub;
import com.CDPrintable.MusicBrainzResources.MusicBrainzJSONReader;
import com.CDPrintable.MusicBrainzResources.MusicBrainzRelease;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.swing.table.DefaultTableModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class MusicBrainzObjectTests {
    private final String releasesJson = readFile("src/test/resources/ReleaseExample.json");
    private final String cdStubsJson = readFile("src/test/resources/CDStubExample.json");

    @Test
    public void invalidJsonString() {
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader("<xml></xml>");
        MusicBrainzRelease[] releases = reader.getReleases();

        assertEquals("{}", reader.toString());

        assertNotNull(releases);
        assertEquals(0, releases.length);
    }

    @ParameterizedTest
    @CsvSource({
            "{\"invalidKey\": []}, releases",
            "{\"releases\": []}, releases",
            "{\"invalidKey\": []}, cdstubs",
            "{\"cdstubs\": []}, cdstubs"
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

    @ParameterizedTest
    @CsvSource({
            "{\"releases\": [{\"title\": null, \"date\": null, \"count\": null, \"id\": null, \"artist-credit\": []}]}, releases",
            "{\"cdstubs\": [{\"title\": null, \"id\": null, \"count\": null, \"artist\": null}]}, cdstubs"
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

    @ParameterizedTest
    @CsvSource({
            "releases, true, null",
            "releases, false, {}",
            "cdstubs, true, null",
            "cdstubs, false, {}",
            "releases, false, {\"releases\": null}",
            "cdstubs, false, {\"cdstubs\": null}",
            "releases, false, {\"invalidKey\": []}",
            "cdstubs, false, {\"invalidKey\": []}"
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
        assertEquals("n/a", tableModel.getValueAt(1, 3));
        assertEquals("", tableModel.getValueAt(1, 4));
    }

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

    private String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            System.out.println("There was an IO exception when reading the examples.");
            return "";
        }
    }
}