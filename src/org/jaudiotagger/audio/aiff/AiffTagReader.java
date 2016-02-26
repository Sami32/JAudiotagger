
package org.jaudiotagger.audio.aiff;

import org.jaudiotagger.audio.aiff.chunk.AiffChunkReader;
import org.jaudiotagger.audio.aiff.chunk.AiffChunkType;
import org.jaudiotagger.audio.aiff.chunk.ID3Chunk;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.generic.DataSource;
import org.jaudiotagger.audio.iff.Chunk;
import org.jaudiotagger.audio.iff.ChunkHeader;
import org.jaudiotagger.audio.iff.ChunkSummary;
import org.jaudiotagger.audio.iff.IffHeaderChunk;
import org.jaudiotagger.logging.Hex;
import org.jaudiotagger.tag.aiff.AiffTag;
import org.jaudiotagger.tag.id3.ID3v22Tag;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

/**
 * Read the AIff file chunks, until finds Aiff Common chunk and then generates AudioHeader from it
 */
public class AiffTagReader extends AiffChunkReader
{
    public static Logger logger = Logger.getLogger("org.jaudiotagger.audio.aiff");

    /**
     * Read editable Metadata
     *
     * @param dataSource The data source
     * @return The {@code AiffTag}
     * @throws CannotReadException
     * @throws IOException
     */
    public AiffTag read(DataSource dataSource) throws CannotReadException, IOException
    {

        AiffAudioHeader aiffAudioHeader = new AiffAudioHeader();
        AiffTag aiffTag = new AiffTag();

        final AiffFileHeader fileHeader = new AiffFileHeader();
        fileHeader.readHeader(dataSource, aiffAudioHeader);
        while (dataSource.position() < dataSource.size())
        {
            if (!readChunk(dataSource, aiffTag))
            {
                logger.severe("UnableToReadProcessChunk:dataSource: " + dataSource);
                break;
            }
        }

        if (aiffTag.getID3Tag() == null)
        {
            //Default still used by iTunes
            aiffTag.setID3Tag(new ID3v22Tag());
        }
        return aiffTag;
    }

    /**
     * Reads an AIFF ID3 Chunk.
     *
     * @return {@code false}, if we were not able to read a valid chunk id
     */
    private boolean readChunk(DataSource dataSource, AiffTag aiffTag) throws IOException
    {
        logger.config("Reading Tag Chunk");

        ChunkHeader chunkHeader = new ChunkHeader(ByteOrder.BIG_ENDIAN);
        if (!chunkHeader.readHeader(dataSource))
        {
            return false;
        }
        logger.config("Reading Chunk:" + chunkHeader.getID() + ":starting at:" + chunkHeader.getStartLocationInFile() + "(" + Hex.asHex(chunkHeader.getStartLocationInFile()) + ")" + ":sizeIncHeader:" + (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE) + ":dataSource:" + dataSource);

        long startLocationOfId3TagInFile = dataSource.position();
        AiffChunkType chunkType = AiffChunkType.get(chunkHeader.getID());
        if (chunkType!=null && chunkType== AiffChunkType.TAG)
        {
            ByteBuffer chunkData = readChunkDataIntoBuffer(dataSource, chunkHeader);
            aiffTag.addChunkSummary(new ChunkSummary(chunkHeader.getID(), chunkHeader.getStartLocationInFile(), chunkHeader.getSize()));

            //If we havent already for an ID3 Tag
            if(aiffTag.getID3Tag()==null)
            {
                Chunk chunk = new ID3Chunk(chunkHeader,chunkData, aiffTag);
                chunk.readChunk();
                aiffTag.setExistingId3Tag(true);
                aiffTag.getID3Tag().setStartLocationInFile(startLocationOfId3TagInFile);
                aiffTag.getID3Tag().setEndLocationInFile(dataSource.position());
            }
            //else otherwise we discard because the first one found is the one that will be used by other apps
            {
                logger.warning("Ignoring ID3Tag because already have one:" + chunkHeader.getID() + ":" + (chunkHeader.getStartLocationInFile() - 1) + "(" + Hex.asHex(chunkHeader.getStartLocationInFile()) + ")" + ":sizeIncHeader:" + (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE) + ":dataSource:" + dataSource);
            }
        }
        //Special handling to recognise ID3Tags written on odd boundary because original preceding chunk odd length but
        //didn't write padding byte
        else if(chunkType!=null && chunkType== AiffChunkType.CORRUPT_TAG_LATE)
        {
            logger.warning("Found Corrupt ID3 Chunk, starting at Odd Location:" + chunkHeader.getID() + ":" + (chunkHeader.getStartLocationInFile() - 1) + "(" + Hex.asHex(chunkHeader.getStartLocationInFile()) + ")"
                    + ":sizeIncHeader:"+ (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE) + ":dataSource:" + dataSource);

            //We only want to know if first metadata tag is misaligned
            if(aiffTag.getID3Tag()==null)
            {
                aiffTag.setIncorrectlyAlignedTag(true);
            }
            dataSource.position(dataSource.position() - (ChunkHeader.CHUNK_HEADER_SIZE + 1));
            return true;
        }
        //Other Special handling for ID3Tags
        else if(chunkType!=null && chunkType== AiffChunkType.CORRUPT_TAG_EARLY)
        {
            logger.warning("Found Corrupt ID3 Chunk, starting at Odd Location:" + chunkHeader.getID() + ":" + (chunkHeader.getStartLocationInFile() + 1) + "(" + Hex.asHex(chunkHeader.getStartLocationInFile()) + ")"
                    + ":sizeIncHeader:"+ (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE) + ":dataSource:" + dataSource);

            //We only want to know if first metadata tag is misaligned
            if(aiffTag.getID3Tag()==null)
            {
                aiffTag.setIncorrectlyAlignedTag(true);
            }
            dataSource.position(dataSource.position() - (ChunkHeader.CHUNK_HEADER_SIZE - 1));
            return true;
        }
        else
        {
            logger.config("Skipping Chunk:" + chunkHeader.getID() + ":" + chunkHeader.getSize() + ":dataSource:" + dataSource);
            aiffTag.addChunkSummary(new ChunkSummary(chunkHeader.getID(), chunkHeader.getStartLocationInFile(), chunkHeader.getSize()));
            dataSource.boundarySafePosition(dataSource.position() + chunkHeader.getSize());
        }
        IffHeaderChunk.ensureOnEqualBoundary(dataSource, chunkHeader);
        return true;
    }
}
