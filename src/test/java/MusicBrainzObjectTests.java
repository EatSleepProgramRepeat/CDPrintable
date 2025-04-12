import com.CDPrintable.MusicBrainzResources.MusicBrainzCDStub;
import com.CDPrintable.MusicBrainzResources.MusicBrainzJSONReader;
import com.CDPrintable.MusicBrainzResources.MusicBrainzRelease;
import org.junit.jupiter.api.Test;

import javax.swing.table.DefaultTableModel;

import static org.junit.jupiter.api.Assertions.*;

public class MusicBrainzObjectTests {
    private String json;

    @Test
    public void invalidJsonString() {
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader("<xml></xml>");
        MusicBrainzRelease[] releases = reader.getReleases();

        assertEquals("{}", reader.toString());
        assertEquals("MusicBrainzRelease", releases[0].getClass().getSimpleName());
    }

    @Test
    void testGetReleasesWithMissingOrNullFields() {
        String jsonString = "{\"releases\": [{\"title\": null, \"date\": null, \"count\": null, \"id\": null, \"artist-credit\": []}]}";
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(jsonString);

        MusicBrainzRelease[] releases = reader.getReleases();

        assertNotNull(releases);
        assertEquals(1, releases.length);
        assertNull(releases[0].getTitle());
        assertNull(releases[0].getDate());
        assertEquals(-1, releases[0].getTrackCount());
        assertNull(releases[0].getId());
        assertEquals(0, releases[0].getArtists().length);
    }

    @Test
    void testGetReleasesWithEmptyReleasesArray() {
        String jsonString = "{\"releases\": []}";
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(jsonString);

        MusicBrainzRelease[] releases = reader.getReleases();

        assertNotNull(releases);
        assertEquals(0, releases.length);
    }

    @Test
    void testGetReleasesWithInvalidJsonStructure() {
        String jsonString = "{\"invalidKey\": []}";
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(jsonString);

        MusicBrainzRelease[] releases = reader.getReleases();

        assertEquals("MusicBrainzRelease", releases[0].getClass().getSimpleName());
    }

    @Test
    void testGetCDStubsWithMissingOrNullFields() {
        String jsonString = "{\"cdstubs\": [{\"title\": null, \"id\": null, \"count\": null, \"artist\": null}]}";
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(jsonString);

        MusicBrainzCDStub[] cdStubs = reader.getCDStubs();

        assertNotNull(cdStubs);
        assertEquals(1, cdStubs.length);
        assertNull(cdStubs[0].getTitle());
        assertNull(cdStubs[0].getId());
        assertEquals(-1, cdStubs[0].getTrackCount());
        assertEquals(1, cdStubs[0].getArtists().length);
        assertNull(cdStubs[0].getArtists()[0]);
    }

    @Test
    void testGetCDStubsWithEmptyCDStubsArray() {
        String jsonString = "{\"cdstubs\": []}";
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(jsonString);

        MusicBrainzCDStub[] cdStubs = reader.getCDStubs();

        assertNotNull(cdStubs);
        assertEquals(0, cdStubs.length);
    }

    @Test
    void testGetCDStubsWithInvalidJsonStructure() {
        String jsonString = "{\"invalidKey\": []}";
        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(jsonString);

        MusicBrainzCDStub[] cdStubs = reader.getCDStubs();

        assertEquals("MusicBrainzCDStub", cdStubs[0].getClass().getSimpleName());
        assertEquals(-1, cdStubs[0].getTrackCount());
        assertEquals(0, cdStubs[0].getArtists().length);
        assertEquals("", cdStubs[0].getId());
        assertEquals("", cdStubs[0].getTitle());
    }

    @Test
    void testGetReleasesAsTableModelWithEmptyArray() {
        DefaultTableModel tableModel = new MusicBrainzJSONReader("").getReleasesAsTableModel(new MusicBrainzRelease[0]);

        assertNotNull(tableModel);
        assertEquals(0, tableModel.getRowCount());
    }

    @Test
    void testGetReleasesAsTableModelWithNullArray() {
        DefaultTableModel tableModel = new MusicBrainzJSONReader("").getReleasesAsTableModel(null);

        assertNotNull(tableModel);
        assertEquals(0, tableModel.getRowCount());
    }

    @Test
    void testGetCDStubsAsTableModelWithEmptyArray() {
        DefaultTableModel tableModel = new MusicBrainzJSONReader("").getCDStubsAsTableModel(new MusicBrainzCDStub[0]);

        assertNotNull(tableModel);
        assertEquals(0, tableModel.getRowCount());
    }

    @Test
    void testGetCDStubsAsTableModelWithNullArray() {
        DefaultTableModel tableModel = new MusicBrainzJSONReader("").getCDStubsAsTableModel(null);

        assertNotNull(tableModel);
        assertEquals(0, tableModel.getRowCount());
    }
}