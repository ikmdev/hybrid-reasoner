package dev.ikm.reasoner.hybrid;

import java.util.List;
import java.util.Set;

import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasoner.ChangeListener;
import org.semanticweb.elk.owlapi.ElkReasonerConfiguration;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.reasoner.Reasoner;
import org.semanticweb.elk.reasoner.completeness.IncompleteResult;
import org.semanticweb.elk.reasoner.config.ReasonerConfiguration;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.reasoner.AxiomNotInProfileException;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.ClassExpressionNotInProfileException;
import org.semanticweb.owlapi.reasoner.FreshEntitiesException;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.semanticweb.owlapi.util.Version;

public class ElkReasonerDelegate implements OWLReasoner {

	private ElkReasoner elkReasoner;

	public ElkReasoner getElkReasoner() {
		return elkReasoner;
	}

	public ElkReasonerDelegate(OWLOntology ontology, ElkReasonerConfiguration config) {
		elkReasoner = new ElkReasonerFactory().createReasoner(ontology, config);
	}

	public Reasoner getInternalReasoner() {
		return elkReasoner.getInternalReasoner();
	}

	public ReasonerConfiguration getConfigurationOptions() {
		return elkReasoner.getConfigurationOptions();
	}

	public void setConfigurationOptions(ReasonerConfiguration config) {
		elkReasoner.setConfigurationOptions(config);
	}

	public void dispose() {
		elkReasoner.dispose();
	}

	public void flush() {
		elkReasoner.flush();
	}

	public IncompleteResult<Node<OWLClass>> computeBottomClassNode() {
		return elkReasoner.computeBottomClassNode();
	}

