package org.dice_research.opal.licenses.edplcm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.licenses.Attribute;
import org.dice_research.opal.licenses.AttributeFactory;
import org.dice_research.opal.licenses.Attributes;
import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;

/**
 * EDP License Compatibility Matrix - Knowledge base.
 * 
 * Does not include attribute 'Sublicensing' as it contains a 'N.A.' value.
 * 
 * Usage: Use {@link #getSortedAttributes()} or {@link #getUrisToLicenses()}.
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
	public static final String URI_PREFIX = "http://example.org/";

	public static final String ATTRIBUTE_ID_SUBLICENSING = attributeIdToUri("Sublicensing");
	public static final String ATTRIBUTE_ID_DERIVATES = attributeIdToUri("Derivative Works");
	public static final String ATTRIBUTE_ID_ALIKE = attributeIdToUri("Share Alike");

	protected boolean isLoaded = false;

	/**
	 * Skip column
	 */
	public boolean skipSublicensing = false;

	/**
	 * Skip row
	 */
	public boolean skipCcPdm = true;

	@Override
	public Attributes getSortedAttributes() {
		if (!isLoaded) {
			try {
				load();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return super.getSortedAttributes();
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

					Attribute attribute = AttributeFactory.get().createAttribute(csvRecord.get(i),
							attributeUris.get(i));
					if (attributeUris.get(i).equals(ATTRIBUTE_ID_ALIKE)) {
						attribute.setTypeAttribueEquality(true);
					} else if (attributeUris.get(i).equals(ATTRIBUTE_ID_DERIVATES)) {
						attribute.setTypePermissionOfDerivates(true);
					}
					addAttribute(attribute);
				}
				typesParsed = true;
			}

			// Skip row
			else if (skipCcPdm && csvRecord.get(csvRecord.size() - 1).equals("CC-PDM 1.0")) {
			}

			// Values
			else {

				Attributes attributes = new Attributes();
				for (int i = 0; i < csvRecord.size() - 2; i++) {
					// Create new attribute based on KB
					if (skipSublicensing && attributeUris.get(i).equals(ATTRIBUTE_ID_SUBLICENSING)) {
						continue;
					}

					Attribute attribute = AttributeFactory.get().createAttribute(
							super.getSortedAttributes().getUriToAttributeMap().get(attributeUris.get(i)), false);

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
				attributes.sort();
				License license = new License().setUri(uri).setName(csvRecord.get(csvRecord.size() - 1))
						.setAttributes(attributes);

				addLicense(license);
			}
		}

		isLoaded = true;
		return this;
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