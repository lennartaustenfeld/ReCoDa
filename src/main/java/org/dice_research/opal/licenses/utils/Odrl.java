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
 * ReCoDa MetaAttributes (attribute types) are expressed as Actions.
 * 
 * TODO: Check best way to store MetaAttributes.
 * 
 * @see https://www.w3.org/TR/odrl-vocab/
 * @see https://www.w3.org/TR/odrl-model/
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

	public void export(KnowledgeBase kb, File file, Lang lang) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			RDFDataMgr.write(fos, this.export(kb), lang);
			fos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Model export(KnowledgeBase kb) {
		Model model = ModelFactory.createDefaultModel();
		for (License license : kb.getLicenses()) {
			Resource rLicense = ResourceFactory.createResource(license.getUri());
			model.add(rLicense, RDF.type, R_POLICY);
			if (license.getName() != null && !license.getName().isEmpty()) {
				model.add(rLicense, RDFS.label, ResourceFactory.createPlainLiteral(license.getName()));
			}

			for (Attribute attribute : license.getAttributes().getUriToAttributeMap().values()) {
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

				if (attribute.isTypeAttribueEquality()) {
					model.add(rAttribute, P_ACTION, R_SHARE_ALIKE);
				}
				if (attribute.isTypePermissionOfDerivates()) {
					model.add(rAttribute, P_ACTION, R_DERIVATIVE_WORKS);
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

		ResIterator policyIt = model.listSubjectsWithProperty(RDF.type, R_POLICY);
		while (policyIt.hasNext()) {
			Resource rPolicy = policyIt.next();
			License license = new License();
			license.setUri(rPolicy.getURI());
			Statement stmt = rPolicy.getProperty(RDFS.label);
			if (stmt != null) {
				license.setName(stmt.getObject().asLiteral().getString());
			}

			Attributes attributes = new Attributes();
			addAttributes(rPolicy, P_PERMISSION, Permission.TYPE, attributes);
			addAttributes(rPolicy, P_PROHIBITION, Prohibition.TYPE, attributes);
			addAttributes(rPolicy, P_OBLIGATION, Requirement.TYPE, attributes);
			license.setAttributes(attributes);

			kb.addLicense(license);

			for (Attribute attribute : attributes.getList()) {
				if (!kb.getSortedAttributes().getUris().contains(attribute.getUri())) {
					kb.getSortedAttributes().addAttribute(attribute);
				}
			}

		}
		return kb;
	}

	private Attributes addAttributes(Resource rPolicy, Property pRule, String attributeType, Attributes attributes) {

		StmtIterator ruleIt = rPolicy.listProperties(pRule);
		while (ruleIt.hasNext()) {
			RDFNode nRule = ruleIt.next().getObject();
			if (nRule.isURIResource()) {
				Resource rRule = nRule.asResource();
				Attribute attribute = AttributeFactory.get().createAttribute(attributeType, rRule.getURI());

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
				attributes.addAttribute(attribute);
			}
		}
		return attributes;
	}
}