	public Node<OWLClass> getBottomClassNode() {
		return elkReasoner.getBottomClassNode();
	}

	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		return elkReasoner.getBottomDataPropertyNode();
	}

	public IncompleteResult<Node<OWLObjectPropertyExpression>> computeBottomObjectPropertyNode() {
		return elkReasoner.computeBottomObjectPropertyNode();
	}

	public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
		return elkReasoner.getBottomObjectPropertyNode();
	}

	public BufferingMode getBufferingMode() {
		return elkReasoner.getBufferingMode();
	}

	public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getDataPropertyDomains(arg0, arg1);
	}

	public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual arg0, OWLDataProperty arg1)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getDataPropertyValues(arg0, arg1);
	}

	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual arg0)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getDifferentIndividuals(arg0);
	}

	public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression arg0) throws ReasonerInterruptedException,
			TimeOutException, FreshEntitiesException, InconsistentOntologyException {
		return elkReasoner.getDisjointClasses(arg0);
	}

	public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression arg0)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getDisjointDataProperties(arg0);
	}

	public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression arg0)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getDisjointObjectProperties(arg0);
	}

	public IncompleteResult<Node<OWLClass>> computeEquivalentClasses(OWLClassExpression ce)
			throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		return elkReasoner.computeEquivalentClasses(ce);
	}

	public Node<OWLClass> getEquivalentClasses(OWLClassExpression ce)
			throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		return elkReasoner.getEquivalentClasses(ce);
	}

	public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty arg0) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		return elkReasoner.getEquivalentDataProperties(arg0);
	}

	public IncompleteResult<Node<OWLObjectPropertyExpression>> computeEquivalentObjectProperties(OWLObjectProperty pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.computeEquivalentObjectProperties(pe);
	}

	public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getEquivalentObjectProperties(pe);
	}

	public FreshEntityPolicy getFreshEntityPolicy() {
		return elkReasoner.getFreshEntityPolicy();
	}

	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
		return elkReasoner.getIndividualNodeSetPolicy();
	}

	public IncompleteResult<NodeSet<OWLNamedIndividual>> computeInstances(OWLClassExpression ce, boolean direct)
			throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		return elkReasoner.computeInstances(ce, direct);
	}

	public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct)
			throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		return elkReasoner.getInstances(ce, direct);
	}

	public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression arg0)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getInverseObjectProperties(arg0);
	}

	public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getObjectPropertyDomains(arg0, arg1);
	}

	public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getObjectPropertyRanges(arg0, arg1);
	}

	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual arg0,
			OWLObjectPropertyExpression arg1) throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		return elkReasoner.getObjectPropertyValues(arg0, arg1);
	}

	public Set<OWLAxiom> getPendingAxiomAdditions() {
		return elkReasoner.getPendingAxiomAdditions();
	}

	public Set<OWLAxiom> getPendingAxiomRemovals() {
		return elkReasoner.getPendingAxiomRemovals();
	}

	public List<OWLOntologyChange> getPendingChanges() {
		return elkReasoner.getPendingChanges();
	}

	public Set<InferenceType> getPrecomputableInferenceTypes() {
		return elkReasoner.getPrecomputableInferenceTypes();
	}

	public String getReasonerName() {
		return elkReasoner.getReasonerName();
	}

	public Version getReasonerVersion() {
		return elkReasoner.getReasonerVersion();
	}

	public OWLOntology getRootOntology() {
		return elkReasoner.getRootOntology();
	}

	public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual arg0) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		return elkReasoner.getSameIndividuals(arg0);
	}

	public IncompleteResult<NodeSet<OWLClass>> computeSubClasses(OWLClassExpression ce, boolean direct)
			throws ReasonerInterruptedException, TimeOutException, FreshEntitiesException,
			InconsistentOntologyException, ClassExpressionNotInProfileException {
		return elkReasoner.computeSubClasses(ce, direct);
	}

	public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct)
			throws ReasonerInterruptedException, TimeOutException, FreshEntitiesException,
			InconsistentOntologyException, ClassExpressionNotInProfileException {
		return elkReasoner.getSubClasses(ce, direct);
	}

	public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getSubDataProperties(arg0, arg1);
	}

	public IncompleteResult<NodeSet<OWLObjectPropertyExpression>> computeSubObjectProperties(OWLObjectProperty pe,
			boolean direct) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.computeSubObjectProperties(pe, direct);
	}

	public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getSubObjectProperties(pe, direct);
	}

	public IncompleteResult<NodeSet<OWLClass>> computeSuperClasses(OWLClassExpression ce, boolean direct)
			throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		return elkReasoner.computeSuperClasses(ce, direct);
	}

	public NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce, boolean direct)
			throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		return elkReasoner.getSuperClasses(ce, direct);
	}

	public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getSuperDataProperties(arg0, arg1);
	}

	public IncompleteResult<NodeSet<OWLObjectPropertyExpression>> computeSuperObjectProperties(OWLObjectProperty pe,
			boolean direct) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.computeSuperObjectProperties(pe, direct);
	}

	public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.getSuperObjectProperties(pe, direct);
	}

	public long getTimeOut() {
		return elkReasoner.getTimeOut();
	}

	public IncompleteResult<Node<OWLClass>> computeTopClassNode() {
		return elkReasoner.computeTopClassNode();
	}

	public Node<OWLClass> getTopClassNode() {
		return elkReasoner.getTopClassNode();
	}

	public Node<OWLDataProperty> getTopDataPropertyNode() {
		return elkReasoner.getTopDataPropertyNode();
	}

	public IncompleteResult<Node<OWLObjectPropertyExpression>> computeTopObjectPropertyNode() {
		return elkReasoner.computeTopObjectPropertyNode();
	}

	public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
		return elkReasoner.getTopObjectPropertyNode();
	}

	public IncompleteResult<NodeSet<OWLClass>> computeTypes(OWLNamedIndividual ind, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		return elkReasoner.computeTypes(ind, direct);
	}

	public NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		return elkReasoner.getTypes(ind, direct);
	}

	public IncompleteResult<Node<OWLClass>> computeUnsatisfiableClasses()
			throws ReasonerInterruptedException, TimeOutException, InconsistentOntologyException {
		return elkReasoner.computeUnsatisfiableClasses();
	}

	public Node<OWLClass> getUnsatisfiableClasses()
			throws ReasonerInterruptedException, TimeOutException, InconsistentOntologyException {
		return elkReasoner.getUnsatisfiableClasses();
	}

	public void interrupt() {
		elkReasoner.interrupt();
	}

	public IncompleteResult<Boolean> checkIsConsistent() {
		return elkReasoner.checkIsConsistent();
	}

	public boolean isConsistent() throws ReasonerInterruptedException, TimeOutException {
		return elkReasoner.isConsistent();
	}

	public IncompleteResult<Boolean> checkEntailment(OWLAxiom owlAxiom)
			throws ReasonerInterruptedException, UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
		return elkReasoner.checkEntailment(owlAxiom);
	}

	public boolean isEntailed(OWLAxiom owlAxiom)
			throws ReasonerInterruptedException, UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
		return elkReasoner.isEntailed(owlAxiom);
	}

	public boolean isEntailed(Set<? extends OWLAxiom> owlAxioms)
			throws ReasonerInterruptedException, UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
		return elkReasoner.isEntailed(owlAxioms);
	}

	public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
		return elkReasoner.isEntailmentCheckingSupported(axiomType);
	}

	public boolean isPrecomputed(InferenceType inferenceType) {
		return elkReasoner.isPrecomputed(inferenceType);
	}

	public IncompleteResult<? extends Boolean> checkSatisfiability(OWLClassExpression classExpression)
			throws ReasonerInterruptedException, TimeOutException, ClassExpressionNotInProfileException,
			FreshEntitiesException, InconsistentOntologyException {
		return elkReasoner.checkSatisfiability(classExpression);
	}

	public boolean isSatisfiable(OWLClassExpression classExpression)
			throws ReasonerInterruptedException, TimeOutException, ClassExpressionNotInProfileException,
			FreshEntitiesException, InconsistentOntologyException {
		return elkReasoner.isSatisfiable(classExpression);
	}

	public void precomputeInferences(InferenceType... inferenceTypes)
			throws ReasonerInterruptedException, TimeOutException, InconsistentOntologyException {
		elkReasoner.precomputeInferences(inferenceTypes);
	}

	public void addListener(ChangeListener listener) {
		elkReasoner.addListener(listener);
	}

	public void removeListener(ChangeListener listener) {
		elkReasoner.removeListener(listener);
	}

}
