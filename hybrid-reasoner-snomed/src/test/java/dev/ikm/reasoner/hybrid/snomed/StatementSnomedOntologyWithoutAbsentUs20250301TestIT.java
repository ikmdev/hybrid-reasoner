package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionUs;

@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyWithoutAbsentUs20250301TestIT extends StatementSnomedOntologyWihoutAbsentTestBase
		implements SnomedVersionUs {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(StatementSnomedOntologyWithoutAbsentUs20250301TestIT.class);

	@Override
	public String getVersion() {
		return "20250301";
	}

	@Override
	public String getInternationalVersion() {
		return "20250101";
	}

	{
		SnomedAssumptionsTestBase sa = new SnomedAssumptionsUs20250301TestIT();
		concepts_cnt = sa.concepts_cnt;
		swec_concepts_cnt = sa.swec_concepts_cnt;
	}

}
