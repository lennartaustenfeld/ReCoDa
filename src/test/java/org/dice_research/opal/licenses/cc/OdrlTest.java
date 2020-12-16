package org.dice_research.opal.licenses.cc;

import java.util.HashSet;
import java.util.Set;

import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;
import org.dice_research.opal.licenses.utils.Odrl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link Odrl}.
 *
 * @author Adrian Wilke
 */
public class OdrlTest {

	private KnowledgeBase kbOriginal;
	private KnowledgeBase kbImported;

	@Before
	public void setUp() throws Exception {
		kbOriginal = CcTestUtils.getKnowledgeBase(CcTestUtils.getCcData());
		kbImported = new Odrl().importModel(new Odrl().export(kbOriginal));
	}

	@Test
	public void testAttributesSize() {
		Assert.assertEquals("Same number of attributes", kbOriginal.getSortedAttributes().getList().size(),
				kbImported.getSortedAttributes().getList().size());
	}

	@Test
	public void testLicenseSize() {
		Assert.assertEquals("Same number of licenses", kbOriginal.getLicenses().size(),
				kbImported.getLicenses().size());
	}

	@Test
	public void testLicenseNames() {
		Set<String> namesOriginal = new HashSet<>();
		for (License kbLicense : kbOriginal.getLicenses()) {
			namesOriginal.add(kbLicense.getName());
		}

		Set<String> namesImported = new HashSet<>();
		for (License kbLicense : kbImported.getLicenses()) {
			namesImported.add(kbLicense.getName());
		}

		Assert.assertTrue("Same license names", namesOriginal.containsAll(namesImported));
		Assert.assertTrue("Same license names", namesImported.containsAll(namesOriginal));
	}

	// TODO license attribute values (probably not stored correctly at current
	// state)

	// TODO license attribute types (including 3 default types and meta attributes)

}
