package org.dice_research.opal.licenses.demo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.dice_research.opal.licenses.Attributes;
import org.dice_research.opal.licenses.BackMapping;
import org.dice_research.opal.licenses.Execution;
import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;

public class ReCoDaDemo {

	private Map<String, Base> bases;

	public Map<String, String> getKnowledgeBases() {
		initialize();
		Map<String, String> map = new TreeMap<>();
		for (Entry<String, Base> entry : bases.entrySet()) {
			map.put(entry.getKey(), entry.getValue().getTitle());
		}
		return map;
	}

	public Map<String, String> getLicenses(String knowledgeBase) {
		initialize();
		if (bases.containsKey(knowledgeBase)) {
			return bases.get(knowledgeBase).getLicenseUrisToNames();
		} else {
			return new TreeMap<>();
		}
	}

	public Map<String, String> getCompatibleLiceses(String knowledgeBase, List<String> licenses) {
		initialize();
		Map<String, String> map = new TreeMap<>();
		if (bases.containsKey(knowledgeBase)) {

			// Set KnowledgeBase
			KnowledgeBase kb = bases.get(knowledgeBase).getKnowledgeBase();
			Execution execution = new Execution().setKnowledgeBase(kb);

			// Create list of input-licenses
			List<License> inputLicenses = new LinkedList<>();
			Set<String> licenseUris = kb.getLicenseUris();
			for (String license : licenses) {
				if (licenseUris.contains(license)) {
					inputLicenses.add(kb.getUrisToLicenses().get(license));
				}
			}

			// Apply operator
			Attributes resultAttributes = execution.applyOperator(inputLicenses);

			// Apply back-mapping
			Set<License> resultingLicenses = new BackMapping().getCompatibleLicenses(inputLicenses, resultAttributes,
					kb);

			// Put results to map
			for (License license : resultingLicenses) {
				map.put(license.getUri(), license.getName());
			}
		}
		return map;
	}

	private void initialize() {
		if (bases == null) {
			bases = new TreeMap<>();
			initializeBase(new BaseCc());
			initializeBase(new BaseCcLcc());
			initializeBase(new BaseEdpLcm());
		}
	}

	private void initializeBase(Base base) {
		bases.put(base.getId(), base);
	}

}