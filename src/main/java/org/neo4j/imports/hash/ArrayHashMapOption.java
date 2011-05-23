package org.neo4j.imports.hash;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * The store format is
 * <list> <li>4 bytes, an integer that is the length of the map's arrays</li>
 * <li>array of [integer][string][long] where the first integer is the length of
 * the following string and the long after that is the value corresponding to
 * that key</li></list>
 * 
 */
public class ArrayHashMapOption
{
    private ArrayHashMap wrapped;
    private RandomAccessFile store;

    public ArrayHashMapOption( ArrayHashMap toWrap, RandomAccessFile store )
    {
        this.wrapped = toWrap;
        this.store = store;
    }

    public boolean inMemory()
    {
        return wrapped != null;
    }

    public ArrayHashMap getValue()
    {
        return wrapped;
    }

    public void persist() throws IOException
    {
        store.seek( 0 );
        store.writeInt( wrapped.getKeys().length );
        store.writeInt( wrapped.size() );
        for ( String key : wrapped.getKeys() )
        {
            if ( key == null || "".equals( key ) ) continue;
            store.writeInt( key.length() );
            store.writeChars( key );
            store.writeLong( wrapped.get( key ) );
        }
        wrapped = null;
    }

    public void restore() throws IOException
    {
        store.seek( 0 );
        wrapped = new ArrayHashMap( store.readInt() );
        int counter = store.readInt();
        while ( counter-- > 0 )
        {
            int currentStringLength = store.readInt();
            char[] newKey = new char[currentStringLength];
            while ( currentStringLength > 0 )
            {
                newKey[newKey.length - currentStringLength] = store.readChar();
                currentStringLength--;
            }
            long value = store.readLong();
            wrapped.put( new String( newKey ), value );
        }
    }

    public void dispose()
    {
        try
        {
            store.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        store = null;
        wrapped = null;
    }

    private ByteBuffer writeMapEntry( ByteBuffer buffer, int arrayOffset )
    {

        String key = wrapped.getKeys()[arrayOffset];
        buffer.putInt( key.length() * 2 );
        for ( char c : key.toCharArray() )
        {
            buffer.putChar( c );
        }
        buffer.putLong( wrapped.getValues()[arrayOffset] );
        return buffer;
    }
}
