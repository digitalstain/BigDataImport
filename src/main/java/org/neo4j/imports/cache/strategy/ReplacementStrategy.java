package org.neo4j.imports.cache.strategy;

public interface ReplacementStrategy<K> {

	public void hit(K key);
	
	public void remove(K key);
	
	public void evict(K key);
	
	public K suggest();
}
