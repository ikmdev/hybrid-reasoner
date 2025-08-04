package dev.ikm.reasoner.hybrid.snomed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.OwlElTransformer;
import dev.ikm.elk.snomed.SnomedDescriptions;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.interval.Interval;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.ConcreteRole;
import dev.ikm.elk.snomed.model.ConcreteRoleType;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.DefinitionType;
import dev.ikm.elk.snomed.owlel.OwlElOntology;
import dev.ikm.elk.snomed.test.SnomedVersionInternational;

@TestInstance(Lifecycle.PER_CLASS)
public class IntervalReasonerInternational20250101TestIT extends SnomedTestBase implements SnomedVersionInternational {

	protected SnomedOntology snomedOntology;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(IntervalReasonerInternational20250101TestIT.class);

	@Override
	public String getVersion() {
		return "20250101";
	}

	@BeforeAll
	public void init() throws Exception {
		log.info("Test case: " + axioms_file);
		OwlElOntology ontology = new OwlElOntology();
		ontology.load(axioms_file);
		log.info("Load complete");
		snomedOntology = new OwlElTransformer().transform(ontology);
		snomedOntology.setDescriptions(SnomedDescriptions.init(descriptions_file));
		snomedOntology.setNames();
	}

	// 395507008 |Premature infant (finding)|
	private static final long pi_sctid = 395507008;

	@Test
	public void premature() throws Exception {
		snomedOntology.addConcreteRoleType(new ConcreteRoleType(-1));
		// {
		// SnomedOntologyReasoner sor = SnomedOntologyReasoner.create(snomedOntology);
		// log.info("subConcepts: " + sor.getSubConcepts(pi_sctid, false).size());
		// }
		int cnt = 0;
		for (Concept con : snomedOntology.getConcepts()) {
			String name = con.getName();
			if (!name.matches("^(Baby premature|Premature infant).*weeks.*"))
				continue;
			if (name.contains("diet"))
				continue;
			cnt++;
			log.info(con.getName());
			{
				String regex = "(\\d+)\\-(\\d+) weeks";
				Pattern pat = Pattern.compile(regex);
				Matcher mat = pat.matcher(name);
				if (mat.find()) {
//					log.info("\t" + mat.group(1) + " " + mat.group(2));
					Interval i = Interval
							.fromString("[" + mat.group(1) + "," + mat.group(2) + "]" + TemporalUnits.Weeks.sctid);
					log.info("\t" + i);
					updateDefinition(con, i);
					continue;
				}
			}
			{
				String regex = "less than (\\d+) weeks";
				Pattern pat = Pattern.compile(regex);
				Matcher mat = pat.matcher(name);
				if (mat.find()) {
//					log.info("\t" + mat.group(1));
					Interval i = Interval.fromString("[0," + mat.group(1) + ")" + TemporalUnits.Weeks.sctid);
					log.info("\t" + i);
					updateDefinition(con, i);
					continue;
				}
			}
			{
				String regex = "(\\d+) .*weeks";
				Pattern pat = Pattern.compile(regex);
				Matcher mat = pat.matcher(name);
				if (mat.find()) {
//					log.info("\t" + mat.group(1));
					Interval i = Interval
							.fromString("[" + mat.group(1) + "," + mat.group(1) + "]" + TemporalUnits.Weeks.sctid);
					log.info("\t" + i);
					updateDefinition(con, i);
					continue;
				}
			}
			throw new RuntimeException(con.getName());
		}
		assertEquals(22, cnt);
		List<ConcreteRoleType> intervalRoles = List.of(snomedOntology.getConcreteRoleType(-1));
		IntervalReasoner ir = IntervalReasoner.create(snomedOntology, intervalRoles);
		ir.getSubConcepts(snomedOntology.getConcept(pi_sctid), false).stream()
				.sorted(Comparator.comparing(Concept::getId)).forEach(con -> log
						.info("\n" + con + "\n" + con.getDefinitions().getFirst() + "\n" + ir.getSuperConcepts(con)));
		{
			List<String> lines = ir.getSubConcepts(snomedOntology.getConcept(pi_sctid), false).stream()
					.sorted(Comparator.comparing(Concept::getId))
					.map(con -> con.getId() + "\t"
							+ con.getDefinitions().getFirst().getUngroupedConcreteRoles().iterator().next().getValue())
					.toList();
			Files.write(Paths.get("target", "intervals-" + getEditionDir() + "-" + getVersion() + ".txt"), lines);
		}
		{
			String file_name = "intervals-sups-" + getEditionDir() + "-" + getVersion() + ".txt";
			List<String> lines = ir.getSubConcepts(snomedOntology.getConcept(pi_sctid), false).stream()
					.sorted(Comparator.comparing(Concept::getId)) //
					.flatMap(con -> ir.getSuperConcepts(con).stream() //
							.sorted(Comparator.comparing(Concept::getId)) //
							.map(sup -> List.of(con, sup)))
					.map(con_sup -> con_sup.get(0) + " " + con_sup.get(1)).toList();
			Files.write(Paths.get("target", file_name), lines);
			List<String> expect_lines = Files.lines(Paths.get("src/test/resources", file_name)).toList();
			assertEquals(expect_lines, lines);
		}
		log.info("-".repeat(20));
		print(ir, snomedOntology.getConcept(pi_sctid), 0);
	}

	private void print(IntervalReasoner ir, Concept concept, int i) {
		log.info("\t".repeat(i) + concept);
		ir.getSubConcepts(concept).forEach(x -> print(ir, x, i + 1));
	}

	private void updateDefinition(Concept con, Interval i) {
		Definition def = con.getDefinitions().getFirst();
		def.setDefinitionType(DefinitionType.EquivalentConcept);
		def.getSuperConcepts().clear();
		def.addSuperConcept(snomedOntology.getConcept(pi_sctid));
		def.getRoleGroups().clear();
		def.getUngroupedRoles().clear();
		def.getUngroupedConcreteRoles().clear();
		def.addUngroupedConcreteRole(
				new ConcreteRole(snomedOntology.getConcreteRoleType(-1), i.toString(), ConcreteRole.ValueType.String));
		log.info("\n" + con + "\n" + def);
	}

}
