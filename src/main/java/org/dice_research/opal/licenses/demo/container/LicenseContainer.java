package org.dice_research.opal.licenses.demo.container;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dice_research.opal.licenses.Attribute;
import org.dice_research.opal.licenses.License;

public class LicenseContainer {

	public License license;

	public LicenseContainer(License license) {
		this.license = license;
	}

	public String getName() {

		// CC default URI rewriting
		if (license.getUri().startsWith("http://creativecommons.org/licenses/")) {
			String name = "CC "
					+ license.getUri().substring("http://creativecommons.org/licenses/".length()).replace('/', ' ');
			if (name.endsWith("legalcode")) {
				name = name.substring(0, name.length() - "legalcode".length());
			}
			return name;

		} else {
			return license.getName();
		}
	}

	public SortedSet<AttributeContainer> getAttributeContainers() {
		SortedSet<AttributeContainer> set = new TreeSet<>();
		for (Attribute attribute : license.getAttributes().getList()) {
			set.add(new AttributeContainer(attribute));
		}
		return set;
	}

	public String getAttributesHtml() {
		StringBuilder stringBuilder = new StringBuilder();
		for (AttributeContainer attCon : getAttributeContainers()) {
			stringBuilder.append(attCon.getHtmlUseValues());
			stringBuilder.append(System.lineSeparator());
		}
		return stringBuilder.toString();
	}
}