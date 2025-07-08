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

import dev.ikm.elk.snomed.ConceptComparer;
import dev.ikm.elk.snomed.NecessaryNormalFormBuilder;
import dev.ikm.elk.snomed.SnomedLoader;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.Role;
import dev.ikm.elk.snomed.model.RoleGroup;

@TestInstance(Lifecycle.PER_CLASS)
public abstract class SnomedAssumptionsTestBase extends StatementSnomedOntologyTestBase {

	private static final Logger log = LoggerFactory.getLogger(SnomedAssumptionsTestBase.class);

	private SnomedOntologyReasoner snomedOntologyReasoner;

	private NecessaryNormalFormBuilder nnfb;

	protected int swec_concepts_cnt = -1;

	protected int grouped_absent_cnt = -1;

	protected int grouped_absent_nnf_cnt = -1;

	@BeforeAll
	public void init() throws Exception {
		super.init();
		snomedOntologyReasoner = SnomedOntologyReasoner.create(snomedOntology);
		snomedOntologyReasoner.flush();
		log.info("Classify complete");
		nnfb = NecessaryNormalFormBuilder.create(snomedOntology, snomedOntologyReasoner.getSuperConcepts(),
				snomedOntologyReasoner.getSuperRoleTypes(false));
		log.info("Init complete");
		SnomedOntology inferredOntology = new SnomedLoader().load(concepts_file, descriptions_file, rels_file,
				values_file);
		log.info("Generate");
		long beg = System.currentTimeMillis();
		ConceptComparer cc = new ConceptComparer(inferredOntology);
		nnfb.generate(cc);
		log.info("Generate in " + ((System.currentTimeMillis() - beg) / 1000));
		assertEquals(0, cc.getMisMatchCount());
	}

	// Example with 2 role groups, one known present, one known absent
	// 433807000 |History of occlusion of cerebral artery without cerebral
	// infarction (situation)|

	@Test
	public void nnf() {
		Set<RoleGroup> rgs = nnfb.getNecessaryNormalForm(433807000).getRoleGroups();
		rgs.forEach(x -> log.info("RG: " + x));
		assertEquals(2, rgs.size());
		assertEquals(1, rgs.stream().filter(AbsentSubsumption::hasAbsentSnomed).count());
	}

	@Test
	public void ungroupedAbsent() throws Exception {
		// no absent concept in ungrouped roles
		for (Concept con : snomedOntology.getConcepts()) {
			assertFalse(AbsentSubsumption.hasUngroupedAbsentSnomed(con));
		}
	}

	@Test
	public void ungroupedAbsentNNF() throws Exception {
		for (Concept con : nnfb.getConcepts()) {
			assertFalse(AbsentSubsumption.hasUngroupedAbsentSnomed(con));
		}
	}

	@Test
	public void groupedAbsent() throws Exception {
		int gr_cnt = 0;
		for (Concept con : snomedOntology.getConcepts()) {
			if (AbsentSubsumption.hasGroupedAbsentSnomed(con)) {
				gr_cnt++;
				assertEquals(1, con.getDefinitions().size());
				assertEquals(1, con.getDefinitions().getFirst().getSuperConcepts().size());
			}
		}
		assertEquals(grouped_absent_cnt, gr_cnt);
	}

	@Test
	public void groupedAbsentNNF() throws Exception {
		int gr_cnt = 0;
		for (Concept con : nnfb.getConcepts()) {
			if (AbsentSubsumption.hasGroupedAbsentSnomed(con)) {
				gr_cnt++;
				assertEquals(1, con.getDefinitions().size());
				assertEquals(1, con.getDefinitions().getFirst().getSuperConcepts().size());
			}
		}
		assertEquals(grouped_absent_nnf_cnt, gr_cnt);
	}

	@Test
	public void sups() {
		Set<Concept> sups = new HashSet<>();
		for (Concept con : snomedOntology.getConcepts()) {
			if (AbsentSubsumption.hasGroupedAbsentSnomed(con))
				sups.addAll(con.getDefinitions().getFirst().getSuperConcepts());
		}
		sups.forEach(x -> log.info("Sup: " + x));
		assertTrue(sups.size() < 20);
	}

	@Test
	public void disjoint() {
		// check that the statement concepts are disjoint from other hierarchies
		Set<Long> subs = snomedOntologyReasoner.getSubConcepts(StatementSnomedOntology.swec_id, false);
		assertEquals(swec_concepts_cnt, subs.size());
		subs.add(StatementSnomedOntology.swec_id);
		for (long id : subs) {
			if (id == StatementSnomedOntology.swec_id)
				continue;
			assertTrue(subs.containsAll(snomedOntologyReasoner.getSuperConcepts(id)));
		}
	}

	@Test
	public void roles() {
		// check that all role values are outside of the statement hierarchy
		Set<Long> subs = snomedOntologyReasoner.getSubConcepts(StatementSnomedOntology.swec_id, false);
		for (long id : subs) {
			Concept concept = snomedOntology.getConcept(id);
			HashSet<Long> deps = new HashSet<>();
			for (Definition def : concept.getDefinitions()) {
				for (Role role : def.getUngroupedRoles()) {
					deps.add(role.getConcept().getId());
				}
				for (RoleGroup rg : def.getRoleGroups()) {
					for (Role role : rg.getRoles()) {
						deps.add(role.getConcept().getId());
					}
				}
			}
			deps.retainAll(subs);
			assertEquals(0, deps.size());
		}
	}

	@Test
	public void singleDef() {
		// check that statements concepts have only 1 definition
		Set<Long> subs = snomedOntologyReasoner.getSubConcepts(StatementSnomedOntology.swec_id, false);
		for (long id : subs) {
			assertEquals(1, snomedOntology.getConcept(id).getDefinitions().size());
		}
	}

	@Test
	public void gci() {
		// check that statements concepts have no gcis
		Set<Long> subs = snomedOntologyReasoner.getSubConcepts(StatementSnomedOntology.swec_id, false);
		for (long id : subs) {
			assertEquals(0, snomedOntology.getConcept(id).getGciDefinitions().size());
		}
	}

	@Test
	public void roots() {
		Set<Long> roots = Set.of(FamilyHistoryIds.clinical_finding_absent_swec,
				FamilyHistoryIds.no_family_history_swec);
		int root_cnt = 0;
		Set<Long> subs = snomedOntologyReasoner.getSubConcepts(StatementSnomedOntology.swec_id, false);
		for (long id : subs) {
			Concept con = snomedOntology.getConcept(id);
			if (!AbsentSubsumption.hasGroupedAbsentSnomed(con))
				continue;
			if (!roots.contains(id)
					&& !snomedOntologyReasoner.getSuperConcepts(id, false).stream().anyMatch(x -> roots.contains(x))) {
				root_cnt++;
				log.info("Not under CFA or NFH: " + con);
				if (!snomedOntologyReasoner.getSuperConcepts(id).contains(FamilyHistoryIds.finding_swec)) {
					log.info("Not under FWEC: " + con);
					snomedOntologyReasoner.getSuperConcepts(con).forEach(x -> log.info("\tSup: " + x));
				}
			}
		}
		assertTrue(root_cnt < 30);
	}

}
