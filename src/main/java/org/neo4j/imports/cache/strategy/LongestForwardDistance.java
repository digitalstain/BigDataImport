package org.neo4j.imports.cache.strategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LongestForwardDistance<K>
{
    private final Map<K, List<Long>> distances;
    private final Set<K> evicted;

    public LongestForwardDistance( Iterable<K> data )
    {
        this.distances = new HashMap<K, List<Long>>();
        evicted = new HashSet<K>();
        train( data );
    }

    private void train( Iterable<K> trainData )
    {
        System.out.println("Training...");
        long start = System.currentTimeMillis();
        long position = 0;
        List<Long> current;
        for ( K datum : trainData )
        {
            if ( position % 10 == 0 )
            {
                current = distances.get( datum );
                if ( current == null )
                {
                    current = new LinkedList<Long>();
                    distances.put( datum, current );
                }
                current.add( position );
            }
            position++;
        }
        System.out.println( "Done, took "
                            + ( System.currentTimeMillis() - start ) / 1000
                            + " ms" );
    }

    public void evict( K toEvict )
    {
        evicted.add( toEvict );
    }

    public void put( K toPut )
    {
        evicted.remove( toPut );
    }

    public K advise( Set<K> currentSet, long position )
    {
        K advice = null; // current candidate to return
        long max = Long.MIN_VALUE; // advice's position - max encountered
        for ( K inMem : currentSet )
        {
            if ( evicted.contains( inMem ) )
            {
                continue;
            }
            List<Long> current = distances.get( inMem );
            if ( current == null )
            {
                // not met again, safe to remove
                advice = inMem;
                System.err.println( "Not met" );
                break;
            }
            while ( current.size() > 0 && current.get( 0 ) < position )
            {
                current.remove( 0 );
            }
            if ( current.size() == 0 )
            {
                // Already skipped, weird but nevertheless
                System.out.println( inMem + " was already skipped at position "
                                    + position );
                distances.remove( inMem );
                advice = inMem;
                break;
            }
            long closestForCurrent = current.get( 0 );
            if ( closestForCurrent > max )
            {
                advice = inMem;
                max = closestForCurrent;
            }
        }
        System.out.println( "Suggested for eviction " + advice
                            + " that is at "
                            + max + " with current position being " + position );
        return advice;
    }
}
