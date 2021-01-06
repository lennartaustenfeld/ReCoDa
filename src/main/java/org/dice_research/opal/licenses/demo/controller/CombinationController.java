package org.dice_research.opal.licenses.demo.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dice_research.opal.licenses.KnowledgeBases;
import org.dice_research.opal.licenses.License;
import org.dice_research.opal.licenses.demo.bases.Base;
import org.dice_research.opal.licenses.demo.bases.Bases;
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

		Base base = Bases.getInstance().get(knowledgebase);

		List<License> licenses = base.getKnowledgeBase().getLicenses();
		Collections.sort(licenses, new Comparator<License>() {

			@Override
			public int compare(License o1, License o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		model.addAttribute("licenses", licenses);
		return "combine";
	}
}