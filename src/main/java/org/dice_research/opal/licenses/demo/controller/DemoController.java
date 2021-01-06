package org.dice_research.opal.licenses.demo.controller;

import java.util.LinkedList;
import java.util.List;

import org.dice_research.opal.licenses.demo.container.BaseContainer;
import org.dice_research.opal.licenses.demo.model.Base;
import org.dice_research.opal.licenses.demo.model.Bases;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DemoController {

	@GetMapping("/demo/list")
	public String list(Model model) {
		List<BaseContainer> list = new LinkedList<>();
		for (Base base : Bases.INSTANCE.getMap().values()) {
			list.add(base.getBaseContainer());
		}
		model.addAttribute("kbs", list);
		return "list";
	}

	@GetMapping("/demo/combine")
	public String combine(@RequestParam(name = "knowledge-base", required = true) String knowledgeBase, Model model) {

		model.addAttribute("licenses",
				Bases.INSTANCE.get(knowledgeBase).getBaseContainer().getLicenseContainersByName());
		return "combine";
	}
}