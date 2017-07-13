package org.jaudiotagger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.tag.*;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class FilePermissionsTest extends AbstractTestCase {

	public void runWriteWriteProtectedFileWithCheckDisabled(String sourceFile) throws Exception {
		File testFile = createFile(sourceFile);
		try {
			testFile.setWritable(false);
			boolean threwException = false;
			try {
				setFieldAndCommit(testFile, false);
			} catch (NoWritePermissionsException success) {
				threwException = true;
			}
			assertTrue("Expected to throw " + NoWritePermissionsException.class.getSimpleName() + " but didn't",
					threwException);
		} finally {
			testFile.delete();
		}
	}

	public void runWriteWriteProtectedFileWithCheckEnabled(String sourceFile) throws Exception {
		File testFile = createFile(sourceFile);
		try {
	        testFile.setWritable(false);
	        boolean threwException = false;
	        try {
	        	setFieldAndCommit(testFile, true);
	        } catch(CannotWriteException success) {
	        	threwException=true;
	        }
	        assertTrue("Expected to throw " + CannotWriteException.class.getSimpleName() + " but didn't", threwException);
		} finally {
			testFile.delete();
		}
	}

	public void runWriteReadOnlyFileWithCheckDisabled(String sourceFile) throws Exception {
		File testFile = createFile(sourceFile);
		try {
	        testFile.setReadOnly();
	        boolean threwException = false;
	        try {
	        	setFieldAndCommit(testFile, false);
	        } catch(NoWritePermissionsException success) {
	        	threwException=true;
	        }
	        assertTrue("Expected to throw " + NoWritePermissionsException.class.getSimpleName() + " but didn't", threwException);
		} finally {
			testFile.delete();
		}
	}

	private File createFile(String sourceFile) {
		String[] baseNameAndExt = sourceFile.split("\\.(?=[^\\.]+$)");
		File testFile = copyAudioToTmp(sourceFile, new File(baseNameAndExt[0] + "WriteProtected." + baseNameAndExt[1]));
		return testFile;
	}

	private void setFieldAndCommit(File testFile, boolean performPreCheck) throws CannotReadException,
			IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, FieldDataInvalidException,
			CannotWriteException {
		TagOptionSingleton.getInstance().setCheckIsWritable(performPreCheck);
		try {
			AudioFile aFile = AudioFileIO.read(testFile);
			Tag tag = aFile.getTag();
			tag.setField(FieldKey.ALBUM, "album");
			aFile.commit();
		} finally {
			testFile.setWritable(true);
			TagOptionSingleton.getInstance().setToDefault();
		}
	}


}
