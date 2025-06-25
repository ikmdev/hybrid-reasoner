package dev.ikm.reasoner.hybrid.snomed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.model.ConcreteRole;
import dev.ikm.elk.snomed.model.ConcreteRoleType;

public class IntervalSubsumptionTest {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(IntervalSubsumptionTest.class);

	@Test
	public void interval() {
		ConcreteRoleType rt = new ConcreteRoleType(1);
		ConcreteRole r1 = new ConcreteRole(rt, "[0,10]", ConcreteRole.ValueType.String);
		ConcreteRole r2 = new ConcreteRole(rt, "[1,9]", ConcreteRole.ValueType.String);
		IntervalSubsumption is = new IntervalSubsumption(null, null, null, null, null, List.of(rt));
		assertTrue(is.isSubsumedBy(r2, r1));
		assertFalse(is.isSubsumedBy(r1, r2));
	}

}
