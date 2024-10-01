package dev.ikm.reasoner.hybrid.snomed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.DefinitionType;

@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyAbsentTestIT extends StatementSnomedOntologyTestBase {

	private static final Logger log = LoggerFactory.getLogger(StatementSnomedOntologyAbsentTestIT.class);

	// 160266009 |No family history of clinical finding (situation)|

	@Test
	public void isSubsumedBy() {
		SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(snomedOntology);
		reasoner.flush();
		int cnt = 0;
		for (Concept con : sso.getStatementConceptsDefiningDependentOrder()) {
			if (!AbsentSubsumption.hasGroupedAbsentSnomed(con))
				continue;
			if (con.getDefinitions().getFirst().getDefinitionType() == DefinitionType.SubConcept)
				continue;
			for (Concept sup : reasoner.getSuperConcepts(con)) {
				if (!AbsentSubsumption.hasGroupedAbsentSnomed(sup))
					continue;
				if (sup.getDefinitions().getFirst().getDefinitionType() == DefinitionType.SubConcept)
					continue;
				if (sso.isSubsumedBy(sup, con)) {
					cnt++;
				} else {
					log.info("Con: " + snomedOntology.getFsn(con.getId()));
					log.info("\tSup: " + snomedOntology.getFsn(sup.getId()));
					log.error("\t" + sso.isSubsumedBy(con, sup) + " " + sso.isSubsumedBy(sup, con) + " "
							+ con.getDefinitions().getFirst().getDefinitionType());
				}
//				assertFalse(sso.isSubsumedBy(con, sup));
//				assertTrue(sso.isSubsumedBy(sup, con));
			}
		}
		assertEquals(433, cnt);
	}

	@Test
	public void noFH() {
		SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(snomedOntology);
		reasoner.flush();
		// 160266009 |No family history of clinical finding (situation)|
		Set<Long> subs = reasoner.getSubConcepts(160266009, false);
		for (long sub : subs) {
			Concept con = snomedOntology.getConcept(sub);
			Set<Concept> sups = con.getDefinitions().getFirst().getSuperConcepts();
			if (!sups.stream().map(Concept::getId).toList().equals(List.of(160266009l)))
				log.info("Def: " + snomedOntology.getFsn(con.getId()) + " " + sups + " "
						+ snomedOntology.getFsn(sups.iterator().next().getId()));
		}
	}

	private void list(StatementSnomedOntology sso, long con, int depth) {
		log.info("\t".repeat(depth) + con + " " + snomedOntology.getFsn(con));
		for (long sub : sso.getSubConcepts(con)) {
			list(sso, sub, depth + 1);
		}
	}

	@Test
	public void classify() {
		long beg = System.currentTimeMillis();
		SnomedIsa isas = sso.classify();
		long end = System.currentTimeMillis();
		log.info("Classify in: " + ((end - beg) / 1000 + " secs"));
//		SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(snomedOntology);
//		reasoner.flush();
		// 57177007 |Family history with explicit context (situation)|
		// 160266009 |No family history of clinical finding (situation)|
//		list(sso, 57177007, 0);
//		for (long id : isas.getOrderedConcepts()) {
//			if (id == SnomedIds.root)
//				continue;
//			Set<Long> exp = reasoner.getSubConcepts(id);
//			Set<Long> act = isas.getChildren(id);
//			if (!exp.equals(act)) {
//				log.info("Con: " + isas.getChildren(id).size() + " - " + snomedOntology.getFsn(id));
//				Set<Long> mis = new HashSet<>(exp);
//				mis.removeAll(act);
//				mis.forEach(child -> log.info("\tMis: " + snomedOntology.getFsn(child)));
//				Set<Long> ext = new HashSet<>(act);
//				ext.removeAll(exp);
//				ext.forEach(child -> log.info("\tExt: " + snomedOntology.getFsn(child)));
////				log.info("Con: " + isas.getChildren(id).size() + " - " + snomedOntology.getFsn(id));
////				isas.getChildren(id).stream().map(child -> snomedOntology.getFsn(child)).sorted()
////						.forEach(child -> log.info("\t" + child));
//			}
////			assertEquals(reasoner.getSubConcepts(id), isas.getChildren(id));
//		}
	}

}
