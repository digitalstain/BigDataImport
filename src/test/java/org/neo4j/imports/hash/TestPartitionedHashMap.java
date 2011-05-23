package org.neo4j.imports.hash;

import java.io.File;

import org.neo4j.imports.hash.persistence.ArrayHashMapOptionFactory;

public class TestPartitionedHashMap extends TestArrayHashMap {

	@Override
	protected SimpleMap<String, Long> getMapInstance(int size) {
		File store = new File("target/partitioned");
		store.mkdirs();
		return new PartitionedHashMap(new ArrayHashMapOptionFactory(store));
	}
}
