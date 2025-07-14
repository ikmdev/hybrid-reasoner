package dev.ikm.reasoner.hybrid.snomed;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionInternational;

@TestInstance(Lifecycle.PER_CLASS)
public class StatementSnomedOntologyAbsentNfhInternational20250101TestIT
		extends StatementSnomedOntologyAbsentNfhTestIBase implements SnomedVersionInternational {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(StatementSnomedOntologyAbsentNfhInternational20250101TestIT.class);

	@Override
	public String getVersion() {
		return "20250101";
	}

	{
		nfh_sub_concept_cnt = 12;
	}

}
