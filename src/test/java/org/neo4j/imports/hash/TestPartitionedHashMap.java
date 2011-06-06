package org.neo4j.imports.hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.neo4j.imports.cache.strategy.TopK;
import org.neo4j.imports.hash.persistence.ArrayHashMapOptionFactory;
import org.neo4j.imports.map.ArrayHashMapOption;
import org.neo4j.imports.map.PartitionedHashMap;
import org.neo4j.imports.map.SimpleMap;

public class TestPartitionedHashMap extends AbstractTestArrayHashMap {

    @Override
    protected SimpleMap<String, Long> getMapInstance(int size) {
        File store = new File("target/partitioned");
        store.mkdirs();
        return new PartitionedHashMap( new ArrayHashMapOptionFactory( store ),
                new TopK<WrappedString>() );
    }

    @Test
    public void persistenceTest() throws Exception
    {
        PartitionedHashMap map = (PartitionedHashMap) getMapInstance( 16 );
        long totalSize = 0;
        int amount = 0;
        for ( long i = -456; i < 1029; i++ )
        {
            amount++;
            totalSize += Long.toString( i ).length();
            assertTrue( map.put( Long.toString( i ), i ) );
        }
        for ( long i = -456; i < 1029; i++ )
        {
            assertEquals( new Long( i ), map.get( Long.toString( i ) ) );
        }
        map.persistSome( totalSize );
        Field mapStore = PartitionedHashMap.class.getDeclaredField( "store" );
        mapStore.setAccessible( true );
        HashMap<WrappedString, ArrayHashMapOption> actualStore = (HashMap<WrappedString, ArrayHashMapOption>) mapStore.get( map );
        for ( Map.Entry<WrappedString, ArrayHashMapOption> entry : actualStore.entrySet() )
        {
            assertNull( entry.getValue().getValue() );
            assertFalse( entry.getValue().inMemory() );
        }
        map.get( "0" );
        boolean foundOneInMemory = false;
        for ( Map.Entry<WrappedString, ArrayHashMapOption> entry : actualStore.entrySet() )
        {
            if ( entry.getValue().inMemory() )
            {
                if ( foundOneInMemory )
                {
                    fail( "There can be only one (in memory)" );
                }
                else
                {
                    foundOneInMemory = true;
                }
            }
        }
        assertTrue( foundOneInMemory );

        for ( long i = -456; i < 1029; i++ )
        {
            assertEquals( new Long( i ), map.get( Long.toString( i ) ) );
        }
        for ( Map.Entry<WrappedString, ArrayHashMapOption> entry : actualStore.entrySet() )
        {
            assertNotNull( entry.getValue().getValue() );
            assertTrue( entry.getValue().inMemory() );
        }
    }
}
