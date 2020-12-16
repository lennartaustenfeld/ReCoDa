package org.dice_research.opal.licenses.cc;

import java.io.IOException;

import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link CcData}.
 * 
 * @see ../package-info.java
 * 
 * @author Adrian Wilke
 */
public class CcDataTest {

	public static final boolean PRINT_KNOWLEDGEBASE = false;

	// Two files in the official data contain errors
	public static final int NUMBER_OF_LICENSE_FILES = 614 - 2;

	private CcData ccData;
	private KnowledgeBase knowledgeBase;

	/**
	 * Checks data directory. Creates objects.
	 */
	@Before
	public void setUp() {
		ccData = CcTestUtils.getCcData();
		knowledgeBase = CcTestUtils.getKnowledgeBase(ccData);
	}

	@Test
	public void testCcData() throws IOException {
		Assert.assertEquals(NUMBER_OF_LICENSE_FILES, ccData.readDirectory().getAllRdfFiles().size());
	}

	@Test
	public void testKnowledgeBase() {

		for (License license : knowledgeBase.getLicenses()) {
			Assert.assertEquals(license.toString(), license.getAttributes().getAttribute(CcData.SHARE_ALIKE).getValue(),
					license.isAttribueEquality());
		}

		Assert.assertEquals(8, knowledgeBase.getLicenses().size());

		Assert.assertEquals(7, knowledgeBase.getSortedAttributes().getList().size());

		if (PRINT_KNOWLEDGEBASE) {
			System.out.println(knowledgeBase.toLines());
		}

	}

}