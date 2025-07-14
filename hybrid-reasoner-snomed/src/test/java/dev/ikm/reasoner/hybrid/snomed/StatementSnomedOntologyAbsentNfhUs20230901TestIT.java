package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionUs;

@Deprecated
@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyAbsentNfhUs20230901TestIT extends StatementSnomedOntologyAbsentNfhTestIBase
		implements SnomedVersionUs {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(StatementSnomedOntologyAbsentNfhUs20230901TestIT.class);

	@Override
	public String getVersion() {
		return "20230901";
	}

	@Override
	public String getInternationalVersion() {
		return "20230630";
	}

	{
		nfh_sub_concept_cnt = 11;
	}

}
