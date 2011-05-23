package org.neo4j.imports.cache.strategy;

/**
 * An attempt at defining the behaviour of a replacement strategy for caches.
 * 
 * @param K The keys the cache that uses this strategy stores.
 */
public interface ReplacementStrategy<K> {

	/**
	 * Notifies the strategy that this key has been requested from users.
	 * This is also the canonical way of adding (first time) keys in the strategy.
	 * @param key The key that has been hit
	 */
	public void hit(K key);
	
	/**
	 * Makes the cache to forget all about the supplied key. All stats are thrown away and
	 * when next added via {@link #hit(Object)} it will be like seeing it for the first time.
	 * 
	 * @param key The key to forget
	 */
	public void remove(K key);
	
	/**
	 * Callback like method that notifies the strategy that this key has been evicted. It is not
	 * expected to be used by all implementations.
	 * 
	 * @param key The key that has been evicted.
	 */
	public void evict(K key);
	
	/**
	 * The core method for implementations, returns a candidate key for evictin from the cache.
	 * There is no requirement that two subsequent calls to this method return the same value, even
	 * if no calls to the other methods are made in between.
	 * @return The key to evict.
	 */
	public K suggest();
}
