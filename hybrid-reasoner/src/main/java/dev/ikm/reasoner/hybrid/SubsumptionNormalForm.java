package dev.ikm.reasoner.hybrid;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

public class SubsumptionNormalForm {

	private boolean subClassOf;

	private boolean isAbsent; // default

	private Set<OWLClass> focusConcepts;

////	private Set<OWLClass> sups;

	private Set<OWLObjectSomeValuesFrom> ungroupedProps;

////	private Set<Set<OWLObjectSomeValuesFrom>> groupedProps;

	public SubsumptionNormalForm() {
		// TODO Auto-generated constructor stub
	}

	public boolean isSubClassOf() {
		return subClassOf;
	}

	public void setSubClassOf(boolean subClass) {
		this.subClassOf = subClass;
	}

	public boolean isAbsent() {
		return this.isAbsent;
	}

	public void setIsAbsent(boolean value) {
		this.isAbsent = value;
	}

	public void setFocusConcepts(Set<OWLClass> focusConcepts) {
		this.focusConcepts = focusConcepts;
	}

	public Set<OWLClass> getFocusConcepts() {
		return focusConcepts;
	}

////	public void setSups(Set<OWLClass> sups) {
////		this.sups = sups;
////	}
////
////	public Set<OWLClass> getSups() {
////		return sups;
////	}

	public Set<OWLObjectSomeValuesFrom> getUngroupedProps() {
		return ungroupedProps;
	}

	public void setUngroupedProps(Set<OWLObjectSomeValuesFrom> ungroupedProps) {
		this.ungroupedProps = ungroupedProps;
	}

////	public Set<Set<OWLObjectSomeValuesFrom>> getGroupedProps() {
////		return groupedProps;
////	}
////
////	public void setGroupedProps(Set<Set<OWLObjectSomeValuesFrom>> groupedProps) {
////		this.groupedProps = groupedProps;
////	}

	public String toString() {
		return absenceToString() + propertiesToString(getUngroupedProps()) + focusConceptsToString(getFocusConcepts());
	}

	public String absenceToString() {
		if (this.isAbsent)
			return "ABSENT statement\n";
		else
			return "PRESENT statement\n";
	}

	public String propertiesToString(Set<OWLObjectSomeValuesFrom> ungroupedProps) {
		String returnString = new String("  Normalized Properties: \n");
		for (OWLObjectSomeValuesFrom owlObjectSomeValuesFrom : ungroupedProps) {
			returnString = returnString + "    " + owlObjectSomeValuesFrom.toString() + "\n";
		}
		return returnString;
	}

	public String focusConceptsToString(Set<OWLClass> focusConcepts) {
		String returnString = new String("  Normalized Focus Concepts: \n");
		for (OWLClass owlClass : focusConcepts) {
			returnString = returnString + "    " + owlClass.toString() + "\n";
		}
		return returnString;
	}

}
