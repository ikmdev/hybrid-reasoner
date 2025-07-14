package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionUs;

@TestInstance(Lifecycle.PER_CLASS)
public class SnomedAssumptionsUs20250301TestIT extends SnomedAssumptionsTestBase implements SnomedVersionUs {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SnomedAssumptionsUs20250301TestIT.class);

	@Override
	public String getVersion() {
		return "20250301";
	}

	@Override
	public String getInternationalVersion() {
		return "20250101";
	}

	{
		concepts_cnt = 378148;
		swec_concepts_cnt = 5555;
		grouped_absent_cnt = 466;
		grouped_absent_nnf_cnt = 466;
	}

}
