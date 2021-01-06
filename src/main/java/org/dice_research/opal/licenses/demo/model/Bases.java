package org.dice_research.opal.licenses.demo.model;

import java.util.Map;
import java.util.TreeMap;

public enum Bases {

	INSTANCE;

	private Map<String, Base> bases = new TreeMap<>();

	public boolean contains(String key) {
		return bases.containsKey(key);
	}

	public Base get(String key) {
		return bases.get(key);
	}

	public Map<String, Base> getMap() {
		return bases;
	}

	public void initializeBase(Base base) {
		bases.put(base.getId(), base);
	}
}