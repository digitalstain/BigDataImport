package org.neo4j.imports.map;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Straightforward hash map implementation of a map from Object to long that
 * stores stuff in two arrays, one for keys and one for values. It works with
 * linear reprobing.
 */
public class GenericArrayHashMap<K, V> implements SimpleMap<K, V>
{
    private Object[] keys;
    private Object[] values;
    private int size;
    private boolean inResize;

    /*
     * The object positioned in every deleted location until the
     * next resize, so that we know the chain doesn't end here, which
     * would be the case if we just overwrote with null.
     */
    private static final Object Tombstone = new Object()
    {
        @Override
        public boolean equals(Object obj)
        {
            return obj == this;
        };
    };

    public GenericArrayHashMap( int initialSize )
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

        keys = new Object[initialSize];
        values = new Object[initialSize];
        size = 0;
        inResize = false;
    }

    @Override
    public boolean put( K key, V value )
    {
        if ( key == null )
        {
            throw new IllegalArgumentException( "key was null" );
        }
        if ( value == null )
        {
            throw new IllegalArgumentException( "value was null" );
        }
        int offset = offset( key );
        int reprobes = 0;
        while ( keys[offset] != null && keys[offset] != Tombstone )
        {
            if ( keys[offset].equals( key ) )
            {
                if ( values[offset] == value )
                {
                    return false;
                }
                else
                {
                    values[offset] = value;
                    return true;
                }
            }
            offset = nextHop( offset );
            if ( offset == offset( key ) )
            {
                return false;
            }
            reprobes++;
        }
        increaseSize();
        keys[offset] = key;
        values[offset] = value;
        checkResize( reprobes );
        return true;
    }

    @Override
    public boolean putIfAbsent( K key, V value )
    {
        if ( key == null )
        {
            throw new IllegalArgumentException( "key awas null" );
        }
        if ( value == null )
        {
            throw new IllegalArgumentException( "value was null" );
        }
        int offset = offset( key );
        int reprobes = 0;
        while ( keys[offset] != null && keys[offset] != Tombstone )
        {
            if ( keys[offset].equals( key ) )
            {
                return false;
            }
            offset = nextHop( offset );
            if ( offset == offset( key ) )
            {
                return false;
            }
            reprobes++;
        }
        keys[offset] = key;
        values[offset] = value;
        increaseSize();
        checkResize( reprobes );
        return true;
    }

    @Override
    public V get( K key )
    {
        int offset = offset( key );
        while ( keys[offset] != null )
        {
            if ( keys[offset].equals( key ) )
            {
                return (V) values[offset];
            }

            offset = nextHop( offset );
            if ( offset == offset( key ) )
                // We wrapped around the array
            {
                return null;
            }
        }
        return null;
    }

    @Override
    public V remove( K key )
    {
        int offset = offset( key );
        while ( keys[offset] != null )
        {
            if ( keys[offset].equals( key ) )
            {
                V toReturn = (V) values[offset];
                keys[offset] = Tombstone;
                size--;
                return toReturn;
            }
            offset = nextHop( offset );
            if ( offset == offset( key ) )
            {
                return null;
            }
        }
        return null;
    }

    @Override
    public Set<K> keySet()
    {
        return new Set<K>()
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
                return get( (K) o ) != null;
            }

            @Override
            public Iterator<K> iterator()
            {
                return new Iterator<K>()
                {
                    private int location = 0;
                    private int hits = 0;

                    @Override
                    public boolean hasNext()
                    {
                        return hits < size;
                    }

                    @Override
                    public K next()
                    {
                        K toReturn = (K) keys[location];
                        while ( toReturn == null || toReturn.equals( Tombstone ) )
                        {
                            location++;
                            toReturn = (K) keys[location];
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
            public boolean add( K e )
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
            public boolean addAll( Collection<? extends K> c )
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

    public int size()
    {
        return size;
    }

    protected void checkResize( int reprobes )
    {
        if ( !inResize && ( reprobes > size / 4 || size > keys.length / 4 ) )
        {
            resize();
        }
    }

    protected void resize()
    {
        inResize = true;
        Object[] oldKeys = keys;
        Object[] oldValues = values;

        keys = new Object[oldKeys.length * 2];
        values = new Object[oldValues.length * 2];

        for ( int i = 0; i < oldKeys.length; i++ )
        {
            K key = (K) oldKeys[i];
            if ( key == null || key == Tombstone )
            {
                continue;
            }
            put( key, (V) oldValues[i] );
        }
        inResize = false;
    }

    protected int offset( Object key )
    {
        return ( key.hashCode() & ( keys.length - 1 ) );
    }

    protected int nextHop( int current )
    {
        return ( current + 1 ) & ( keys.length - 1 );
    }

    protected void increaseSize()
    {
        if ( !inResize )
        {
            size++;
        }
    }

    Object[] getKeys()
    {
        return keys;
    }

    Object[] getValues()
    {
        return values;
    }
}
