package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionInternational;

@TestInstance(Lifecycle.PER_CLASS)
public class SnomedAssumptionsInternational20240701TestIT extends SnomedAssumptionsTestBase
		implements SnomedVersionInternational {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SnomedAssumptionsInternational20240701TestIT.class);

	@Override
	public String getVersion() {
		return "20240701";
	}

	{
		concepts_cnt = 368175;
		swec_concepts_cnt = 4961;
		grouped_absent_cnt = 432;
		grouped_absent_nnf_cnt = 432;
	}

}
