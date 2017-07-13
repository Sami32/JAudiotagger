/**
 * 
 */
package org.jaudiotagger.audio.asf.io;

import org.jaudiotagger.audio.asf.data.ContentBranding;
import org.jaudiotagger.audio.asf.data.GUID;
import org.jaudiotagger.audio.asf.data.MetadataContainerUtils;
import org.jaudiotagger.audio.asf.util.Utils;
import org.jaudiotagger.audio.generic.DataSource;
import org.jaudiotagger.audio.generic.MemoryDataSource;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
/**
 * @author Christian Laireiter
 * 
 */
public class ContentBrandingData {

    @Test
    public void testContentBrandingWriteRead() throws IOException {
        ContentBranding cb = new ContentBranding();
        cb.setCopyRightURL("CP URL");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        cb.writeInto(bos);
        assertEquals(cb.getCurrentAsfChunkSize(), bos.toByteArray().length);
        DataSource dataSource = new MemoryDataSource(bos.toByteArray());
        assertEquals(GUID.GUID_CONTENT_BRANDING, Utils.readGUID(dataSource));
        ContentBranding read = (ContentBranding) new ContentBrandingReader()
                .read(GUID.GUID_CONTENT_BRANDING, dataSource, 0);
        MetadataContainerUtils.equals(cb, read);
    }

}
