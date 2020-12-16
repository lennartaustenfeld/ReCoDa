package org.dice_research.opal.licenses.cc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.licenses.Attribute;
import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;
import org.dice_research.opal.licenses.Permission;
import org.dice_research.opal.licenses.Prohibition;
import org.dice_research.opal.licenses.Requirement;
import org.dice_research.opal.licenses.utils.Cfg;
import org.dice_research.opal.licenses.utils.LineStorage;

/**
 * Creates Creative Commons knowledge base.
 * 
 * <p>
 * Data: To get the underlying data, clone
 * https://github.com/projekt-opal/cc.licenserdf (or
 * https://github.com/creativecommons/cc.licenserdf).
 * </p>
 *
 * <p>
 * After cloning, specify the data directory with
 * {@link #setSourceDirectory(String)}. Get the files related to 'License
 * Compatibility Chart' using {@link #getMatixFiles()}. Create the knowledge
 * base using {@link #createKnowledgeBase(List)}.
 * </p>
 * 
 * <p>
 * Additionally: Get a list of all RDF files using {@link #getAllRdfFiles()}.
 * Get a list of non-replaced lices files using {@link #getNonReplacedFiles()}.
 * </p>
 *
 * @author Adrian Wilke
 */
public class CcData {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final String CC_NS = "http://creativecommons.org/ns#";
	public static final Resource LICENSE = ResourceFactory.createResource(CC_NS + "License");
	public static final Property PROP_PROHIBITS = ResourceFactory.createProperty(CC_NS + "prohibits");
	public static final Property PROP_REQUIRES = ResourceFactory.createProperty(CC_NS + "requires");
	public static final Property PROP_PERMITS = ResourceFactory.createProperty(CC_NS + "permits");
	public static final Property PROP_REPLACED_BY = DCTerms.isReplacedBy;
	public static final Property PROP_TITLE = DCTerms.title;
	public static final Property PROP_ID = DC.identifier;

	public static final String DERIVATIVE_WORKS = "http://creativecommons.org/ns#DerivativeWorks";
	public static final String SHARE_ALIKE = "http://creativecommons.org/ns#ShareAlike";

	private String sourceDirectory;
	private static List<File> allRdfFiles;

	/**
	 * Test run.
	 */
	public static void main(String[] args) throws Exception {

		CcData cc = new CcData().setSourceDirectory(Cfg.getCcLicenseRdf()).readDirectory();

		List<File> rdfFiles = cc.getMatixFiles();
		for (File file : rdfFiles) {
			System.out.println(file);
		}
		System.out.println();

		KnowledgeBase knowledgeBase = cc.createKnowledgeBase(rdfFiles);
		System.out.println(knowledgeBase.getSortedAttributes().toLines());
		for (License license : knowledgeBase.getLicenses()) {
			System.out.println(license);
			System.out.println(Arrays.toString(license.getAttributes().getValuesArray()));
		}
	}

	/**
	 * Creates knowledge base including data of all files.
	 */
	public KnowledgeBase createKnowledgeBase(List<File> files) {
		for (File file : files) {
			if (!file.exists()) {
				throw new RuntimeException("File not found: " + file);
			}
		}

		KnowledgeBase knowledgeBase = new KnowledgeBase();

		// Get attributes sorted by type and URI. Add them to knowledge base.

		Set<String> permissions = new TreeSet<>();
		Set<String> prohibitions = new TreeSet<>();
		Set<String> requirements = new TreeSet<>();
		for (File file : files) {
			extractAttributes(file, permissions, prohibitions, requirements);
		}
		for (String attribute : permissions) {

			Attribute permission = new Permission().setUri(attribute);

			// Special case: http://creativecommons.org/ns#DerivativeWorks (Permission)
			if (permission.getUri().equals(DERIVATIVE_WORKS)) {
				permission.setTypePermissionOfDerivates(true);
			}

			knowledgeBase.addAttribute(permission);

		}
		for (String attribute : prohibitions) {
			knowledgeBase.addAttribute(new Prohibition().setUri(attribute));
		}
		for (String attribute : requirements) {

			Attribute requirement = new Requirement().setUri(attribute);

			// Special case: http://creativecommons.org/ns#ShareAlike (Requirement)
			if (requirement.getUri().equals(SHARE_ALIKE)) {
				requirement.setTypeAttribueEquality(true);
			}

			knowledgeBase.addAttribute(requirement);
		}

		// Add licenses

		for (File file : files) {
			addLicense(file, knowledgeBase);
		}

		return knowledgeBase;
	}

