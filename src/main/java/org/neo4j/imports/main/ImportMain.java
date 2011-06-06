package org.neo4j.imports.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.neo4j.imports.cache.strategy.LeastRecentlyUsed;
import org.neo4j.imports.hash.Hasher;
import org.neo4j.imports.hash.TrainableTwoStreamHasher;
import org.neo4j.imports.hash.WrappedString;
import org.neo4j.imports.hash.persistence.ArrayHashMapOptionFactory;
import org.neo4j.imports.map.PartitionedHashMap;
import org.neo4j.imports.memory.MemoryObserver;
import org.neo4j.imports.memory.MemoryTracker;
import org.neo4j.imports.memory.MxMemoryTracker;
import org.neo4j.imports.parse.CSVInputIterable;
import org.neo4j.imports.parse.InputIterable;
import org.neo4j.imports.parse.ParseResult;


public class ImportMain implements MemoryObserver
{

    private final MemoryTracker tracker;
    private PartitionedHashMap store;

    public static void main(String[] args) throws Exception
    {
        new ImportMain().start();
    }

    public ImportMain()
    {
        tracker = new MxMemoryTracker();
        tracker.registerMemoryObserver( this );
    }

    @Override
    public void memoryLow()
    {
        synchronized ( store )
        {
            System.out.println( "=========== Notified of low mem ============" );
            System.out.println( "Asking to store "
                                + Runtime.getRuntime().maxMemory() / 5
                                + " bytes" );
            store.persistSome( Runtime.getRuntime().maxMemory() / 4 );
            tracker.reset();
        }
    }

    private synchronized void start() throws Exception
    {
        BufferedReader readMe = new BufferedReader( new FileReader( "rels2" ) );
        InputIterable<String> iterable = new CSVInputIterable(readMe);
        Hasher hasher = new TrainableTwoStreamHasher( iterable );
        WrappedString.setHasher( hasher );

        readMe = new BufferedReader( new FileReader( "rels2" ) );
        iterable = new CSVInputIterable( readMe );
        ArrayHashMapOptionFactory fac = new ArrayHashMapOptionFactory(
                new File( "target/testRun" ) );
        LeastRecentlyUsed<WrappedString> strategy = new LeastRecentlyUsed<WrappedString>();
        store = new PartitionedHashMap( fac, strategy );
        tracker.launch();
        long line = 0;
        for ( ParseResult<String> result : iterable )
        {
            if ( line % 25000 == 0 )
            {
                System.out.println( "line is " + line );
            }
            synchronized ( store )
            {
                if ( store.get( result.getFirstNode() ) == null )
                {
                    store.put( result.getFirstNode(), line );
                }
                if ( store.get( result.getSecondNode() ) == null )
                {
                    store.put( result.getSecondNode(), line );
                }
            }
            line++;
            if ( line > 2500000 ) break;
        }
    }
}
