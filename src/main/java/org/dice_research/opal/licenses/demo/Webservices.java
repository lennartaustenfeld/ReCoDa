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
import org.dice_research.opal.licenses.demo.bases.Base;
import org.dice_research.opal.licenses.demo.bases.Bases;

public class Webservices {

	public Map<String, String> getKnowledgeBases() {
		Map<String, String> map = new TreeMap<>();
		for (Entry<String, Base> entry : Bases.getInstance().getMap().entrySet()) {
			map.put(entry.getKey(), entry.getValue().getTitle());
		}
		return map;
	}

	public Map<String, String> getLicenses(String knowledgeBase) {
		if (Bases.getInstance().getMap().containsKey(knowledgeBase)) {
			return Bases.getInstance().getMap().get(knowledgeBase).getLicenseUrisToNames();
		} else {
			return new TreeMap<>();
		}
	}

	public Map<String, String> getCompatibleLiceses(String knowledgeBase, List<String> licenses) {
		Map<String, String> map = new TreeMap<>();
		if (Bases.getInstance().getMap().containsKey(knowledgeBase)) {

			// Set KnowledgeBase
			KnowledgeBase kb = Bases.getInstance().getMap().get(knowledgeBase).getKnowledgeBase();
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

}