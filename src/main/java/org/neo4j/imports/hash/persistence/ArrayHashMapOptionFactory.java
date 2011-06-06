package org.neo4j.imports.hash.persistence;

import java.io.File;
import java.io.IOException;

import org.neo4j.imports.map.ArrayHashMap;
import org.neo4j.imports.map.ArrayHashMapOption;

/**
 * Given a directory, it manufactures persistable options for ArrayHashMaps.
 */
public class ArrayHashMapOptionFactory
{
    private final File storeDir;

    public ArrayHashMapOptionFactory( File storeDir )
    {
        if ( !storeDir.isDirectory() )
        {
            throw new IllegalArgumentException( storeDir.getAbsolutePath()
                    + " is not a directory" );
        }
        this.storeDir = storeDir;
    }

    public ArrayHashMapOption wrap( ArrayHashMap toWrap, String withHash )
    throws IOException
    {
        File store = new File( storeDir, withHash );
        ArrayHashMapOption toReturn = new ArrayHashMapOption( toWrap, store );
        return toReturn;
    }
}
