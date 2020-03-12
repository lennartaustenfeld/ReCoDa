package org.dice_research.opal.licenses.operator;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A knowledge base comprises known attributes and a list of known licenses.
 *
 * @author Adrian Wilke
 */
public class KnowledgeBase {

	private Attributes attributes = new Attributes();
	private LinkedHashMap<String, License> urisToLicenses = new LinkedHashMap<String, License>();

	public KnowledgeBase addAttribute(Attribute attribute) {
		attributes.addAttribute(attribute);
		return this;
	}

	public void addLicense(License license) {
		urisToLicenses.put(license.getUri(), license);
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public Collection<License> getLicenses() {
		return urisToLicenses.values();
	}

	public LinkedHashMap<String, License> getUrisToLicenses() {
		return urisToLicenses;
	}

	public List<License> getMatchingLicenses(boolean[] attributeValues, boolean internal) {
		List<License> licenses = new LinkedList<>();
		for (License license : getLicenses()) {
			if (internal) {
				if (Arrays.equals(attributeValues, license.getAttributes().getInternalArray())) {
					licenses.add(license);
				}
			} else {
				if (Arrays.equals(attributeValues, license.getAttributes().getValuesArray())) {
					licenses.add(license);
				}
			}
		}
		return licenses;
	}
}