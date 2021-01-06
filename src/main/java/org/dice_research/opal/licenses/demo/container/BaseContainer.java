package org.dice_research.opal.licenses.demo.container;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dice_research.opal.licenses.Attribute;
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

	public int getNumberOfLicenses() {
		return base.getKnowledgeBase().getUrisToLicenses().size();
	}

	public List<AttributeContainer> getAttributeContainers() {
		List<AttributeContainer> list = new LinkedList<>();
		for (Attribute attribute : base.getKnowledgeBase().getSortedAttributes().getList()) {
			list.add(new AttributeContainer(attribute));
		}
		return list;
	}

	public int getNumberOfAttributes() {
		return base.getKnowledgeBase().getSortedAttributes().getUriToAttributeMap().size();
	}

	public String getAttributesHtml() {
		StringBuilder stringBuilder = new StringBuilder();
		for (AttributeContainer attCon : getAttributeContainers()) {
			stringBuilder.append(attCon.getHtml());
			stringBuilder.append(System.lineSeparator());
		}
		return stringBuilder.toString();
	}

	public SortedSet<String> getAttributeUriSuffixes() {
		SortedSet<String> set = new TreeSet<>();
		for (AttributeContainer attributeConteiner : getAttributeContainers()) {
			set.add(attributeConteiner.getUriSuffix());
		}
		return set;
	}

	public List<LicenseContainer> getLicenseContainersByName() {
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