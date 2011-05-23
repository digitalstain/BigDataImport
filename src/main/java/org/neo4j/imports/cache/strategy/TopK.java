package org.neo4j.imports.cache.strategy;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TopK<K> implements ReplacementStrategy<K> {

	private final Map<K, TopKEntry<K>> keyToCount;
	private final List<TopKEntry<K>> countOrder;

	private final static TopKEntryComparator Comparator = new TopKEntryComparator();

	public TopK()
	{
		this.keyToCount = new HashMap<K, TopKEntry<K>>();
		this.countOrder = new LinkedList<TopK.TopKEntry<K>>();
	}

	@Override
	public void hit(K key)
	{
		TopKEntry<K> current = keyToCount.get(key);
		if (current == null)
		{
			TopKEntry<K> incoming = TopKEntry.forKey(key);
			keyToCount.put(key, incoming);
			countOrder.add(incoming);
			current = incoming;
		}
		current.count++;
		Collections.sort(countOrder, Comparator);
	}

	@Override
	public void remove(K key)
	{
	}

	@Override
	public void evict(K key)
	{
	}

	@Override
	public K suggest()
	{
		return countOrder.get(0).key;
	}
	
	private static class TopKEntry<K>
	{
		K key;
		int count;
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (!(obj instanceof TopKEntry))
			{
				return false;
			}
			TopKEntry<?> other = (TopKEntry<?>) obj;
			return key.equals(other.key);
		}
		
		@Override
		public int hashCode() {
			return key.hashCode();
		}
		
		public static <K> TopKEntry<K> forKey(K key)
		{
			TopKEntry<K> toReturn = new TopKEntry<K>();
			toReturn.key = key;
			return toReturn;
		}
	}
	
	private static class TopKEntryComparator<K> implements Comparator<TopKEntry<K>>
	{
		@Override
		public int compare(TopKEntry<K> o1, TopKEntry<K> o2) {
			return o1.count - o2.count;
		}
	}
}
