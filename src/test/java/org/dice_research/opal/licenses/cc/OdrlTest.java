package org.dice_research.opal.licenses.cc;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.dice_research.opal.licenses.Attribute;
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
		kbImported = new Odrl(true).importModel(new Odrl(true).export(kbOriginal));
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

	@Test
	public void testAttributesSize() {
		Assert.assertEquals("Same number of attributes", kbOriginal.getSortedAttributes().getList().size(),
				kbImported.getSortedAttributes().getList().size());
	}

	@Test
	public void testAttributesValues() {
		for (License licOriginal : kbOriginal.getLicenses()) {
			License licImported = kbImported.getLicense(licOriginal.getUri());
			Assert.assertNotNull("License imported" + licImported);

			for (Entry<String, Attribute> originalAttributeEntry : licOriginal.getAttributes().getUriToAttributeMap()
					.entrySet()) {
				String originalAttributeUri = originalAttributeEntry.getKey();
				Attribute originalAttribute = originalAttributeEntry.getValue();
				Attribute importedAttribute = licImported.getAttributes().getAttribute(originalAttributeUri);
				Assert.assertNotNull("Attribute available " + licImported.getUri() + " " + originalAttributeUri,
						importedAttribute);
				Assert.assertEquals("Same attribute values", originalAttribute.getValue(),
						importedAttribute.getValue());
			}

		}
	}

	@Test
	public void testMetaAttributes() {
		for (Attribute attribute : kbOriginal.getSortedAttributes().getList()) {
			boolean original = attribute.isTypePermissionOfDerivates();
			boolean imported = kbImported.getSortedAttributes().getAttribute(attribute.getUri())
					.isTypePermissionOfDerivates();
			Assert.assertEquals("TypePermissionOfDerivates", original, imported);

			original = attribute.isMetaAttribute();
			imported = kbImported.getSortedAttributes().getAttribute(attribute.getUri()).isMetaAttribute();
			Assert.assertEquals("isMetaAttribute", original, imported);

			original = attribute.isTypeAttribueEquality();
			imported = kbImported.getSortedAttributes().getAttribute(attribute.getUri()).isTypeAttribueEquality();
			Assert.assertEquals("TypeAttribueEquality", original, imported);
		}
	}

	@Test
	public void testWriting() {
		File file = null;
		try {
			file = File.createTempFile(getClass().getName(), ".ttl");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		file.deleteOnExit();

		new Odrl(true).export(kbOriginal, file, Lang.TURTLE);
		KnowledgeBase kbFileImport = new Odrl(true).importFile(file, Lang.TURTLE);

		Assert.assertEquals(kbOriginal.getLicenses().size(), kbFileImport.getLicenses().size());
		Assert.assertEquals(kbOriginal.getSortedAttributes().getList().size(),
				kbFileImport.getSortedAttributes().getList().size());

		// Human test
		if (Boolean.FALSE) {
			System.out.println(getClass().getName());
			System.out.println();
			System.out.println(kbOriginal.toLines());
			System.out.println(kbFileImport.toLines());
		}
	}

}