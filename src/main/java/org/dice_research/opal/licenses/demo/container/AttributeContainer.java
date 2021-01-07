package org.dice_research.opal.licenses.demo.container;

import org.dice_research.opal.licenses.Attribute;

public class AttributeContainer implements Comparable<AttributeContainer> {

	public Attribute attribute;

	public AttributeContainer(Attribute attribute) {
		this.attribute = attribute;
	}

	public String getUriSuffix() {
		if (attribute.getUri().contains("#")) {
			return attribute.getUri().substring(attribute.getUri().indexOf('#') + 1);
		} else if (attribute.getUri().contains("/")) {
			return attribute.getUri().substring(attribute.getUri().lastIndexOf('/') + 1);
		} else {
			return attribute.getUri();
		}
	}

	public String getCssClasses(boolean useValues) {
		StringBuilder sb = new StringBuilder();
		sb.append("d-inline-block py-1 px-2 m-1 ");
		sb.append("rounded ");
		sb.append("recoda-attribute ");
		if (attribute.isTypeAttribueEquality()) {
			if (!useValues || attribute.getValue()) {
				sb.append("text-light recoda-equality");
			} else {
				sb.append("recoda-disabled");
			}
		} else if (attribute.isTypePermissionOfDerivates()) {
			if (!useValues || attribute.getValue()) {
				sb.append("text-light recoda-derivates");
			} else {
				sb.append("recoda-disabled");
			}
		} else {
			if (!useValues || attribute.getValue()) {
				sb.append("recoda-");
				sb.append(attribute.getType().toLowerCase());
			} else {
				sb.append("recoda-disabled");
			}
		}
		return sb.toString();
	}

	public String getHtml() {
		return "<div class=\"" + getCssClasses(false) + "\">" + getUriSuffix() + "</div>";
	}

	public String getHtmlUseValues() {
		return "<div class=\"" + getCssClasses(true) + "\">" + getUriSuffix() + "</div>";
	}

	@Override
	public int compareTo(AttributeContainer a) {
		int type = attribute.getType().compareTo(a.attribute.getType());
		if (type != 0) {
			return type;
		}

		if (attribute.isMetaAttribute() && !a.attribute.isMetaAttribute()) {
			return 1;
		} else if (!attribute.isMetaAttribute() && a.attribute.isMetaAttribute()) {
			return -1;
		}

		return attribute.getUri().compareTo(a.attribute.getUri());
	}
}