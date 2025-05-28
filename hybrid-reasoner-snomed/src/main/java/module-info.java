open module dev.ikm.reasoner.hybrid.snomed {

	requires org.slf4j;

	requires dev.ikm.elk.snomed;
	requires dev.ikm.elk.snomed.owlel;
	requires dev.ikm.elk.snomed.reasoner;
	requires dev.ikm.elk.snomed.test;

	exports dev.ikm.reasoner.hybrid.snomed;
}
