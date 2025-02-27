package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestInstance(Lifecycle.PER_CLASS)
public class SnomedAssumptionsUs20240301TestIT extends SnomedAssumptionsTestBase {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SnomedAssumptionsUs20240301TestIT.class);

	protected String getVersion() {
		return "20240301";
	}

	{
		swec_concepts_cnt = 5428;
		grouped_absent_cnt = 458;
		grouped_absent_nnf_cnt = 458;
	}

}
