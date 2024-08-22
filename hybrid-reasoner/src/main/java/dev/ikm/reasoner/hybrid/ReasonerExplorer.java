package dev.ikm.reasoner.hybrid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owlapi.ElkConverter;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.reasoner.Reasoner;
import org.semanticweb.elk.reasoner.completeness.Incompleteness;
import org.semanticweb.elk.reasoner.taxonomy.ConcurrentClassTaxonomy;
import org.semanticweb.elk.reasoner.taxonomy.model.Taxonomy;
import org.semanticweb.elk.reasoner.taxonomy.model.TaxonomyNode;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements various print routines to aid in debugging
 */
public class ReasonerExplorer {

	private static final Logger log = LoggerFactory.getLogger(ReasonerExplorer.class);

	public static void printHierarchy(Node<OWLClass> node, int level, OWLReasoner reasoner) {
		if (node.isBottomNode()) {
		} else {
			String pad = "";
			for (int i = 0; i < level * 2; i++) {
				pad += " ";
			}
			log.info(pad + node);
		}
		// Now recurse
		Set<OWLClass> entities = node.getEntities();
		for (OWLClass owlClass : entities) {
			NodeSet<OWLClass> subNodesNodeSet = reasoner.getSubClasses(owlClass, true);
			for (Node<OWLClass> owlSubClass : subNodesNodeSet) {
				printHierarchy(owlSubClass, level + 1, reasoner);
			}
		}
	}

//	public static void printCurrentReasonerTaxonomy(ElkReasoner reasoner, OWLOntology ontology, boolean makeChange,
//			boolean statementHierarchyOnly) throws Exception {
//		ElkReasoner elkReasoner = (ElkReasoner) reasoner;
//		Reasoner internalReasoner = elkReasoner.getInternalReasoner();
//		Taxonomy<ElkClass> taxonomy = Incompleteness.getValue(internalReasoner.getTaxonomy());
//		TaxonomyNode<ElkClass> topNode = taxonomy.getTopNode();
//		printTaxonomy(topNode, 0, true, (ConcurrentClassTaxonomy) taxonomy, ontology, makeChange,
//				statementHierarchyOnly);
//	}

	public static void printCurrentReasonerTaxonomy(ElkReasoner reasoner, boolean makeChange) throws Exception {
		ElkReasoner elkReasoner = (ElkReasoner) reasoner;
		Reasoner internalReasoner = elkReasoner.getInternalReasoner();
		Taxonomy<ElkClass> taxonomy = Incompleteness.getValue(internalReasoner.getTaxonomy());
		TaxonomyNode<ElkClass> topNode = taxonomy.getTopNode();
		printTaxonomy(topNode, 0, true, (ConcurrentClassTaxonomy) taxonomy, makeChange);
	}

	public static void printCurrentReasonerTaxonomySHIELD(OWLReasoner reasoner, boolean makeChange) throws Exception {
		ElkReasoner elkReasoner = (ElkReasoner) reasoner;
		Reasoner internalReasoner = elkReasoner.getInternalReasoner();
		Taxonomy<ElkClass> taxonomy = Incompleteness.getValue(internalReasoner.getTaxonomy());
		TaxonomyNode<ElkClass> topNode = taxonomy.getTopNode();
		printTaxonomy(topNode, 0, true, (ConcurrentClassTaxonomy) taxonomy, makeChange);
	}

	public static void printCurrentReasonerTaxonomySHIELD(Reasoner internalReasoner, boolean makeChange)
			throws Exception {
		Taxonomy<ElkClass> taxonomy = Incompleteness.getValue(internalReasoner.getTaxonomy());
		TaxonomyNode<ElkClass> topNode = taxonomy.getTopNode();
		printTaxonomy(topNode, 0, true, taxonomy, makeChange);
	}

	public static synchronized void printTaxonomy(TaxonomyNode<ElkClass> node, int level, boolean alreadyDone,
			Taxonomy<ElkClass> taxonomy, boolean makeChange) {
		if (node.getCanonicalMember().getIri().equals(taxonomy.getBottomNode().getCanonicalMember().getIri())) {
			// don't print and stop recursion
		} else {
			ElkClass elkClass = node.getCanonicalMember();
			log.info(indentSpaces(level) + getShortFormElkClassName(elkClass));
			Set<? extends TaxonomyNode<ElkClass>> subNodes = node.getDirectSubNodes();
			LinkedList<? extends TaxonomyNode<ElkClass>> sortedSubNodes = sortNodesElk(subNodes);
			for (TaxonomyNode<ElkClass> elkSubNode : sortedSubNodes) {
				printTaxonomy(elkSubNode, level + 1, alreadyDone, taxonomy, makeChange);
			}
		}
	}

