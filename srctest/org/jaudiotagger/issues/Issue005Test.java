package org.jaudiotagger.issues;

import org.jaudiotagger.AbstractTestCase;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test trying to read non existent mp3 file
 */
public class Issue005Test extends AbstractTestCase {

    @Test
    public void testReadingNonExistentFile() throws Exception {
        Exception e = null;
        try {
            File orig = new File("testdata", "testNonExistent.mp3");
            MP3File f = (MP3File) AudioFileIO.read(orig);
        } catch (Exception ex) {
            e = ex;
        }
        assertTrue(e instanceof FileNotFoundException);
    }


    @Test
    public void testReadingNonExistentFileMp3() throws Exception {
        Exception e = null;
        try {
            File orig = new File("testdata", "testNonExistent.mp3");
            MP3File f = new MP3File(orig);
        } catch (Exception ex) {
            e = ex;
        }
        assertTrue(e instanceof FileNotFoundException);
    }


    @Test
    public void testReadingNonExistentFileFlac() throws Exception {
        Exception e = null;
        try {
            File orig = new File("testdata", "testNonExistent.flac");
            AudioFile af = AudioFileIO.read(orig);
            af.getTag();
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
        assertTrue(e instanceof FileNotFoundException);
    }


    @Test
    public void testReadingNonExistentFileOgg() throws Exception {
        Exception e = null;
        try {
            File orig = new File("testdata", "testNonExistent.ogg");
            AudioFile af = AudioFileIO.read(orig);
            af.getTag();
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
        assertTrue(e instanceof FileNotFoundException);
    }


    @Test
    public void testReadingNonExistentFileM4a() throws Exception {
        Exception e = null;
        try {
            File orig = new File("testdata", "testNonExistent.m4a");
            AudioFile af = AudioFileIO.read(orig);
            af.getTag();
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
        assertTrue(e instanceof FileNotFoundException);
    }


    @Test
    public void testReadingNonExistentFileWma() throws Exception {
        Exception e = null;
        try {
            File orig = new File("testdata", "testNonExistent.wma");
            AudioFile af = AudioFileIO.read(orig);
            af.getTag();
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
        assertTrue(e instanceof FileNotFoundException);
    }


    @Test
    public void testReadingNonExistentFileWav() throws Exception {
        Exception e = null;
        try {
            File orig = new File("testdata", "testNonExistent.wav");
            AudioFile af = AudioFileIO.read(orig);
            af.getTag();
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
        assertTrue(e instanceof FileNotFoundException);
    }

}