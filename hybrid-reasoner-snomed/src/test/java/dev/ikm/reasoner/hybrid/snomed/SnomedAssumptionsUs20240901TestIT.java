package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestInstance(Lifecycle.PER_CLASS)
public class SnomedAssumptionsUs20240901TestIT extends SnomedAssumptionsTestBase {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SnomedAssumptionsUs20240901TestIT.class);

	protected String getVersion() {
		return "20240901";
	}

	{
		swec_concepts_cnt = 5509;
		grouped_absent_cnt = 463;
		grouped_absent_nnf_cnt = 463;
	}

}
