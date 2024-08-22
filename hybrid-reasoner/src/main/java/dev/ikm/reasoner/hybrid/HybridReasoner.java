package dev.ikm.reasoner.hybrid;

import org.semanticweb.elk.owlapi.ElkReasonerConfiguration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HybridReasoner extends ElkReasonerDelegate {

	private static final Logger log = LoggerFactory.getLogger(HybridReasoner.class);

	private static boolean debug = false;

	private HybridReasonerProperties properties;

	private StatementClassifier statementClassifier;

	/**
	 * Constructor creates super class ElkReasonerDelegate, which delegates to
	 * ElkReasoner. The preComputeInferences() method is overridden to invoke the
	 * hybrid reasoner on all concepts rooted at the concept in the input ontology
	 * specified by DefaultProperties.STATEMENT_CONCEPT_NAMESPACE and
	 * DefaultProperties.STATEMENT_CONCEPT_NAME (e.g.,
	 * "http://www.hhs.fda.org/shield/SWEC-Ontology#Statement-Concept" in the test
	 * ontologies)
	 * 
	 * @param ontology
	 * @param config
	 * @throws IllegalConfigurationException
	 */
	public HybridReasoner(OWLOntology ontology, ElkReasonerConfiguration config, HybridReasonerProperties properties)
			throws IllegalConfigurationException {
		super(ontology, config);
		this.properties = properties;
	}

	@Override
	public void precomputeInferences(InferenceType... inferenceTypes)
			throws ReasonerInterruptedException, TimeOutException, InconsistentOntologyException {
		super.precomputeInferences(inferenceTypes);
		try {
			preComputeHierarchy();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void preComputeHierarchy() throws Exception {
		// Comment re Protege:
		// use the secondary progress monitor by default, when necessary, we switch to
		// the primary progress monitor; this is to avoid bugs with progress monitors in
		// Protege

		StatementOntology statementOntology = StatementOntology.createStatementOntology(this.getRootOntology(),
				properties);
		if (statementOntology.getAxiomCount() == 0) {
			log.warn("No Statement Concepts found in ontology for " + properties.statementConcept());
			return;
		}
		// Instantiation of a StatementClassifierSHIELD classifier creates
		// subsumption-normal-form representations of each statement in the
		// statementOntology. These representations are needed by the subsequent
		// classifyStatementConcepts operation.
		this.statementClassifier = new StatementClassifier(this.getRootOntology(), this.getElkReasoner(),
				statementOntology.getOntology(), statementOntology.getReasoner(), statementOntology.getProperties());
		statementClassifier.classifyStatementConcepts();
		if (debug) {
			System.out.println("POST-MIGRATION KERNEL REASONER TAXONOMY - IN preComputeHierarchy");
			ReasonerExplorer.printCurrentReasonerTaxonomy(this.getElkReasoner(), false);
		}
	}

	@Override
	public String getReasonerName() {
		return this.getClass().toString();
	}

}
