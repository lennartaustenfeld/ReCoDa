package org.dice_research.opal.licenses.demo.container;

import org.dice_research.opal.licenses.Attribute;

public class AttributeContainer {

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

	public String getCssClasses() {
		StringBuilder sb = new StringBuilder();
		sb.append("d-inline-block py-1 px-2 m-1 ");
		sb.append("rounded ");
		sb.append("recoda-attribute ");
		if (attribute.isTypeAttribueEquality()) {
			sb.append("text-white recoda-equality");
		} else if (attribute.isTypePermissionOfDerivates()) {
			sb.append("text-white recoda-derivates");
		} else {
			sb.append("recoda-");
			sb.append(attribute.getType().toLowerCase());
		}
		return sb.toString();
	}

	public String getHtml() {
		return "<div class=\"" + getCssClasses() + "\">" + getUriSuffix() + "</div>";
	}
}