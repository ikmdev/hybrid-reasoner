package dev.ikm.reasoner.hybrid.snomed;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.collections.api.set.primitive.ImmutableLongSet;
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

	protected int concepts_cnt = -1;

	protected int swec_concepts_cnt = -1;

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
		sso.getSubConcepts(con).forEach(sub -> {
			list(sso, sub, depth + 1);
		});
	}

	// Helper method to convert ImmutableLongSet to Set<Long>
	protected Set<Long> toSet(ImmutableLongSet immutableLongSet) {
		Set<Long> boxedSet = new HashSet<>();
		immutableLongSet.forEach(boxedSet::add);
		return boxedSet;
	}

}