	/**
	 * Adds license to knowledge base.
	 */
	private void addLicense(File file, KnowledgeBase knowledgeBase) {
		StmtIterator stmtIterator;
		Model model = RDFDataMgr.loadModel(file.toURI().toString(), Lang.RDFXML);

		ResIterator licensesIt = model.listResourcesWithProperty(RDF.type, LICENSE);
		while (licensesIt.hasNext()) {
			License license = new License();

			// URI

			Resource licenseResource = licensesIt.next();
			license.setUri(licenseResource.getURI());

			// Name

			stmtIterator = licenseResource.listProperties(PROP_ID);
			if (stmtIterator.hasNext()) {
				license.setName(stmtIterator.next().getObject().toString());
			}
			if (stmtIterator.hasNext()) {
				throw new RuntimeException(
						"Multiple IDs " + license.getName() + " " + stmtIterator.next() + " " + file.getAbsolutePath());
			}

			// Attributes

			Set<String> permissions = new TreeSet<>();
			Set<String> prohibitions = new TreeSet<>();
			Set<String> requirements = new TreeSet<>();

			// Collect triple information

			stmtIterator = licenseResource.listProperties(PROP_PERMITS);
			while (stmtIterator.hasNext()) {
				permissions.add(stmtIterator.next().getObject().asResource().getURI());
			}

			stmtIterator = licenseResource.listProperties(PROP_PROHIBITS);
			while (stmtIterator.hasNext()) {
				prohibitions.add(stmtIterator.next().getObject().asResource().getURI());
			}

			stmtIterator = licenseResource.listProperties(PROP_REQUIRES);
			while (stmtIterator.hasNext()) {
				requirements.add(stmtIterator.next().getObject().asResource().getURI());
			}

			// Add value true/false

			for (Attribute attribute : knowledgeBase.getSortedAttributes().getList()) {

				if (attribute instanceof Permission) {

					Attribute permission = new Permission().setUri(attribute.getUri())
							.setValue(permissions.contains(attribute.getUri()));

					// Special case: http://creativecommons.org/ns#DerivativeWorks (Permission)
					if (attribute.getUri().equals(DERIVATIVE_WORKS)) {
						permission.setTypePermissionOfDerivates(true);
					}

					license.getAttributes().addAttribute(permission);

				}

				else if (attribute instanceof Prohibition) {
					license.getAttributes().addAttribute(new Prohibition().setUri(attribute.getUri())
							.setValue(prohibitions.contains(attribute.getUri())));
				}

				else if (attribute instanceof Requirement) {

					Attribute requirement = new Requirement().setUri(attribute.getUri())
							.setValue(requirements.contains(attribute.getUri()));

					// Special case: http://creativecommons.org/ns#ShareAlike (Requirement)
					if (attribute.getUri().equals(SHARE_ALIKE)) {
						requirement.setTypeAttribueEquality(true);
					}

					license.getAttributes().addAttribute(requirement);

				}

				else {
					throw new RuntimeException("Unknown type of attribute");
				}

			}

			// Check duplicate license URIs
			if (knowledgeBase.getLicenseUris().contains(license.getUri())) {
				LOGGER.warn("URI already contained: " + license.getUri() + " " + file.getAbsolutePath());
			}

			knowledgeBase.addLicense(license);
		}
	}

