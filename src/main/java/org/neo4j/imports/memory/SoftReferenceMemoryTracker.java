package org.neo4j.imports.memory;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.List;

public class SoftReferenceMemoryTracker implements Runnable, MemoryTracker
{
    private Thread running;

    private final List<MemoryObserver> observers;

    public SoftReferenceMemoryTracker()
    {
        observers = new LinkedList<MemoryObserver>();
    }

    public void launch()
    {
        if (running == null)
        {
            running = new Thread(this);
            running.setDaemon(true);
            running.start();
        }
    }

    @Override
    public void run() {
        ReferenceQueue<byte[]> q = new ReferenceQueue<byte[]>();
        SoftReference<byte[]> r = new SoftReference<byte[]>(
                new byte[1024 * 1024 * 2], q );
        try
        {
            System.out.println(" +++ Trying to remove");
            q.remove();
            System.out.println(" +++ removed");
        }
        catch (InterruptedException e)
        {
            // Interrupted while waiting, we are done
            System.out.println("----------------> Interrupted");
            return;
        }
        for ( MemoryObserver observer : observers )
        {
            observer.memoryLow();
        }
        System.out.println("----------------> free Memory is at "+Runtime.getRuntime().freeMemory()/1024 +" Kb");
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
