package org.dice_research.opal.licenses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dice_research.opal.licenses.edplcm.EdpLcmKnowledgeBase;
import org.dice_research.opal.licenses.edplcm.EdpLcmUris;
import org.dice_research.opal.licenses.edplcm.EpdLcmDerivates;
import org.dice_research.opal.licenses.utils.F1Score;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link EpdLcmDerivates} and {@link EdpLcmKnowledgeBase}.
 * 
 * @see package-info.java
 * 
 * @author Adrian Wilke
 */
public class EdpLcmEvaluationSingleTest {

	private EpdLcmDerivates derivates;
	private EdpLcmKnowledgeBase knowledgeBase;

	@Before
	public void setUp() throws Exception {
		derivates = new EpdLcmDerivates();
		knowledgeBase = new EdpLcmKnowledgeBase().load();
	}

	/**
	 * Difference in PatentGrant (Permission) and Sublicensing (Permission).
	 * 
	 * Is Share-Alike (Meta-Requirement) is also set.
	 */
	@Test
	public void testDifferentPermissions() throws IOException {
		List<String> uris = new LinkedList<>();
		uris.add(EdpLcmUris.ODC_BY);
		uris.add(EdpLcmUris.EUPL_1_1);
		check(uris);
	}

	/**
	 * Case also worked in CC evaluation.
	 */
	@Test
	public void testWorking() throws IOException {
		List<String> uris = new LinkedList<>();
		uris.add(EdpLcmUris.CC0_1_0);
		uris.add(EdpLcmUris.CC_BY_4_0);
		check(uris);
	}

	public void check(List<String> licenseUris) throws IOException {
		boolean status = true;
		int errorCounter = 0;
		int successCounter = 0;
		StringBuilder stringBuilder = new StringBuilder();
		F1Score f1Score = new F1Score();

		// Combine licenses to check every cell in matrix
		for (License licenseA : knowledgeBase.getLicenses()) {

			if (!licenseUris.contains(licenseA.getUri()))
				continue;

			for (License licenseB : knowledgeBase.getLicenses()) {

				if (!licenseUris.contains(licenseB.getUri()))
					continue;

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
				boolean result = EdpLcmEvaluationTest.checkResults(licenseA, licenseB, resultingLicenses, derivates,
						stringBuilder, f1Score, null);
				if (result) {
					successCounter++;
				} else {
					errorCounter++;
				}
				status = status & result;
			}
		}

		// Print debugging info, if test failed
		if (!status) {
			stringBuilder.append("Success: ");
			stringBuilder.append(successCounter);
			stringBuilder.append("  ");
			stringBuilder.append(1.0 * successCounter / (successCounter + errorCounter));
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append("Errors:  ");
			stringBuilder.append(errorCounter);
			stringBuilder.append("  ");
			stringBuilder.append(1.0 * errorCounter / (successCounter + errorCounter));
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append("Expected compatibility results:");
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append(derivates);
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append("KnowledgeBase attributes:");
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append(knowledgeBase.toLines());
			System.out.println(stringBuilder.toString());
		}
		Assert.assertTrue("EDP LCM compatibility", status);
	}

}