package dev.ikm.reasoner.hybrid.snomed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.DefiningSubsumption;
import dev.ikm.elk.snomed.NecessaryNormalFormBuilder;
import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.Definition;

public class StatementSnomedOntology {

	private static final Logger log = LoggerFactory.getLogger(StatementSnomedOntology.class);

	// 243796009 |Situation with explicit context (situation)|
	public static final long swec_id = 243796009;

	// 408729009 |Finding context (attribute)|
	public static final long finding_context_id = 408729009;

	// 410516002 |Known absent (qualifier value)|
	public static final long known_absent_id = 410516002;

	// 246090004 |Associated finding (attribute)|
	public static final long associated_finding_id = 246090004;

	private SnomedOntology ontology;

	private SnomedIsa definingIsa;

	private ArrayList<Concept> conceptsDefiningDependentOrder = new ArrayList<>();

	private ArrayList<Concept> statementConceptsDefiningDependentOrder = new ArrayList<>();

	private NecessaryNormalFormBuilder nnfBuilder;

	private DefiningSubsumption definingSubsumption;

	public SnomedOntology getOntology() {
		return ontology;
	}

	public ArrayList<Concept> getConceptsDefiningDependentOrder() {
		return conceptsDefiningDependentOrder;
	}

	public ArrayList<Concept> getStatementConceptsDefiningDependentOrder() {
		return statementConceptsDefiningDependentOrder;
	}

	private StatementSnomedOntology(SnomedOntology ontology) {
		super();
		this.ontology = ontology;
	}

	public static StatementSnomedOntology create(SnomedOntology ontology) {
		return create(ontology, true);
	}

	public static StatementSnomedOntology create(SnomedOntology ontology, boolean useAbsent) {
		StatementSnomedOntology ret = new StatementSnomedOntology(ontology);
		ret.init(useAbsent);
		return ret;
	}

	private void init(boolean useAbsent) {
		initConcepts();
		generateNNF();
		if (useAbsent) {
			definingSubsumption = new AbsentSubsumption(ontology, definingIsa, nnfBuilder.getIsa(),
					nnfBuilder.getSuperRolesTypes(), nnfBuilder.getNecessaryNormalForm());
		} else {
			definingSubsumption = new DefiningSubsumption(ontology, definingIsa, nnfBuilder.getIsa(),
					nnfBuilder.getSuperRolesTypes(), nnfBuilder.getNecessaryNormalForm());
		}
	}

	private void initConcepts() {
		HashMap<Long, Set<Long>> definingSuperConcepts = new HashMap<>();
		HashMap<Long, Set<Long>> dependentOnConcepts = new HashMap<>();
		for (Concept concept : ontology.getConcepts()) {
			long id = concept.getId();
			definingSuperConcepts.put(id, new HashSet<>());
			for (Definition def : concept.getDefinitions()) {
				for (Concept sup : def.getSuperConcepts()) {
					definingSuperConcepts.get(id).add(sup.getId());
				}
			}
			dependentOnConcepts.put(id, ontology.getDependentOnConcepts(concept.getId()));
		}
		definingIsa = SnomedIsa.init(definingSuperConcepts);
		{
			SnomedIsa deps = SnomedIsa.init(dependentOnConcepts);
			deps.getOrderedConcepts().stream().map(id -> ontology.getConcept(id))
					.forEach(con -> conceptsDefiningDependentOrder.add(con));
		}
		log.info("Concepts: " + conceptsDefiningDependentOrder.size());
		if (conceptsDefiningDependentOrder.size() != ontology.getConcepts().size())
			throw new RuntimeException("Size");
		{
			for (Concept con : conceptsDefiningDependentOrder) {
				if (definingIsa.hasAncestor(con.getId(), swec_id))
					statementConceptsDefiningDependentOrder.add(con);
			}
		}
	}

