package org.neo4j.imports.cache.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TestLeastRecentlyUsed
{
    @Test
    public void testSanity()
    {
        ReplacementStrategy<String> strategy = new LeastRecentlyUsed<String>();
        assertNull( strategy.suggest() );
        strategy.hit( "foo" );
        assertEquals( "foo", strategy.suggest() );
        strategy.hit( "bar" );
        assertEquals( "foo", strategy.suggest() );
        assertEquals( "foo", strategy.suggest() );
        strategy.evict( "foo" );
        assertEquals( "bar", strategy.suggest() );
        assertEquals( "bar", strategy.suggest() );
        strategy.evict( "bar" );
        assertNull( strategy.suggest() );
    }

    @Test
    public void testRemove()
    {
        ReplacementStrategy<String> strategy = new LeastRecentlyUsed<String>();
        // Removing a non existent entry should not be a problem
        strategy.remove( "foo" );
        strategy.hit( "foo" );
        assertEquals( "foo", strategy.suggest() );
        strategy.remove( "foo" );
        // Evicting a removed entry should not be a problem
        strategy.evict( "foo" );
        assertNull( strategy.suggest() );
        strategy.hit( "bar" );
        strategy.hit( "bar2" );
        // Removing a removed entry should not be a problem
        strategy.remove( "foo" );
        assertEquals( "bar", strategy.suggest() );
        strategy.evict( "bar" );
        // Removing an evicted entry should not be a problem
        strategy.remove( "bar" );
        assertEquals( "bar2", strategy.suggest() );
    }
}
