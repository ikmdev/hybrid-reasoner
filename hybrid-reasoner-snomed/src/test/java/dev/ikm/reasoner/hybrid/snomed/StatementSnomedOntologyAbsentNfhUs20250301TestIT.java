package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionUs;

@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyAbsentNfhUs20250301TestIT extends StatementSnomedOntologyAbsentNfhTestIBase
		implements SnomedVersionUs {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(StatementSnomedOntologyAbsentNfhUs20250301TestIT.class);

	@Override
	public String getVersion() {
		return "20250301";
	}

	@Override
	public String getInternationalVersion() {
		return "20250101";
	}

	{
		nfh_sub_concept_cnt = 13;
	}

}
