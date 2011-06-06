package org.neo4j.imports.cache.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.neo4j.imports.hash.WrappedString;

public class TestTopK {

    @Test
    public void sanityCheck()
    {
        ReplacementStrategy<String> topK = new TopK<String>();
        topK.hit("1");
        topK.hit("2");
        topK.hit("3");
        topK.hit("1");
        topK.hit("2");
        assertEquals("3", topK.suggest());

        topK = new TopK<String>();
        topK.hit("1");
        topK.hit("1");
        topK.hit("2");
        topK.hit("2");
        topK.hit("3");
        assertEquals("3", topK.suggest());

        topK = new TopK<String>();
        topK.hit("3");
        topK.hit("1");
        topK.hit("2");
        topK.hit("2");
        topK.hit("1");
        assertEquals("3", topK.suggest());
    }

    @Test
    public void testEvict()
    {
        ReplacementStrategy<String> topK = new TopK<String>();
        topK.hit( "1" );
        assertEquals("1", topK.suggest());
        topK.evict( "1" );
        assertNull( topK.suggest() );
        topK.hit( "2" );
        assertEquals( "2", topK.suggest() );
        topK.evict( "2" );
        assertNull( topK.suggest() );
        topK.evict( "2" );
        assertNull( topK.suggest() );
        topK.hit( "1" );
        assertEquals( "1", topK.suggest() );
        topK.hit( "1" );
        assertEquals( "1", topK.suggest() );
        topK.hit( "2" );
        assertEquals( "2", topK.suggest() );

    }

    @Test
    public void testTransitions()
    {
        ReplacementStrategy<String> topK = new TopK<String>();
        topK.hit("1");
        assertEquals("1", topK.suggest());
        topK.hit("2");
        assertEquals("1", topK.suggest());
        topK.hit("3");
        assertEquals("1", topK.suggest());
        topK.hit("1");
        assertEquals("2", topK.suggest());
        topK.hit("2");
        assertEquals("3", topK.suggest());
    }

    @Test
    public void testWithWrappedString()
    {
        ReplacementStrategy<WrappedString> topK = new TopK<WrappedString>();

        topK.hit( new WrappedString( "1" ) );
        assertEquals( new WrappedString( "1" ), topK.suggest() );
        topK.hit( new WrappedString( "2" ) );
        assertEquals( new WrappedString( "1" ), topK.suggest() );
        topK.hit( new WrappedString( "3" ) );
        assertEquals( new WrappedString( "1" ), topK.suggest() );
        topK.hit( new WrappedString( "1" ) );
        assertEquals( new WrappedString( "2" ), topK.suggest() );
        topK.hit( new WrappedString( "2" ) );
        assertEquals( new WrappedString( "3" ), topK.suggest() );

        topK.hit( new WrappedString( "1" ) );
        assertEquals( new WrappedString( "3" ), topK.suggest() );
        topK.evict( new WrappedString( "1" ) );
        assertEquals( new WrappedString( "3" ), topK.suggest() );
        topK.hit( new WrappedString( "2" ) );
        assertEquals( new WrappedString( "3" ), topK.suggest() );
        topK.evict( new WrappedString( "2" ) );
        assertEquals( new WrappedString( "3" ), topK.suggest() );
        topK.evict( new WrappedString( "2" ) );
        assertEquals( new WrappedString( "3" ), topK.suggest() );
        topK.hit( new WrappedString( "1" ) );
        assertEquals( new WrappedString( "3" ), topK.suggest() );
        topK.hit( new WrappedString( "1" ) );
        assertEquals( new WrappedString( "3" ), topK.suggest() );
        topK.hit( new WrappedString( "2" ) );
        assertEquals( new WrappedString( "3" ), topK.suggest() );
        topK.hit( new WrappedString( "3" ) );
        topK.hit( new WrappedString( "3" ) );
        topK.hit( new WrappedString( "3" ) );
        assertEquals( new WrappedString( "3" ), topK.suggest() );
        topK.hit( new WrappedString( "3" ) );
        assertEquals( new WrappedString( "2" ), topK.suggest() );

    }
}
