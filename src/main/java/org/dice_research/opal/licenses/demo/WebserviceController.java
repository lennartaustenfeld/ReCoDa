package org.dice_research.opal.licenses.demo;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebserviceController {

	private ReCoDaDemo reCoDaDemo = new ReCoDaDemo();

	@CrossOrigin
	@GetMapping("/")
	String root() {
		return "ReCoDa - https://github.com/dice-group/ReCoDa";
	}

	@CrossOrigin
	@GetMapping("/knowledge-bases")
	Map<String, String> knowledgeBases() {
		return reCoDaDemo.getKnowledgeBases();
	}

	@CrossOrigin
	@GetMapping("/licenses")
	Map<String, String> licenses(String knowledgeBase) {
		return reCoDaDemo.getLicenses(knowledgeBase);
	}

	@CrossOrigin
	@GetMapping("/compatible-licenses")
	Map<String, String> compatibleLicenses(@RequestParam("knowledgeBase") String knowledgeBase,
			@RequestParam("licenses") List<String> licenses) {
		return reCoDaDemo.getCompatibleLiceses(knowledgeBase, licenses);
	}

}