package org.dice_research.opal.licenses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackMapping {

	/**
	 * Gets compatible licenses.
	 * 
	 * Creates set of call of
	 * {@link #getCompatibleLicensesList(List, Attributes, KnowledgeBase)}.
	 */
	public Set<License> getCompatibleLicenses(List<License> inputLicenses, Attributes setting,
			KnowledgeBase knowledgeBase) {
		return new HashSet<License>(getCompatibleLicensesList(inputLicenses, setting, knowledgeBase));
	}

	/**
	 * Gets compatible licenses.
	 * 
	 * @param inputLicenses Combination of licenses for which other compatible
	 *                      licenses are requested
	 * @param setting       Attributes, based on internal values, typically computed
	 *                      by operator
	 * @param knowledgeBase Knowledgebase with all known licenses
	 *
	 * @return List of compatible licenses
	 */
	public List<License> getCompatibleLicensesList(List<License> inputLicenses, Attributes setting,
			KnowledgeBase knowledgeBase) {

		// No license to check -> no result
		if (inputLicenses.isEmpty()) {
			return new ArrayList<>(0);
		}

		// If a derivates-allowed permission is not set for an input license -> no
		// result
		for (Attribute attribute : knowledgeBase.getSortedAttributes().getList()) {
			if (attribute.isTypePermissionOfDerivates()) {
				// If there is one license not allowing derivates -> no result
				for (License license : inputLicenses) {
					if (!license.getAttributes().getAttribute(attribute.getUri()).getValue()) {
						return new ArrayList<>(0);
					}
				}
			}
		}

		// Filter by attributes
		List<License> resultingLicenses = removeLessRestrictive(setting, knowledgeBase.getLicenses(), false);

		// Remove incompatible share-alike restrictions
		for (License shareAlikeInputLicense : inputLicenses) {
			for (License license : knowledgeBase.getLicenses()) {
				if (shareAlikeInputLicense.isAttribueEquality()) {
					List<License> licenseList = new LinkedList<>();
					licenseList.add(license);
					if (removeNotCompatibleShareAlike(shareAlikeInputLicense, licenseList, false).isEmpty()) {
						resultingLicenses.remove(license);
					}
				}
			}
		}

		return resultingLicenses;
	}

	/**
	 * Gets matching licenses based on internal values.
	 */
	protected List<License> removeLessRestrictive(Attributes setting, List<License> licenses,
			boolean includeMetaAttributes) {
		List<License> results = new LinkedList<>();
		List<Attribute> settingAttributes = setting.getList();
		boolean[] settingValues = setting.getValuesArray();
		licenseLoop: for (License license : licenses) {
			boolean[] licenseValues = license.getAttributes().getValuesArray();
			valueLoop: for (int i = 0; i < settingValues.length; i++) {

				if (!includeMetaAttributes && settingAttributes.get(i).isMetaAttribute()) {
					continue valueLoop;
				}

				if (settingAttributes.get(i).getType().equals(Permission.TYPE)) {
					// Not compatible: Setting restricted and license open
					if (!settingValues[i] && licenseValues[i]) {
						continue licenseLoop;
					}
				}

				else if (settingAttributes.get(i).getType().equals(Prohibition.TYPE)) {
					// Not compatible: Setting restricted and license open
					if (settingValues[i] && !licenseValues[i]) {
						continue licenseLoop;
					}
				}

				else if (settingAttributes.get(i).getType().equals(Requirement.TYPE)) {
					// Not compatible: Setting restricted and license open
					if (settingValues[i] && !licenseValues[i]) {
						continue licenseLoop;
					}
				}

				else {
					throw new RuntimeException("Unknown type");
				}
			}

			results.add(license);
		}
		return results;
	}

	/**
	 * Gets matching licenses based on internal values.
	 * 
	 * Used for share-alike comparison.
	 */
	protected List<License> removeNotCompatibleShareAlike(License shareAlikeLicense, List<License> licenses,
			boolean includeMetaAttributes) {
		List<License> results = new LinkedList<>();
		List<Attribute> settingAttributes = shareAlikeLicense.getAttributes().getList();
		boolean[] settingValues = shareAlikeLicense.getAttributes().getValuesArray();
		short ccVersion = extractCreativeCommonsVersion(shareAlikeLicense.getUri());
		licenseLoop: for (License license : licenses) {
			boolean[] licenseValues = license.getAttributes().getValuesArray();
			valueLoop: for (int i = 0; i < settingValues.length; i++) {

				// Ignore meta attributes except of share-alike
				if (!includeMetaAttributes && settingAttributes.get(i).isMetaAttribute()
						&& !settingAttributes.get(i).isTypeAttribueEquality()) {
					continue valueLoop;
				}

				// Not compatible: Difference in permission / prohibition / requirement
				if (settingValues[i] != licenseValues[i]) {
					continue licenseLoop;
				}
			}

			// CC SA*: Also exclude, if input-license version is lower
			if (ccVersion != -1) {
				short licenseCcVersion = extractCreativeCommonsVersion(license.getUri());
				if (licenseCcVersion != -1 && ccVersion < licenseCcVersion) {
					continue licenseLoop;
				}
			}

			results.add(license);
		}
		return results;
	}

	/**
	 * Searches for 'creativecommons' string and extracts major version.
	 */
	private short extractCreativeCommonsVersion(String uri) {
		Pattern pattern = Pattern.compile(".*creativecommons.*(\\d)\\.\\d.*");
		Matcher matcher = pattern.matcher(uri);
		if (matcher.matches()) {
			return Short.parseShort(matcher.group(1));
		} else {
			return -1;
		}

	}
}