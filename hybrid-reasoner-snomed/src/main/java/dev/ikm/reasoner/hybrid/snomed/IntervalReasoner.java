package dev.ikm.reasoner.hybrid.snomed;

import java.util.HashMap;
import java.util.List;

import org.semanticweb.elk.owl.interfaces.ElkClassExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.ConcreteRole;
import dev.ikm.elk.snomed.model.ConcreteRoleType;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.DefinitionType;
import dev.ikm.elk.snomed.model.Role;
import dev.ikm.elk.snomed.model.RoleType;

public class IntervalReasoner extends SnomedOntologyReasoner {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(IntervalReasoner.class);

	private List<ConcreteRoleType> intervalRoles;

	private HashMap<String, Concept> interval_concepts = new HashMap<>();

	private long next_interval_concept_id = Long.MIN_VALUE;

	private Concept interval_concept;

	protected IntervalReasoner(List<ConcreteRoleType> intervalRoles) {
		super();
		this.intervalRoles = intervalRoles;
	}

	public static IntervalReasoner create(SnomedOntology snomedOntology, List<ConcreteRoleType> intervalRoles) {
		IntervalReasoner sor = new IntervalReasoner(intervalRoles);
		sor.init(snomedOntology);
		sor.computeInferences();
		return sor;
	}

	private void processIntervalConcepts() {
		for (String interval_str1 : interval_concepts.keySet()) {
			Concept interval_con1 = interval_concepts.get(interval_str1);
			Interval interval1 = Interval.fromString(interval_str1);
			for (String interval_str2 : interval_concepts.keySet()) {
				if (interval_str1.equals(interval_str2))
					continue;
				Interval interval2 = Interval.fromString(interval_str2);
				Concept interval_con2 = interval_concepts.get(interval_str2);
				if (interval1.contains(interval2) && interval1.getUnitOfMeasure() == interval2.getUnitOfMeasure())
					interval_con2.getDefinitions().getFirst().addSuperConcept(interval_con1);
			}
		}
		interval_concepts.values().forEach(this::process);
	}

	@Override
	protected void process() {
		interval_concept = new Concept(next_interval_concept_id++);
		Definition def = new Definition();
		def.setDefinitionType(DefinitionType.SubConcept);
		interval_concept.addDefinition(def);
		process(interval_concept);
		super.process();
		processIntervalConcepts();
	}

	@Override
	protected void process(ConcreteRoleType crt) {
		if (intervalRoles.contains(crt)) {
			process(new RoleType(crt.getId()));
		} else {
			super.process(crt);
		}
	}

	@Override
	protected ElkClassExpression process(ConcreteRole concreteRole) {
		if (intervalRoles.contains(concreteRole.getConcreteRoleType())) {
			String interval_value_str = Interval.fromString(concreteRole.getValue()).toString();
			if (interval_concepts.get(interval_value_str) == null) {
				Concept new_con = new Concept(next_interval_concept_id++);
				Definition new_def = new Definition();
				new_def.setDefinitionType(DefinitionType.SubConcept);
				new_def.addSuperConcept(interval_concept);
				new_con.addDefinition(new_def);
				process(new_con);
				interval_concepts.put(interval_value_str, new_con);
			}
			Concept interval_concept = interval_concepts.get(interval_value_str);
			Role interval_role = new Role(new RoleType(concreteRole.getConcreteRoleType().getId()), interval_concept);
			return process(interval_role);

		} else {
			return super.process(concreteRole);
		}
	}

}
