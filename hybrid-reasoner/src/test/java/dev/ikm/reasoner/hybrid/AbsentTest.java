package dev.ikm.reasoner.hybrid;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbsentTest {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbsentTest.class);

	@Test
	public void familyHistory() throws Exception {
		String dir = "src/test/resources";
		String test_case = "SWEC-Ontology-Family-History";
		HybridReasonerTestUtil.loadAndCompare(dir, test_case, "ofn");
	}

	@Test
	public void familyHistorySnomed() throws Exception {
		String dir = "src/test/resources";
		String test_case = "SWEC-Ontology-Family-History-SNOMED";
		HybridReasonerTestUtil.loadAndCompare(dir, test_case, "ofn");
	}

}
