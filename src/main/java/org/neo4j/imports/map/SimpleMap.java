package org.neo4j.imports.map;

import java.util.Set;

/**
 * A minimalistic interface capturing the essence of a hash map without the
 * bells and whistles of the JDK version. Also, more useful for persisting to
 * disk.
 */
public interface SimpleMap<K, V>
{
    /**
     * Inserts this key/value entry in the map. If the mapping for the key
     * already exists, it is replaced by the provided one. If value is null, it
     * is up to the implementation to decide what to do with it
     * 
     * @param key The key
     * @param value The value
     * @return true iff the map changed because of this operation, ie the
     *         mapping did not exist
     */
    public boolean put( K key, V value );

    /**
     * Inserts this mapping provided there is no entry for this key. Returns
     * false otherwise
     * 
     * @param key The key
     * @param value The value
     * @return true iff there was no mapping present for the key, ie the
     *         insertion succeeded
     */
    public boolean putIfAbsent( K key, V value );

    /**
     * Returns the value corresponding to the argument or null if there is none
     * 
     * @param key The key
     * @return The mapped value or null if none
     */
    public V get( K key );

    /**
     * Removes the mapping for this key, returning the mapped value if it
     * existed.
     * 
     * @param key The key for which to remove any mapping
     * @return The value it mapped to, or null if any
     */
    public V remove( K key );

    /**
     * Returns a (possibly empty) Set of the keys present. Useful mostly for
     * looping through the map.
     * 
     * @return A Set of the keys with a valid mapping, empty if none
     */
    public Set<K> keySet();
    
    /**
     * Return the size of the map, defined as the number of keys present, equal
     * to the number of mappings present.
     * 
     * @return The size of the map
     */
    public int size();
}
