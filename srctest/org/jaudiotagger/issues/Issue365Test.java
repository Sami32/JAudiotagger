package org.jaudiotagger.issues;

import org.jaudiotagger.AbstractTestCase;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test reading an ogg file with ID3 tag at start
 */
public class Issue365Test extends AbstractTestCase {
    @Test
    public void testIssue() throws Exception {
        Exception caught = null;
        try {
            File orig = new File("testdata", "test90.ogg");
            if (!orig.isFile()) {
                System.err.println("Unable to test file - not available");
                return;
            }

            File testFile = copyAudioToTmp("test90.ogg");
            AudioFile af = AudioFileIO.read(testFile);
            af.getTag().setField(FieldKey.ARTIST, "fred");
            af.commit();
            af = AudioFileIO.read(testFile);
            assertEquals("fred", af.getTag().getFirst(FieldKey.ARTIST));
        } catch (Exception e) {
            e.printStackTrace();
            caught = e;
        }
        assertNull(caught);
    }
}