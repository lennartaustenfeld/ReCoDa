package org.dice_research.opal.licenses.demo.controller;

import org.dice_research.opal.licenses.KnowledgeBases;
import org.dice_research.opal.licenses.demo.model.Bases;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CombinationController {

	@GetMapping("/demo/combine")
	public String query(@RequestParam(name = "knowledgebase", required = false) String knowledgebase, Model model) {

		if (knowledgebase == null) {
			knowledgebase = KnowledgeBases.ID_CC_MATRIX;
		}

		model.addAttribute("licenses", Bases.INSTANCE.get(knowledgebase).getBaseContainer().getLicenseContainers());
		return "combine";
	}
}