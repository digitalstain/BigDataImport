package org.neo4j.imports.hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.neo4j.imports.hash.persistence.ArrayHashMapOptionFactory;

public class TestArrayHashMapPersistence
{
    @Test
    public void testSanity() throws Exception
    {
        File storeDir = new File( "target/foo" );
        storeDir.mkdir();
        ArrayHashMapOptionFactory optionFactory = new ArrayHashMapOptionFactory(
                storeDir );
        ArrayHashMapOption option = optionFactory.wrap( new ArrayHashMap( 3 ), "000" );
        assertTrue( option.inMemory() );

        String reallyLongString = "@@@@@@@@@@@@@@@@@FFFFFFFFFFFFfγγγγγγγγγγγγγγγγγγγγγγγγγγγγγγγγγγΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΦΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑΑ";
        Long fooValue = new Long( 1333243243243243211L );

        option.getValue().put( reallyLongString, fooValue );
        assertTrue( option.inMemory() );
        assertEquals( fooValue, option.getValue().get( reallyLongString ) );
        assertTrue( option.inMemory() );
        option.persist();
        assertFalse( option.inMemory() );
        assertNull( option.getValue() );
        option.restore();
        assertTrue( option.inMemory() );
        assertNotNull( option.getValue() );
        assertEquals( fooValue, option.getValue().get( reallyLongString ) );
    }

    @Test
    public void testInvariantsWithPersistence() throws Exception
    {
        File storeDir = new File( "bar" );
        storeDir.mkdir();
        ArrayHashMapOptionFactory optionFactory = new ArrayHashMapOptionFactory(
                storeDir );

        ArrayHashMapOption option = optionFactory.wrap( new ArrayHashMap( 2 ),
        "001" );

        option.getValue().put( "foo", 1L );
        option.getValue().put( "bar", 2L );
        assertEquals( 2, option.getValue().size() );
        assertEquals( new Long( 1L ), option.getValue().get( "foo" ) );
        assertEquals( new Long( 2L ), option.getValue().get( "bar" ) );

        option.persist();
        option.restore();

        assertEquals( 2, option.getValue().size() );
        assertEquals( new Long( 1L ), option.getValue().get( "foo" ) );
        assertEquals( new Long( 2L ), option.getValue().get( "bar" ) );

        assertTrue( option.getValue().putIfAbsent( "foobar", 3L ) );
        assertEquals( 3, option.getValue().size() );

        assertTrue( option.getValue().put( "foo", 4L ) );
        assertEquals( 3, option.getValue().size() );

        assertEquals( new Long( 2L ), option.getValue().remove( "bar" ) );
        assertEquals( 2, option.getValue().size() );

        assertEquals( new Long( 4L ), option.getValue().get( "foo" ) );
        assertNull( option.getValue().get( "bar" ) );

        assertEquals( new Long( 4L ), option.getValue().get( "foo" ) );
        option.persist();
        option.restore();
        assertEquals( new Long( 4L ), option.getValue().get( "foo" ) );
        assertEquals( new Long( 3L ), option.getValue().get( "foobar" ) );
    }
}
