package dev.ikm.reasoner.hybrid.snomed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.OwlElTransformer;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.interval.Interval;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.ConcreteRole;
import dev.ikm.elk.snomed.model.ConcreteRoleType;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.DefinitionType;
import dev.ikm.elk.snomed.model.Role;
import dev.ikm.elk.snomed.model.RoleType;
import dev.ikm.elk.snomed.owlel.OwlElOntology;

public class IntervalReasonerTest {

	private static final Logger log = LoggerFactory.getLogger(IntervalReasonerTest.class);

	@Test
	public void classify() throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("src/test/resources/DataHasIntervalValue.owl"));
		OwlElOntology ontology = new OwlElOntology();
		ontology.load(lines);
		SnomedOntology snomedOntology = new OwlElTransformer().transform(ontology);
		List<ConcreteRoleType> intervalRoles = List.of(snomedOntology.getConcreteRoleType(201));
		HashMap<Long, RoleType> intervalRoleRoles = new HashMap<>();
		for (ConcreteRoleType role : intervalRoles) {
			RoleType new_role = new RoleType(-role.getId());
			intervalRoleRoles.put(role.getId(), new_role);
			snomedOntology.addRoleType(new_role);
		}
		HashMap<String, Concept> interval_concepts = new HashMap<>();
		long next_interval_concept_id = Long.MIN_VALUE;
		Concept interval_con = new Concept(next_interval_concept_id++);
		{
			Definition new_def = new Definition();
			new_def.setDefinitionType(DefinitionType.SubConcept);
			interval_con.addDefinition(new_def);
			snomedOntology.addConcept(interval_con);
		}
		for (Concept con : new ArrayList<>(snomedOntology.getConcepts())) {
			for (Definition def : con.getDefinitions()) {
				for (ConcreteRole role : def.getUngroupedConcreteRoles()) {
					if (intervalRoles.contains(role.getConcreteRoleType())) {
						String interval_value_str = Interval.fromString(role.getValue()).toString();
						if (interval_concepts.get(interval_value_str) == null) {
							Concept new_con = new Concept(next_interval_concept_id++);
							log.info("Huh: " + new_con.getId() + " " + Long.MIN_VALUE);
							Definition new_def = new Definition();
							new_def.setDefinitionType(DefinitionType.SubConcept);
							new_def.addSuperConcept(interval_con);
							new_con.addDefinition(new_def);
							snomedOntology.addConcept(new_con);
							interval_concepts.put(interval_value_str, new_con);
						}
						Concept interval_concept = interval_concepts.get(interval_value_str);
						def.getUngroupedConcreteRoles().remove(role);
						Role new_role = new Role(intervalRoleRoles.get(role.getConcreteRoleType().getId()),
								interval_concept);
						def.getUngroupedRoles().add(new_role);
					}
				}
			}
		}
		for (String interval_str1 : interval_concepts.keySet()) {
			Concept interval_con1 = interval_concepts.get(interval_str1);
			Interval interval1 = Interval.fromString(interval_str1);
			for (String interval_str2 : interval_concepts.keySet()) {
				if (interval_str1.equals(interval_str2))
					continue;
				Interval interval2 = Interval.fromString(interval_str2);
				Concept interval_con2 = interval_concepts.get(interval_str2);
				if (interval1.contains(interval2))
					interval_con2.getDefinitions().getFirst().addSuperConcept(interval_con1);
			}
		}
		SnomedOntologyReasoner sor = SnomedOntologyReasoner.create(snomedOntology);
		snomedOntology.getConcepts().stream().filter(con -> con.getId() != 0)
				.sorted(Comparator.comparing(Concept::getId)).forEach(con -> log
						.info("\n" + con + "\n" + con.getDefinitions().getFirst() + "\n" + sor.getSuperConcepts(con)));
		assertEquals(Set.of(0l), sor.getSuperConcepts(1));
		assertEquals(Set.of(1l), sor.getSuperConcepts(2));
		assertEquals(Set.of(1l), sor.getSuperConcepts(3));
		assertEquals(Set.of(2l, 3l), sor.getSuperConcepts(4));
	}

	@Test
	public void classifyIR() throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("src/test/resources/DataHasIntervalValue.owl"));
		OwlElOntology ontology = new OwlElOntology();
		ontology.load(lines);
		SnomedOntology snomedOntology = new OwlElTransformer().transform(ontology);
		List<ConcreteRoleType> intervalRoles = List.of(snomedOntology.getConcreteRoleType(201));
		IntervalReasoner ir = IntervalReasoner.create(snomedOntology, intervalRoles);
		snomedOntology.getConcepts().stream().filter(con -> con.getId() != 0)
				.sorted(Comparator.comparing(Concept::getId)).forEach(con -> log
						.info("\n" + con + "\n" + con.getDefinitions().getFirst() + "\n" + ir.getSuperConcepts(con)));
		assertEquals(Set.of(0l), ir.getSuperConcepts(1));
		assertEquals(Set.of(1l), ir.getSuperConcepts(2));
		assertEquals(Set.of(1l), ir.getSuperConcepts(3));
		assertEquals(Set.of(2l, 3l), ir.getSuperConcepts(4));
	}

}
