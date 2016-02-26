/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Raphaël Slinckx <raphael@slinckx.net>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jaudiotagger.audio.dsf;

import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.generic.AudioFileWriter2;
import org.jaudiotagger.audio.generic.DataSource;
import org.jaudiotagger.audio.generic.FileDataSource;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Write/delete tag info for Dsf file
 */
public class DsfFileWriter extends AudioFileWriter2
{
    protected void writeTag(Tag tag, Path file) throws CannotWriteException
    {
        FileChannel fc;
        DataSource dataSource = null;
        try
        {
            fc = FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.READ);
            dataSource = new FileDataSource(fc);
            DsdChunk dsd = DsdChunk.readChunk(Utils.readFileDataIntoBufferLE(dataSource, DsdChunk.DSD_HEADER_LENGTH));
            if (dsd != null)
            {
                if (dsd.getMetadataOffset() > 0)
                {
                    fc.position(dsd.getMetadataOffset());
                    ID3Chunk id3Chunk = ID3Chunk.readChunk(Utils.readFileDataIntoBufferLE(dataSource, (int) (dataSource.size() - dataSource.position())));
                    if (id3Chunk != null)
                    {
                        //Remove Existing tag
                        fc.position(dsd.getMetadataOffset());
                        final ByteBuffer bb = convert((AbstractID3v2Tag) tag);
                        fc.write(bb);
                    }
                    else
                    {
                        throw new CannotWriteException(file + "Could not find existing ID3v2 Tag");
                    }
                }
                else
                {
                    //Write new tag and new offset and size
                    fc.position(fc.size());
                    dsd.setMetadataOffset(fc.size());
                    final ByteBuffer bb = convert((AbstractID3v2Tag) tag);
                    fc.write(bb);
                    dsd.setFileLength(fc.size());
                    fc.position(0);
                    fc.write(dsd.write());
                }
            }
        }
        catch(IOException ioe)
        {
            throw new CannotWriteException(ioe.getMessage());
        }finally {
            Utils.closeQuietly(dataSource);// This will close the FileChannel
        }
    }

    /**
     * Convert ID3 tag into a ByteBuffer, also ensures always even to avoid problems
     *
     * @param tag
     * @return
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer convert(final AbstractID3v2Tag tag) throws UnsupportedEncodingException
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            long existingTagSize = tag.getSize();

            //If existingTag is uneven size lets make it even
            if( existingTagSize > 0)
            {
                if(Utils.isOddLength(existingTagSize))
                {
                    existingTagSize++;
                }
            }

            //Write Tag to buffer
            tag.write(baos, (int) existingTagSize);

            //If the tag is now odd because we needed to increase size and the data made it odd sized
            //we redo adding a padding byte to make it even
            if((baos.toByteArray().length & 1)!=0)
            {
                int newSize = baos.toByteArray().length + 1;
                baos = new ByteArrayOutputStream();
                tag.write(baos, newSize);
            }
            final ByteBuffer buf = ByteBuffer.wrap(baos.toByteArray());
            buf.rewind();
            return buf;
        }
        catch (IOException ioe)
        {
            //Should never happen as not writing to file at this point
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Delete Metadata tag
     *
     * @param tag
     * @param file
     * @throws CannotWriteException
     * @throws IOException
     */
    @Override
    protected void deleteTag(Tag tag, Path file) throws CannotWriteException
    {

        FileChannel fc;
        DataSource dataSource = null;
        try
        {
            fc = FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.READ);
            dataSource = new FileDataSource(fc);
            DsdChunk dsd = DsdChunk.readChunk(Utils.readFileDataIntoBufferLE(dataSource, DsdChunk.DSD_HEADER_LENGTH));
            if (dsd != null)
            {
                if (dsd.getMetadataOffset() > 0)
                {
                    fc.position(dsd.getMetadataOffset());
                    ID3Chunk id3Chunk = ID3Chunk.readChunk(Utils.readFileDataIntoBufferLE(dataSource, (int) (fc.size() - fc.position())));
                    if (id3Chunk != null)
                    {
                        fc.truncate(dsd.getMetadataOffset());
                        //set correct value for fileLength and zero offset
                        dsd.setMetadataOffset(0);
                        dsd.setFileLength(fc.size());
                        fc.position(0);
                        fc.write(dsd.write());
                    }
                }
                else
                {
                    //Do Nothing;
                }
            }
        }
        catch(IOException ioe)
        {
            throw new CannotWriteException(file + ":"+ioe.getMessage());
        }finally {
            Utils.closeQuietly(dataSource);// This will close the file channel
        }
    }


}

