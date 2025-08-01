package dev.ikm.reasoner.hybrid.snomed;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import dev.ikm.elk.snomed.NecessaryNormalFormBuilder;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.model.ConcreteRoleType;

public class IntervalNecessaryNormalFormBuilder extends NecessaryNormalFormBuilder {

	private List<ConcreteRoleType> intervalRoles;

	protected IntervalNecessaryNormalFormBuilder(SnomedOntology snomedOntology, long root,
			List<ConcreteRoleType> intervalRoles) {
		super(snomedOntology, root);
		this.intervalRoles = intervalRoles;
	}

	public static NecessaryNormalFormBuilder create(SnomedOntology snomedOntology,
			HashMap<Long, Set<Long>> superConcepts, HashMap<Long, Set<Long>> superRoleTypes, long root,
			List<ConcreteRoleType> intervalRoles) {
		IntervalNecessaryNormalFormBuilder nnfb = new IntervalNecessaryNormalFormBuilder(snomedOntology, root,
				intervalRoles);
		nnfb.initConcepts(superConcepts);
		nnfb.initRoles(superRoleTypes);
		nnfb.initSubsumption();
		return nnfb;
	}

	protected void initSubsumption() {
		nnfSubsumption = new IntervalSubsumption(isa, superRolesTypes, necessaryNormalForm, intervalRoles);
	}

}
