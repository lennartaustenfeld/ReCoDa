package org.dice_research.opal.licenses.cc;

import java.util.LinkedList;
import java.util.List;

import org.dice_research.opal.licenses.Attribute;
import org.dice_research.opal.licenses.AttributeFactory;
import org.dice_research.opal.licenses.Attributes;
import org.dice_research.opal.licenses.BackMapping;
import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link BackMapping}.
 * 
 * @see ../package-info.java
 * 
 * @author Adrian Wilke
 */
public class CcBackMappingTest extends BackMapping {

	private KnowledgeBase knowledgeBase;
	private License BY, BY_NC, BY_SA, BY_NC_SA;

	/**
	 * Checks data directory. Creates objects.
	 */
	@Before
	public void setUp() {
		knowledgeBase = CcTestUtils.getKnowledgeBase(CcTestUtils.getCcData());
		BY = knowledgeBase.getLicense(CcMatrix.I2_BY);
		BY_SA = knowledgeBase.getLicense(CcMatrix.I3_BY_SA);
		BY_NC = knowledgeBase.getLicense(CcMatrix.I4_BY_NC);
		BY_NC_SA = knowledgeBase.getLicense(CcMatrix.I6_BY_NC_SA);
	}

	/**
	 * E.g. If setting for CommercialUse (Prohibition) is 1, the result must not
	 * contain 0.
	 */
	@Test
	public void testRemoveLessRestrictive() {
		Attributes setting = BY_NC.getAttributes();
		List<License> licenses = new LinkedList<>();
		licenses.add(BY);
		licenses.add(BY_NC);
		List<License> resultingLicenses = removeLessRestrictive(setting, licenses, false);

		Assert.assertTrue("BY NC", resultingLicenses.contains(BY_NC));
		Assert.assertFalse("not BY", resultingLicenses.contains(BY));
	}

	/**
	 * E.g. If setting for CommercialUse (Prohibition) is 0, the result must not
	 * contain 1.
	 */
	@Test
	public void testRemoveNotShareAlike() {

		License shareAlikeLicense = BY;
		List<License> licenses = new LinkedList<>();
		licenses.add(BY);
		licenses.add(BY_NC);
		licenses.add(BY_NC_SA);
		List<License> resultingLicenses = removeNotCompatibleShareAlike(shareAlikeLicense, licenses, false);

		Assert.assertTrue("BY", resultingLicenses.contains(BY));
		Assert.assertFalse("not BY NC", resultingLicenses.contains(BY_NC));
		Assert.assertFalse("not BY NC SA", resultingLicenses.contains(BY_NC_SA));

		// Test meta attributes

		shareAlikeLicense = BY_NC_SA;
		resultingLicenses = removeNotCompatibleShareAlike(shareAlikeLicense, licenses, true);
		Assert.assertFalse("not BY", resultingLicenses.contains(BY));
		Assert.assertFalse("not BY NC", resultingLicenses.contains(BY_NC));
		Assert.assertTrue("BY NC SA", resultingLicenses.contains(BY_NC_SA));
	}

	@Test
	public void testCcSaVersions() {

		// Create new CC SA with lower version
		Attributes attributes = new Attributes();
		for (Attribute attribute : BY_SA.getAttributes().getList()) {
			attributes.addAttribute(AttributeFactory.get().createAttribute(attribute, true));
		}
		License license = new License().setName("by-sa-3").setUri("http://creativecommons.org/licenses/by-sa/3.0/")
				.setAttributes(attributes);

		Assert.assertFalse(BY_SA.getUri().equals(license.getUri()));

		List<License> licenses = new LinkedList<>();
		licenses.add(BY_SA);
		licenses.add(license);

		List<License> resultingLicenses = removeNotCompatibleShareAlike(license, licenses, false);
		Assert.assertTrue("BY SA 3", resultingLicenses.contains(license));
		Assert.assertFalse("not BY SA 4", resultingLicenses.contains(BY_SA));

		resultingLicenses = removeNotCompatibleShareAlike(BY_SA, licenses, false);
		Assert.assertTrue("BY SA 3", resultingLicenses.contains(license));
		Assert.assertTrue("BY SA 4", resultingLicenses.contains(BY_SA));
	}

}