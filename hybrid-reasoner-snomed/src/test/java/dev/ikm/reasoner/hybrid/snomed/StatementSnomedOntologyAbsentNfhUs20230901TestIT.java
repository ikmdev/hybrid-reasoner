package dev.ikm.reasoner.hybrid.snomed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
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
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.DefinitionType;
import dev.ikm.elk.snomed.test.SnomedVersionUs;

@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyAbsentNfhUs20230901TestIT extends StatementSnomedOntologyTestBase
		implements SnomedVersionUs {

	private static final Logger log = LoggerFactory.getLogger(StatementSnomedOntologyAbsentNfhUs20230901TestIT.class);

	@Override
	public String getVersion() {
		return "20230901";
	}

	@Override
	public String getInternationalVersion() {
		return "20230630";
	}

	private StatementSnomedOntology sso;

	@BeforeAll
	public void init() throws Exception {
		long beg = System.currentTimeMillis();
		super.init();
		{
			Concept nfh_cf = snomedOntology.getConcept(FamilyHistoryIds.no_family_history_swec);
			Definition def = nfh_cf.getDefinitions().getFirst();
			def.setDefinitionType(DefinitionType.SubConcept);
			def.getSuperConcepts().clear();
			def.addSuperConcept(snomedOntology.getConcept(FamilyHistoryIds.family_history_swec));
			def.addSuperConcept(snomedOntology.getConcept(FamilyHistoryIds.finding_swec));
			def.getRoleGroups().clear();
			// 704008007 |No family history of asthma (situation)|
			Concept con = snomedOntology.getConcept(704008007);
			con.getDefinitions().getFirst().getSuperConcepts().clear();
			con.getDefinitions().getFirst().addSuperConcept(nfh_cf);
		}
		sso = StatementSnomedOntology.create(snomedOntology, true, SnomedIds.root,
				StatementSnomedOntology.swec_nfh_sctids);
		long end = System.currentTimeMillis();
		log.info("Init in: " + ((end - beg) / 1000 + " secs"));
	}

//	160266009 No family history of clinical finding (situation)
// 		704008007 No family history of asthma (situation)
// 			408553000 No family history of respiratory disease (situation)
// 		160273004 No family history: Hypertension (situation)
// 			160270001 No family history: Cardiovascular disease (situation)
// 				160252004 No family history of cardiovascular accident or stroke (situation)
// 		310251007 No family history: Osteoporosis (situation)
// 		160267000 No family history: Glaucoma (situation)
// 		275106000 No family history: Angina (situation)
// 			266882009 No family history: Ischemic heart disease (situation)
// 				160270001 No family history: Cardiovascular disease (situation)
// 					160252004 No family history of cardiovascular accident or stroke (situation)
// 		297250002 No family history of stroke (situation)
// 			160271002 No family history of stroke and/or transient ischemic attack (situation)
// 			160270001 No family history: Cardiovascular disease (situation)
// 				160252004 No family history of cardiovascular accident or stroke (situation)
// 		313376005 No family history: breast carcinoma (situation)
// 			160250007 No family history of malignancy (situation)
// 		160268005 No family history: Allergy (situation)
// 		160274005 No family history diabetes (situation)
// 		313342001 No family history: Venous thrombosis (situation)
// 			160270001 No family history: Cardiovascular disease (situation)
// 				160252004 No family history of cardiovascular accident or stroke (situation)
// 		408552005 No family history of chronic obstructive pulmonary disease (situation)
// 			408553000 No family history of respiratory disease (situation)

	private void checkParents(long con, Set<Long> expect_parents) {
		Set<Long> actual_parents = sso.getSuperConcepts(con);
		assertEquals(expect_parents, actual_parents, "Concept " + con + " " + snomedOntology.getFsn(con));
	}

	@Test
	public void classify() throws Exception {
		SnomedIsa isas = SnomedIsa.init(rels_file);
		long beg = System.currentTimeMillis();
		sso.classify();
		long end = System.currentTimeMillis();
		log.info("Classify in: " + ((end - beg) / 1000 + " secs"));
		list(sso, FamilyHistoryIds.family_history_swec, 0);
		checkParents(408553000l, Set.of(704008007l, 408552005l));
		checkParents(160270001, Set.of(160273004l, 266882009l, 297250002l, 313342001l));
		checkParents(160250007l, Set.of(313376005l));
		assertEquals(11, sso.getSubConcepts(FamilyHistoryIds.no_family_history_swec).size());
		for (long parent : sso.getSuperConcepts(FamilyHistoryIds.no_family_history_swec)) {
			log.info("Par: " + sso.getOntology().getConcept(parent));
		}
		for (Concept con : snomedOntology.getConcepts()) {
			long id = con.getId();
			if (List.of(SnomedIds.concept_model_object_attribute, SnomedIds.concept_model_data_attribute).contains(id))
				continue;
			if (id == FamilyHistoryIds.no_family_history_swec)
				continue;
			if (isas.hasAncestor(id, FamilyHistoryIds.no_family_history_swec))
				continue;
			if (!isas.getChildren(id).equals(sso.getSubConcepts(id))) {
				log.error("" + con);
				ArrayList<Long> missing = new ArrayList<>(isas.getChildren(id));
				missing.removeAll(sso.getSubConcepts(id));
				log.error("Missing: " + missing);
				ArrayList<Long> extra = new ArrayList<>(sso.getSubConcepts(id));
				extra.removeAll(isas.getChildren(id));
				log.error("Extra: " + extra);
			}
			assertEquals(isas.getChildren(id), sso.getSubConcepts(id), "Children of " + con);
			assertEquals(isas.getParents(id), sso.getSuperConcepts(id), "Parents of " + con);
		}
	}

}
