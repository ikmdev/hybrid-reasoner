package dev.ikm.reasoner.hybrid.snomed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedDescriptions;
import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.owl.OwlTransformer;
import dev.ikm.elk.snomed.owl.SnomedOwlOntology;

@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyAbsentTestIT {

	private static final Logger log = LoggerFactory.getLogger(StatementSnomedOntologyAbsentTestIT.class);

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
		return "20240301";
	}

	protected Path axioms_file = Paths.get(getDir(),
			"sct2_sRefset_OWLExpressionSnapshot_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path rels_file = Paths.get(getDir(),
			"sct2_Relationship_Snapshot_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path values_file = Paths.get(getDir(),
			"sct2_RelationshipConcreteValues_Snapshot_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path descriptions_file = Paths.get(getDir(),
			"sct2_Description_Snapshot-en_" + getEdition() + "_" + getVersion() + ".txt");

	private SnomedOntology snomedOntology;

	private StatementSnomedOntology sso;

	@BeforeAll
	public void init() throws Exception {
		log.info("Test case: " + axioms_file);
		long beg = System.currentTimeMillis();
		SnomedOwlOntology snomedOwlOntology = SnomedOwlOntology.createOntology();
		snomedOwlOntology.loadOntology(axioms_file);
		log.info("Load complete");
		snomedOntology = new OwlTransformer().transform(snomedOwlOntology);
		snomedOntology.setDescriptions(SnomedDescriptions.init(descriptions_file));
		sso = StatementSnomedOntology.create(snomedOntology);
		long end = System.currentTimeMillis();
		log.info("Init in: " + ((end - beg) / 1000 + " secs"));
	}

	// TODO dup of other IT
	public boolean hasGroupedAbsent(Concept con) {
		return con.getDefinitions().stream().flatMap(def -> def.getRoleGroups().stream())
				.anyMatch(rg -> AbsentSubsumption.hasAbsent(rg));
	}

	// 160266009 |No family history of clinical finding (situation)|

	@Test
	public void isSubsumedBy() {
		SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(snomedOntology);
		reasoner.flush();
		Set<Long> subs = reasoner.getSubConcepts(160266009, false);
		for (Concept con : sso.getStatementConceptsDefiningDependentOrder()) {
//			if (!subs.contains(con.getId()))
//				continue;
			for (Concept sup : reasoner.getSuperConcepts(con)) {
				if (hasGroupedAbsent(con) && hasGroupedAbsent(sup)) {
					if (sso.isSubsumedBy(con, sup) || !sso.isSubsumedBy(sup, con)) {
						log.info("Con: " + snomedOntology.getFsn(con.getId()));
						log.info("\tSup: " + snomedOntology.getFsn(sup.getId()));
						log.error("\t" + sso.isSubsumedBy(con, sup) + " " + sso.isSubsumedBy(sup, con) + " "
								+ con.getDefinitions().getFirst().getDefinitionType());
					}
				}
//				assertFalse(sso.isSubsumedBy(con, sup));
//				assertTrue(sso.isSubsumedBy(sup, con));
			}
		}
	}

//	@Test
	public void classify() {
		long beg = System.currentTimeMillis();
		SnomedIsa isas = sso.classify();
		long end = System.currentTimeMillis();
		log.info("Classify in: " + ((end - beg) / 1000 + " secs"));
		SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(snomedOntology);
		reasoner.flush();
		for (long id : isas.getOrderedConcepts()) {
			if (id == SnomedIds.root)
				continue;
			Set<Long> exp = reasoner.getSubConcepts(id);
			Set<Long> act = isas.getChildren(id);
			if (!exp.equals(act)) {
				log.info("Con: " + isas.getChildren(id).size() + " - " + snomedOntology.getFsn(id));
				Set<Long> mis = new HashSet<>(exp);
				mis.removeAll(act);
				mis.forEach(child -> log.info("\tMis: " + snomedOntology.getFsn(child)));
				Set<Long> ext = new HashSet<>(act);
				ext.removeAll(exp);
				ext.forEach(child -> log.info("\tExt: " + snomedOntology.getFsn(child)));
//				log.info("Con: " + isas.getChildren(id).size() + " - " + snomedOntology.getFsn(id));
//				isas.getChildren(id).stream().map(child -> snomedOntology.getFsn(child)).sorted()
//						.forEach(child -> log.info("\t" + child));
			}
//			assertEquals(reasoner.getSubConcepts(id), isas.getChildren(id));
		}
	}

}
