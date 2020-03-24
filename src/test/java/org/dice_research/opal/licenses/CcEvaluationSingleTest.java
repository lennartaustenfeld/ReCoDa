package org.dice_research.opal.licenses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.dice_research.opal.licenses.cc.CcMatrix;
import org.dice_research.opal.licenses.utils.ArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests single pair of Creative Commons license compatibility.
 * 
 * @see https://wiki.creativecommons.org/index.php?title=Wiki/cc_license_compatibility&oldid=70058
 * 
 * @author Adrian Wilke
 */
public class CcEvaluationSingleTest {

	public KnowledgeBase knowledgeBase;
	public CcMatrix matrix;

	/**
	 * Checks data directory. Creates objects.
	 */
	@Before
	public void setUp() {
		knowledgeBase = CcTestUtils.getKnowledgeBase();
		matrix = new CcMatrix();
	}

	/**
	 * Tests if CC matrix results are computed correctly.
	 */
	@Test
	public void testCreativeCommonsCompatibility() {
		boolean status = true;

		String licenseUriA = CcTestUtils.BY_SA;
		String licenseUriB = CcTestUtils.BY_NC;
		System.out.println(licenseUriA);
		System.out.println(
				Arrays.toString(knowledgeBase.getLicense(licenseUriA).getAttributes().getInternalValuesArray()));
		System.out.println(licenseUriB);
		System.out.println(
				Arrays.toString(knowledgeBase.getLicense(licenseUriB).getAttributes().getInternalValuesArray()));
		System.out.println();

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
//				boolean[] result = resultAttributes.getInternalValuesArray();O

				// Back-mapping
				resultingLicenses = new BackMapping().getCompatibleLicenses(inputLicenses, resultAttributes,
						knowledgeBase);

				// Check license combination and update result status
				status = status & checkResults(licenseA, licenseB, resultingLicenses, stringBuilder);
			}
		}

		// Print
		System.out.println("Resulting licenses: ");
		for (License resultingLicense : resultingLicenses) {
			System.out.println(resultingLicense);
		}
		System.out.println();
		System.out.println();

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
	private boolean checkResults(License licenseA, License licenseB, List<License> resultingLicenses,
			StringBuilder stringBuilder) {
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