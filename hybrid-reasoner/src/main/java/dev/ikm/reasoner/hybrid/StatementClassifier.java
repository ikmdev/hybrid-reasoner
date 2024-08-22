package dev.ikm.reasoner.hybrid;

import java.util.List;
import java.util.Set;

import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.interfaces.ElkObject;
import org.semanticweb.elk.owl.iris.ElkFullIri;
import org.semanticweb.elk.owl.managers.ElkObjectEntityRecyclingFactory;
import org.semanticweb.elk.owlapi.ElkConverter;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.wrapper.OwlConverter;
import org.semanticweb.elk.reasoner.completeness.Incompleteness;
import org.semanticweb.elk.reasoner.taxonomy.ConcurrentClassTaxonomy;
import org.semanticweb.elk.reasoner.taxonomy.impl.NonBottomGenericTaxonomyNode;
import org.semanticweb.elk.reasoner.taxonomy.model.TaxonomyNode;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatementClassifier {

	private static final Logger log = LoggerFactory.getLogger(StatementClassifier.class);

	private static boolean debug = false;

	private OWLOntology kernelOntology;
	private ElkReasoner kernelElkReasoner;
	private OWLOntology statementOntology;
	private OWLReasoner statementOwlReasoner;
	private HybridReasonerProperties properties;

	private ElkObject.Factory kernelElkObjectFactory;
	private ConcurrentClassTaxonomy kernelTaxonomy;

	private SubsumptionNormalFormBuilder subsumptionNormalFormBuilder;
	private HybridSubsumptionTester subsumptionTester;

	private int countClassified = 0;

	public StatementClassifier(OWLOntology kernelOntology, ElkReasoner kernelElkReasoner, OWLOntology statementOntology,
			OWLReasoner statementOwlReasoner, HybridReasonerProperties properties) throws Exception {
		this.kernelOntology = kernelOntology;
		this.kernelElkReasoner = kernelElkReasoner;
		this.statementOntology = statementOntology;
		this.statementOwlReasoner = statementOwlReasoner;
		this.properties = properties;
		kernelElkObjectFactory = new ElkObjectEntityRecyclingFactory();
		kernelTaxonomy = (ConcurrentClassTaxonomy) Incompleteness
				.getValue(kernelElkReasoner.getInternalReasoner().getTaxonomy());
		createSubsumptionNormalFormBuilder();
		subsumptionTester = new HybridSubsumptionTester(kernelOntology, properties.temporalAnnotationOwlIri());
	}

	private void createSubsumptionNormalFormBuilder() {
		subsumptionNormalFormBuilder = new SubsumptionNormalFormBuilder(kernelOntology, kernelElkReasoner,
				statementOntology, statementOwlReasoner, properties);
		subsumptionNormalFormBuilder.init();
		subsumptionNormalFormBuilder.generate();
	}

	public void classifyStatementConcepts() throws Exception {
		if (debug) {
			log.info("ORIGINAL KERNEL REASONER TAXONOMY - IN CLASSIFIER - BEFORE REMOVAL OF STATEMENT-CONCEPT");
			ReasonerExplorer.printCurrentReasonerTaxonomy(kernelElkReasoner, false);
		}
		String statementConceptIri = properties.statementConcept();
		ElkFullIri iri = new ElkFullIri(properties.statementConcept());
		ElkClass kernelStatementConcept = kernelElkObjectFactory.getClass(iri);
		NonBottomGenericTaxonomyNode.Projection<ElkClass> kernelStatementNode = kernelTaxonomy
				.getNonBottomNode(kernelStatementConcept);
		if (kernelStatementNode == null)
			throw new Exception("Not found: " + statementConceptIri);
		if (kernelStatementNode.equals(kernelTaxonomy.getTopNode()))
			throw new Exception("isTopNode " + kernelStatementNode);
		if (kernelStatementNode.equals(kernelTaxonomy.getBottomNode()))
			throw new Exception("isBottomNode " + kernelStatementNode);
		// Remove statement hierarchy from kernelTaxonomy, keeping statementNode
		removeTaxonomySubTree(kernelStatementNode, kernelTaxonomy, false);
		if (debug) {
			log.info("ORIGINAL KERNEL REASONER TAXONOMY - IN CLASSIFIER - AFTER REMOVAL OF STATEMENT-CONCEPT");
			ReasonerExplorer.printCurrentReasonerTaxonomy((ElkReasoner) kernelElkReasoner, false);
		}
		// Iterate through each statement concept from the statement hierarchy and
		// correctly classify it in the kernel hierarchy, below the statementConcept
		for (OWLClass candidateClass : getStatementClassificationConcepts()) {
			ElkClass candidateElkClass = OwlConverter.getInstance().convert(candidateClass);
			NonBottomGenericTaxonomyNode.Projection<ElkClass> kernelCandidateNode = kernelTaxonomy
					.getCreateNode(List.of(candidateElkClass));
			SubsumptionNormalForm candidateSNF = subsumptionNormalFormBuilder.getSNF(candidateClass);
			if (debug)
				log.info("Classify: " + kernelCandidateNode.getCanonicalMember().getIri());
			classifyStatementConcept(kernelCandidateNode, candidateSNF, kernelStatementNode);
			if (debug) {
				if (++countClassified % 10 == 0)
					log.info("Classified " + countClassified);
			}
		}
	}

	private void removeTaxonomySubTree(NonBottomGenericTaxonomyNode.Projection<ElkClass> node,
			ConcurrentClassTaxonomy taxonomy, boolean removeNode) {
		for (NonBottomGenericTaxonomyNode.Projection<ElkClass> childOfTaxonomyNode : Set
				.copyOf(node.getDirectNonBottomSubNodes())) {
			// perform depth-first removal of all nodes in sub-tree, i.e., remove
			// descendants before disconnecting/removing this node
			removeTaxonomySubTree(childOfTaxonomyNode, taxonomy, true);
		}
		if (removeNode) {
			for (NonBottomGenericTaxonomyNode.Projection<ElkClass> superNode : Set
					.copyOf(node.getDirectNonBottomSuperNodes())) {
				superNode.removeDirectSubNode(node);
				node.removeDirectSuperNode(superNode);
			}
			taxonomy.removeNode(node.getCanonicalMember());
		}
	}

	private boolean classifyStatementConcept(NonBottomGenericTaxonomyNode.Projection<ElkClass> candidateNode,
			SubsumptionNormalForm candidateSNF, NonBottomGenericTaxonomyNode.Projection<ElkClass> predicateNode) {
		boolean successfullyClassified = false;
		OWLClass predicateClass = ElkConverter.getInstance().convert(predicateNode.getCanonicalMember());
		SubsumptionNormalForm predicateSNF = subsumptionNormalFormBuilder.getSNF(predicateClass);
		if (debug) {
			log.info("Testing subsumption (MostSpecificSubsumers)=> Candidate: "
					+ candidateNode.getCanonicalMember().getIri() + "  Predicate: "
					+ predicateNode.getCanonicalMember().getIri());
		}
		if (subsumptionTester.isSubsumedBy(candidateSNF, predicateSNF, kernelElkReasoner, statementOwlReasoner)
				&& !candidateNode.equals(predicateNode)) {
			if (debug)
				log.info("       Answer:  YES");
			for (NonBottomGenericTaxonomyNode.Projection<ElkClass> predicateChildNode : predicateNode
					.getDirectNonBottomSubNodes()) {
				// Recurses here
				if (classifyStatementConcept(candidateNode, candidateSNF, predicateChildNode)) {
					successfullyClassified = true;
				}
			}
			if (successfullyClassified == false) {
				// candidate was not subsumed by any of the predicates' descendants,
				// so connect it as a new child of the predicate itself
				if (!nodeAlreadyConnectedTo(candidateNode, predicateNode)) {
					// Could occur if the candidateNode can arrive at the predicateNode via multiple
					// paths (i.e., if multiple inheritance exists)
					// connect candidate to predicate, then go on to find most general subsumees of
					// the candidate node
					connectNodes(candidateNode, predicateNode);
					if (debug)
						log.info(" ==> Classifying " + candidateNode.getCanonicalMember().getIri() + " below "
								+ predicateNode.getCanonicalMember().getIri());
					findAndConnectMostGeneralSubsumees(candidateNode, candidateSNF, predicateNode);
					// We know that, if any exist, they must be descendants of the predicate node
				} else {
					if (debug)
						log.info(" ==> Not Connecting " + candidateNode.getCanonicalMember().getIri()
								+ " already connected to " + predicateNode.getCanonicalMember().getIri());

				}
			}
			return true;
		} else {
			if (debug)
				log.info("       Answer:  NO");
			return false;
		}
	}

	private void findAndConnectMostGeneralSubsumees(NonBottomGenericTaxonomyNode.Projection<ElkClass> predicateNode,
			SubsumptionNormalForm predicateSNF,
			NonBottomGenericTaxonomyNode.Projection<ElkClass> parentCandidateNode) {
		for (NonBottomGenericTaxonomyNode.Projection<ElkClass> candidateNode : Set
				.copyOf(parentCandidateNode.getDirectNonBottomSubNodes())) {
			// for each child of the parentCandidateNode, check if it is
			// subsumed by the predicateNode
//			if (candidateNode instanceof UpdateableBottomNode) {
			// Do nothing and exit; no need to connect the bottom node to the predicate node
			// -- it already has a bottom node below it by default if it has no children
//			} else {
			OWLClass candidateClass = ElkConverter.getInstance().convert(candidateNode.getCanonicalMember());
			SubsumptionNormalForm candidateSNF = subsumptionNormalFormBuilder.getSNF(candidateClass);
			if (debug)
				log.info("Testing subsumption (MostGeneralSubsumees) => Candidate: "
						+ candidateNode.getCanonicalMember().getIri() + "  Predicate: "
						+ predicateNode.getCanonicalMember().getIri());
			// Check if the current child of parentCandidateNode is subsumed by the
			// predicateNode
			// Only want properly subsumed concepts, not self-subsumed
			if (subsumptionTester.isSubsumedBy(candidateSNF, predicateSNF, kernelElkReasoner, statementOwlReasoner)
					&& !candidateNode.equals(predicateNode)) {
				if (debug)
					log.info("       Answer:  YES");
				// Another path may already exist from predicateNode to candidateNode if
				// multiple inheritance
				if (!nodeAlreadySubsumedBy(candidateNode, predicateNode)) {
					connectNodes(candidateNode, predicateNode);
					// Want to connect the *top-most* subsumee, so don't recurse below here
					if (debug)
						log.info(" ==> Classifying " + predicateNode.getCanonicalMember().getIri() + " above "
								+ candidateNode.getCanonicalMember().getIri());
				}
				if (predicateNodeAlsoHasParentCandidateNodeAsASuperClass(predicateNode, parentCandidateNode)) {
					// i.e., if candidateNode and predicateNode are currently siblings
					// the existing link provides no additional information and is redundant
					disconnectNodes(candidateNode, parentCandidateNode);
					// the existing link provides no additional information and is redundant
					if (debug)
						log.info(" ==> Disconnecting link [ONE] between " + candidateNode.getCanonicalMember().getIri()
								+ " and its parent " + parentCandidateNode.getCanonicalMember().getIri());
				}
				for (NonBottomGenericTaxonomyNode.Projection<ElkClass> candidateChildNode : candidateNode
						.getDirectNonBottomSubNodes()) {
					// for each child of the parentCandidateNode, check if it is subsumed by the
					// predicateNode
					if (predicateNodeAlsoHasCandidatesChildNodeAsASubClass(predicateNode, candidateChildNode)) {
						// i.e., if candidateNode and predicateNode are currently siblings
						disconnectNodes(candidateChildNode, predicateNode);
						// the existing link provides no additional information and is redundant
						if (debug)
							log.info(" ==> Disconnecting link [TWO] between "
									+ candidateChildNode.getCanonicalMember().getIri() + " and its parent "
									+ predicateNode.getCanonicalMember().getIri());
					}
				}
			} else {
				if (debug)
					log.info("       Answer:  NO");
				findAndConnectMostGeneralSubsumees(predicateNode, predicateSNF, candidateNode);
				// if the current child of parentCandidateNode is not subsumed by the
				// predicateNode, recurse to the current child's children and continue looking
			}
		}
	}

	private void connectNode(NonBottomGenericTaxonomyNode.Projection<ElkClass> node,
			List<NonBottomGenericTaxonomyNode.Projection<ElkClass>> superNodeList) {
		for (NonBottomGenericTaxonomyNode.Projection<ElkClass> superNode : superNodeList) {
			superNode.addDirectSubNode(node);
			node.addDirectSuperNode(superNode);
		}
	}

	private void connectNodes(NonBottomGenericTaxonomyNode.Projection<ElkClass> subNode,
			NonBottomGenericTaxonomyNode.Projection<ElkClass> superNode) {
		superNode.addDirectSubNode(subNode);
		subNode.addDirectSuperNode(superNode);

	}

	private void disconnectNodes(NonBottomGenericTaxonomyNode.Projection<ElkClass> subNode,
			NonBottomGenericTaxonomyNode.Projection<ElkClass> superNode) {
		superNode.removeDirectSubNode(subNode);
		subNode.removeDirectSuperNode(superNode);

	}

	private boolean predicateNodeAlsoHasParentCandidateNodeAsASuperClass(TaxonomyNode<ElkClass> predicateNode,
			TaxonomyNode<ElkClass> parentCandidateNode) {
		if (predicateNode.getDirectSuperNodes().contains(parentCandidateNode)) {
			return true;
		} else
			return false;
	}

	private boolean predicateNodeAlsoHasCandidatesChildNodeAsASubClass(TaxonomyNode<ElkClass> predicateNode,
			TaxonomyNode<ElkClass> candidateChildNode) {
		if (predicateNode.getDirectSubNodes().contains(candidateChildNode)) {
			return true;
		} else
			return false;
	}

	private boolean nodeAlreadyConnectedTo(TaxonomyNode<ElkClass> candidateNode, TaxonomyNode<ElkClass> predicateNode) {
		if (candidateNode.getDirectSuperNodes().contains(predicateNode)) {
			return true;
		} else
			return false;
	}

	private boolean nodeAlreadySubsumedBy(TaxonomyNode<ElkClass> candidateNode, TaxonomyNode<ElkClass> predicateNode) {
		if (candidateNode.getAllSuperNodes().contains(predicateNode)) {
			return true;
		} else
			return false;
	}

	public Set<OWLClass> getStatementClassificationConcepts() {
		OWLDataFactory factory = statementOntology.getOWLOntologyManager().getOWLDataFactory();
		OWLClass statementConcept = factory.getOWLClass(IRI.create(properties.statementConcept()));
		Set<OWLClass> ret = statementOwlReasoner.getSubClasses(statementConcept, false).getFlattened();
		ret.remove(factory.getOWLNothing());
		return ret;
	}

}
