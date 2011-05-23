package org.neo4j.imports.hash;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.neo4j.imports.hash.persistence.ArrayHashMapOptionFactory;

public class PartitionedHashMap implements SimpleMap<String, Long>
{
	private static final int DefaultBits = 20;

	private final ArrayHashMapOptionFactory optionFactory;
	
	private int size;

	private static int hashFor(Object key, int bitsToKeep)
	{
		/*
		 * "Black magic (TM)" here:
		 * -1 is all 1s. store arrays are always a power of 2 in size,
		 * so length-1 is all 1s at the end, all 0s before that. XOR that
		 * with -1 gives reversed all the bits. This is the mask for the MSBs
		 * of the hash code. Example:
		 * 
		 * key.hashCode() is whatever
		 * keys.length is 32 = 00...0100000
		 * keys.length - 1 is 00...0011111
		 * -1 ^ (keys.length - 1) is 11...1100000
		 * 
		 * which is the mask for the first sizeOfInBits(int)-lg(keys.length) bits
		 * That is shifted lg(keys.length) bits to the right to avoid collisions on
		 * zero. [lg is logarithm base 2]
		 */
		int oneBits = (1 << bitsToKeep) - 1;

		int preHash = (key.hashCode() & (-1 ^ oneBits));
		while (oneBits > 0)
		{
			preHash >>= 1;
			oneBits >>= 1;
		}
		return preHash;
	}

	private GenericArrayHashMap<String, ArrayHashMapOption> store;
	
	public PartitionedHashMap(ArrayHashMapOptionFactory optionFactory)
	{
		this.optionFactory = optionFactory;
		store = new GenericArrayHashMap<String, ArrayHashMapOption>(1<<DefaultBits)
		{
			@Override
			protected int offset(Object key)
			{
				return PartitionedHashMap.hashFor(key, DefaultBits);
			}

			@Override
			protected void resize()
			{
				/* 
				 * We never resize.
				 * Collisions are resolved in the individual hash maps
				 */
			}
		};
	}

	@Override
	public boolean put(String key, Long value)
	{
		ArrayHashMapOption temp = store.get(key);
		if (temp == null)
		{
			try
			{
				temp = optionFactory.wrap(new ArrayHashMap(32), Integer.toHexString(hashFor(key, DefaultBits)));
				store.put(key, temp);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		size -= temp.getValue().size();
		boolean toReturn = temp.getValue().put(key, value);
		size += temp.getValue().size();
		return toReturn;
	}

	@Override
	public boolean putIfAbsent(String key, Long value)
	{
		ArrayHashMapOption temp = store.get(key);
		if (temp == null)
		{
			try
			{
				temp = optionFactory.wrap(new ArrayHashMap(32), Integer.toHexString(hashFor(key, DefaultBits)));
				store.put(key, temp);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		size -= temp.getValue().size();
		boolean toReturn = temp.getValue().putIfAbsent(key, value);
		size += temp.getValue().size();
		return toReturn;
	}

	@Override
	public Long get(String key)
	{
		ArrayHashMapOption temp = store.get(key);
		if (temp == null)
		{
			return null;
		}
		return temp.getValue().get(key);
	}

	@Override
	public Long remove(String key)
	{
		ArrayHashMapOption temp = store.get(key);
		if (temp == null)
		{
			return null;
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
                    private int location = 0;
                    
                    private Iterator<String> topKeySet = store.keySet().iterator();
                    
                    private Iterator<String> currentKeySet = topKeySet.hasNext() ? store.get(topKeySet.next()).getValue().keySet().iterator() : null;

                    @Override
                    public boolean hasNext()
                    {
                        return currentKeySet != null;
                    }

                    @Override
                    public String next()
                    {
                        if (currentKeySet.hasNext())
                        {
                        	return currentKeySet.next();
                        }
                        if (topKeySet.hasNext())
                        {
                        	currentKeySet = store.get(topKeySet.next()).getValue().keySet().iterator();
                        	return currentKeySet.next();
                        }
                        currentKeySet = null;
                        return null;
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
}
