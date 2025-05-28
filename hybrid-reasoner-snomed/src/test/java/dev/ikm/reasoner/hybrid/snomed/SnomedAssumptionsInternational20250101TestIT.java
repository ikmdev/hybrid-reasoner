package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionInternational;

@TestInstance(Lifecycle.PER_CLASS)
public class SnomedAssumptionsInternational20250101TestIT extends SnomedAssumptionsTestBase implements SnomedVersionInternational {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SnomedAssumptionsInternational20250101TestIT.class);

	@Override
	public String getVersion() {
		return "20250101";
	}

	{
		swec_concepts_cnt = 5007;
		grouped_absent_cnt = 435;
		grouped_absent_nnf_cnt = 435;
	}

}
