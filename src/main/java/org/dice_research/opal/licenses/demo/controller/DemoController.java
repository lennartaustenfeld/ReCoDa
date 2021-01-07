package org.dice_research.opal.licenses.demo.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dice_research.opal.licenses.License;
import org.dice_research.opal.licenses.demo.ReCoDa;
import org.dice_research.opal.licenses.demo.container.BaseContainer;
import org.dice_research.opal.licenses.demo.container.LicenseContainer;
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
		model.addAttribute("knowledgebase", knowledgeBase);
		return "licenses";
	}

	@GetMapping("/demo/compatible-licenses")
	public String compatibleLicenses(@RequestParam Map<String, String> params, Model model) {
		Base base = null;
		List<String> inputLicenseUris = new LinkedList<>();
		for (Entry<String, String> param : params.entrySet()) {
			if (param.getKey().equals("knowledge-base")) {
				base = Bases.INSTANCE.getMap().get(param.getValue());
			} else if (param.getValue().equals("1")) {
				inputLicenseUris.add(param.getKey());
			}
		}

		List<LicenseContainer> compatibleLicenseContainers = new LinkedList<>();
		if (!inputLicenseUris.isEmpty()) {
			for (License license : new ReCoDa().setKnowledgeBase(base.getKnowledgeBase())
					.setInputLicenses(inputLicenseUris).execute().getCompatibleLicenses()) {
				compatibleLicenseContainers.add(new LicenseContainer(license));
			}
		}

		List<LicenseContainer> inputLicenseContainers = new LinkedList<>();
		for (String inputLicenseUri : inputLicenseUris) {
			inputLicenseContainers.add(new LicenseContainer(base.getKnowledgeBase().getLicense(inputLicenseUri)));
		}

		model.addAttribute("knowledgeBase", base.getId());
		model.addAttribute("compatibleLicenses", compatibleLicenseContainers);
		model.addAttribute("inputLicenses", inputLicenseContainers);
		return "compatible-licenses";
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