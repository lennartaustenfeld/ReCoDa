package org.dice_research.opal.licenses.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dice_research.opal.licenses.Attribute;
import org.dice_research.opal.licenses.AttributeFactory;
import org.dice_research.opal.licenses.Attributes;
import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;
import org.dice_research.opal.licenses.Permission;
import org.dice_research.opal.licenses.Prohibition;
import org.dice_research.opal.licenses.Requirement;

/**
 * ODRL.
 * 
 * ReCoDa MetaAttributes (attribute types) are expressed as Actions. By default,
 * these are not included in the import and export. That can be changed in the
 * constructor {@link #Odrl(boolean)}.
 * 
 * @see https://www.w3.org/TR/odrl-vocab/
 * @see https://www.w3.org/TR/odrl-model/
 * @see https://www.w3.org/ns/odrl/2/ODRL21 (examples)
 * @see https://creativecommons.org/ns
 *
 * @author Adrian Wilke
 */
public class Odrl {

	public static final String ODRL2 = "http://www.w3.org/ns/odrl/2/";
	public static final Resource R_POLICY = ResourceFactory.createResource(ODRL2 + "Policy");
	public static final Resource R_RULE_DUTY = ResourceFactory.createResource(ODRL2 + "Duty");
	public static final Resource R_RULE_PERMISSION = ResourceFactory.createResource(ODRL2 + "Permission");
	public static final Resource R_RULE_PROHIBITION = ResourceFactory.createResource(ODRL2 + "Prohibition");
	public static final Property P_OBLIGATION = ResourceFactory.createProperty(ODRL2 + "obligation");
	public static final Property P_PERMISSION = ResourceFactory.createProperty(ODRL2 + "permission");
	public static final Property P_PROHIBITION = ResourceFactory.createProperty(ODRL2 + "prohibition");
	public static final Property P_ACTION = ResourceFactory.createProperty(ODRL2 + "action");

	public static final String CC = "http://creativecommons.org/ns#";
	public static final Resource R_DERIVATIVE_WORKS = ResourceFactory.createResource(CC + "DerivativeWorks");
	public static final Resource R_SHARE_ALIKE = ResourceFactory.createResource(CC + "ShareAlike");

	private boolean includeMetaAttributes;

	/**
	 * Constructor.
	 * 
	 * Will not include meta attributes.
	 */
	public Odrl() {
		this.includeMetaAttributes = false;
	}

	/**
	 * Constructor.
	 * 
	 * @param includeMetaAttributes include meta attributes to import/export.
	 */
	public Odrl(boolean includeMetaAttributes) {
		this.includeMetaAttributes = includeMetaAttributes;
	}

