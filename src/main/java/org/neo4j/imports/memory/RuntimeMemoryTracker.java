package org.neo4j.imports.memory;

import java.util.LinkedList;
import java.util.List;

public class RuntimeMemoryTracker implements MemoryTracker
{

    private Thread running;

    private final List<MemoryObserver> observers;

    public RuntimeMemoryTracker()
    {
        observers = new LinkedList<MemoryObserver>();
    }

    @Override
    public void launch()
    {
        running = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                Runtime runtime = Runtime.getRuntime();
                while ( runtime.freeMemory() > 1024 * 1024 * 42 )
                {
                    System.err.println( "Free memory is "
                            + runtime.freeMemory() / 1024 + " kb" );
                    try
                    {
                        Thread.sleep( 200 );
                    }
                    catch ( InterruptedException e )
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                for ( MemoryObserver observer : observers )
                {
                    observer.memoryLow();
                }
                System.out.println( "----------------> free Memory is at "
                        + Runtime.getRuntime().freeMemory() / 1024
                        + " Kb" );
            }
        } );
        running.setDaemon( true );
        running.start();

    }

    @Override
    public void reset()
    {
        stop();
        launch();
    }

    @Override
    public void stop()
    {
        running.interrupt();
        running = null;
    }

    @Override
    public void registerMemoryObserver( MemoryObserver observer )
    {
        observers.add( observer );
    }
}
