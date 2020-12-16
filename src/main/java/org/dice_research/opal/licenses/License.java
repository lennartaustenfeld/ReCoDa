package org.dice_research.opal.licenses;

/**
 * License identified by its URI and related attributes.
 *
 * @author Adrian Wilke
 */
public class License {

	public boolean isAttribueEquality() {
		return getAttributes().isAttribueEquality();
	}

	private Attributes attributes = new Attributes();
	private String name;
	private String uri;

	public Attributes getAttributes() {
		return attributes;
	}

	/**
	 * Gets name of license.
	 * 
	 * @throws NullPointerException if not set
	 */
	public String getName() {
		if (name == null) {
			throw new NullPointerException();
		}
		return name;
	}

	/**
	 * Gets URI identifying this license.
	 * 
	 * @throws NullPointerException if not set
	 */
	public String getUri() throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException();
		}
		return uri;
	}

	public License setAttributes(Attributes attributes) {
		this.attributes = attributes;
		return this;
	}

	/**
	 * Sets name of license.
	 * 
	 * @throws NullPointerException if given URI is null
	 */
	public License setName(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
		return this;
	}

	/**
	 * Sets URI identifying this license.
	 * 
	 * @throws NullPointerException if given URI is null
	 */
	public License setUri(String uri) {
		if (uri == null) {
			throw new NullPointerException();
		}
		this.uri = uri;
		return this;
	}

	@Override
	public String toString() {
		return getUri() + (name == null ? "" : " (" + name + ")");
	}
}