package org.dice_research.opal.licenses.demo.controller;

import org.dice_research.opal.licenses.demo.model.Bases;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DemoController {

	@GetMapping("/demo/combine")
	public String query(@RequestParam(name = "knowledge-base", required = true) String knowledgeBase, Model model) {

		model.addAttribute("licenses",
				Bases.INSTANCE.get(knowledgeBase).getBaseContainer().getLicenseContainersByName());
		return "combine";
	}
}