	public void export(KnowledgeBase kb, File file, Lang lang) {
		Model model = this.export(kb);

		if (lang.equals(Lang.TURTLE)) {
			model.setNsPrefix("cc", CC);
			model.setNsPrefix("odrl", ODRL2);
		}

		try {
			FileOutputStream fos = new FileOutputStream(file);
			RDFDataMgr.write(fos, model, lang);
			fos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// TODO: use blank nodes https://www.w3.org/ns/odrl/2/ODRL21
	public Model export(KnowledgeBase kb) {
		Model model = ModelFactory.createDefaultModel();
		for (License license : kb.getLicenses()) {

			// Add license
			Resource rLicense = ResourceFactory.createResource(license.getUri());
			model.add(rLicense, RDF.type, R_POLICY);
			if (license.getName() != null && !license.getName().isEmpty()) {
				model.add(rLicense, RDFS.label, ResourceFactory.createPlainLiteral(license.getName()));
			}

			for (Attribute attribute : license.getAttributes().getUriToAttributeMap().values()) {

				// Only add existing/true attributes as triples
				if (!attribute.getValue()) {
					continue;
				}

				Resource rAttribute = ResourceFactory.createResource(attribute.getUri());

				if (attribute instanceof Permission) {
					model.add(rAttribute, RDF.type, R_RULE_PERMISSION);
					model.add(rLicense, P_PERMISSION, rAttribute);
				} else if (attribute instanceof Prohibition) {
					model.add(rAttribute, RDF.type, R_RULE_PROHIBITION);
					model.add(rLicense, P_PROHIBITION, rAttribute);
				} else if (attribute instanceof Requirement) {
					model.add(rAttribute, RDF.type, R_RULE_DUTY);
					model.add(rLicense, P_OBLIGATION, rAttribute);
				} else {
					throw new RuntimeException("Unknown attibute type");
				}

				// Optional: Add meta attributes
				if (includeMetaAttributes) {
					if (attribute.isTypeAttribueEquality()) {
						model.add(rAttribute, P_ACTION, R_SHARE_ALIKE);
					}
					if (attribute.isTypePermissionOfDerivates()) {
						model.add(rAttribute, P_ACTION, R_DERIVATIVE_WORKS);
					}
				}
			}
		}
		return model;
	}

	public KnowledgeBase importFile(File file, Lang lang) {
		return importModel(RDFDataMgr.loadModel(file.toURI().toString(), lang));
	}

	public KnowledgeBase importModel(Model model) {
		KnowledgeBase kb = new KnowledgeBase();
		ResIterator policyIt;
		StmtIterator ruleIt;

		// First iteration: Collect attributes from all licenses
		policyIt = model.listSubjectsWithProperty(RDF.type, R_POLICY);
		while (policyIt.hasNext()) {
			Resource rPolicy = policyIt.next();

			ruleIt = rPolicy.listProperties(P_PERMISSION);
			while (ruleIt.hasNext()) {
				RDFNode nRule = ruleIt.next().getObject();
				if (nRule.isURIResource()) {
					Resource rRule = nRule.asResource();
					Attribute attribute = AttributeFactory.get().createAttribute(Permission.TYPE, rRule.getURI());
					if (!kb.getSortedAttributes().getUris().contains(attribute.getUri())) {
						addMetaAttribute(rRule, attribute);
						kb.getSortedAttributes().addAttribute(attribute);
					}
				}
			}

			ruleIt = rPolicy.listProperties(P_PROHIBITION);
			while (ruleIt.hasNext()) {
				RDFNode nRule = ruleIt.next().getObject();
				if (nRule.isURIResource()) {
					Resource rRule = nRule.asResource();
					Attribute attribute = AttributeFactory.get().createAttribute(Prohibition.TYPE, rRule.getURI());
					if (!kb.getSortedAttributes().getUris().contains(attribute.getUri())) {
						addMetaAttribute(rRule, attribute);
						kb.getSortedAttributes().addAttribute(attribute);
					}
				}
			}

			ruleIt = rPolicy.listProperties(P_OBLIGATION);
			while (ruleIt.hasNext()) {
				RDFNode nRule = ruleIt.next().getObject();
				if (nRule.isURIResource()) {
					Resource rRule = nRule.asResource();
					Attribute attribute = AttributeFactory.get().createAttribute(Requirement.TYPE, rRule.getURI());
					if (!kb.getSortedAttributes().getUris().contains(attribute.getUri())) {
						addMetaAttribute(rRule, attribute);
						kb.getSortedAttributes().addAttribute(attribute);
					}
				}
			}

		}

		// Second iteration: Add attributes contained in triples
		policyIt = model.listSubjectsWithProperty(RDF.type, R_POLICY);
		while (policyIt.hasNext()) {
			Resource rPolicy = policyIt.next();
			License license = new License();
			license.setUri(rPolicy.getURI());
			Statement stmt = rPolicy.getProperty(RDFS.label);
			if (stmt != null) {
				license.setName(stmt.getObject().asLiteral().getString());
			}

			// Add all attributes
			Attributes attributes = new Attributes();
			for (Attribute attribute : kb.getSortedAttributes().getList()) {
				attributes.addAttribute(AttributeFactory.get().createAttribute(attribute, false).setValue(false));
			}
			license.setAttributes(attributes);

			// Update attribute values
			updateAttributes(rPolicy, P_PERMISSION, attributes);
			updateAttributes(rPolicy, P_PROHIBITION, attributes);
			updateAttributes(rPolicy, P_OBLIGATION, attributes);

			// Add license with attributes
			kb.addLicense(license);
		}

		// Third iteration: Add attributes not contained in triples
		for (License license : kb.getLicenses()) {
			for (Attribute attribute : kb.getSortedAttributes().getList()) {
				if (!license.getAttributes().getUris().contains(attribute.getUri())) {
					license.getAttributes()
							.addAttribute(AttributeFactory.get().createAttribute(attribute, false).setValue(false));
				}
			}
		}

		return kb;
	}

	private void updateAttributes(Resource rPolicy, Property pRule, Attributes attributes) {

		StmtIterator ruleIt = rPolicy.listProperties(pRule);
		while (ruleIt.hasNext()) {
			RDFNode nRule = ruleIt.next().getObject();
			if (nRule.isURIResource()) {
				Resource rRule = nRule.asResource();
				attributes.getAttribute(rRule.getURI()).setValue(true);
			}
		}
	}

	private void addMetaAttribute(Resource rRule, Attribute attribute) {
		// Optional: Add meta attributes
		if (includeMetaAttributes) {
			Statement stmt = rRule.getProperty(P_ACTION);
			if (stmt != null) {
				RDFNode nAction = stmt.getObject();
				if (nAction.isURIResource()) {
					String actionUri = nAction.asResource().getURI();
					if (actionUri.equals(R_SHARE_ALIKE.getURI())) {
						attribute.setTypeAttribueEquality(true);
					} else if (actionUri.equals(R_DERIVATIVE_WORKS.getURI())) {
						attribute.setTypePermissionOfDerivates(true);
					}
				}
			}
		}
	}
}