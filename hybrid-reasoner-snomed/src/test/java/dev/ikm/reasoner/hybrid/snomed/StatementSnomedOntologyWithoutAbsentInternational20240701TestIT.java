package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionInternational;

@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyWithoutAbsentInternational20240701TestIT
		extends StatementSnomedOntologyWithoutAbsentTestBase implements SnomedVersionInternational {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(StatementSnomedOntologyWithoutAbsentInternational20240701TestIT.class);

	@Override
	public String getVersion() {
		return "20240701";
	}

	{
		SnomedAssumptionsTestBase sa = new SnomedAssumptionsInternational20240701TestIT();
		concepts_cnt = sa.concepts_cnt;
		swec_concepts_cnt = sa.swec_concepts_cnt;
	}

}
