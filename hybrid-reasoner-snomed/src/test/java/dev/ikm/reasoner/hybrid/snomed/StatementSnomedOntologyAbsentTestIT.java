package dev.ikm.reasoner.hybrid.snomed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.DefinitionType;

@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyAbsentTestIT extends StatementSnomedOntologyTestBase {

	private static final Logger log = LoggerFactory.getLogger(StatementSnomedOntologyAbsentTestIT.class);

	@BeforeAll
	public void init() throws Exception {
		long beg = System.currentTimeMillis();
		super.init();
		sso = StatementSnomedOntology.create(snomedOntology, true);
		long end = System.currentTimeMillis();
		log.info("Init in: " + ((end - beg) / 1000 + " secs"));
	}

	@Test
	public void isSubsumedBy() {
		SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(snomedOntology);
		reasoner.flush();
		int cnt = 0;
		int cnt_n = 0;
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
					cnt_n++;
					log.info("Con: " + snomedOntology.getFsn(con.getId()));
					log.info("\tSup: " + snomedOntology.getFsn(sup.getId()));
					log.error("\t" + sso.isSubsumedBy(con, sup) + " " + sso.isSubsumedBy(sup, con));
				}
			}
		}
		assertEquals(431, cnt);
		assertEquals(7, cnt_n);
	}

	@Test
	public void noFH() {
		SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(snomedOntology);
		reasoner.flush();
		Set<Long> subs = reasoner.getSubConcepts(FamilyHistoryIds.no_family_history_swec, false);
		for (long sub : subs) {
			Concept con = snomedOntology.getConcept(sub);
			Set<Concept> sups = con.getDefinitions().getFirst().getSuperConcepts();
			if (!sups.stream().map(Concept::getId).toList().equals(List.of(FamilyHistoryIds.no_family_history_swec)))
				log.info("Def: " + snomedOntology.getFsn(con.getId()) + " " + sups + " "
						+ snomedOntology.getFsn(sups.iterator().next().getId()));
		}
	}

	@Test
	public void classify() {
		long beg = System.currentTimeMillis();
		sso.classify();
		long end = System.currentTimeMillis();
		log.info("Classify in: " + ((end - beg) / 1000 + " secs"));
		list(FamilyHistoryIds.family_history_swec, 0);
	}

}
