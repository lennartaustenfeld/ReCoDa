package org.dice_research.opal.licenses.demo.container;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;
import org.dice_research.opal.licenses.demo.model.Base;

public class BaseContainer {

	public Base base;

	public BaseContainer(Base base) {
		this.base = base;
	}

	public String getId() {
		return base.getId();
	}

	public KnowledgeBase getKnowledgeBase() {
		return base.getKnowledgeBase();
	}

	public String getTitle() {
		return base.getTitle();
	}

	public List<LicenseContainer> getLicenseContainers() {
		List<LicenseContainer> list = new LinkedList<>();
		for (License license : getKnowledgeBase().getLicenses()) {
			list.add(new LicenseContainer(license));
		}
		Collections.sort(list, new Comparator<LicenseContainer>() {

			@Override
			public int compare(LicenseContainer o1, LicenseContainer o2) {
				return o1.license.getName().compareToIgnoreCase(o2.license.getName());
			}
		});
		return list;
	}

}