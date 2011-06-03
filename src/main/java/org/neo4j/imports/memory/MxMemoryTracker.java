package org.neo4j.imports.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.LinkedList;
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

public class MxMemoryTracker implements MemoryTracker
{

    private final List<MemoryObserver> observers;
    private Thread running;

    public MxMemoryTracker()
    {
        observers = new LinkedList<MemoryObserver>();
        MemoryPoolMXBean heap = null;
        for ( MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans() )
        {
            if ( pool.getType() == MemoryType.HEAP
                    && pool.isUsageThresholdSupported() )
            {
                heap = pool;
                break;
            }
        }
        if ( heap == null )
        {
            throw new RuntimeException(
            "Could not locate a suitable memory pool" );
        }
        heap.setUsageThreshold( (long) ( heap.getUsage().getMax() * 0.80 ) );
    }

    @Override
    public void launch()
    {
        running = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
                NotificationEmitter emitter = (NotificationEmitter) mbean;
                emitter.addNotificationListener( new NotificationListener()
                {
                    @Override
                    public void handleNotification( Notification notification,
                            Object handback )
                    {
                        if ( notification.getType().equals(
                                MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED ) )
                        {
                            for ( MemoryObserver observer : observers )
                            {
                                observer.memoryLow();
                            }
                        }
                    }
                }, null, null );
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

    }

    @Override
    public void registerMemoryObserver( MemoryObserver observer )
    {
        observers.add( observer );
    }
}
