package org.dice_research.opal.licenses.demo.bases;

import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.KnowledgeBases;

public class BaseCcLcc extends Base {

	private KnowledgeBase knowledgeBase;

	@Override
	public String getId() {
		return KnowledgeBases.ID_CC_MATRIX;
	}

	@Override
	public KnowledgeBase getKnowledgeBase() {
		if (knowledgeBase == null) {
			knowledgeBase = new KnowledgeBases().importCreativeCommonsMatrix();
		}
		return knowledgeBase;
	}

	@Override
	public String getTitle() {
		return "Creative Commons - License Compatibility Chart";
	}
}