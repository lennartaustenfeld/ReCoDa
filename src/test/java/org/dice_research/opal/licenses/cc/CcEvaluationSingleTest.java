package org.dice_research.opal.licenses.cc;

import java.util.ArrayList;
import java.util.List;

import org.dice_research.opal.licenses.Attributes;
import org.dice_research.opal.licenses.BackMapping;
import org.dice_research.opal.licenses.Execution;
import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests single pair of Creative Commons license compatibility.
 * 
 * @see ../package-info.java
 * 
 * @see https://wiki.creativecommons.org/index.php?title=Wiki/cc_license_compatibility&oldid=70058
 * 
 * @author Adrian Wilke
 */
public class CcEvaluationSingleTest {

	private KnowledgeBase knowledgeBase;
	private CcMatrix matrix;

	/**
	 * Checks data directory. Creates objects.
	 */
	@Before
	public void setUp() {
		knowledgeBase = CcTestUtils.getKnowledgeBase(CcTestUtils.getCcData());
		matrix = new CcMatrix();
	}

	@Test
	public void test_SA_NC() {
		String licenseUriA = CcMatrix.I3_BY_SA;
		String licenseUriB = CcMatrix.I4_BY_NC;
		check(licenseUriA, licenseUriB);
	}

	/**
	 * Tests if CC matrix results are computed correctly.
	 */
	public void check(String licenseUriA, String licenseUriB) {
		boolean status = true;

		StringBuilder stringBuilder = new StringBuilder();
		List<License> resultingLicenses = new ArrayList<>(0);

		// Combine licenses to check every cell in matrix
		for (License licenseA : knowledgeBase.getLicenses()) {

			if (!licenseA.getUri().equals(licenseUriA))
				continue;

			for (License licenseB : knowledgeBase.getLicenses()) {

				if (!licenseB.getUri().equals(licenseUriB))
					continue;

				List<License> inputLicenses = new ArrayList<>(2);
				inputLicenses.add(licenseA);
				inputLicenses.add(licenseB);

				// Operator used to compute array of internal values
				Execution execution = new Execution().setKnowledgeBase(knowledgeBase);
				Attributes resultAttributes = execution.applyOperator(inputLicenses);

				// Back-mapping
				resultingLicenses = new BackMapping().getCompatibleLicensesList(inputLicenses, resultAttributes,
						knowledgeBase);

				// Check license combination and update result status
				status = status
						& CcEvaluationTest.checkResults(licenseA, licenseB, resultingLicenses, matrix, stringBuilder);
			}
		}

		// Print debugging info, if test failed
		if (!status) {
			stringBuilder.append("Expected compatibility results:");
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append(matrix);
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append("KnowledgeBase attributes:");
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append(knowledgeBase.toLines());
			System.out.println(stringBuilder.toString());
		}
		Assert.assertTrue("Creative Commons compatibility", status);
	}

}