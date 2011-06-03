package org.neo4j.imports.memory;

/**
 * Classes interested in getting notifications on low
 * available memory should implement this interface and register
 * themselves with a MemoryTracker.  
 */
public interface MemoryObserver
{
	public void memoryLow();
}
