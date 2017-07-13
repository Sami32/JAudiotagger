package org.jaudiotagger.issues;

import org.jaudiotagger.AbstractTestCase;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.generic.FileDataSource;
import org.jaudiotagger.audio.mp4.Mp4AtomTree;
import org.jaudiotagger.tag.FieldKey;
import org.junit.Test;

import java.io.File;
import java.io.RandomAccessFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unable to write, offsets do not match
 */
public class Issue291Test extends AbstractTestCase {
    @Test
    public void testSavingFile() {
        File orig = new File("testdata", "test83.mp4");
        if (!orig.isFile()) {
            System.err.println("Unable to test file - not available");
            return;
        }


        File testFile = null;
        Exception exceptionCaught = null;
        try {
            testFile = AbstractTestCase.copyAudioToTmp("test83.mp4");
            AudioFile af = AudioFileIO.read(testFile);
            System.out.println("Tag is" + af.getTag().toString());
            af.getTag().setField(af.getTag().createField(FieldKey.ARTIST, "Kenny Rankin1"));
            af.commit();
            af = AudioFileIO.read(testFile);
            assertEquals("Kenny Rankin1", af.getTag().getFirst(FieldKey.ARTIST));
        } catch (Exception e) {
            e.printStackTrace();
            exceptionCaught = e;
        }

        assertNull(exceptionCaught);
    }


    @Test
    public void testPrintAtomTree() {
        File orig = new File("testdata", "test83.mp4");
        if (!orig.isFile()) {
            System.err.println("Unable to test file - not available");
            return;
        }


        File testFile = null;
        Exception exceptionCaught = null;
        try {
            testFile = AbstractTestCase.copyAudioToTmp("test83.mp4");
            Mp4AtomTree atomTree = new Mp4AtomTree(new FileDataSource(new RandomAccessFile(testFile, "r")));
            atomTree.printAtomTree();
        } catch (Exception e) {
            e.printStackTrace();
            exceptionCaught = e;
        }

        assertNull(exceptionCaught);
    }
}
