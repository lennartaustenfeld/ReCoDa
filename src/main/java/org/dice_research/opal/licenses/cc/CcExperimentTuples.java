package org.dice_research.opal.licenses.cc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.licenses.Attributes;
import org.dice_research.opal.licenses.BackMapping;
import org.dice_research.opal.licenses.Execution;
import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;
import org.dice_research.opal.licenses.utils.ArrayUtil;
import org.dice_research.opal.licenses.utils.Cfg;

/**
 * Experiment to evaluate the compatibility results of CC license pairs.
 * 
 * @author Adrian Wilke
 */
public class CcExperimentTuples {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) throws Exception {
		CcExperimentTuples experiment = new CcExperimentTuples();
		experiment.loadData(Cfg.getCcLicenseRdf());
		experiment.execute();
		experiment.printSpecialCases();
	}

	private KnowledgeBase knowledgeBase;

	/**
	 * Prints special cases for investigation: Why can't 'CC0' be used for
	 * 'sampling+'?
	 * 
	 * Only difference between 'sampling' and 'sampling+': Permission Sharing is set
	 * in 'sampling+'.
	 */
	public void printSpecialCases() {
		String licenseUri;
		License license;

		licenseUri = "http://creativecommons.org/licenses/by/3.0/";
		license = knowledgeBase.getLicense(licenseUri);
		System.out.println(ArrayUtil.intString(license.getAttributes().getValuesArray()) + " " + license);

		licenseUri = "http://creativecommons.org/licenses/sampling/1.0/";
		license = knowledgeBase.getLicense(licenseUri);
		System.out.println(ArrayUtil.intString(license.getAttributes().getValuesArray()) + " " + license);

		System.out.println();

		licenseUri = "http://creativecommons.org/publicdomain/zero/1.0/";
		license = knowledgeBase.getLicense(licenseUri);
		System.out.println(ArrayUtil.intString(license.getAttributes().getValuesArray()) + " " + license);

		licenseUri = "http://creativecommons.org/licenses/publicdomain/";
		license = knowledgeBase.getLicense(licenseUri);
		System.out.println(ArrayUtil.intString(license.getAttributes().getValuesArray()) + " " + license);

		licenseUri = "http://creativecommons.org/publicdomain/mark/1.0/";
		license = knowledgeBase.getLicense(licenseUri);
		System.out.println(ArrayUtil.intString(license.getAttributes().getValuesArray()) + " " + license);

		System.out.println();

		licenseUri = "http://creativecommons.org/licenses/sampling+/1.0/";
		license = knowledgeBase.getLicense(licenseUri);
		System.out.println(ArrayUtil.intString(license.getAttributes().getValuesArray()) + " " + license);

		licenseUri = "http://creativecommons.org/licenses/sampling+/1.0/de/";
		license = knowledgeBase.getLicense(licenseUri);
		System.out.println(ArrayUtil.intString(license.getAttributes().getValuesArray()) + " " + license);

		licenseUri = "http://creativecommons.org/licenses/sampling+/1.0/tw/";
		license = knowledgeBase.getLicense(licenseUri);
		System.out.println(ArrayUtil.intString(license.getAttributes().getValuesArray()) + " " + license);

		licenseUri = "http://creativecommons.org/licenses/sampling+/1.0/br/";
		license = knowledgeBase.getLicense(licenseUri);
		System.out.println(ArrayUtil.intString(license.getAttributes().getValuesArray()) + " " + license);

		System.out.println(Arrays.toString(knowledgeBase.getSortedAttributes().getShortFormArray()));
		System.out.println(knowledgeBase.toLines());
	}

	public CcExperimentTuples loadData(String dataDirectory) throws IOException {

		// Check availability of data
		if (!new File(dataDirectory).exists()) {
			LOGGER.error("Directory not found: " + new File(dataDirectory).getAbsolutePath());
		}

		// Get data
		CcData data = new CcData().setSourceDirectory(dataDirectory).readDirectory();
		List<File> files = data.getAllRdfFiles();
		knowledgeBase = data.createKnowledgeBase(files);

		LOGGER.info("Created knowledge base, number of licenses: " + knowledgeBase.getLicenses().size());
		return this;
	}

	public CcExperimentTuples execute() throws IOException {
		int progressCounter = 0;
		List<ResultContainer> results = new LinkedList<>();
		for (License licenseA : knowledgeBase.getLicenses()) {
			for (License licenseB : knowledgeBase.getLicenses()) {

				// Only compare pairs once.
				if (licenseA.getUri().compareTo(licenseB.getUri()) > 0) {
					continue;
				}

				List<License> inputLicenses = new ArrayList<>(2);
				inputLicenses.add(licenseA);
				inputLicenses.add(licenseB);

				// Operator used to compute array of internal values
				Execution execution = new Execution().setKnowledgeBase(knowledgeBase);
				Attributes resultAttributes = execution.applyOperator(inputLicenses);

				// Back-mapping
				Set<License> resultingLicenses = new BackMapping().getCompatibleLicenses(inputLicenses,
						resultAttributes, knowledgeBase);

				results.add(new ResultContainer(licenseA, licenseB, resultingLicenses));
			}

			// Print progress
			progressCounter++;
			if (progressCounter % 25 == 0) {
				LOGGER.info(progressCounter + " / " + knowledgeBase.getLicenses().size());
			}
		}

		// Collect number of compatible licenses
		Map<Integer, List<ResultContainer>> resultStats = new TreeMap<>();
		for (ResultContainer result : results) {
			int size = result.resultingLicenses.size();
			if (!resultStats.containsKey(size)) {
				resultStats.put(size, new LinkedList<>());
			}
			resultStats.get(size).add(result);
		}

		// Print number of results (pairs)
		System.out.println("Results:" + results.size());
		// Print overview: No. of compatible licenses ; no. of pairs
		for (Entry<Integer, List<ResultContainer>> entry : resultStats.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue().size());
		}
		// Print license URIs for small result sets
		for (Entry<Integer, List<ResultContainer>> entry : resultStats.entrySet()) {
			if (entry.getValue().size() < 50) {
				for (ResultContainer resultContainer : entry.getValue()) {
					System.out.println(resultContainer);
				}
			}
		}
		// Print non-compatible license URIs
		for (Entry<Integer, List<ResultContainer>> entry : resultStats.entrySet()) {
			if (entry.getKey() > 600) {
				for (ResultContainer resultContainer : entry.getValue()) {
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append(entry.getKey());
					stringBuilder.append("*");
					stringBuilder.append("\t");
					stringBuilder.append(resultContainer.licenseA.getUri());
					stringBuilder.append("\t");
					stringBuilder.append(resultContainer.licenseB.getUri());
					stringBuilder.append("\t");
					List<License> nonCompatible = knowledgeBase.getLicenses();
					nonCompatible.removeAll(resultContainer.resultingLicenses);
					stringBuilder.append(nonCompatible.toString());
					System.out.println(stringBuilder.toString());
				}
			}
		}

		return this;
	}

	public class ResultContainer {
		public License licenseA;
		public License licenseB;
		public Set<License> resultingLicenses;

		public ResultContainer(License licenseA, License licenseB, Set<License> resultingLicenses) {
			this.licenseA = licenseA;
			this.licenseB = licenseB;
			this.resultingLicenses = resultingLicenses;
		}

		@Override
		public String toString() {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(resultingLicenses.size());
			stringBuilder.append("\t");
			stringBuilder.append(licenseA.getUri());
			stringBuilder.append("\t");
			stringBuilder.append(licenseB.getUri());
			stringBuilder.append("\t");
			stringBuilder.append(resultingLicenses);
			return stringBuilder.toString();
		}
	}
}