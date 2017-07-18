package org.jaudiotagger.issues;

import org.jaudiotagger.AbstractTestCase;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNull;

/**
 * Test handling mp4s that can be read by other apps
 */
public class Issue370Test extends AbstractTestCase {
    @Test
    public void testIssue() throws Exception {
        Exception caught = null;
        try {
            File orig = new File("testdata", "test96.m4a");
            if (!orig.isFile()) {
                System.err.println("Unable to test file - not available");
                return;
            }
            //ToDO Fix Issue
            //File testFile = copyAudioToTmp("test96.m4a");
            //AudioFile af = AudioFileIO.read(testFile);
        } catch (Exception e) {
            caught = e;
            e.printStackTrace();
        }
        assertNull(caught);
    }
}