	private void generateNNF() {
		ArrayList<Concept> concepts = new ArrayList<>(ontology.getConcepts());
		concepts.removeAll(statementConceptsDefiningDependentOrder);
		SnomedOntology nso = new SnomedOntology(concepts, ontology.getRoleTypes(), ontology.getConcreteRoleTypes());
		SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(nso);
		reasoner.flush();
		nnfBuilder = NecessaryNormalFormBuilder.create(nso, reasoner.getSuperConcepts(),
				reasoner.getSuperRoleTypes(false));
		// TODO can we get rid of this??? role chains issue
		nnfBuilder.generate();
		for (Concept con : getStatementConceptsDefiningDependentOrder()) {
			nnfBuilder.generateNNF(con, true);
		}
	}

	/*
	 * Return true if con1 is subsumed by con2
	 */
	public boolean isSubsumedBy(Concept con1, Concept con2) {
		if (con2.getId() == swec_id)
			return true;
		return definingSubsumption.isSubsumedByStructural(con1, con2);
	}

	private static final long bottom_id = Long.MIN_VALUE;

	public SnomedIsa classify() {
		HashMap<Long, Set<Long>> parents = new HashMap<>();
		parents.put(swec_id, Set.of(SnomedIds.root));
		SnomedIsa isas = SnomedIsa.init(parents);
		for (Concept con : statementConceptsDefiningDependentOrder) {
//			log.info("Con: " + ontology.getFsn(con.getId()));
			Set<Long> mss = new HashSet<>();
			findMss(con, SnomedIds.root, isas, new HashSet<>(), new HashSet<>(), mss);
//			mss.forEach(id -> log.info("\tMSS: " + ontology.getFsn(id)));
			Set<Long> mgs = new HashSet<>();
			findMgs(con, bottom_id, isas, new HashSet<>(), new HashSet<>(), getLeaves(mss, isas), mgs);
//			mgs.forEach(id -> log.info("\tMGS: " + ontology.getFsn(id)));
			parents.put(con.getId(), new HashSet<>(mss));
			HashSet<Long> mss_ancs = new HashSet<>(mss);
			for (long mss1 : mss) {
				mss_ancs.addAll(isas.getAncestors(mss1));
			}
			for (long mgs1 : mgs) {
				parents.get(mgs1).removeAll(mss_ancs);
				parents.get(mgs1).add(con.getId());
			}
			isas = SnomedIsa.init(parents);
		}
		return isas;
	}

	private boolean findMss(Concept con_to_classify, long con_id, SnomedIsa isas, Set<Long> visited,
			Set<Long> visited_found, Set<Long> result) {
		boolean found_mss = false;
		for (long child_id : isas.getChildren(con_id)) {
			if (visited.contains(child_id)) {
				if (visited_found.contains(child_id))
					found_mss = true;
				continue;
			}
			Concept child = ontology.getConcept(child_id);
			if (isSubsumedBy(con_to_classify, child)) {
				found_mss = true;
				if (!findMss(con_to_classify, child_id, isas, visited, visited_found, result))
					result.add(child_id);
				visited_found.add(child_id);
			}
			visited.add(child_id);
		}
		return found_mss;
	}

	private boolean findMgs(Concept con_to_classify, long con_id, SnomedIsa isas, Set<Long> visited,
			Set<Long> visited_found, Set<Long> leaves, Set<Long> result) {
		boolean found_mgs = false;
		for (long parent_id : (con_id == bottom_id ? leaves : isas.getParents(con_id))) {
			if (visited.contains(parent_id)) {
				if (visited_found.contains(parent_id))
					found_mgs = true;
				continue;
			}
			Concept parent = ontology.getConcept(parent_id);
			if (isSubsumedBy(parent, con_to_classify)) {
				found_mgs = true;
				if (!findMgs(con_to_classify, parent_id, isas, visited, visited_found, leaves, result))
					result.add(parent_id);
				visited_found.add(parent_id);
			}
			visited.add(parent_id);
		}
		return found_mgs;
	}

	private Set<Long> getLeaves(Set<Long> mss, SnomedIsa isas) {
		HashSet<Long> leaves = new HashSet<>();
		for (long mss1 : mss) {
			isas.getDescendants(mss1).stream().filter(id -> isas.getChildren(id).isEmpty())
					.forEach(id -> leaves.add(id));
		}
		return leaves;
	}

}
