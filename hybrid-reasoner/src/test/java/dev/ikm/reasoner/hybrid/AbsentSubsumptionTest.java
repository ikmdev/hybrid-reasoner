package dev.ikm.reasoner.hybrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbsentSubsumptionTest {

	private static final Logger log = LoggerFactory.getLogger(AbsentSubsumptionTest.class);

	@Test
	public void familyHistory() throws Exception {
		String dir = "src/test/resources";
		String test_case = "SWEC-Ontology-Family-History";
		log.info("Test case: " + test_case);
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = ontologyManager
				.loadOntologyFromOntologyDocument(Paths.get(dir, test_case + ".ofn").toFile());
		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
		reasoner.precomputeInferences();
		//
		StatementOntology statementOntology = StatementOntology.createStatementOntology(ontology,
				HybridReasonerProperties.SWEC);
		SubsumptionNormalFormBuilder subsumptionNormalFormBuilder = new SubsumptionNormalFormBuilder(
				ontology, reasoner, statementOntology.getOntology(), statementOntology.getReasoner(),
				statementOntology.getProperties());
		subsumptionNormalFormBuilder.init();
		subsumptionNormalFormBuilder.generate();
		HybridSubsumptionTester tester = new HybridSubsumptionTester(ontology,
				statementOntology.getProperties().temporalAnnotationOwlIri());
		List<OWLClass> family_history_cons = ontology.getClassesInSignature().stream()
				.filter(x -> x.getIRI().getShortForm().startsWith("Family-history"))
				.filter(x -> !x.getIRI().getShortForm().contains("explicit-context"))
				.filter(x -> !x.getIRI().getShortForm().contains("clinical-finding")).toList();
		assertEquals(6, family_history_cons.size());
		for (OWLClass sub : family_history_cons) {
			OWLClass sub_no = ontologyManager.getOWLDataFactory()
					.getOWLClass(IRI.create(sub.getIRI().toString().replace("Family", "No-family")));
			SubsumptionNormalForm sub_no_snf = subsumptionNormalFormBuilder.getSNF(sub_no);
			for (OWLClass sup : family_history_cons) {
				OWLClass sup_no = ontologyManager.getOWLDataFactory()
						.getOWLClass(IRI.create(sup.getIRI().toString().replace("Family", "No-family")));
//				log.info("Sub: " + sub);
//				log.info("Sub: " + sub_no);
//				log.info("Sup: " + sup);
//				log.info("Sup: " + sup_no);
				SubsumptionNormalForm sup_no_snf = subsumptionNormalFormBuilder.getSNF(sup_no);
				if (reasoner.getSuperClasses(sub, false).getFlattened().contains(sup))
					assertTrue(tester.isSubsumedBy(sup_no_snf, sub_no_snf, reasoner, statementOntology.getReasoner()));
			}
		}
	}

}
