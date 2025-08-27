package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedOntology;

@TestInstance(Lifecycle.PER_CLASS)
public class IntervalReasonerInternational20250801TestIT extends IntervalReasonerInternationalTestBase {

	protected SnomedOntology snomedOntology;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(IntervalReasonerInternational20250801TestIT.class);

	@Override
	public String getVersion() {
		return "20250801";
	}

}
