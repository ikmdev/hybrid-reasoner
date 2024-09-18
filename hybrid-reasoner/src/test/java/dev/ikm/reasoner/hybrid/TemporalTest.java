package dev.ikm.reasoner.hybrid;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemporalTest {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TemporalTest.class);

	@Test
	public void ExamplePtncya() throws Exception {
		String dir = "src/test/resources";
		String test_case = "SWEC-Ontology-Example-PROPERTY-TESTING-NO-CLASSIFIED-YES-ANNOTATION";
		HybridReasonerTestUtil.loadAndCompare(dir, test_case, "owx");
	}

}