	public static synchronized void printTaxonomy(TaxonomyNode<ElkClass> node, int level, boolean alreadyDone,
			ConcurrentClassTaxonomy taxonomy, OWLOntology ontology, boolean makeChange) {
		if (node.getCanonicalMember().getIri().equals(taxonomy.getBottomNode().getCanonicalMember().getIri())) {
			// don't print and stop recursion
		} else {
			ElkClass elkClass = node.getCanonicalMember();
			log.info(
					indentSpaces(level) + getShortFormElkClassName(elkClass) + getAnnnotationLabel(elkClass, ontology));
			Set<? extends TaxonomyNode<ElkClass>> subNodes = node.getDirectSubNodes();
			LinkedList<? extends TaxonomyNode<ElkClass>> sortedSubNodes = sortNodesElk(subNodes);
			for (TaxonomyNode<ElkClass> elkSubNode : sortedSubNodes) {
				printTaxonomy(elkSubNode, level + 1, alreadyDone, taxonomy, ontology, makeChange);
			}
		}
	}

	private static String getAnnnotationLabel(ElkClass elkClass, OWLOntology ontology) {
		OWLClass owlClass = ElkConverter.getInstance().convert(elkClass);
		Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(owlClass, ontology);
		for (OWLAnnotation owlAnnotation : annotations) {
			if (owlAnnotation.getProperty().getIRI().toString().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				String quotedValue = owlAnnotation.getValue().toString();
				// remove beginning/ending double quotes
				return "   (" + quotedValue.substring(1, quotedValue.length() - 4) + ")";
			}
		}
		return "";
	}

	public synchronized void printTaxonomy(Node<OWLClass> node, int level, boolean alreadyDone,
			StructuralReasoner reasoner, boolean makeChange) {
		if (node.equals(reasoner.getBottomClassNode())) {
			// don't print and stop recursion
		} else {
			OWLClass owlClass = node.getEntities().iterator().next();
			log.info(indentSpaces(level) + owlClass.getIRI());
			// Now recurse
			OWLClass thisClass = node.getEntities().iterator().next();
			NodeSet<OWLClass> subNodes = reasoner.getSubClasses(thisClass, true);
			for (Node<OWLClass> owlSubNode : subNodes) {
				printTaxonomy(owlSubNode, level + 1, alreadyDone, reasoner, makeChange);
			}
		}
	}

	public synchronized static void printCurrentReasonerOwlTaxonomy(OWLReasoner reasoner) {
		Node<OWLClass> topClassNode = reasoner.getTopClassNode();
		printOwlTaxonomy(topClassNode, 0, reasoner);
	}

	public synchronized static void printOwlTaxonomy(Node<OWLClass> node, int level, OWLReasoner reasoner) {
		if (node.equals(reasoner.getBottomClassNode())) {
			log.info(indentSpaces(level) + node.getEntities().iterator().next().getIRI().getShortForm());
		} else {
			OWLClass owlClass = node.getEntities().iterator().next();
			log.info(indentSpaces(level) + owlClass.getIRI().getShortForm());
			// Now recurse
			OWLClass thisClass = node.getEntities().iterator().next();
			NodeSet<OWLClass> subNodes = reasoner.getSubClasses(thisClass, true);
			LinkedList<Node<OWLClass>> sortedSubNodes = sortNodesOwl(subNodes);
			for (Node<OWLClass> owlSubNode : sortedSubNodes) {
				printOwlTaxonomy(owlSubNode, level + 1, reasoner);
			}
		}
	}

//	public static synchronized void printTaxonomy(TaxonomyNode<ElkClass> node, int level, boolean alreadyDone,
//			ConcurrentClassTaxonomy taxonomy, OWLOntology ontology, boolean makeChange,
//			boolean statementHierarchyOnly) {
//		if (!statementHierarchyOnly)
//			printTaxonomy(node, level, alreadyDone, taxonomy, ontology, makeChange);
//		else {
//			printTaxonomy(
//					breadthFirstSearchForStatementRootInKernelTaxonomy(node,
//							DefaultProperties.STATEMENT_CONCEPT_NAMESPACE, DefaultProperties.STATEMENT_CONCEPT_NAME),
//					level, alreadyDone, taxonomy, ontology, makeChange);
//		}
//	}

//	private static TaxonomyNode<ElkClass> breadthFirstSearchForStatementRootInKernelTaxonomy(
//			TaxonomyNode<ElkClass> taxonomyTopElkNode, String statementConceptNamespace, String statementConceptName) {
//		OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
//		OWLClass targetNamedClass = factory.getOWLClass(IRI.create(statementConceptNamespace + statementConceptName));
//		OWLClassNode targetOwlNode = new OWLClassNode(targetNamedClass);
//		List<TaxonomyNode<ElkClass>> nodeList = new ArrayList<TaxonomyNode<ElkClass>>();
//		nodeList.add(taxonomyTopElkNode);
//		int index = 0;
//		while (index < nodeList.size()) {
//			TaxonomyNode<ElkClass> currentTaxonomyElkNode = nodeList.get(index);
//			for (ElkClass currentElkClass : currentTaxonomyElkNode) {
//				// iterate through each member ElkClass of the node
//				if (ElkConverter.getInstance().convert(currentElkClass)
//						.equals(targetOwlNode.getEntities().iterator().next())) {
//					return currentTaxonomyElkNode;
//				}
//			} // if currentTaxonomyElkNode != targetOwlNode, then add children
//				// ofcurrentTaxonomyElkNode to end of nodeList and continue
//				// iterating through nodeList (breadth-first search)
//			nodeList.addAll(currentTaxonomyElkNode.getDirectSubNodes());
//			index++;
//		} // if targetOwlNode not found anywhere in the kernelReasoner taxonomy, return
//			// null (although this search will take a long time with a large ontology...)
//		return null;
//	}

//	private Node<OWLClass> getNamedOwlClassNodeFromStatementReasoner(String targetNodeIri,
//			OWLOntology statementOntology, OWLReasoner statementOwlReasoner) {
//		// First, retrieve the OWLClass node that represents the root concept of the
//		// statement hierarchy
//		// from the statementOwlReasoner ("Statement-Concept", in the default version of
//		// the statement sub-ontology)
//		OWLDataFactory factory = statementOntology.getOWLOntologyManager().getOWLDataFactory();
//		OWLClass targetNamedClass = factory.getOWLClass(IRI.create(targetNodeIri));
//		OWLClass topStatementReasonerClassNode = statementOwlReasoner.getTopClassNode().getEntities().iterator().next();
//		NodeSet<OWLClass> subNodes = statementOwlReasoner.getSubClasses(topStatementReasonerClassNode, false);
//		for (Node<OWLClass> subNode : subNodes) {
//			if (subNode.getEntities().iterator().next().equals(targetNamedClass)) {
//				return subNode;
//
//			}
//		}
//		return null;
//	}

