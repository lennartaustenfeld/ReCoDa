package org.dice_research.opal.licenses;

/**
 * Handles special meanings of attributes.
 * 
 * Flags mean the type of attribute, not the value.
 * 
 * @author Adrian Wilke
 */
public abstract class MetaAttribute {

	private boolean isTypePermissionOfDerivates = false;
	private boolean isTypeAttribueEquality = false;

	public boolean isMetaAttribute() {
		return isTypePermissionOfDerivates || isTypeAttribueEquality;
	}

	public boolean isTypePermissionOfDerivates() {
		return isTypePermissionOfDerivates;
	}

	public boolean isTypeAttribueEquality() {
		return isTypeAttribueEquality;
	}

	public MetaAttribute setTypePermissionOfDerivates(boolean typePermissionOfDerivates) {
		this.isTypePermissionOfDerivates = typePermissionOfDerivates;
		return this;
	}

	public MetaAttribute setTypeAttribueEquality(boolean typeAttribueEquality) {
		this.isTypeAttribueEquality = typeAttribueEquality;
		return this;
	}

}