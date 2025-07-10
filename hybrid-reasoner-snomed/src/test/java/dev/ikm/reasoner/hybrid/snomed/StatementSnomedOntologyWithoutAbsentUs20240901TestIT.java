package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionUs;

@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyWithoutAbsentUs20240901TestIT extends StatementSnomedOntologyWihoutAbsentTestBase
		implements SnomedVersionUs {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(StatementSnomedOntologyWithoutAbsentUs20240901TestIT.class);

	@Override
	public String getVersion() {
		return "20240901";
	}

	@Override
	public String getInternationalVersion() {
		return "20240701";
	}

	{
		SnomedAssumptionsTestBase sa = new SnomedAssumptionsUs20240901TestIT();
		concepts_cnt = sa.concepts_cnt;
		swec_concepts_cnt = sa.swec_concepts_cnt;
	}

}
