package org.dice_research.opal.licenses.demo;

import java.util.Map;
import java.util.TreeMap;

import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;

public abstract class Base {

	public abstract String getId();

	public abstract KnowledgeBase getKnowledgeBase();

	public abstract String getTitle();

	public Map<String, String> getLicenseUrisToNames() {
		Map<String, String> map = new TreeMap<>();
		for (License license : getKnowledgeBase().getLicenses()) {
			map.put(license.getUri(), license.getName());
		}
		return map;
	}
}