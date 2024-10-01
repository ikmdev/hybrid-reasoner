package dev.ikm.reasoner.hybrid.snomed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedDescriptions;
import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.DefinitionType;
import dev.ikm.elk.snomed.owl.OwlTransformer;
import dev.ikm.elk.snomed.owl.SnomedOwlOntology;

@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyAbsentNfhTestIT extends StatementSnomedOntologyTestBase {

	private static final Logger log = LoggerFactory.getLogger(StatementSnomedOntologyAbsentNfhTestIT.class);

	@BeforeAll
	public void init() throws Exception {
		log.info("Test case: " + axioms_file);
		long beg = System.currentTimeMillis();
		SnomedOwlOntology snomedOwlOntology = SnomedOwlOntology.createOntology();
		snomedOwlOntology.loadOntology(axioms_file);
		log.info("Load complete");
		snomedOntology = new OwlTransformer().transform(snomedOwlOntology);
		snomedOntology.setDescriptions(SnomedDescriptions.init(descriptions_file));
		{
			// 160266009 |No family history of clinical finding (situation)|
			Concept nfh_cf = snomedOntology.getConcept(160266009);
			Definition def = nfh_cf.getDefinitions().getFirst();
			def.setDefinitionType(DefinitionType.SubConcept);
			// 57177007 |Family history with explicit context (situation)|
			def.getSuperConcepts().clear();
			def.addSuperConcept(snomedOntology.getConcept(57177007));
			def.getRoleGroups().clear();
			// 704008007 |No family history of asthma (situation)|
			Concept con = snomedOntology.getConcept(704008007);
			con.getDefinitions().getFirst().getSuperConcepts().clear();
			con.getDefinitions().getFirst().addSuperConcept(nfh_cf);
		}
		sso = StatementSnomedOntology.create(snomedOntology, true);
		long end = System.currentTimeMillis();
		log.info("Init in: " + ((end - beg) / 1000 + " secs"));
	}

	private void list(long con, int depth) {
		log.info("\t".repeat(depth) + con + " " + snomedOntology.getFsn(con));
		for (long sub : sso.getSubConcepts(con)) {
			list(sub, depth + 1);
		}
	}

	private void list(SnomedOntologyReasoner reasoner, long con, int depth) {
		log.info("\t".repeat(depth) + con + " " + snomedOntology.getFsn(con));
		for (long sub : reasoner.getSubConcepts(con)) {
			list(reasoner, sub, depth + 1);
		}
	}

//	@Test
	public void noFH() {
		SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(snomedOntology);
		reasoner.flush();
		// 160266009 |No family history of clinical finding (situation)|
		Set<Long> cons = reasoner.getSubConcepts(160266009, false);
		cons.add(160266009l);
		for (long con : cons) {
			log.info(con + " " + snomedOntology.getFsn(con));
			for (Concept par : snomedOntology.getConcept(con).getDefinitions().getFirst().getSuperConcepts()) {
				log.info("\t" + par + " " + snomedOntology.getFsn(par.getId()));
			}
		}
		list(reasoner, 160266009, 0);
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
		list(57177007, 0);
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
