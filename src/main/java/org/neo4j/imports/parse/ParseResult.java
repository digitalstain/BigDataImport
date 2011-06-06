package org.neo4j.imports.parse;

public class ParseResult<K>
{
	private final K firstNode;
	private final K secondNode;
	private final K relType;
	
	ParseResult(K firstNode, K secondNode, K relType)
	{
		this.firstNode = firstNode;
		this.secondNode = secondNode;
		this.relType = relType;
	}
	
	ParseResult(K firstNode, K secondNode)
	{
		this(firstNode, secondNode, null);
	}
	
	ParseResult(K firstNode)
	{
		this(firstNode, null, null);
	}
	
	public K getFirstNode()
	{
		return firstNode;
	}
	
	public K getSecondNode()
	{
		return secondNode;
	}
	
	public K getRelType()
	{
		return relType;
	}
}
