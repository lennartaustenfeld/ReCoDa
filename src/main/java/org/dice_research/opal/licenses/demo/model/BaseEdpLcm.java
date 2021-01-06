package org.dice_research.opal.licenses.demo.model;

import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.KnowledgeBases;

public class BaseEdpLcm extends Base {

	private KnowledgeBase knowledgeBase;

	@Override
	public String getId() {
		return KnowledgeBases.ID_EDP_LCM;
	}

	@Override
	public KnowledgeBase getKnowledgeBase() {
		if (knowledgeBase == null) {
			knowledgeBase = new KnowledgeBases().importEDP_Matrix();
		}
		return knowledgeBase;
	}

	@Override
	public String getTitle() {
		return "European Data Portal - License Compatibility Matrix";
	}
}