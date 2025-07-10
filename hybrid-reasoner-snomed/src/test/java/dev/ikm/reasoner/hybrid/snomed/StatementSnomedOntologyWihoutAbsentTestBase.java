package dev.ikm.reasoner.hybrid.snomed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.elk.snomed.model.Concept;

@TestInstance(Lifecycle.PER_CLASS)
public abstract class StatementSnomedOntologyWihoutAbsentTestBase extends StatementSnomedOntologyTestBase {

	private static final Logger log = LoggerFactory.getLogger(StatementSnomedOntologyWihoutAbsentTestBase.class);

	private StatementSnomedOntology sso;

	private SnomedIsa isas;

	@BeforeAll
	public void init() throws Exception {
		long beg = System.currentTimeMillis();
		super.init();
		sso = StatementSnomedOntology.create(snomedOntology, false);
		long end = System.currentTimeMillis();
		log.info("Init in: " + ((end - beg) / 1000 + " secs"));
		isas = SnomedIsa.init(rels_file);
	}

	@Test
	public void getConcepts() {
		assertEquals(snomedOntology.getConcepts().size(), sso.getConceptsDefiningDependentOrder().size());
		assertEquals(concepts_cnt, sso.getConceptsDefiningDependentOrder().size());
	}

	@Test
	public void getStatementConcepts() {
		assertEquals(swec_concepts_cnt, sso.getStatementConceptsDefiningDependentOrder().size());
	}

	@Test
	public void getConceptsDefiningDependentOrder() {
		HashSet<Long> priors = new HashSet<>();
		for (Concept con : sso.getConceptsDefiningDependentOrder()) {
			for (long dep : snomedOntology.getDependentOnConcepts(con.getId())) {
				assertTrue(priors.contains(dep));
			}
			priors.add(con.getId());
		}
	}

	@Test
	public void isSubsumedBy() {
		for (Concept con : sso.getStatementConceptsDefiningDependentOrder()) {
			for (long sup_id : isas.getParents(con.getId())) {
				Concept sup = snomedOntology.getConcept(sup_id);
				assertTrue(sso.isSubsumedBy(con, sup));
				assertFalse(sso.isSubsumedBy(sup, con));
			}
		}
	}

	// @Test
	// This takes about a minute
	public void subsumes() {
		for (Concept con1 : sso.getStatementConceptsDefiningDependentOrder()) {
			for (Concept con2 : sso.getStatementConceptsDefiningDependentOrder()) {
				if (con1 != con2)
					sso.isSubsumedBy(con1, con2);
			}
		}
	}

	@Test
	public void classify() throws Exception {
		long beg = System.currentTimeMillis();
		SnomedIsa sso_isas = sso.classify();
		long end = System.currentTimeMillis();
		log.info("Classify in: " + ((end - beg) / 1000 + " secs"));
		for (long id : sso_isas.getOrderedConcepts()) {
			if (id == SnomedIds.root)
				continue;
			Set<Long> exp = isas.getChildren(id);
			Set<Long> act = sso_isas.getChildren(id);
			if (!exp.equals(act)) {
				log.info("Con: " + sso_isas.getChildren(id).size() + " - " + snomedOntology.getFsn(id));
				Set<Long> mis = new HashSet<>(exp);
				mis.removeAll(act);
				mis.forEach(child -> log.info("\tMis: " + snomedOntology.getFsn(child)));
				Set<Long> ext = new HashSet<>(act);
				ext.removeAll(exp);
				ext.forEach(child -> log.info("\tExt: " + snomedOntology.getFsn(child)));
			}
			assertEquals(exp, act);
			assertEquals(exp, sso.getSubConcepts(id));
		}
		for (Concept con : snomedOntology.getConcepts()) {
			long id = con.getId();
			if (isas.hasAncestor(id, SnomedIds.linkage_concept))
				continue;
			assertEquals(isas.getChildren(id), sso.getSubConcepts(id), "" + con);
			assertEquals(isas.getParents(id), sso.getSuperConcepts(id), "" + con);
		}
	}

}
