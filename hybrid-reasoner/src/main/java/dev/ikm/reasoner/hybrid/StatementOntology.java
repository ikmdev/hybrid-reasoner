package dev.ikm.reasoner.hybrid;

import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatementOntology {

	private static final Logger log = LoggerFactory.getLogger(StatementOntology.class);

	private static boolean debug = false;

	private OWLOntology sourceOntology;

	private HybridReasonerProperties properties;

	private OWLClass sourceStatementConcept;

	private OWLOntologyManager ontologyManager;

	private OWLOntology ontology;

	private OWLReasoner reasoner;

	private int axiomCount = 0;

	public OWLOntology getSourceOntology() {
		return sourceOntology;
	}

	public HybridReasonerProperties getProperties() {
		return properties;
	}

	public OWLOntology getOntology() {
		return ontology;
	}

	public OWLReasoner getReasoner() {
		return reasoner;
	}

	public int getAxiomCount() {
		return axiomCount;
	}

	public StatementOntology(OWLOntology sourceOntology, HybridReasonerProperties properties) {
		super();
		this.sourceOntology = sourceOntology;
		this.properties = properties;
	}

	public static StatementOntology createStatementOntology(OWLOntology sourceOntology,
			HybridReasonerProperties properties) throws Exception {
		StatementOntology instance = new StatementOntology(sourceOntology, properties);
		instance.create();
		return instance;
	}

	private void create() throws Exception {
		{
			OWLDataFactory factory = sourceOntology.getOWLOntologyManager().getOWLDataFactory();
			sourceStatementConcept = factory.getOWLClass(IRI.create(properties.statementConcept()));
		}
		ontologyManager = OWLManager.createOWLOntologyManager();
		// Will be populated with statement axioms by copyStatementAxioms
		ontology = ontologyManager.createOntology();
		// The statement axioms (rooted at sourceStatementConcept) are all
		// copied from the sourceOntology into the statementOntology.
		copyStatementAxioms();
		if (axiomCount == 0)
			return;
		log.info("Completed copying axioms from original to statement ontology");
		// A StructuralReasoner instance is created to represent the stated hierarchy
		// for the statementOntology. This hierarchy is needed for the later
		// classification of the statement concepts into the kernel reasoner's taxonomy.
		StructuralReasonerFactory structuralReasonerFactory = new StructuralReasonerFactory();
		reasoner = structuralReasonerFactory.createNonBufferingReasoner(ontology);
		if (debug) {
			System.out.println("ORIGINAL STATEMENT REASONER TAXONOMY");
			ReasonerExplorer.printCurrentReasonerOwlTaxonomy(reasoner);
		}
	}

	/**
	 * Copy all axioms defining classes that are sub-types of the sourceClass from
	 * the sourceOntology and put them in the destination ontology. These classes
	 * will get reclassified using the hybrid reasoner logic. It is assumed that the
	 * sub-hierarchy rooted at sourceClass includes no concepts that are referenced
	 * by the remainder of the sourceOntology
	 */
	private synchronized void copyStatementAxioms() {
		// Very important that it's OWLOntologyManager from *destinationOntology*, not
		// sourceOntology
		// OWLOntologyManager ontologyManager =
		// destinationOntology.getOWLOntologyManager();
		Set<OWLClass> subClasses = getStructuralSubClasses(sourceStatementConcept);
		for (OWLClass owlClass : subClasses) {
			Set<OWLClassAxiom> defnAxioms = sourceOntology.getAxioms(owlClass, Imports.INCLUDED);
			for (OWLAxiom owlAxiom : defnAxioms) {
				if (debug)
					log.info("Adding the Definition Axiom for: " + owlClass);
				ontologyManager.addAxiom(ontology, owlAxiom);
				axiomCount++;
			}
			Set<OWLDeclarationAxiom> declAxioms = sourceOntology.getDeclarationAxioms(owlClass);
			if (debug)
				log.info("Got declaratons for: " + owlClass);
			for (OWLAxiom owlAxiom : declAxioms) {
				if (debug)
					log.info("REASONER: Adding the Declaration Axiom for: " + owlClass);
				ontologyManager.addAxiom(ontology, owlAxiom);
				axiomCount++;
			}
		}
	}

	private synchronized Set<OWLClass> getStructuralSubClasses(OWLClass clazz) {
		// Structural reasoner creates *stated* hierarchy for the sourceOntology; used
		// to identify all sub-classes
		StructuralReasonerFactory structuralReasonerFactory = new StructuralReasonerFactory();
		OWLReasoner sourceReasoner = structuralReasonerFactory.createNonBufferingReasoner(sourceOntology);
		Set<OWLClass> subClassSet = sourceReasoner.getSubClasses(clazz, false).getFlattened();
		// include the class concept also
		subClassSet.add(clazz);
		return subClassSet;
	}

}
