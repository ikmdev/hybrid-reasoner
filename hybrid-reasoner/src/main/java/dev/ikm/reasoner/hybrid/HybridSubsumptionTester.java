package dev.ikm.reasoner.hybrid;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;

import dev.ikm.reasoner.hybrid.temporal.TemporalExpressionSubsumptionTester;

public class HybridSubsumptionTester {

	private OWLOntology ontology;

	private OWLOntologyManager manager;

	private TemporalExpressionSubsumptionTester temporalSubsumptionTester;

	private String temporalAnnotationOwlIRI;

	public HybridSubsumptionTester(OWLOntology ontology, String temporalAnnotationOwlIRI) {
		this.ontology = ontology;
		this.manager = ontology.getOWLOntologyManager();
		this.temporalAnnotationOwlIRI = temporalAnnotationOwlIRI;
		this.temporalSubsumptionTester = new TemporalExpressionSubsumptionTester();
	}

	public boolean isSubsumedBy(OWLClassExpression subNodeClassExpression, OWLClassExpression superNodeClassExpression,
			OWLReasoner owlReasoner) {
		if (pattern(subNodeClassExpression, superNodeClassExpression, Pattern.BOTH_OWLCLASS)) {
			return isSubsumedBy((OWLClass) subNodeClassExpression, (OWLClass) superNodeClassExpression, owlReasoner);
		} else if (pattern(subNodeClassExpression, superNodeClassExpression, Pattern.BOTH_OWLOBJECTSOMEVALUES)) {
			return isSubsumedBy((OWLObjectSomeValuesFrom) subNodeClassExpression,
					(OWLObjectSomeValuesFrom) superNodeClassExpression, owlReasoner);
		} else if (pattern(subNodeClassExpression, superNodeClassExpression, Pattern.BOTH_OWLPROPERTY)) {
			return isSubsumedBy((OWLObjectProperty) subNodeClassExpression,
					(OWLObjectProperty) superNodeClassExpression);
		} else if (pattern(subNodeClassExpression, superNodeClassExpression, Pattern.BOTH_OWLOBJECTINTERSECTION)) {
			return isSubsumedBy((OWLObjectIntersectionOf) subNodeClassExpression,
					(OWLObjectIntersectionOf) superNodeClassExpression, owlReasoner);
		} else
			// Default if the pair of expression types aren't handled above
			// (i.e., the OWLClassExpression types can't be compared or are unrecognized)
			return false;
	}

	public boolean isSubsumedBy(OWLObjectIntersectionOf subNodeClassExpression,
			OWLObjectIntersectionOf superNodeClassExpression, OWLReasoner owlReasoner) {
		Set<OWLClassExpression> subExpressions = subNodeClassExpression.getOperands();
		Set<OWLClassExpression> superExpressions = superNodeClassExpression.getOperands();
		for (OWLClassExpression subExpr : subExpressions) {
			for (OWLClassExpression superExpr : superExpressions) {
				if (subExpr == superExpr)
					return true;
				return superExpressions.stream()
						.allMatch(p2 -> subExpressions.stream().anyMatch(p1 -> isSubsumedBy(p1, p2, owlReasoner)));
			}
		}
		return false;
	}

	public boolean isSubsumedBy(OWLObjectSomeValuesFrom subNodeClassExpression,
			OWLObjectSomeValuesFrom superNodeClassExpression, OWLReasoner owlReasoner) {
		OWLObjectProperty subNodeproperty = subNodeClassExpression.getProperty().getNamedProperty();
		OWLClassExpression subNodeFiller = subNodeClassExpression.getFiller();
		OWLObjectProperty superNodeProperty = superNodeClassExpression.getProperty().getNamedProperty();
		OWLClassExpression superNodeFiller = superNodeClassExpression.getFiller();
		if (isSubsumedBy(subNodeproperty, superNodeProperty) && isSubsumedBy(intersectionIfSingleton(subNodeFiller),
				intersectionIfSingleton(superNodeFiller), owlReasoner))
			return true;
		return false;
	}

	public boolean isSubsumedBy(OWLObjectProperty subNodePropertyExpression,
			OWLObjectProperty superNodePropertyExpression) {
		// TODO Later, generalize to handle property hierarchies and chains in the
		// kernel ontology
		if (subNodePropertyExpression.equals(superNodePropertyExpression))
			return true;
		if (hasTemporalDefinition(subNodePropertyExpression) && hasTemporalDefinition(superNodePropertyExpression))
			return isPropertySubsumedByAsInferred(subNodePropertyExpression, superNodePropertyExpression);
		if (!hasTemporalDefinition(subNodePropertyExpression) && !hasTemporalDefinition(superNodePropertyExpression))
			return isPropertySubsumedByAsStated(subNodePropertyExpression, superNodePropertyExpression);
		return false;
	}

	public boolean isSubsumedBy(OWLClass subNodeOwlClass, OWLClass superNodeOwlClass, OWLReasoner owlReasoner) {
		// equivalent classes subsume each other
		if (subNodeOwlClass.equals(superNodeOwlClass))
			return true;
		Set<OWLClass> subClassSet = owlReasoner.getSubClasses(superNodeOwlClass, false).getFlattened();
		for (OWLClass candidateOwlSubClass : subClassSet) {
			if (candidateOwlSubClass.equals(subNodeOwlClass))
				return true;
		}
		return false;
	}

	// Reverses subsumption logic if both expressions are negated ("absent");
	// otherwise, standard logic applies.
	public boolean isSubsumedBy(SubsumptionNormalForm subSNF, SubsumptionNormalForm superSNF,
			OWLReasoner kernelReasoner, OWLReasoner statementReasoner) {
		if (subSNF.isAbsent() && superSNF.isAbsent())
			return isSubsumedByInternal(superSNF, subSNF, kernelReasoner, statementReasoner);
		else
			return isSubsumedByInternal(subSNF, superSNF, kernelReasoner, statementReasoner);
	}

