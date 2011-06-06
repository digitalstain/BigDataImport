package org.neo4j.imports.map;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.neo4j.imports.cache.strategy.ReplacementStrategy;
import org.neo4j.imports.hash.WrappedString;
import org.neo4j.imports.hash.persistence.ArrayHashMapOptionFactory;

public class PartitionedHashMap implements SimpleMap<String, Long>
{
    static int currentHash = 0;

    static int insertCount = 0;

    private final ArrayHashMapOptionFactory optionFactory;

    private final HashMap<WrappedString, ArrayHashMapOption> store;

    private final ReplacementStrategy<WrappedString> strategy;

    private int size;

    private int fileNumber;

    public PartitionedHashMap( ArrayHashMapOptionFactory optionFactory,
            ReplacementStrategy<WrappedString> strategy )
    {
        this.optionFactory = optionFactory;
        this.store = new HashMap<WrappedString, ArrayHashMapOption>();
        this.strategy = strategy;
        fileNumber = 0;
    }

    @Override
    public synchronized boolean put( String key, Long value )
    {
        WrappedString toInsert = new WrappedString( key );
        strategy.hit( toInsert );
        ArrayHashMapOption temp = store.get( toInsert );
        if (temp == null)
        {
            try
            {
                temp = optionFactory.wrap( new ArrayHashMap( 32 ),
                        Integer.toHexString( fileNumber++ ) );
                store.put( toInsert, temp );
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            try
            {
                long readInSize = temp.restore();
                if ( readInSize > -1 )
                    System.out.println( "Restoring partition for put() with id "
                                    + temp.getName() + " at size " + readInSize );
            }
            catch ( IOException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        size -= temp.getValue().size();
        boolean toReturn = temp.getValue().put( key, value );
        size += temp.getValue().size();
        return toReturn;
    }

    @Override
    public synchronized boolean putIfAbsent( String key, Long value )
    {
        WrappedString toInsert = new WrappedString( key );
        strategy.hit( toInsert ); // Regardless of whether it is
        ArrayHashMapOption temp = store.get( toInsert );
        if (temp == null)
        {
            try
            {
                temp = optionFactory.wrap( new ArrayHashMap( 32 ),
                        Integer.toHexString( fileNumber++ ) );
                store.put( toInsert, temp );
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            try
            {
                long readInSize = temp.restore();
                if ( readInSize > -1 )
                    System.out.println( "Restoring partition for putIfAbsent() with id "
                                        + temp.getName() + " at size "
                                        + readInSize );
            }
            catch ( IOException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        size -= temp.getValue().size();
        boolean toReturn = temp.getValue().putIfAbsent(key, value);
        // inserted or not
        size += temp.getValue().size();
        return toReturn;
    }

    @Override
    public synchronized Long get( String key )
    {
        WrappedString toGet = new WrappedString( key );
        strategy.hit( toGet ); // Regardless if it is in
        ArrayHashMapOption temp = store.get( toGet );
        if (temp == null)
        {
            return null;
        }
        try
        {
            long readInSize = temp.restore();
            if ( readInSize > -1 )
                System.out.println( "Restoring partition for get() with id "
                                    + temp.getName() + " at size " + readInSize );
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Long toReturn = temp.getValue().get( key );
        // there or not
        return toReturn;

    }

    @Override
    public synchronized Long remove( String key )
    {
        WrappedString toRemove = new WrappedString( key );
        strategy.remove( toRemove );
        ArrayHashMapOption temp = store.get( toRemove );
        if (temp == null)
        {
            return null;
        }
        try
        {
            long readInSize = temp.restore();
            if ( readInSize > -1 )
                System.out.println( "Restoring partition with id "
                                    + temp.getName() + " at size " + readInSize );
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        size -= temp.getValue().size();
        Long toReturn = temp.getValue().remove(key);
        size += temp.getValue().size();
        return toReturn;
    }

    @Override
    public Set<String> keySet()
    {
        return new Set<String>()
        {
            @Override
            public int size()
            {
                return size;
            }

            @Override
            public boolean isEmpty()
            {
                return size == 0;
            }

            @Override
            public boolean contains( Object o )
            {
                return get( (String) o ) != null;
            }

            @Override
            public Iterator<String> iterator()
            {
                return new Iterator<String>()
                {

                    private final Iterator<WrappedString> topLevelIterator = store.keySet().iterator();
                    private Iterator<String> currentIterator =
                        topLevelIterator.hasNext()? store.get( topLevelIterator.next()).getValue().keySet().iterator() : null;

                        @Override
                        public void remove()
                        {
                            throw new UnsupportedOperationException("Read only data set");
                        }

                        @Override
                        public boolean hasNext()
                        {
                            return currentIterator != null;
                        }

                        @Override
                        public String next()
                        {
                            String toReturn;
                            toReturn = currentIterator.next();
                            if ( currentIterator.hasNext() )
                            {
                                return toReturn;
                            }
                            while ( topLevelIterator.hasNext() && !(
                                    currentIterator = store.get(
                                            topLevelIterator.next() ).getValue().keySet().iterator() ).hasNext() );
                            if ( !currentIterator.hasNext() )
                            {
                                currentIterator = null;
                            }
                            return toReturn;
                        }
                };
            }

            @Override
            public Object[] toArray()
            {
                throw new UnsupportedOperationException( "Not yet" );
            }

            @Override
            public <T> T[] toArray( T[] a )
            {
                throw new UnsupportedOperationException( "Not yet" );
            }

            @Override
            public boolean add( String e )
            {
                throw new UnsupportedOperationException("Read only data set");
            }

            @Override
            public boolean remove( Object o )
            {
                throw new UnsupportedOperationException("Read only data set");
            }

            @Override
            public boolean containsAll( Collection<?> c )
            {
                throw new UnsupportedOperationException( "Not yet" );
            }

            @Override
            public boolean addAll( Collection<? extends String> c )
            {
                throw new UnsupportedOperationException("Read only data set");
            }

            @Override
            public boolean retainAll( Collection<?> c )
            {
                throw new UnsupportedOperationException("Read only data set");
            }

            @Override
            public boolean removeAll( Collection<?> c )
            {
                throw new UnsupportedOperationException("Read only data set");
            }

            @Override
            public void clear()
            {
                throw new UnsupportedOperationException("Read only data set");
            }

        };
    }

    @Override
    public synchronized int size()
    {
        return size;
    }

    public synchronized void persistSome( long targetSize )
    {
        boolean done = false;
        long bytesSaved = 0;
        int persisted = 0;
        while ( !done )
        {
            try
            {
                WrappedString suggested = strategy.suggest();
                if ( suggested == null )
                {
                    for ( Map.Entry<WrappedString, ArrayHashMapOption> entry : store.entrySet() )
                    {
                        if ( entry.getValue().inMemory() )
                        {
                            System.out.println(
                                    "Found in memory option while strategy was oblivious" );
                            entry.getValue().persist();
                        }
                    }
                }
                ArrayHashMapOption currentOption = store.get(suggested );
                if ( currentOption == null )
                {
                    throw new IllegalStateException( "could not find "
                                                     + suggested
                                                     + " in the store" );
                }
                /*
                System.out.println( "Suggested for eviction was "
                        + suggested.hashCode() + " with size "
                        + currentOption.getValue().size() );
                */
                if ( currentOption.inMemory() )
                {
                    bytesSaved += currentOption.getValue().getKeys().length * 8;
                    bytesSaved += currentOption.persist();
                    strategy.evict( suggested );
                    persisted++;
                }
                else
                {
                    throw new IllegalStateException(
                            suggested
                                    + " is already evicted but was proposed nonetheless." );
                }
                if ( bytesSaved > targetSize )
                {
                    System.out.println( "We are done by size" );
                    done = true;
                }
                if ( persisted > 130 )
                {
                    System.out.println( "We are done by count" );
                    // done = true;
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
        System.out.println( "Evicted " + bytesSaved + " bytes in total" );
    }
}
