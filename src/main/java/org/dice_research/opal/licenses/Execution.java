package org.dice_research.opal.licenses;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Methods for execution.
 * 
 * @author Adrian Wilke
 */
public class Execution {

	private KnowledgeBase knowledgeBase;

	/**
	 * Gets set of compatible licenses.
	 */
	public Set<License> applyBackMapping(List<License> inputLicenses, Attributes setting) {
		return new BackMapping().getCompatibleLicenses(inputLicenses, setting, knowledgeBase);
	}

	/**
	 * Applies operator on the given licenses list. Returns operator result in
	 * attributes object based on attributes in knowledge base (types, URIs).
	 */
	public Attributes applyOperator(List<License> licenses) {
		if (licenses == null || licenses.isEmpty()) {
			throw new RuntimeException("No license provided");
		}

		// Collect internal values of licenses
		List<boolean[]> internalValues = new ArrayList<>(licenses.size());
		for (License license : licenses) {
			internalValues.add(license.getAttributes().getInternalValuesArray());
		}

		// Apply operator
		boolean[] result = new Operator().compute(internalValues);

		// Put results in attribute object
		Attributes attributes = new Attributes();
		for (int i = 0; i < knowledgeBase.getSortedAttributes().getList().size(); i++) {
			attributes.addAttribute(
					AttributeFactory.get().createAttribute(knowledgeBase.getSortedAttributes().getList().get(i), false)
							.setValueByInternal(result[i]));
		}
		return attributes;
	}

	/**
	 * Sets the knowledge base with licenses and attributes.
	 */
	public Execution setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
		return this;
	}

}