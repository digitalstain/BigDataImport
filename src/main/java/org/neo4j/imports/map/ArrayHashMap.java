package org.neo4j.imports.map;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Straightforward hash map implementation of a map from Object to long that
 * stores stuff in two arrays, one for keys and one for values. It works with
 * linear reprobing.
 */
public class ArrayHashMap implements SimpleMap<String, Long>
{
    private String[] keys;
    private long[] values;
    private int size;
    private boolean inResize;

    /*
     * The object positioned in every deleted location until the
     * next resize, so that we know the chain doesn't end here, which
     * would be the case if we just overwrote with null.
     */
    public static final String Tombstone = "";


    public ArrayHashMap( int initialSize )
    {
        /*
         * Bring up to closest power of 2, for
         * offsets from hash to work.
         */
        int log2 = 0;
        while ( ( initialSize >>= 1 ) > 0 )
        {
            log2++;
        }
        log2++;
        initialSize = 1 << log2;

        keys = new String[initialSize];
        values = new long[initialSize];
        size = 0;
        inResize = false;
    }

    @Override
    public boolean put( String key, Long value )
    {
        if ( key == null )
        {
            throw new IllegalArgumentException( "key was null" );
        }
        if ( value == null )
        {
            throw new IllegalArgumentException( "value was null" );
        }

        int iteration = 1;
        int hash = hash(key);
        int offset = nextHop( hash, iteration++ );
        int reprobes = 0;
        while ( keys[offset] != null && keys[offset] != Tombstone )
        {
            if ( keys[offset].equals( key ) )
            {
                if ( values[offset] == value.longValue() )
                {
                    return false;
                }
                else
                {
                    values[offset] = value.longValue();
                    return true;
                }
            }
            offset = nextHop( hash, iteration++);
            if ( offset == nextHop(hash, 1) )
            {
                return false;
            }
            reprobes++;
        }
        increaseSize();
        keys[offset] = key;
        values[offset] = value.longValue();
        checkResize( reprobes );
        return true;
    }

    @Override
    public boolean putIfAbsent( String key, Long value )
    {
        if ( key == null )
        {
            throw new IllegalArgumentException( "key awas null" );
        }
        if ( value == null )
        {
            throw new IllegalArgumentException( "value was null" );
        }
        int iteration = 1;
        int hash = hash(key);

        int offset = nextHop( hash, iteration++ );
        int reprobes = 0;
        while ( keys[offset] != null && keys[offset] != Tombstone )
        {
            if ( keys[offset].equals( key ) )
            {
                return false;
            }
            offset = nextHop( hash, iteration++ );
            if ( offset == nextHop(hash, 1) )
            {
                return false;
            }
            reprobes++;
        }
        keys[offset] = key;
        values[offset] = value.longValue();
        increaseSize();
        checkResize( reprobes );
        return true;
    }

    @Override
    public Long get( String key )
    {
        int hash = hash(key);
        int iteration = 1;
        int offset = nextHop(hash, iteration++ );
        while ( keys[offset] != null )
        {
            if ( keys[offset].equals( key ) )
            {
                return values[offset];
            }

            offset = nextHop(hash, iteration++ );
            if ( offset == nextHop( hash, 1 ) )
                // We wrapped around the array
            {
                return null;
            }
        }
        return null;
    }

    @Override
    public Long remove( String key )
    {
        int iteration = 1;
        int hash = hash(key);
        int offset = nextHop( hash, iteration++ );
        while ( keys[offset] != null )
        {
            if ( keys[offset].equals( key ) )
            {
                Long toReturn = values[offset];
                keys[offset] = Tombstone;
                size--;
                return toReturn;
            }
            offset = nextHop( hash, iteration++ );
            if ( offset == nextHop( hash, 1 ) )
            {
                return null;
            }
        }
        return null;
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
                    private int location = 0;
                    private int hits = 0;

                    @Override
                    public boolean hasNext()
                    {
                        return hits < size;
                    }

                    @Override
                    public String next()
                    {
                        String toReturn = keys[location];
                        while( toReturn == null || toReturn.equals(Tombstone))
                        {
                            location++;
                            toReturn = keys[location];
                        }
                        hits++;
                        location++;
                        return toReturn;
                    }

                    @Override
                    public void remove()
                    {
                        throw new UnsupportedOperationException("Read only data set");
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
    public int size()
    {
        return size;
    }

    private void checkResize( int reprobes )
    {
        if ( !inResize && ( reprobes > size / 4 || size > keys.length / 4 ) )
        {
            resize();
        }
    }

    protected void resize()
    {
        inResize = true;
        String[] oldKeys = keys;
        long[] oldValues = values;

        keys = new String[oldKeys.length * 2];
        values = new long[oldValues.length * 2];

        for ( int i = 0; i < oldKeys.length; i++ )
        {
            String key = oldKeys[i];
            if ( key == null || key == Tombstone )
            {
                continue;
            }
            put( key, oldValues[i] );
        }
        inResize = false;
    }

    protected int nextHop( int hash, int iteration )
    {
        return ((int)( hash + 0.5*iteration + 0.5*iteration*iteration  )) & ( keys.length - 1 );
    }

    private void increaseSize()
    {
        if ( !inResize )
        {
            size++;
        }
    }

    String[] getKeys()
    {
        return keys;
    }

    long[] getValues()
    {
        return values;
    }

    protected int hash(Object key)
    {
        return key.hashCode();
    }
}
