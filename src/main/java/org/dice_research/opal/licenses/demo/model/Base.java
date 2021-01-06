package org.dice_research.opal.licenses.demo.model;

import java.util.Map;
import java.util.TreeMap;

import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;
import org.dice_research.opal.licenses.demo.container.BaseContainer;

public abstract class Base {

	private BaseContainer baseContainer;

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

	public BaseContainer getBaseContainer() {
		if (baseContainer == null) {
			baseContainer = new BaseContainer(this);
		}
		return baseContainer;
	}
}