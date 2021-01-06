package org.dice_research.opal.licenses.demo;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dice_research.opal.licenses.Attributes;
import org.dice_research.opal.licenses.BackMapping;
import org.dice_research.opal.licenses.Execution;
import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;

public class ReCoDa {

	private KnowledgeBase knowledgeBase;
	private List<License> inputLicenses;
	private Set<License> compatibleLicenses;

	public ReCoDa setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
		return this;
	}

	public ReCoDa setInputLicenses(List<String> inputLicenseIds) {
		inputLicenses = new LinkedList<>();
		for (String license : inputLicenseIds) {
			if (knowledgeBase.getUrisToLicenses().containsKey(license)) {
				inputLicenses.add(knowledgeBase.getUrisToLicenses().get(license));
			}
		}
		return this;
	}

	public ReCoDa execute() {
		if (knowledgeBase == null) {
			throw new RuntimeException("No knowledge base set.");
		}
		if (inputLicenses == null) {
			throw new RuntimeException("No input licenses set.");
		}

		// Set KnowledgeBase
		Execution execution = new Execution().setKnowledgeBase(knowledgeBase);

		// Apply operator
		Attributes resultAttributes = execution.applyOperator(inputLicenses);

		// Apply back-mapping
		compatibleLicenses = new BackMapping().getCompatibleLicenses(inputLicenses, resultAttributes, knowledgeBase);

		return this;
	}

	public Set<License> getCompatibleLicenses() {
		if (compatibleLicenses == null) {
			throw new RuntimeException("Computation was not exexuted.");
		}

		return compatibleLicenses;
	}

}