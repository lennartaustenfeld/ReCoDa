package org.dice_research.opal.licenses.transform;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.KnowledgeBases;
import org.dice_research.opal.licenses.edplcm.EdpLcmKnowledgeBase;
import org.dice_research.opal.licenses.utils.CollectionUtil;

/**
 *
 * @author Adrian Wilke
 */
public class KbGenEdp extends KbGen {

	public static void main(String[] args) throws Exception {
		File file = new KbGenEdp().export();
		System.out.println("Exported: " + file.getAbsolutePath());
	}

	@Override
	public Collection<String> getAttribueEqualityUris() {
		return CollectionUtil.stringToSet(EdpLcmKnowledgeBase.ATTRIBUTE_ID_ALIKE);
	}

	@Override
	public Collection<String> getPermissionOfDerivatesUris() {
		return CollectionUtil.stringToSet(EdpLcmKnowledgeBase.ATTRIBUTE_ID_DERIVATES);
	}

	@Override
	public String getTitle() {
		return KnowledgeBases.ID_EDP_LCM;
	}

	@Override
	public KnowledgeBase getKnowledgeBase() {
		try {
			return new EdpLcmKnowledgeBase().load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<String, String> getPrefixes() {
		Map<String, String> prefixes = new HashMap<>();
		prefixes.put("ex", "http://example.org/");
		return prefixes;
	}

}