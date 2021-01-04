package org.dice_research.opal.licenses.transform;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.utils.CcRel;

/**
 * Abstract class to create a {@link KnowledgeBase} in turtle format using the
 * {@link CcRel} vocabulary.
 *
 * @author Adrian Wilke
 */
public abstract class KbGen {

	public static final File DIRECTORY_RESOURCES = new File("src/main/resources");

	/**
	 * Creates new set containing the given string.
	 */
	public static Set<String> stringToSet(String string) {
		Set<String> set = new HashSet<>();
		set.add(string);
		return set;
	}

	/**
	 * Exports a {@link KnowledgeBase} in turtle format using the {@link CcRel}
	 * vocabulary.
	 */
	public File export() {

		List<String> commentLines = new LinkedList<>();
		commentLines.add("Relicensing Combined Datasets (ReCoDa)");
		commentLines.add("https://github.com/dice-group/ReCoDa/");
		if (!getAttribueEqualityUris().isEmpty()) {
			commentLines.add("");
			commentLines.add("AttribueEquality:");
			commentLines.add(getAttribueEqualityUris().toString());
		}
		if (!getPermissionOfDerivatesUris().isEmpty()) {
			commentLines.add("");
			commentLines.add("PermissionOfDerivates:");
			commentLines.add(getPermissionOfDerivatesUris().toString());
		}

		File file = new File(DIRECTORY_RESOURCES, getTitle() + ".ttl");
		new CcRel(getAttribueEqualityUris(), getPermissionOfDerivatesUris()).exportTurtle(getKnowledgeBase(), file,
				getPrefixes(), commentLines);
		return file;
	}

	public abstract Collection<String> getAttribueEqualityUris();

	public abstract Collection<String> getPermissionOfDerivatesUris();

	public abstract String getTitle();

	public abstract KnowledgeBase getKnowledgeBase();

	public abstract Map<String, String> getPrefixes();

}