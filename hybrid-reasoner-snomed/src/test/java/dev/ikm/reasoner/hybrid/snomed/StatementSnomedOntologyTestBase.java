package dev.ikm.reasoner.hybrid.snomed;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.OwlElTransformer;
import dev.ikm.elk.snomed.SnomedDescriptions;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.owlel.OwlElOntology;

@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyTestBase {

	private static final Logger log = LoggerFactory.getLogger(StatementSnomedOntologyTestBase.class);

	protected String getDir() {
		return "target/data/snomed-test-data-" + getEditionDir() + "-" + getVersion();
	}

	protected String getEdition() {
		return "US1000124";
	}

	protected String getEditionDir() {
		return "us";
	}

	protected String getVersion() {
		return "20230901";
	}

	protected Path axioms_file = Paths.get(getDir(),
			"sct2_sRefset_OWLExpressionSnapshot_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path concepts_file = Paths.get(getDir(),
			"sct2_Concept_Snapshot_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path rels_file = Paths.get(getDir(),
			"sct2_Relationship_Snapshot_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path values_file = Paths.get(getDir(),
			"sct2_RelationshipConcreteValues_Snapshot_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path descriptions_file = Paths.get(getDir(),
			"sct2_Description_Snapshot-en_" + getEdition() + "_" + getVersion() + ".txt");

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
