package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionInternational;

@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyWithoutAbsentInternational20250101TestIT
		extends StatementSnomedOntologyWihoutAbsentTestBase implements SnomedVersionInternational {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(StatementSnomedOntologyWithoutAbsentInternational20250101TestIT.class);

	@Override
	public String getVersion() {
		return "20250101";
	}

	{
		SnomedAssumptionsTestBase sa = new SnomedAssumptionsInternational20250101TestIT();
		concepts_cnt = sa.concepts_cnt;
		swec_concepts_cnt = sa.swec_concepts_cnt;
	}

}
