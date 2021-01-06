package org.dice_research.opal.licenses.demo.bases;

import java.util.Map;
import java.util.TreeMap;

public enum Bases {

	INSTANCE;

	public static Bases getInstance() {
		return INSTANCE;
	}

	private Map<String, Base> bases;

	private Bases() {
		bases = new TreeMap<>();
		initializeBase(new BaseCc());
		initializeBase(new BaseCcLcc());
		initializeBase(new BaseEdpLcm());
	}

	public boolean contains(String key) {
		return bases.containsKey(key);
	}

	public Base get(String key) {
		return bases.get(key);
	}

	public Map<String, Base> getMap() {
		return bases;
	}

	private void initializeBase(Base base) {
		bases.put(base.getId(), base);
	}
}