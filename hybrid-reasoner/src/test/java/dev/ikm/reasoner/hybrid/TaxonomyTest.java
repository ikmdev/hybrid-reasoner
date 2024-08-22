package dev.ikm.reasoner.hybrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.interfaces.ElkObject;
import org.semanticweb.elk.owl.iris.ElkFullIri;
import org.semanticweb.elk.owl.managers.ElkObjectEntityRecyclingFactory;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.owlapi.wrapper.OwlConverter;
import org.semanticweb.elk.reasoner.completeness.Incompleteness;
import org.semanticweb.elk.reasoner.taxonomy.ConcurrentClassTaxonomy;
import org.semanticweb.elk.reasoner.taxonomy.impl.NonBottomGenericTaxonomyNode;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaxonomyTest {

	private static final Logger log = LoggerFactory.getLogger(TaxonomyTest.class);

	@Test
	public void statementConceptOwl() throws Exception {
		String dir = "src/test/resources";
		String test_case = "SWEC-Ontology-Family-History";
		log.info("Test case: " + test_case);
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = ontologyManager
				.loadOntologyFromOntologyDocument(Paths.get(dir, test_case + ".ofn").toFile());
		OWLClass owl_statement_concept = ontologyManager.getOWLDataFactory()
				.getOWLClass(IRI.create(HybridReasonerProperties.SWEC.statementConcept()));
		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ontology);
		reasoner.precomputeInferences();
		ConcurrentClassTaxonomy taxonomy = (ConcurrentClassTaxonomy) Incompleteness
				.getValue(reasoner.getInternalReasoner().getTaxonomy());
		ElkClass elk_statement_concept = OwlConverter.getInstance().convert(owl_statement_concept);
		NonBottomGenericTaxonomyNode.Projection<ElkClass> statement_node = taxonomy.getNonBottomNode(elk_statement_concept);
		log.info("SN: " + statement_node + " " + statement_node.getClass());
		assertEquals(17, statement_node.getAllSubNodes().size());
		assertTrue(statement_node.getAllSubNodes().contains(taxonomy.getBottomNode()));
	}

	@Test
	public void statementConceptElk() throws Exception {
		String dir = "src/test/resources";
		String test_case = "SWEC-Ontology-Family-History";
		log.info("Test case: " + test_case);
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = ontologyManager
				.loadOntologyFromOntologyDocument(Paths.get(dir, test_case + ".ofn").toFile());
		ElkObject.Factory object_factory = new ElkObjectEntityRecyclingFactory();
		ElkFullIri iri = new ElkFullIri(HybridReasonerProperties.SWEC.statementConcept());
		ElkClass elk_stmt_con = object_factory.getClass(iri);
		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ontology);
		reasoner.precomputeInferences();
		ConcurrentClassTaxonomy taxonomy = (ConcurrentClassTaxonomy) Incompleteness
				.getValue(reasoner.getInternalReasoner().getTaxonomy());
		NonBottomGenericTaxonomyNode.Projection<ElkClass> statement_node = taxonomy.getNonBottomNode(elk_stmt_con);
		log.info("SN: " + statement_node + " " + statement_node.getClass());
		assertEquals(17, statement_node.getAllSubNodes().size());
		assertTrue(statement_node.getAllSubNodes().contains(taxonomy.getBottomNode()));
	}

}
