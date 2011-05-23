package org.neo4j.imports.hash;

import java.util.Set;

import org.neo4j.imports.cache.strategy.ReplacementStrategy;

public class PartitionedHashMap implements SimpleHashMap<String, Long>
{
	private final ReplacementStrategy<String> strategy;
	
	public PartitionedHashMap(ReplacementStrategy<String> strategy)
	{
		this.strategy = strategy;
	}
	
    @Override
    public boolean put( String key, Long value )
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean putIfAbsent( String key, Long value )
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Long get( String key )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long remove( String key )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> keySet()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
