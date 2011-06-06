package org.neo4j.imports.hash;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import org.junit.Ignore;
import org.junit.Test;

public class TestStringPeristence
{
    @Test
    @Ignore
    public void testStringPersistence() throws Exception
    {
        RandomAccessFile fos = new RandomAccessFile(
                "target/stringPersistenceTest", "rw" );
        ByteBuffer buf = fos.getChannel().map( MapMode.READ_WRITE, 0, 18 );
        String toStore = "toStore";
        buf.putInt( toStore.length() * 2 );
        for ( char c : toStore.toCharArray() )
        {
            buf.putChar( c );
        }
        fos.close();
    }
}
