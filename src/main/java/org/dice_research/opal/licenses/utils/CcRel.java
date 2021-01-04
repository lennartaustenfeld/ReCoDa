package org.dice_research.opal.licenses.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.opal.licenses.Attribute;
import org.dice_research.opal.licenses.AttributeFactory;
import org.dice_research.opal.licenses.Attributes;
import org.dice_research.opal.licenses.KnowledgeBase;
import org.dice_research.opal.licenses.License;
import org.dice_research.opal.licenses.Permission;
import org.dice_research.opal.licenses.Prohibition;
import org.dice_research.opal.licenses.Requirement;

/**
 * ccREL: The Creative Commons Rights Expression Language.
 * 
 * @see https://www.w3.org/Submission/ccREL/
 * @see https://creativecommons.org/ns
 *
 * @author Adrian Wilke
 */
public class CcRel {

	public static final String CCREL = "http://creativecommons.org/ns#";
	public static final Resource R_LICENSE = ResourceFactory.createResource(CCREL + "License");
	public static final Property P_REQUIRES = ResourceFactory.createProperty(CCREL + "requires");
	public static final Property P_PERMITS = ResourceFactory.createProperty(CCREL + "permits");
	public static final Property P_PROHIBITS = ResourceFactory.createProperty(CCREL + "prohibits");
	public static final Property P_TITLE = DCTerms.title;

	private Collection<String> attribueEqualityUris;
	private Collection<String> permissionOfDerivatesUris;

	public CcRel(Collection<String> attribueEqualityUris, Collection<String> permissionOfDerivatesUris) {
		this.attribueEqualityUris = attribueEqualityUris;
		this.permissionOfDerivatesUris = permissionOfDerivatesUris;
	}

	public void export(KnowledgeBase kb, File file, Lang lang) {
		Model model = this.export(kb);

		if (lang.equals(Lang.TURTLE)) {
			exportTurtle(kb, file, null, null);
			return;
		}

		try {
			FileOutputStream fos = new FileOutputStream(file);
			RDFDataMgr.write(fos, model, lang);
			fos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void exportTurtle(KnowledgeBase kb, File file, Map<String, String> prefixes, List<String> commentLines) {
		Model model = this.export(kb);

		// Prefixes
		model.setNsPrefix("cc", CCREL);
		model.setNsPrefix("dct", DCTerms.NS);
		if (prefixes != null) {
			for (Entry<String, String> prefix : prefixes.entrySet()) {
				model.setNsPrefix(prefix.getKey(), prefix.getValue());
			}
		}

		try {
			FileOutputStream fos = new FileOutputStream(file);

			// Comments
			if (commentLines != null) {
				for (String line : commentLines) {
					fos.write("# ".getBytes());
					fos.write(line.getBytes());
					fos.write("\n".getBytes());
				}
				fos.write("\n".getBytes());
			}

			RDFDataMgr.write(fos, model, Lang.TURTLE);
			fos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Model export(KnowledgeBase kb) {
		Model model = ModelFactory.createDefaultModel();
		for (License license : kb.getLicenses()) {

			// Add license
			Resource rLicense = ResourceFactory.createResource(license.getUri());
			model.add(rLicense, RDF.type, R_LICENSE);
			if (license.getName() != null && !license.getName().isEmpty()) {
				model.add(rLicense, P_TITLE, ResourceFactory.createPlainLiteral(license.getName()));
			}

			for (Attribute attribute : license.getAttributes().getUriToAttributeMap().values()) {

				// Only add existing/true attributes as triples
				if (!attribute.getValue()) {
					continue;
				}

				Resource rAttribute = ResourceFactory.createResource(attribute.getUri());

				if (attribute instanceof Permission) {
					model.add(rLicense, P_PERMITS, rAttribute);
				} else if (attribute instanceof Prohibition) {
					model.add(rLicense, P_PROHIBITS, rAttribute);
				} else if (attribute instanceof Requirement) {
					model.add(rLicense, P_REQUIRES, rAttribute);
				} else {
					throw new RuntimeException("Unknown attibute type");
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
		policyIt = model.listSubjectsWithProperty(RDF.type, R_LICENSE);
		while (policyIt.hasNext()) {
			Resource rPolicy = policyIt.next();

			ruleIt = rPolicy.listProperties(P_PERMITS);
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

			ruleIt = rPolicy.listProperties(P_PROHIBITS);
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

			ruleIt = rPolicy.listProperties(P_REQUIRES);
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
		policyIt = model.listSubjectsWithProperty(RDF.type, R_LICENSE);
		while (policyIt.hasNext()) {
			Resource rPolicy = policyIt.next();
			License license = new License();
			license.setUri(rPolicy.getURI());
			Statement stmt = rPolicy.getProperty(P_TITLE);
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
			updateAttributes(rPolicy, P_PERMITS, attributes);
			updateAttributes(rPolicy, P_PROHIBITS, attributes);
			updateAttributes(rPolicy, P_REQUIRES, attributes);

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
		if (attribueEqualityUris.contains(rRule.getURI())) {
			attribute.setTypeAttribueEquality(true);
		} else if (permissionOfDerivatesUris.contains(rRule.getURI())) {
			attribute.setTypePermissionOfDerivates(true);
		}
	}

}