	/**
	 * Extracts attribute URIs from files.
	 */
	private void extractAttributes(File file, Set<String> permissions, Set<String> prohibitions,
			Set<String> requirements) {
		StmtIterator stmtIterator;
		Model model = RDFDataMgr.loadModel(file.toURI().toString(), Lang.RDFXML);

		ResIterator licensesIt = model.listResourcesWithProperty(RDF.type, LICENSE);
		while (licensesIt.hasNext()) {
			Resource licenseResource = licensesIt.next();

			stmtIterator = licenseResource.listProperties(PROP_PERMITS);
			while (stmtIterator.hasNext()) {
				permissions.add(stmtIterator.next().getObject().asResource().getURI());
			}

			stmtIterator = licenseResource.listProperties(PROP_PROHIBITS);
			while (stmtIterator.hasNext()) {
				prohibitions.add(stmtIterator.next().getObject().asResource().getURI());
			}

			stmtIterator = licenseResource.listProperties(PROP_REQUIRES);
			while (stmtIterator.hasNext()) {
				requirements.add(stmtIterator.next().getObject().asResource().getURI());
			}
		}
	}

	/**
	 * Gets licenses of 'License Compatibility Chart'
	 * 
	 * @see https://wiki.creativecommons.org/index.php?title=Wiki/cc_license_compatibility&oldid=70058
	 */
	public List<File> getMatixFiles() {
		List<File> files = new LinkedList<>();
		files.add(new File(sourceDirectory, "creativecommons.org_publicdomain_mark_1.0_.rdf"));
		files.add(new File(sourceDirectory, "creativecommons.org_publicdomain_zero_1.0_.rdf"));
		files.add(new File(sourceDirectory, "creativecommons.org_licenses_by_4.0_.rdf"));
		files.add(new File(sourceDirectory, "creativecommons.org_licenses_by-sa_4.0_.rdf"));
		files.add(new File(sourceDirectory, "creativecommons.org_licenses_by-nc_4.0_.rdf"));
		files.add(new File(sourceDirectory, "creativecommons.org_licenses_by-nd_4.0_.rdf"));
		files.add(new File(sourceDirectory, "creativecommons.org_licenses_by-nc-sa_4.0_.rdf"));
		files.add(new File(sourceDirectory, "creativecommons.org_licenses_by-nc-nd_4.0_.rdf"));
		return files;
	}

	/**
	 * Filters records not having {@link #PROP_REPLACED_BY} from all RDF files.
	 */
	public List<File> getNonReplacedFiles() {

		// Cache
		String storageId = "cc-non-replaced";
		if (LineStorage.exists(storageId)) {
			return LineStorage.read(storageId).stream()
					.collect(Collectors.mapping(l -> new File(l), Collectors.toList()));
		}

		// Filter
		List<File> files = new LinkedList<>();
		for (File file : getAllRdfFiles()) {
			Model model = RDFDataMgr.loadModel(file.toURI().toString(), Lang.RDFXML);

			ResIterator licensesIt = model.listResourcesWithProperty(RDF.type, LICENSE);
			while (licensesIt.hasNext()) {
				Resource licenseResource = licensesIt.next();

				StmtIterator idIt = licenseResource.listProperties(PROP_REPLACED_BY);
				if (idIt.hasNext()) {
					continue;
				} else {
					files.add(file);
				}
			}
		}

		// Cache
		LineStorage.write(storageId,
				files.stream().map(f -> Paths.get(f.getAbsolutePath()).toString()).collect(Collectors.toList()));

		return files;
	}

	/**
	 * Gets RDF files in directory.
	 */
	public List<File> getAllRdfFiles() {
		if (allRdfFiles == null) {
			return new ArrayList<>(0);
		} else {
			return allRdfFiles;
		}
	}

	/**
	 * Gets RDF files in directory.
	 */
	public CcData readDirectory() throws IOException {

		if (sourceDirectory == null) {
			throw new NullPointerException("Source directory not set.");
		}

		File directory = new File(sourceDirectory);
		if (!directory.canRead()) {
			throw new IOException("Can not read " + directory);
		}

		allRdfFiles = Files.list(Paths.get(directory.toURI())).map(p -> p.toFile())
				.filter(f -> f.getName().endsWith("rdf"))

				// Can be removed, if official cc.licenserdf repository is bugfixed
				.filter(f -> !f.getName().equals("creativecommons.org_licenses_by-nc-sa_3.0_za_.rdf"))
				.filter(f -> !f.getName().equals("creativecommons.org_licenses_by-nc_3.0_za_.rdf"))

				.collect(Collectors.toList());

		return this;
	}

	/**
	 * Sets directory with CC RDF files.
	 */
	public CcData setSourceDirectory(String sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
		return this;
	}

}