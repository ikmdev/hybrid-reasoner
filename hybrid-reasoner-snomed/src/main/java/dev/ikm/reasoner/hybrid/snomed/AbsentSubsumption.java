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
import java.util.Set;

import dev.ikm.elk.snomed.DefiningSubsumption;
import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.Role;
import dev.ikm.elk.snomed.model.RoleGroup;
import dev.ikm.elk.snomed.model.RoleType;

public class AbsentSubsumption extends DefiningSubsumption {
	
	public static boolean hasAbsent(Set<Role> roles) {
		return roles.stream().anyMatch(x -> x.getRoleType().getId() == StatementSnomedOntology.finding_context_id
				&& x.getConcept().getId() == StatementSnomedOntology.known_absent_id);
	}
	
	public static boolean hasAbsent(RoleGroup rg) {
		return hasAbsent(rg.getRoles());
	}

	public AbsentSubsumption(SnomedOntology ontology, SnomedIsa definingIsa, SnomedIsa isa,
			HashMap<RoleType, Set<RoleType>> superRoles, HashMap<Concept, Definition> necessaryNormalForm) {
		super(ontology, definingIsa, isa, superRoles, necessaryNormalForm);
	}
	
	@Override
	protected boolean isSubsumedBy(RoleGroup rg1, RoleGroup rg2) {
		if (hasAbsent(rg1) && hasAbsent(rg2))
			return super.isSubsumedBy(rg2, rg1);
		return super.isSubsumedBy(rg1, rg2);
	}

}
