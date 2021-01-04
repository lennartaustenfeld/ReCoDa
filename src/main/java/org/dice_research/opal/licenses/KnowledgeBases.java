package org.dice_research.opal.licenses;

import java.io.File;

import org.apache.jena.riot.Lang;
import org.dice_research.opal.licenses.utils.CcRel;
import org.dice_research.opal.licenses.utils.CollectionUtil;

/**
 * Imports Knowledge Bases.
 *
 * @author Adrian Wilke
 */
public class KnowledgeBases {

	public static final String ID_CC = "CreativeCommons";;
	public static final String ID_CC_MATRIX = "CreativeCommons-LicenseCompatibilityChart";
	public static final String ID_EDP_LCM = "EuropeanDataPortal-LicenseCompatibilityMatrix";

	public static final File KB_DIRECTORY = new File("src/main/resources/knowledge-bases");
	public static final String KB_FILE_SUFFIX = ".ttl";

	/**
	 * Creative Commons licenses
	 * 
	 * @see https://github.com/projekt-opal/cc.licenserdf
	 */
	public KnowledgeBase importCreativeCommons() {
		return importKnowledgeBase(ID_CC, "http://creativecommons.org/ns#ShareAlike",
				"http://creativecommons.org/ns#DerivativeWorks");
	}

	/**
	 * Creative Commons - License Compatibility Chart
	 * 
	 * @see https://github.com/projekt-opal/cc.licenserdf
	 * @see https://wiki.creativecommons.org/index.php?title=Wiki/cc_license_compatibility&oldid=70058
	 */
	public KnowledgeBase importCreativeCommonsMatrix() {
		return importKnowledgeBase(ID_CC_MATRIX, "http://creativecommons.org/ns#ShareAlike",
				"http://creativecommons.org/ns#DerivativeWorks");
	}

	/**
	 * European Data Portal - License Compatibility Matrix
	 * 
	 * @see http://www.europeandataportal.eu/sites/default/files/edp-licence-compatibility-published_v1_0.xlsx
	 */
	public KnowledgeBase importEDP_Matrix() {
		return importKnowledgeBase(ID_EDP_LCM, "http://example.org/Share-Alike", "http://example.org/Derivative-Works");
	}

	private KnowledgeBase importKnowledgeBase(String knowledgeBaseId, String attribueEqualityUri,
			String permissionOfDerivatesUri) {
		return new CcRel(CollectionUtil.stringToSet(attribueEqualityUri),
				CollectionUtil.stringToSet(permissionOfDerivatesUri))
						.importFile(new File(KB_DIRECTORY, knowledgeBaseId + KB_FILE_SUFFIX), Lang.TURTLE);
	}
}