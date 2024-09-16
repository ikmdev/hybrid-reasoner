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

import dev.ikm.elk.snomed.ConceptComparer;
import dev.ikm.elk.snomed.NecessaryNormalFormBuilder;
import dev.ikm.elk.snomed.SnomedConcreteRoles;
import dev.ikm.elk.snomed.SnomedDescriptions;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.SnomedRoles;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.Role;
import dev.ikm.elk.snomed.model.RoleGroup;
import dev.ikm.elk.snomed.owl.OwlTransformer;
import dev.ikm.elk.snomed.owl.SnomedOwlOntology;

@TestInstance(Lifecycle.PER_CLASS)
public class SnomedTestIT {

	private static final Logger log = LoggerFactory.getLogger(SnomedTestIT.class);

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

	private SnomedDescriptions descrs;

	private SnomedOntology snomedOntology;

	private SnomedOntologyReasoner snomedOntologyReasoner;

	private NecessaryNormalFormBuilder nnfb;

	@BeforeAll
	public void init() throws Exception {
		log.info("Test case: " + axioms_file);
		SnomedOwlOntology snomedOwlOntology = SnomedOwlOntology.createOntology();
		snomedOwlOntology.loadOntology(axioms_file);
		descrs = SnomedDescriptions.init(descriptions_file);
		log.info("Load complete");
		snomedOntology = new OwlTransformer().transform(snomedOwlOntology);
		snomedOntologyReasoner = SnomedOntologyReasoner.create(snomedOntology);
		snomedOntologyReasoner.flush();
		log.info("Classify complete");
		nnfb = NecessaryNormalFormBuilder.create(snomedOntology,
				snomedOntologyReasoner.getSuperConcepts(), snomedOntologyReasoner.getSuperRoleTypes(false));
		log.info("Init complete");
		SnomedRoles roles = SnomedRoles.init(rels_file);
		SnomedConcreteRoles values = SnomedConcreteRoles.init(values_file);
		log.info("Generate");
		long beg = System.currentTimeMillis();
		ConceptComparer cc = new ConceptComparer(roles, values);
		nnfb.generate(cc);
		log.info("Generate in " + ((System.currentTimeMillis() - beg) / 1000));
	}

	public boolean hasUngroupedAbsent(Concept con) {
		return con.getDefinitions().stream().anyMatch(def -> AbsentSubsumption.hasAbsent(def.getUngroupedRoles()));
	}

	public boolean hasGroupedAbsent(Concept con) {
		return con.getDefinitions().stream().flatMap(def -> def.getRoleGroups().stream())
				.anyMatch(rg -> AbsentSubsumption.hasAbsent(rg));
	}

	// Example with 2 role groups, one known present, one known absent
	// 433807000 |History of occlusion of cerebral artery without cerebral
	// infarction (situation)|

	@Test
	public void nnf() {
		Set<RoleGroup> rgs = nnfb.getNecessaryNormalForm(433807000).getRoleGroups();
		rgs.forEach(x -> log.info("RG: " + x));
		assertEquals(2, rgs.size());
		assertEquals(1, rgs.stream().filter(AbsentSubsumption::hasAbsent).count());
	}

	@Test
	public void ungroupedAbsent() throws Exception {
		// no absent concept in ungrouped roles
		for (Concept con : snomedOntology.getConcepts()) {
			assertFalse(hasUngroupedAbsent(con));
		}
	}

	@Test
	public void ungroupedAbsentNNF() throws Exception {
		for (Concept con : nnfb.getConcepts()) {
			assertFalse(hasUngroupedAbsent(con));
		}
	}

	@Test
	public void groupedAbsent() throws Exception {
		int gr_cnt = 0;
		for (Concept con : snomedOntology.getConcepts()) {
			if (hasGroupedAbsent(con)) {
				gr_cnt++;
				assertEquals(1, con.getDefinitions().size());
				assertEquals(1, con.getDefinitions().getFirst().getSuperConcepts().size());
			}
		}
		assertEquals(458, gr_cnt);
	}

	@Test
	public void groupedAbsentNNF() throws Exception {
		int gr_cnt = 0;
		for (Concept con : nnfb.getConcepts()) {
			if (hasGroupedAbsent(con)) {
				gr_cnt++;
				assertEquals(1, con.getDefinitions().size());
				assertEquals(1, con.getDefinitions().getFirst().getSuperConcepts().size());
			}
		}
		assertEquals(458, gr_cnt);
	}

	@Test
	public void sups() {
		Set<Concept> sups = new HashSet<>();
		for (Concept con : snomedOntology.getConcepts()) {
			if (hasGroupedAbsent(con))
				sups.addAll(con.getDefinitions().getFirst().getSuperConcepts());
		}
		sups.forEach(x -> log.info("Sup: " + x + " " + descrs.getFsn(x.getId())));
		assertEquals(19, sups.size());
	}

	@Test
	public void disjoint() {
		// check that the statement concepts are disjoint from other hierarchies
		Set<Long> subs = snomedOntologyReasoner.getSubConcepts(StatementSnomedOntology.swec_id, false);
		assertEquals(5428, subs.size());
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

}
