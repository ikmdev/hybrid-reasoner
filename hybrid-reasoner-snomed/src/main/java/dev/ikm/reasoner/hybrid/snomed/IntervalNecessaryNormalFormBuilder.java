package dev.ikm.reasoner.hybrid.snomed;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import dev.ikm.elk.snomed.NecessaryNormalFormBuilder;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.model.ConcreteRoleType;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.api.set.primitive.MutableLongSet;

public class IntervalNecessaryNormalFormBuilder extends NecessaryNormalFormBuilder {

	private List<ConcreteRoleType> intervalRoles;

	protected IntervalNecessaryNormalFormBuilder(SnomedOntology snomedOntology, long root,
			List<ConcreteRoleType> intervalRoles, ProgressUpdater progressUpdater) {
		super(snomedOntology, root, progressUpdater);
		this.intervalRoles = intervalRoles;
	}

	public static NecessaryNormalFormBuilder create(SnomedOntology snomedOntology,
													MutableLongObjectMap<MutableLongSet> superConcepts,
													MutableLongObjectMap<MutableLongSet> superRoleTypes, long root,
                                                    List<ConcreteRoleType> intervalRoles, ProgressUpdater progressUpdater) {
		IntervalNecessaryNormalFormBuilder nnfb = new IntervalNecessaryNormalFormBuilder(snomedOntology, root,
				intervalRoles, progressUpdater);
		nnfb.initConcepts(superConcepts);
		nnfb.initRoles(superRoleTypes);
		nnfb.initSubsumption();
		return nnfb;
	}

	protected void initSubsumption() {
		nnfSubsumption = new IntervalSubsumption(isa, superRolesTypes, necessaryNormalForm, intervalRoles);
	}

}
