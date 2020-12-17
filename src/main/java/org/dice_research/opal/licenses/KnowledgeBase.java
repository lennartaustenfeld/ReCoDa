package org.dice_research.opal.licenses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.dice_research.opal.licenses.utils.ArrayUtil;

/**
 * A knowledge base comprises known attributes and a list of known licenses.
 *
 * @author Adrian Wilke
 */
public class KnowledgeBase {

	private Attributes attributes = new Attributes();
	private LinkedHashMap<String, License> urisToLicenses = new LinkedHashMap<>();
	private boolean attributesSorted = false;

	public KnowledgeBase addAttribute(Attribute attribute) {
		attributes.addAttribute(attribute);
		attributesSorted = false;
		return this;
	}

	public void addLicense(License license) {
		urisToLicenses.put(license.getUri(), license);
	}

	/**
	 * Sorts the attributes in knowledge base if not sorted. Returns them.
	 */
	public Attributes getSortedAttributes() {
		if (!attributesSorted) {
			attributes.sort();
		}
		return attributes;
	}

	public License getLicense(String uri) {
		return urisToLicenses.get(uri);
	}

	public List<License> getLicenses() {
		return new ArrayList<License>(urisToLicenses.values());
	}

	public Set<String> getLicenseUris() {
		return urisToLicenses.keySet();
	}

	public LinkedHashMap<String, License> getUrisToLicenses() {
		return urisToLicenses;
	}

	public String toLines() {
		StringBuilder stringBuilder = new StringBuilder();
		for (Attribute attribute : getSortedAttributes().getList()) {
			stringBuilder.append(attribute);
			stringBuilder.append(System.lineSeparator());
		}

		List<License> licenses = getLicenses();
		Collections.sort(licenses, new Comparator<License>() {
			@Override
			public int compare(License o1, License o2) {
				return o1.getUri().compareTo(o2.getUri());
			}
		});
		for (License license : licenses) {
			stringBuilder.append(ArrayUtil.intString(license.getAttributes().getValuesArray()));
			stringBuilder.append(" ");
			stringBuilder.append(ArrayUtil.intString(license.getAttributes().getInternalValuesArray()));
			stringBuilder.append(" ");
			stringBuilder.append(license.getUri());
			stringBuilder.append(System.lineSeparator());
		}
		return stringBuilder.toString();
	}
}