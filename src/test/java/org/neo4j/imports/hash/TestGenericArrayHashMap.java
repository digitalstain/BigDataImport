package org.neo4j.imports.hash;

public class TestGenericArrayHashMap extends TestArrayHashMap {

	@Override
	protected SimpleMap<String, Long> getMapInstance(int size) {
		return new GenericArrayHashMap<String, Long>(size);
	}

}
