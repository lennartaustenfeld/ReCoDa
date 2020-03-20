package org.dice_research.opal.licenses.edplcm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.licenses.Attribute;
import org.dice_research.opal.licenses.Attributes;
import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;
import org.dice_research.opal.licenses.Permission;
import org.dice_research.opal.licenses.Prohibition;
import org.dice_research.opal.licenses.Requirement;

/**
 * EDP License Compatibility Matrix - Knowledge base.
 * 
 * Does not include attribute 'Sublicensing' as it contains a 'N.A.' value.
 * 
 * Usage: Use {@link #getAttributes()} or {@link #getUrisToLicenses()}.
 * 
 * Based on "European Data Portal Licence Compatibility Matrix" and sheet
 * "Licence Descriptions".
 * 
 * Source file:
 * https://www.europeandataportal.eu/en/content/licence-assistant-european-data-portal-licence-compatibility-matrix
 * http://www.europeandataportal.eu/sites/default/files/edp-licence-compatibility-published_v1_0.xlsx
 *
 * @author Adrian Wilke
 */
public class EdpLcmKnowledgeBase extends KnowledgeBase {

	private static final Logger LOGGER = LogManager.getLogger();

	public static final String RESOURCE_CSV = "edp-licence-compatibility-matrix-licence-descriptions.csv";
	public static final String RESOURCE_TXT = "edp-licence-compatibility-matrix-share-alike.txt";
	public static final String URI_PREFIX = "http://example.org/";

	public static final String ATTRIBUTE_ID_SUBLICENSING = attributeIdToUri("Sublicensing");
	public static final String ATTRIBUTE_ID_DERIVATES = attributeIdToUri("Derivative Works");
	public static final String ATTRIBUTE_ID_ALIKE = attributeIdToUri("Share Alike");

	protected boolean isLoaded = false;
	protected boolean skipSublicensing = true;