	public synchronized static void printOwlTaxonomyFullIri(Node<OWLClass> node, int level, OWLReasoner reasoner) {
		if (node.equals(reasoner.getBottomClassNode())) {
			printFullIri(level, node.getEntities().iterator().next().getIRI());
		} else {
			OWLClass owlClass = node.getEntities().iterator().next();
			printFullIri(level, owlClass.getIRI());
			// Now recurse
			OWLClass thisClass = node.getEntities().iterator().next();
			NodeSet<OWLClass> subNodes = reasoner.getSubClasses(thisClass, true);
			for (Node<OWLClass> owlSubNode : subNodes) {
				printOwlTaxonomyFullIri(owlSubNode, level + 1, reasoner);
			}
		}
	}

	private static void printFullIri(int level, IRI iri) {
		log.info(indentSpaces(level) + "FullIRI: " + iri.getNamespace() + " ||  " + iri.getRemainder());
	}

	public static String indentSpaces(int level) {
		String indentString = "";
		for (int i = 0; i < level * 2; i++) {
			indentString = indentString + " ";
		}
		return indentString;
	}

	private static String getShortFormElkClassName(ElkClass elkClass) {
		OWLClass owlClass = ElkConverter.getInstance().convert(elkClass);
		return owlClass.getIRI().getShortForm();
	}

	private static String getShortFormOwlClassName(OWLClass owlClass) {
		return owlClass.getIRI().getShortForm();
	}

	private static LinkedList<? extends TaxonomyNode<ElkClass>> sortNodesElk(
			Set<? extends TaxonomyNode<ElkClass>> inputSet) {
		LinkedList<? extends TaxonomyNode<ElkClass>> list = new LinkedList<>(inputSet);
		list.sort(new Comparator<TaxonomyNode<ElkClass>>() {
			@Override
			public int compare(TaxonomyNode<ElkClass> node1, TaxonomyNode<ElkClass> node2) {
				return Collator.getInstance().compare(getShortFormElkClassName(node1.getCanonicalMember()),
						getShortFormElkClassName(node2.getCanonicalMember()));
			}
		});
		return list;
	}

	private static LinkedList<Node<OWLClass>> sortNodesOwl(NodeSet<OWLClass> inputNodeSet) {
		Set<Node<OWLClass>> inputSet = new HashSet<>();
		for (Node<OWLClass> node : inputNodeSet) {
			inputSet.add(node);
		}
		LinkedList<Node<OWLClass>> list = new LinkedList<Node<OWLClass>>(inputSet);
		list.sort(new Comparator<Node<OWLClass>>() {
			@Override
			public int compare(Node<OWLClass> node1, Node<OWLClass> node2) {
				return Collator.getInstance().compare(getShortFormOwlClassName(node1.getEntities().iterator().next()),
						getShortFormOwlClassName(node2.getEntities().iterator().next()));
			}
		});
		return list;
	}

	public static void pause(String msg) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		log.info("Pausing at " + msg + " <enter something>:");
		reader.readLine();
	}

}
