package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.OwlElTransformer;
import dev.ikm.elk.snomed.SnomedDescriptions;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.owlel.OwlElOntology;

@TestInstance(Lifecycle.PER_CLASS)
public abstract class StatementSnomedOntologyTestBase extends SnomedTestBase {

	private static final Logger log = LoggerFactory.getLogger(StatementSnomedOntologyTestBase.class);

	protected SnomedOntology snomedOntology;

	public void init() throws Exception {
		log.info("Test case: " + axioms_file);
		OwlElOntology ontology = new OwlElOntology();
		ontology.load(axioms_file);
		log.info("Load complete");
		snomedOntology = new OwlElTransformer().transform(ontology);
		snomedOntology.setDescriptions(SnomedDescriptions.init(descriptions_file));
		snomedOntology.setNames();
	}

	protected void list(StatementSnomedOntology sso, long con, int depth) {
		log.info("\t".repeat(depth) + con + " " + snomedOntology.getFsn(con));
		for (long sub : sso.getSubConcepts(con)) {
			list(sso, sub, depth + 1);
		}
	}

}
