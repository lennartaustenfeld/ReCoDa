package org.dice_research.opal.licenses.utils;

/**
 * Configuration values via system properties.
 * 
 * Usage: To set a system property using the command line, use
 * '-Dproperty=value'.
 *
 * @see https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html
 *
 * @author Adrian Wilke
 */
public abstract class Cfg {

	public static final String KEY_CC_LICENSERDF = "cc.licenserdf";

	/**
	 * Gets value for system property key.
	 * 
	 * @throws RuntimeException if system property key not found
	 */
	public static final String get(String key) throws Exception {
		String value = System.getProperty(key);
		if (value == null) {
			throw new Exception("Could not find system property " + key);
		}
		return value;
	}

	/**
	 * Gets directory of CC.LicenseRDF.
	 * 
	 * @throws RuntimeException if system property key not found
	 */
	public static final String getCcLicenseRdf() throws Exception {
		return get(KEY_CC_LICENSERDF);
	}

}