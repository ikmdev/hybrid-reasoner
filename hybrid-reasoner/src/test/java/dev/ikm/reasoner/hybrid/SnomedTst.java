package dev.ikm.reasoner.hybrid;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.owl.SnomedOwlOntology;

public class SnomedTst {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SnomedTst.class);

	protected String getDir() {
		return "target/data/snomed-test-data-" + getEditionDir() + "-" + getVersion();
	}

	protected String getEdition() {
		return "INT";
	}

	protected String getEditionDir() {
		return "intl";
	}

	protected String getVersion() {
		return "20190731";
	}

	protected Path axioms_file = Paths.get(getDir(),
			"sct2_sRefset_OWLExpressionSnapshot_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path rels_file = Paths.get(getDir(),
			"sct2_Relationship_Snapshot_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path values_file = Paths.get(getDir(),
			"sct2_RelationshipConcreteValues_Snapshot_" + getEdition() + "_" + getVersion() + ".txt");

	@Test
	public void snomed() throws Exception {
		log.info("Test case: " + axioms_file);
		SnomedOwlOntology snomed_ontology = SnomedOwlOntology.createOntology();
		snomed_ontology.loadOntology(axioms_file);
		OWLOntology owl_ontology = snomed_ontology.getOntology();
		HybridReasonerFactory reasonerFactory = new HybridReasonerFactory(HybridReasonerProperties.SNOMED);
		OWLReasoner reasoner = reasonerFactory.createReasoner(owl_ontology);
		reasoner.precomputeInferences();
		ArrayList<String> sups = new ArrayList<>();
		for (OWLClass clazz : owl_ontology.getClassesInSignature()) {
			 log.info("" + clazz.getIRI());
			for (OWLClass sup : reasoner.getSuperClasses(clazz, true).getFlattened()) {
				String line = clazz.getIRI().getShortForm() + "\t" + sup.getIRI().getShortForm();
				// log.info(line);
				sups.add(line);
			}
		}
		Collections.sort(sups);
//		String sups_file = test_case + "-sups" + ".txt";
////			Files.write(Paths.get("target", sups_file), sups, StandardOpenOption.CREATE);
//		List<String> expect_sups = Files.readAllLines(Paths.get(dir, sups_file));
//		expect_sups.stream().filter(x -> !sups.contains(x)).forEach(x -> log.info("Exp: " + x));
//		sups.stream().filter(x -> !expect_sups.contains(x)).forEach(x -> log.info("Was: " + x));
//		assertEquals(expect_sups, sups);
//		reasoner.dispose();
	}

}