	private boolean isSubsumedByInternal(SubsumptionNormalForm subSNF, SubsumptionNormalForm superSNF,
			OWLReasoner kernelReasoner, OWLReasoner statementReasoner) {
		boolean focusConceptsSubsumedBy = superSNF.getFocusConcepts().stream().allMatch(conSup -> subSNF
				.getFocusConcepts().stream().anyMatch(conSub -> isSubsumedBy(conSub, conSup, statementReasoner)));
		boolean rolesSubsumedBy = superSNF.getUngroupedProps().stream().allMatch(propSup -> subSNF.getUngroupedProps()
				.stream().anyMatch(propSub -> isSubsumedBy(propSub, propSup, kernelReasoner)));
		boolean isSubsumedBy = focusConceptsSubsumedBy && rolesSubsumedBy;
		return isSubsumedBy;
	}

	private boolean isPropertySubsumedByAsStated(OWLObjectProperty subNodePropertyExpression,
			OWLObjectProperty superNodePropertyExpression) {
		if (subNodePropertyExpression.equals(superNodePropertyExpression))
			return true;
		if (subNodePropertyExpression.isOWLTopObjectProperty())
			return false;
		Set<OWLObjectProperty> directParentPropertiesAsStated = getDirectParentPropertiesAsStated(
				subNodePropertyExpression);
		for (OWLObjectProperty candidateSubsumedProperty : directParentPropertiesAsStated) {
			if (isPropertySubsumedByAsStated(candidateSubsumedProperty, superNodePropertyExpression))
				return true;
		}
		return false;
	}

	private boolean isPropertySubsumedByAsInferred(OWLObjectProperty subNodePropertyExpression,
			OWLObjectProperty superNodePropertyExpression) {
		String superExpression = getTemporalPropertyDefinitionAsString(superNodePropertyExpression);
		String subExpression = getTemporalPropertyDefinitionAsString(subNodePropertyExpression);
		return temporalSubsumptionTester.subsumes(superExpression, subExpression);
	}

	private boolean hasTemporalDefinition(OWLObjectProperty owlProperty) {
		Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(owlProperty, ontology);
		for (OWLAnnotation owlAnnotation : annotations) {
			if (owlAnnotation.getProperty().getIRI().toString().equals(this.temporalAnnotationOwlIRI))
				return true;
		}
		return false;
	}

	private String getTemporalPropertyDefinitionAsString(OWLObjectProperty propertyExpression) {
		String definition = "";
		Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(propertyExpression, ontology);
		for (OWLAnnotation owlAnnotation : annotations) {
			if (owlAnnotation.getProperty().getIRI().toString().equals(this.temporalAnnotationOwlIRI)) {
				String quotedValue = owlAnnotation.getValue().toString();
				// remove beginning/ending double quotes
				return quotedValue.substring(1, quotedValue.length() - 1);
			}
		}
		return definition;
	}

	private Set<OWLObjectProperty> getDirectParentPropertiesAsStated(OWLObjectProperty subNodePropertyExpression) {
		Set<OWLObjectProperty> parentProperties = new HashSet<>();
		Set<OWLObjectPropertyAxiom> subPropertyAxioms = this.ontology.getAxioms(subNodePropertyExpression,
				Imports.EXCLUDED);
		for (OWLObjectPropertyAxiom subPropertyAxiom : subPropertyAxioms) {
			if (subPropertyAxiom instanceof OWLSubObjectPropertyOfAxiom) {
				OWLObjectProperty superProperty = (((OWLSubObjectPropertyOfAxiom) subPropertyAxiom).getSuperProperty()
						.asOWLObjectProperty());
				parentProperties.add(superProperty);
			}
		}
		return parentProperties;
	}

	private boolean pattern(OWLClassExpression subNodeClassExpression, OWLClassExpression superNodeClassExpression,
			Pattern targetPattern) {
		Pattern thisPattern = Pattern.NOTHANDLEDPATTERN;
		if (subNodeClassExpression instanceof OWLClass && superNodeClassExpression instanceof OWLClass)
			thisPattern = Pattern.BOTH_OWLCLASS;
		else if (subNodeClassExpression instanceof OWLObjectSomeValuesFrom
				&& superNodeClassExpression instanceof OWLObjectSomeValuesFrom)
			thisPattern = Pattern.BOTH_OWLOBJECTSOMEVALUES;
		else if (subNodeClassExpression instanceof OWLObjectProperty
				&& superNodeClassExpression instanceof OWLObjectProperty)
			thisPattern = Pattern.BOTH_OWLPROPERTY;
		else if (subNodeClassExpression instanceof OWLObjectIntersectionOf
				&& superNodeClassExpression instanceof OWLObjectIntersectionOf)
			thisPattern = Pattern.BOTH_OWLOBJECTINTERSECTION;
		return (thisPattern == targetPattern);
	}

	private OWLClassExpression intersectionIfSingleton(OWLClassExpression classExpression) {
		if (!(classExpression instanceof OWLObjectIntersectionOf)) {
			return manager.getOWLDataFactory().getOWLObjectIntersectionOf(classExpression);
		} else {
			return classExpression;
		}
	}

	private enum Pattern {
		BOTH_OWLCLASS, BOTH_OWLPROPERTY, BOTH_OWLOBJECTINTERSECTION, BOTH_OWLOBJECTSOMEVALUES, NOTHANDLEDPATTERN;
	}

}
