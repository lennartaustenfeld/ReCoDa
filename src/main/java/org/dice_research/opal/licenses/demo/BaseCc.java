package org.dice_research.opal.licenses.demo;

import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.KnowledgeBases;

public class BaseCc extends Base {

	private KnowledgeBase knowledgeBase;

	@Override
	public String getId() {
		return KnowledgeBases.ID_CC;
	}

	@Override
	public KnowledgeBase getKnowledgeBase() {
		if (knowledgeBase == null) {
			knowledgeBase = new KnowledgeBases().importCreativeCommons();
		}
		return knowledgeBase;
	}

	@Override
	public String getTitle() {
		return "Creative Commons";
	}
}