	@Override
	public Attributes getAttributes() {
		if (!isLoaded) {
			try {
				load();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return super.getAttributes();
	}

	@Override
	public LinkedHashMap<String, License> getUrisToLicenses() {
		if (!isLoaded) {
			try {
				load();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return super.getUrisToLicenses();
	}

	public EdpLcmKnowledgeBase load() throws IOException {
		List<String> attributeUris = new LinkedList<>();
		boolean idsParsed = false;
		boolean typesParsed = false;

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(RESOURCE_CSV);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		Iterable<CSVRecord> csvRecords = CSVFormat.DEFAULT.parse(bufferedReader);
		for (CSVRecord csvRecord : csvRecords) {

			boolean derivatesAllowed = true;
			boolean shareAlike = false;

			// First line: Collect IDs
			if (!idsParsed) {
				for (int i = 0; i < csvRecord.size() - 2; i++) {
					attributeUris.add(attributeIdToUri(csvRecord.get(i)));
				}
				idsParsed = true;
			}

			// Second line: Create attributes
			else if (!typesParsed) {
				for (int i = 0; i < csvRecord.size() - 2; i++) {
					if (skipSublicensing && attributeUris.get(i).equals(ATTRIBUTE_ID_SUBLICENSING)) {
						continue;
					}
					addAttribute(createAttribute(attributeUris.get(i), csvRecord.get(i)));
				}
				typesParsed = true;
			}

			// Values
			else {
				Attributes attributes = new Attributes();
				for (int i = 0; i < csvRecord.size() - 2; i++) {
					// Create new attribute based on KB
					if (skipSublicensing && attributeUris.get(i).equals(ATTRIBUTE_ID_SUBLICENSING)) {
						continue;
					}

					if (attributeUris.get(i).equals(ATTRIBUTE_ID_DERIVATES)) {
						if (csvRecord.get(i).equals("0")) {
							derivatesAllowed = false;
						}
					}
					if (attributeUris.get(i).equals(ATTRIBUTE_ID_ALIKE)) {
						if (csvRecord.get(i).equals("1")) {
							shareAlike = true;
						}
					}

					Attribute attribute = createAttribute(
							super.getAttributes().getUriToAttributeMap().get(attributeUris.get(i)));
					try {
						addAttributeValue(attribute, csvRecord.get(i));
					} catch (IllegalArgumentException e) {
						// Ignore attribute with value '-1'
						LOGGER.warn(e);
						continue;
					}
					attributes.addAttribute(attribute);
				}

				String uri = csvRecord.get(csvRecord.size() - 2);

				License license = new License().setUri(uri).setName(csvRecord.get(csvRecord.size() - 1))
						.setAttributes(attributes);

//				if (!derivatesAllowed) {
//					license.derivatesAllowed = false;
//				}
//				if (shareAlike) {
//					license.shareAlike = true;
//				}

				addLicense(license);
			}
		}

		// Load share-alike

		inputStream = getClass().getClassLoader().getResourceAsStream(RESOURCE_TXT);
		String licenseUri = null;
		List<String> shareAlike = new LinkedList<>();
		for (String line : IOUtils.readLines(inputStream, StandardCharsets.UTF_8)) {
			if (line.trim().isEmpty()) {
				if (licenseUri != null && !shareAlike.isEmpty()) {
					putShareAlike(licenseUri, shareAlike);
				}
				licenseUri = null;
				shareAlike = new LinkedList<>();
			} else {
				if (licenseUri == null) {
					licenseUri = line;
				} else {
					shareAlike.add(line);
				}
			}
		}
		inputStream.close();

		isLoaded = true;
		return this;
	}

	/**
	 * Creates Attribute based on type.
	 * 
	 * @throws IllegalArgumentException if the given type is unknown
	 */
	protected Attribute createAttribute(String uri, String type) {
		Attribute attribute;
		if (type.equals(Permission.TYPE)) {
			attribute = new Permission();
		} else if (type.equals(Prohibition.TYPE)) {
			attribute = new Prohibition();
		} else if (type.equals(Requirement.TYPE)) {
			attribute = new Requirement();
		} else {
			throw new IllegalArgumentException("Unkown type: " + type + ", URI: " + uri);
		}
		return attribute.setUri(uri);
	}

	/**
	 * Creates new attribute instance based on given attribute.
	 * 
	 * @throws IllegalArgumentException if the given type is unknown
	 */
	protected Attribute createAttribute(Attribute attribute) {
		Attribute newAttribute;
		if (attribute instanceof Permission) {
			newAttribute = new Permission();
		} else if (attribute instanceof Prohibition) {
			newAttribute = new Prohibition();
		} else if (attribute instanceof Requirement) {
			newAttribute = new Requirement();
		} else {
			throw new IllegalArgumentException("Unkown type: " + attribute);
		}
		if (attribute.hasValue()) {
			newAttribute.setValue(attribute.getValue());
		}
		return newAttribute.setUri(attribute.getUri());
	}

	/**
	 * Checks if value is 0 or 1 and adds value to attribute.
	 * 
	 * @throws IllegalArgumentException if value could not be parsed
	 */
	protected void addAttributeValue(Attribute attribute, String value) {
		if (!value.equals("0") && !value.equals("1")) {
			// E.g. if value is -1
			throw new IllegalArgumentException("Value is " + value + ", " + attribute);
		} else {
			attribute.setValue(value.equals("0") ? false : true);
		}
	}

	/**
	 * Creates URI from id.
	 */
	public static String attributeIdToUri(String id) {
		try {
			return new URI(URI_PREFIX + id.replaceAll(" ", "-")).toString();
		} catch (URISyntaxException e) {
			LOGGER.warn(e);
			return id;
		}
	}

	/**
	 * Gets license URI for license name.
	 * 
	 * @throws IllegalArgumentException if the name was not found
	 * @throws RuntimeException         if the name exists multiple times
	 */
	public String getUriForName(String licenseName) throws IllegalArgumentException, RuntimeException {
		String uri = null;
		List<License> licenses = new LinkedList<>(getLicenses());
		int i;
		for (i = 0; i < licenses.size(); i++) {
			if (licenses.get(i).getName().equals(licenseName)) {
				uri = licenses.get(i).getUri();
				break;
			}
		}
		for (i = i + 1; i < licenses.size(); i++) {
			if (licenses.get(i).getName().equals(licenseName)) {
				throw new RuntimeException("Found duplicate name: " + licenseName);
			}
		}
		if (uri == null) {
			throw new IllegalArgumentException("Unknown name: " + licenseName);
		}
		return uri;
	}
}