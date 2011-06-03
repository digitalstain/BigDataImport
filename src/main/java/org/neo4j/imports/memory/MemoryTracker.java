package org.neo4j.imports.memory;

public interface MemoryTracker
{
	public void launch();
	
	public void reset();
	
	public void stop();
	
	public void registerMemoryObserver(MemoryObserver observer);
}
