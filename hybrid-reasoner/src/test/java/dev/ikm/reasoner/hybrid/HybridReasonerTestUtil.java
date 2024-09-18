package dev.ikm.reasoner.hybrid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HybridReasonerTestUtil {

	private static final Logger log = LoggerFactory.getLogger(HybridReasonerTestUtil.class);

	private static boolean write_sups = false;

	private static boolean log_taxonomy = false;

	public static void loadAndCompare(String dir, String test_case, String ext) throws Exception {
		log.info("Test case: " + test_case);
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = ontologyManager
				.loadOntologyFromOntologyDocument(Paths.get(dir, test_case + "." + ext).toFile());
		HybridReasonerFactory reasonerFactory = new HybridReasonerFactory(HybridReasonerProperties.SWEC);
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
		reasoner.precomputeInferences();
		if (log_taxonomy) {
			log.info("Owl taxonomy:");
			ReasonerExplorer.printOwlTaxonomy(reasoner.getTopClassNode(), 0, reasoner);
		}
		ArrayList<String> sups = new ArrayList<>();
		for (OWLClass clazz : ontology.getClassesInSignature()) {
			for (OWLClass sup : reasoner.getSuperClasses(clazz, true).getFlattened()) {
				String line = clazz.getIRI().getShortForm() + "\t" + sup.getIRI().getShortForm();
				// log.info(line);
				sups.add(line);
			}
		}
		Collections.sort(sups);
		String sups_file = test_case + "-sups" + ".txt";
		if (write_sups)
			Files.write(Paths.get("target", sups_file), sups, StandardOpenOption.CREATE);
		List<String> expect_sups = Files.readAllLines(Paths.get(dir, sups_file));
		expect_sups.stream().filter(x -> !sups.contains(x)).forEach(x -> log.info("Exp: " + x));
		sups.stream().filter(x -> !expect_sups.contains(x)).forEach(x -> log.info("Was: " + x));
		assertEquals(expect_sups, sups);
		reasoner.dispose();
	}

}
