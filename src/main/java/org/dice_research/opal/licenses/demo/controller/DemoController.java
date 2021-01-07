package org.dice_research.opal.licenses.demo.controller;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dice_research.opal.licenses.demo.container.BaseContainer;
import org.dice_research.opal.licenses.demo.model.Base;
import org.dice_research.opal.licenses.demo.model.Bases;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DemoController {

	@GetMapping("/demo/knowledge-bases")
	public String knowledgeBases(Model model) {
		SortedSet<BaseContainer> list = new TreeSet<>();
		for (Base base : Bases.INSTANCE.getMap().values()) {
			list.add(base.getBaseContainer());
		}
		model.addAttribute("kbs", list);
		return "knowledge-bases";
	}

	@GetMapping("/demo/licenses")
	public String licenses(@RequestParam(name = "knowledge-base", required = true) String knowledgeBase, Model model) {
		model.addAttribute("licenses",
				Bases.INSTANCE.get(knowledgeBase).getBaseContainer().getLicenseContainersByName());
		return "licenses";
	}

	@GetMapping("/demo/webservices")
	public String webservices(Model model) {
		return "webservices";
	}

	@GetMapping("/demo/about")
	public String about(Model model) {
		return "about";
	}
}