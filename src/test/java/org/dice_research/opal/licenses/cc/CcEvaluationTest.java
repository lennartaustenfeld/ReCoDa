package org.dice_research.opal.licenses.cc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dice_research.opal.licenses.Attributes;
import org.dice_research.opal.licenses.BackMapping;
import org.dice_research.opal.licenses.Execution;
import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;
import org.dice_research.opal.licenses.utils.ArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests Creative Commons license compatibility.
 * 
 * @see ../package-info.java
 * 
 * @see https://wiki.creativecommons.org/index.php?title=Wiki/cc_license_compatibility&oldid=70058
 * 
 * @author Adrian Wilke
 */
public class CcEvaluationTest {

	public KnowledgeBase knowledgeBase;
	public CcMatrix matrix;

	/**
	 * Checks data directory. Creates objects.
	 */
	@Before
	public void setUp() {
		knowledgeBase = CcTestUtils.getKnowledgeBase(CcTestUtils.getCcData());
		matrix = new CcMatrix();
	}

	/**
	 * Tests if CC matrix results are computed correctly.
	 */
	@Test
	public void testCreativeCommonsCompatibility() {
		boolean status = true;
		StringBuilder stringBuilder = new StringBuilder();

		// Combine licenses to check every cell in matrix
		for (License licenseA : knowledgeBase.getLicenses()) {

			for (License licenseB : knowledgeBase.getLicenses()) {

				List<License> inputLicenses = new ArrayList<>(2);
				inputLicenses.add(licenseA);
				inputLicenses.add(licenseB);

				// Operator used to compute array of internal values
				Execution execution = new Execution().setKnowledgeBase(knowledgeBase);
				Attributes resultAttributes = execution.applyOperator(inputLicenses);

				// Back-mapping
				List<License> resultingLicenses = new BackMapping().getCompatibleLicensesList(inputLicenses,
						resultAttributes, knowledgeBase);

				// Check license combination and update result status
				status = status & checkResults(licenseA, licenseB, resultingLicenses, matrix, stringBuilder);
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

	/**
	 * Checks single license. Used in {@link #testCreativeCommonsCompatibility()}.
	 */
	public static boolean checkResults(License licenseA, License licenseB, List<License> resultingLicenses,
			CcMatrix matrix, StringBuilder stringBuilder) {
		boolean status = true;
		List<String> resultingUris = resultingLicenses.stream().map(l -> l.getUri()).collect(Collectors.toList());
		boolean matrixValue = matrix.getBoolean(licenseA.getUri(), licenseB.getUri());

		// From wiki: 'Use at least the most restrictive licensing of the two'
		License mostRestrictive = licenseA;
		if (matrix.getUris().indexOf(licenseB.getUri()) > matrix.getUris().indexOf(licenseA.getUri())) {
			mostRestrictive = licenseB;
		}

		// Check result and add debugging information if test failed
		if (matrixValue && !resultingUris.contains(mostRestrictive.getUri())) {
			stringBuilder.append("Missing: ");
			stringBuilder.append(ArrayUtil.intString(mostRestrictive.getAttributes().getValuesArray()));
			stringBuilder.append(" ");
			stringBuilder.append(ArrayUtil.intString(mostRestrictive.getAttributes().getInternalValuesArray()));
			stringBuilder.append(" ");
			stringBuilder.append(mostRestrictive.getUri());
			stringBuilder.append(System.lineSeparator());
			status = false;
		} else if (!matrixValue && resultingUris.contains(mostRestrictive.getUri())) {
			stringBuilder.append("Wrong:   ");
			stringBuilder.append(ArrayUtil.intString(mostRestrictive.getAttributes().getValuesArray()));
			stringBuilder.append(" ");
			stringBuilder.append(ArrayUtil.intString(mostRestrictive.getAttributes().getInternalValuesArray()));
			stringBuilder.append(" ");
			stringBuilder.append(mostRestrictive.getUri());
			stringBuilder.append(System.lineSeparator());
			status = false;
		}

		// Add debugging information if test failed
		if (!status) {
			stringBuilder.append("Checked: ");
			stringBuilder.append(ArrayUtil.intString(licenseA.getAttributes().getValuesArray()));
			stringBuilder.append(" ");
			stringBuilder.append(ArrayUtil.intString(licenseA.getAttributes().getInternalValuesArray()));
			stringBuilder.append(" ");
			stringBuilder.append(licenseA.toString());
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append("         ");
			stringBuilder.append(ArrayUtil.intString(licenseB.getAttributes().getValuesArray()));
			stringBuilder.append(" ");
			stringBuilder.append(ArrayUtil.intString(licenseB.getAttributes().getInternalValuesArray()));
			stringBuilder.append(" ");
			stringBuilder.append(licenseB.toString());
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append(System.lineSeparator());
		}

		return status;
	}

	/**
	 * Tests if URIs from matrix and from RDF files are equal.
	 */
	@Test
	public void testEqualLicenseUris() {
		Assert.assertTrue(matrix.getUris().containsAll(knowledgeBase.getLicenseUris()));
		Assert.assertTrue(knowledgeBase.getLicenseUris().containsAll(matrix.getUris()));
	}
}