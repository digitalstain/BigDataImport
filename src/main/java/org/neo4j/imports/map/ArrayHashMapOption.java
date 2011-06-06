package org.neo4j.imports.map;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A wrapper around a ArrayHashMap that
 * 
 */
public class ArrayHashMapOption
{
    private ArrayHashMap wrapped;
    private File store;

    public ArrayHashMapOption( ArrayHashMap toWrap, File store )
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

    public long persist() throws IOException
    {
        if(wrapped == null)
        {
            return -1;
        }
        long bytesWritten = 0;
        RandomAccessFile writer = new RandomAccessFile( store, "rw" );
        writer.setLength( 0 );
        writer.writeInt( wrapped.getKeys().length );
        bytesWritten += 4;
        writer.writeInt( wrapped.size() );
        bytesWritten += 4;
        String currentKey;
        for ( int i = 0; i < wrapped.getKeys().length; i++ )
        {
            currentKey = wrapped.getKeys()[i];
            if ( currentKey == null
                    || ArrayHashMap.Tombstone.equals( currentKey ) ) continue;
            writer.writeInt( currentKey.length() );
            bytesWritten += 4;
            writer.writeChars( currentKey );
            bytesWritten += currentKey.length() * 2;
            writer.writeLong( wrapped.getValues()[i] );
            bytesWritten += 8;
        }
        wrapped = null;
        writer.close();
        return bytesWritten;
    }

    public long restore() throws IOException
    {
        if ( wrapped != null )
        {
            return -1;
        }
        long bytesRead = 0;
        RandomAccessFile reader = new RandomAccessFile( store, "rw" );
        wrapped = new ArrayHashMap( reader.readInt() );
        bytesRead += 4;
        int counter = reader.readInt();
        bytesRead += 4;
        while ( counter-- > 0 )
        {
            int currentStringLength = reader.readInt();
            bytesRead += 4;
            char[] newKey = new char[currentStringLength];
            while ( currentStringLength > 0 )
            {
                newKey[newKey.length - currentStringLength] = reader.readChar();
                currentStringLength--;
                bytesRead += 2;
            }
            long value = reader.readLong();
            bytesRead += 8;
            wrapped.put( new String( newKey ), value );
        }
        reader.close();
        return bytesRead;
    }

    public void dispose()
    {
        store = null;
        wrapped = null;
    }

    public String getName()
    {
        return store == null ? "" : store.getName();
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }
        if ( obj == this )
        {
            return true;
        }
        if ( !( obj instanceof ArrayHashMapOption ) )
        {
            return false;
        }
        ArrayHashMapOption other = (ArrayHashMapOption) obj;
        return other.store.equals( this.store );
    }
}
