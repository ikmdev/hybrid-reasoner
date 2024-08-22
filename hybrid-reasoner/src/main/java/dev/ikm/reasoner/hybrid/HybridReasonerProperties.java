package dev.ikm.reasoner.hybrid;

public record HybridReasonerProperties( //
		String statementConceptNamespace, String statementConceptName, //
		String absencePropertyNamespace, String absencePropertyName, //
		String absenceValueNamespace, String absenceValueName, //
		String roleGroupPropertyNamespace, String roleGroupPropertyName, //
		String temporalAnnotationOwlIri) {

	public HybridReasonerProperties(String namespace, //
			String statementConceptName, //
			String absencePropertyName, //
			String absenceValueName, //
			String roleGroupPropertyName, //
			String temporalAnnotationOwlIri) {
		this(namespace, statementConceptName, namespace, absencePropertyName, namespace, absenceValueName, namespace,
				roleGroupPropertyName, temporalAnnotationOwlIri);
	}

	public String statementConcept() {
		return statementConceptNamespace + statementConceptName;
	}

	public String absenceProperty() {
		return absencePropertyNamespace + absencePropertyName;
	}

	public String absenceValue() {
		return absenceValueNamespace + absenceValueName;
	}

	public String roleGroupProperty() {
		return roleGroupPropertyNamespace + roleGroupPropertyName;
	}

	public static HybridReasonerProperties SWEC = new HybridReasonerProperties(
			"http://www.hhs.fda.org/shield/SWEC-Ontology#", "Statement-Concept", "Situation-Presence", "Absent",
			"Role-Group", "http://www.w3.org/2000/01/rdf-schema#isDefinedBy");

	public static HybridReasonerProperties SNOMED = new HybridReasonerProperties(
			//
			"http://snomed.info/id/",
			// 243796009 |Situation with explicit context (situation)|
			"243796009",
			// 408729009 |Finding context (attribute)|
			"408729009",
			// 410516002 |Known absent (qualifier value)|
			"410516002",
			// 609096000 |Role group (attribute)|
			"609096000",
			//
			"http://www.w3.org/2000/01/rdf-schema#isDefinedBy");

}
