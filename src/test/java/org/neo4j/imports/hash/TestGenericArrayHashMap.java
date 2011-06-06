package org.neo4j.imports.hash;

import org.neo4j.imports.map.GenericArrayHashMap;
import org.neo4j.imports.map.SimpleMap;

public class TestGenericArrayHashMap extends AbstractTestArrayHashMap {

	@Override
	protected SimpleMap<String, Long> getMapInstance(int size) {
		return new GenericArrayHashMap<String, Long>(size);
	}

}
