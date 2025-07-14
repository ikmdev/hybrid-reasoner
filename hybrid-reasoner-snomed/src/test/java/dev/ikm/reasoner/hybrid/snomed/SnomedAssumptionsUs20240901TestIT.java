package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionUs;

@TestInstance(Lifecycle.PER_CLASS)
public class SnomedAssumptionsUs20240901TestIT extends SnomedAssumptionsTestBase implements SnomedVersionUs {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SnomedAssumptionsUs20240901TestIT.class);

	@Override
	public String getVersion() {
		return "20240901";
	}

	@Override
	public String getInternationalVersion() {
		return "20240701";
	}

	{
		concepts_cnt = 375519;
		swec_concepts_cnt = 5509;
		grouped_absent_cnt = 463;
		grouped_absent_nnf_cnt = 463;
	}

}
