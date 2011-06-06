package org.neo4j.imports.hash;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.neo4j.imports.map.SimpleMap;

public abstract class AbstractTestArrayHashMap
{
    protected SimpleMap<?,?> map;

    protected abstract SimpleMap<String, Long> getMapInstance(int size);

    @Test
    public void testSanity()
    {
        SimpleMap<String, Long> map = getMapInstance( 16 );
        assertNull( map.get( "1" ) );
        Long first = new Long( 10 );
        assertTrue( map.put( "1", first ) );
        assertEquals( first, map.get( "1" ) );
        assertEquals( first, map.remove( "1" ) );
        assertNull( map.get( "1" ) );
    }

    @Test
    public void testOverwrite()
    {
        SimpleMap<String, Long> map = getMapInstance( 4 );
        assertNull( map.get( "1" ) );
        Long first = new Long( 10 );
        Long second = new Long( 11 );

        assertTrue( map.put( "1", first ) );
        assertEquals(1, map.size());
        assertEquals( first, map.get( "1" ) );
        assertFalse( map.put( "1", first ) );
        assertEquals(1, map.size());
        assertEquals( first, map.get( "1" ) );

        assertFalse( map.putIfAbsent( "1", second ) );
        assertEquals(1, map.size());
        assertEquals( first, map.get( "1" ) );
        assertTrue( map.put( "1", second ) );
        assertEquals(1, map.size());
        assertEquals( second, map.get( "1" ) );
        assertFalse( map.put( "1", second ) );
        assertEquals(1, map.size());
        assertEquals( second, map.get( "1" ) );

        assertTrue( map.putIfAbsent( "2", first ) );
        assertEquals(2, map.size());
        assertEquals( first, map.get( "2" ) );
    }

    @Test
    public void testDelete()
    {
        SimpleMap<String, Long> map = getMapInstance( 16 );
        assertNull( map.get( "1" ) );
        Long first = new Long( 10 );
        Long second = new Long( 11 );
        assertTrue( map.put( "1", first ) );
        assertEquals( first, map.get( "1" ) );

        assertTrue( map.putIfAbsent( "2", second ) );
        assertEquals( second, map.get( "2" ) );

        assertEquals( first, map.remove( "1" ) );
        assertNull( map.remove( "1" ) );

        assertEquals( second, map.remove( "2" ) );
        assertNull( map.remove( "2" ) );

        map = getMapInstance( 3 );

        map.put( "foo", 1L );
        map.put( "bar", 2L );
        assertEquals( 2, map.size() );
        assertEquals( new Long( 1L ), map.get( "foo" ) );
        assertEquals( new Long( 2L ), map.get( "bar" ) );

        assertEquals( 2, map.size() );
        assertEquals( new Long( 1L ), map.get( "foo" ) );
        assertEquals( new Long( 2L ), map.get( "bar" ) );

        assertTrue( map.putIfAbsent( "foobar", 3L ) );
        assertTrue( map.put( "foo", 4L ) );
        assertEquals( new Long( 2L ), map.remove( "bar" ) );
    }

    @Test
    public void testIteration()
    {
        SimpleMap<String, Long> map = getMapInstance( 32 );

        int size = 32;
        Object[] toInsert = new Object[size];
        for ( int i = 0; i < size; i += 2 )
        {
            toInsert[i] = Integer.toBinaryString( i ); // the key
            toInsert[i + 1] = new Long( i + 1 ); // the value
            assertTrue( map.putIfAbsent( (String) toInsert[i],
                    (Long) toInsert[i + 1] ) );
            assertEquals( toInsert[i + 1], map.get( (String) toInsert[i] ) );
        }

        assertEquals( size / 2, map.size() );
        for ( int i = 0; i < size / 2; i++ )
        {
            String key = (String) toInsert[i * 2];
            assertEquals( toInsert[i * 2 + 1], map.get( key ) );
        }

        Set<String> keys = map.keySet();

        assertEquals( size / 2, keys.size() );
        for ( int i = 0; i < size / 2; i++ )
        {
            String key = (String) toInsert[i * 2];
            assertTrue( keys.contains( key ) );
            assertEquals( toInsert[i * 2 + 1], map.get( key ) );
        }
    }

    @Test
    public void testResize()
    {
        SimpleMap<String, Long> map = getMapInstance( 5 );

        int size = 198;
        Object[] toInsert = new Object[size];
        for ( int i = 0; i < size; i += 2 )
        {
            toInsert[i] = Integer.toString( i ); // the key
            toInsert[i + 1] = new Long( i + 1 ); // the value
            assertTrue( map.putIfAbsent( (String) toInsert[i],
                    (Long) toInsert[i + 1] ) );
            assertEquals( i / 2 + 1, map.size() );
        }

        Set<String> keys = map.keySet();

        assertEquals( size / 2, keys.size() );
        for ( int i = 0; i < size / 2; i++ )
        {
            String key = (String) toInsert[i * 2];
            assertTrue( keys.contains( key ) );
            assertEquals( toInsert[i * 2 + 1], map.get( key ) );
        }
    }

    @Test
    public void manyInsertsCompareWithJavaUtilHashMap()
    {
        SimpleMap<String, Long> map = getMapInstance( 5 );
        HashMap<String, Long> check = new HashMap<String, Long>();

        for ( long i = -1000; i < 300000; i++ )
        {
            assertTrue(map.put(Long.toHexString(i), i));
            check.put(Long.toHexString(i), i);
        }

        assertEquals(check.size(), map.size());
        int first = 0, second = 0;
        for (String key : map.keySet())
        {
            assertNotNull(key);
            assertEquals(check.get(key), map.get(key));
            first++;
        }

        Set<String> unique = new HashSet<String>();
        for ( String key : map.keySet() )
        {
            assertTrue( unique.add( key ) );
        }

        for (String key : check.keySet())
        {
            assertEquals(check.get(key), map.get(key));
            second++;
        }
        assertEquals( check.size(), first );
        assertEquals( map.size(), second );
        assertEquals( first, second );
    }
}
