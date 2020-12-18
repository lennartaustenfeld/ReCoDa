package org.dice_research.opal.licenses.cc;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.utils.CcRel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link CcRel}.
 *
 * @author Adrian Wilke
 */
public class CcRelTest {

	private KnowledgeBase kbOriginal;
	private Set<String> attribueEqualityUris = new HashSet<>();
	private Set<String> permissionOfDerivatesUris = new HashSet<>();

	@Before
	public void setUp() throws Exception {
		kbOriginal = CcTestUtils.getKnowledgeBase(CcTestUtils.getCcData());
		attribueEqualityUris.add("http://creativecommons.org/ns#ShareAlike");
		permissionOfDerivatesUris.add("http://creativecommons.org/ns#DerivativeWorks");
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

		new CcRel(attribueEqualityUris, permissionOfDerivatesUris).export(kbOriginal, file, Lang.TURTLE);
		KnowledgeBase kbFileImport = new CcRel(attribueEqualityUris, permissionOfDerivatesUris).importFile(file,
				Lang.TURTLE);

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