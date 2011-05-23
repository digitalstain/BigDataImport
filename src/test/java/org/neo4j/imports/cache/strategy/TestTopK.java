package org.neo4j.imports.cache.strategy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestTopK {

	@Test
	public void sanityCheck()
	{
		ReplacementStrategy<String> topK = new TopK<String>();
		topK.hit("1");
		topK.hit("2");
		topK.hit("3");
		topK.hit("1");
		topK.hit("2");
		assertEquals("3", topK.suggest());

		topK = new TopK<String>();
		topK.hit("1");
		topK.hit("1");
		topK.hit("2");
		topK.hit("2");
		topK.hit("3");
		assertEquals("3", topK.suggest());

		topK = new TopK<String>();
		topK.hit("3");
		topK.hit("1");
		topK.hit("2");
		topK.hit("2");
		topK.hit("1");
		assertEquals("3", topK.suggest());
	}
	
	@Test
	public void testRemove()
	{
		
	}
	
	@Test
	public void testSize()
	{
		
	}
	
	@Test
	public void testTransitions()
	{
		ReplacementStrategy<String> topK = new TopK<String>();
		topK.hit("1");
		assertEquals("1", topK.suggest());		
		topK.hit("2");
		assertEquals("1", topK.suggest());
		topK.hit("3");
		assertEquals("1", topK.suggest());
		topK.hit("1");
		assertEquals("2", topK.suggest());
		topK.hit("2");
		assertEquals("3", topK.suggest());
	}
}
