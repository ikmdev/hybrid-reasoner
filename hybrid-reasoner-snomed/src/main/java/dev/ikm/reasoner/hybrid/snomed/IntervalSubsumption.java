package dev.ikm.reasoner.hybrid.snomed;

/*-
 * #%L
 * ELK Integration with SNOMED
 * %%
 * Copyright (C) 2023 - 2024 Integrated Knowledge Management
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import dev.ikm.elk.snomed.DefiningSubsumption;
import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.ConcreteRole;
import dev.ikm.elk.snomed.model.ConcreteRoleType;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.RoleType;

public class IntervalSubsumption extends DefiningSubsumption {

	private List<ConcreteRoleType> intervalRoles;

	public IntervalSubsumption(SnomedOntology ontology, SnomedIsa definingIsa, SnomedIsa isa,
			HashMap<RoleType, Set<RoleType>> superRoles, HashMap<Concept, Definition> necessaryNormalForm,
			List<ConcreteRoleType> intervalRoles) {
		super(ontology, definingIsa, isa, superRoles, necessaryNormalForm);
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
