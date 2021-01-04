package org.dice_research.opal.licenses.utils;

import java.util.HashSet;
import java.util.Set;

public abstract class CollectionUtil {

	/**
	 * Creates new set containing the given string.
	 */
	public static Set<String> stringToSet(String string) {
		Set<String> set = new HashSet<>();
		set.add(string);
		return set;
	}

}