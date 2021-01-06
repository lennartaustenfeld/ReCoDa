package org.dice_research.opal.licenses.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.dice_research.opal.licenses.License;
import org.dice_research.opal.licenses.demo.ReCoDa;
import org.dice_research.opal.licenses.demo.model.Base;
import org.dice_research.opal.licenses.demo.model.Bases;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebserviceController {

	@CrossOrigin
	@GetMapping("/webservice/knowledge-bases")
	Map<String, String> knowledgeBases() {
		Map<String, String> map = new TreeMap<>();
		for (Entry<String, Base> entry : Bases.INSTANCE.getMap().entrySet()) {
			map.put(entry.getKey(), entry.getValue().getTitle());
		}
		return map;
	}

	@CrossOrigin
	@GetMapping("/webservice/licenses")
	Map<String, String> licenses(@RequestParam(name = "knowledge-base", required = true) String knowledgeBase) {
		if (Bases.INSTANCE.getMap().containsKey(knowledgeBase)) {
			return Bases.INSTANCE.getMap().get(knowledgeBase).getLicenseUrisToNames();
		} else {
			return new TreeMap<>();
		}
	}

	@CrossOrigin
	@GetMapping("/webservice/compatible-licenses")
	Map<String, String> compatibleLicenses(@RequestParam(name = "knowledge-base", required = true) String knowledgeBase,
			@RequestParam(name = "input-licenses", required = true) List<String> inputLicenses) {
		Map<String, String> map = new TreeMap<>();
		if (Bases.INSTANCE.getMap().containsKey(knowledgeBase)) {
			for (License compatibleLicense : new ReCoDa()
					.setKnowledgeBase(Bases.INSTANCE.getMap().get(knowledgeBase).getKnowledgeBase())
					.setInputLicenses(inputLicenses).execute().getCompatibleLicenses()) {
				map.put(compatibleLicense.getUri(), compatibleLicense.getName());
			}
		}
		return map;
	}

}