package dev.ikm.reasoner.hybrid.snomed;

import java.util.List;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;

import dev.ikm.elk.snomed.NNFSubsumption;
import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.elk.snomed.interval.Interval;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.ConcreteRole;
import dev.ikm.elk.snomed.model.ConcreteRoleType;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.RoleType;

public class IntervalSubsumption extends NNFSubsumption {

	private List<ConcreteRoleType> intervalRoles;

	public IntervalSubsumption(SnomedIsa isa, MutableMap<RoleType, MutableSet<RoleType>> superRoles,
			MutableMap<Concept, Definition> necessaryNormalForm, List<ConcreteRoleType> intervalRoles) {
		super(isa, superRoles, necessaryNormalForm);
		this.intervalRoles = intervalRoles;
	}

	@Override
	protected boolean isSubsumedBy(ConcreteRole role1, ConcreteRole role2) {
		if (role1.getConcreteRoleType().equals(role2.getConcreteRoleType())
				&& intervalRoles.contains(role1.getConcreteRoleType())) {
			Interval i1 = Interval.fromString(role1.getValue());
			Interval i2 = Interval.fromString(role2.getValue());
			return i2.contains(i1);
		}
		return super.isSubsumedBy(role1, role2);
	}

}
