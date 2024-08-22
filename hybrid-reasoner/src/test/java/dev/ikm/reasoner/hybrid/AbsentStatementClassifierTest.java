package dev.ikm.reasoner.hybrid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbsentStatementClassifierTest {

	private static final Logger log = LoggerFactory.getLogger(AbsentStatementClassifierTest.class);

	@Test
	public void familyHistory() throws Exception {
		String dir = "src/test/resources";
		String test_case = "SWEC-Ontology-Family-History";
		log.info("Test case: " + test_case);
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = ontologyManager
				.loadOntologyFromOntologyDocument(Paths.get(dir, test_case + ".ofn").toFile());
		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ontology);
		reasoner.precomputeInferences();
		//
		StatementOntology statementOntology = StatementOntology.createStatementOntology(ontology,
				HybridReasonerProperties.SWEC);
		StatementClassifier statementClassifier = new StatementClassifier(ontology, reasoner,
				statementOntology.getOntology(), statementOntology.getReasoner(), statementOntology.getProperties());
		assertEquals(16, statementClassifier.getStatementClassificationConcepts().size());
	}

}
