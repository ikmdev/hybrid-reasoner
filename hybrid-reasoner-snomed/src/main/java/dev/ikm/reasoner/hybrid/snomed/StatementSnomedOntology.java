package dev.ikm.reasoner.hybrid.snomed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.primitive.LongObjectMaps;
import org.eclipse.collections.api.factory.primitive.LongSets;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.api.set.primitive.ImmutableLongSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
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

//TODO: Refactor to use primitive collections...
public class StatementSnomedOntology {

	private static final Logger log = LoggerFactory.getLogger(StatementSnomedOntology.class);

	// 243796009 |Situation with explicit context (situation)|
	public static final long swec_id = 243796009;

	// 408729009 |Finding context (attribute)|
	public static final long finding_context_id = 408729009;

	// 410516002 |Known absent (qualifier value)|
	public static final long known_absent_id = 410516002;

	// 246090004 |Associated finding (attribute)|
	// public static final long associated_finding_id = 246090004;

	public record SwecIds(long swec, long swec_parent, long findingContext, long knownAbsent) {
	}

	public static final SwecIds swec_sctids = new SwecIds(swec_id, SnomedIds.root, finding_context_id, known_absent_id);

	public static final SwecIds swec_nfh_sctids = new SwecIds(FamilyHistoryIds.no_family_history_swec,
			FamilyHistoryIds.finding_swec, finding_context_id, known_absent_id);

	private SnomedOntology ontology;

	private long root;

	private SnomedIsa definingIsa;

	private SwecIds swecIds;

	private ArrayList<Concept> conceptsDefiningDependentOrder = new ArrayList<>();

	private ArrayList<Concept> statementConceptsDefiningDependentOrder = new ArrayList<>();

	private NecessaryNormalFormBuilder nnfBuilder;

	private DefiningSubsumption subsumption;

	private SnomedOntology nsoOntology;

	private SnomedOntologyReasoner nsoReasoner;

	private SnomedIsa isas;

	public SnomedOntology getOntology() {
		return ontology;
	}

	public ArrayList<Concept> getConceptsDefiningDependentOrder() {
		return conceptsDefiningDependentOrder;
	}

	public ArrayList<Concept> getStatementConceptsDefiningDependentOrder() {
		return statementConceptsDefiningDependentOrder;
	}

	private StatementSnomedOntology(SnomedOntology ontology, long root, SwecIds swecIds) {
		super();
		this.ontology = ontology;
		this.root = root;
		this.swecIds = swecIds;
	}

	public static StatementSnomedOntology create(SnomedOntology ontology, boolean useAbsent) {
		return create(ontology, useAbsent, SnomedIds.root, swec_sctids);
	}

	public static StatementSnomedOntology create(SnomedOntology ontology, long root, SwecIds swecIds) {
		return create(ontology, true, root, swecIds);
	}

	public static StatementSnomedOntology create(SnomedOntology ontology, boolean useAbsent, long root,
			SwecIds swecIds) {
		StatementSnomedOntology ret = new StatementSnomedOntology(ontology, root, swecIds);
		ret.init(useAbsent);
		return ret;
	}

	private void init(boolean useAbsent) {
		initConcepts();
		generateNNF();
		if (useAbsent) {
			subsumption = new AbsentSubsumption(ontology, definingIsa, nnfBuilder.getIsa(),
					nnfBuilder.getSuperRolesTypes(), nnfBuilder.getNecessaryNormalForm(), swecIds);
		} else {
			subsumption = new DefiningSubsumption(ontology, definingIsa, nnfBuilder.getIsa(),
					nnfBuilder.getSuperRolesTypes(), nnfBuilder.getNecessaryNormalForm());
		}
	}

	private void initConcepts() {
		// Use primitive maps instead of HashMap<Long, Set<Long>>
		MutableLongObjectMap<MutableLongSet> definingSuperConcepts = LongObjectMaps.mutable.empty();
		MutableLongObjectMap<MutableLongSet> dependentOnConcepts = LongObjectMaps.mutable.empty();
		
		for (Concept concept : ontology.getConcepts()) {
			long id = concept.getId();
			if (id == root)
				continue;
			
			// Initialize with empty primitive set
			definingSuperConcepts.put(id, LongSets.mutable.empty());
			
			for (Definition def : concept.getDefinitions()) {
				for (Concept sup : def.getSuperConcepts()) {
					if (id == sup.getId())
						log.error("Self cycle: " + id);
					definingSuperConcepts.get(id).add(sup.getId());
				}
			}
			
			// getDependentOnConcepts now returns MutableLongSet
			dependentOnConcepts.put(id, ontology.getDependentOnConcepts(concept.getId()));
		}
		definingIsa = SnomedIsa.init(definingSuperConcepts, root);
		{
			SnomedIsa deps = SnomedIsa.init(dependentOnConcepts, root);
			deps.getOrderedConcepts().forEach(id -> conceptsDefiningDependentOrder.add(ontology.getConcept(id)));		}
		log.info("Concepts: " + conceptsDefiningDependentOrder.size());
		if (conceptsDefiningDependentOrder.size() != ontology.getConcepts().size()) {
			String msg = "Size: " + conceptsDefiningDependentOrder.size() + " != " + ontology.getConcepts().size();
			log.error(msg);
			// TODO Figure out what do about this in Solor
			HashSet<Long> ontology_ids = ontology.getConcepts().stream().map(Concept::getId)
					.collect(Collectors.toCollection(HashSet::new));
			ontology_ids.removeAll(conceptsDefiningDependentOrder.stream().map(Concept::getId).toList());
			ontology_ids.forEach(x -> log.error("Missing: " + x + " " + ontology.getConcept(x).getDefinitions()));
			if (root == SnomedIds.root)
				throw new RuntimeException(msg);
		}
		for (Concept con : conceptsDefiningDependentOrder) {
			if (definingIsa.hasAncestor(con.getId(), swecIds.swec))
				statementConceptsDefiningDependentOrder.add(con);
		}
		log.info("Statement concepts: " + statementConceptsDefiningDependentOrder.size());
	}

	private void generateNNF() {
		ArrayList<Concept> concepts = new ArrayList<>(ontology.getConcepts());
		concepts.removeAll(statementConceptsDefiningDependentOrder);
		nsoOntology = new SnomedOntology(concepts, ontology.getRoleTypes(), ontology.getConcreteRoleTypes());
		nsoReasoner = SnomedOntologyReasoner.create(nsoOntology);
		nsoReasoner.flush();
		nnfBuilder = NecessaryNormalFormBuilder.create(nsoOntology, nsoReasoner.getSuperConcepts(),
				nsoReasoner.getSuperRoleTypes(false), root, (workDone, max) -> {});
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
		if (con2.getId() == swecIds.swec)
			return true;
		return subsumption.isSubsumedByStructural(con1, con2);
	}

	private static final long bottom_id = Long.MIN_VALUE;

	public SnomedIsa classify() {
		// Use primitive map instead of HashMap<Long, Set<Long>>
		MutableLongObjectMap<MutableLongSet> parents = LongObjectMaps.mutable.empty();
		parents.put(swecIds.swec, LongSets.mutable.with(swecIds.swec_parent));
		isas = SnomedIsa.init(parents);
		
		for (Concept con : statementConceptsDefiningDependentOrder) {
//			log.info("Con: " + ontology.getFsn(con.getId()));
			MutableLongSet mss = LongSets.mutable.empty();
			findMss(con, swecIds.swec_parent, isas, LongSets.mutable.empty(), LongSets.mutable.empty(), mss);
//			mss.forEach(id -> log.info("\tMSS: " + ontology.getFsn(id)));
			
			MutableLongSet mgs = LongSets.mutable.empty();
			findMgs(con, bottom_id, isas, LongSets.mutable.empty(), LongSets.mutable.empty(), getLeaves(mss, isas), mgs);
//			mgs.forEach(id -> log.info("\tMGS: " + ontology.getFsn(id)));
			
			parents.put(con.getId(), mss.toImmutable().toSet());
			
			MutableLongSet mss_ancs = LongSets.mutable.withAll(mss);
			mss.forEach(mss1 -> mss_ancs.addAll(isas.getAncestors(mss1)));
			
			mgs.forEach(mgs1 -> {
				parents.get(mgs1).removeAll(mss_ancs);
				parents.get(mgs1).add(con.getId());
			});
			
			isas = SnomedIsa.init(parents);
		}
		return isas;
	}

	private boolean findMss(Concept con_to_classify, long con_id, SnomedIsa isas, MutableLongSet visited,
			MutableLongSet visited_found, MutableLongSet result) {
		boolean[] found_mss = {false}; // Use array to allow modification in lambda

		isas.getChildren(con_id).forEach(child_id -> {
			if (visited.contains(child_id)) {
				if (visited_found.contains(child_id))
					found_mss[0] = true;
				return; // continue in lambda
			}
			Concept child = ontology.getConcept(child_id);
			if (isSubsumedBy(con_to_classify, child)) {
				found_mss[0] = true;
				if (!findMss(con_to_classify, child_id, isas, visited, visited_found, result))
					result.add(child_id);
				visited_found.add(child_id);
			}
			visited.add(child_id);
		});

		return found_mss[0];
	}

	private boolean findMgs(Concept con_to_classify, long con_id, SnomedIsa isas, MutableLongSet visited,
			MutableLongSet visited_found, ImmutableLongSet leaves, MutableLongSet result) {
		boolean found_mgs = false;
		ImmutableLongSet toCheck = (con_id == bottom_id ? leaves : isas.getParents(con_id));
		for (long parent_id : toCheck.toArray()) {
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

	private ImmutableLongSet getLeaves(MutableLongSet mss, SnomedIsa isas) {
		MutableLongSet leaves = LongSets.mutable.empty();
		mss.forEach(mss1 -> {
			isas.getDescendants(mss1).select(id -> isas.getChildren(id).isEmpty())
					.forEach(leaves::add);
		});
		return leaves.toImmutable();
	}

	public Set<Long> getEquivalentConcepts(long id) {
		if (nsoOntology.getConcept(id) != null) {
			// Convert primitive MutableLongSet to Set<Long>
			MutableLongSet equivalentPrimitive = nsoReasoner.getEquivalentConcepts(id);
			Set<Long> equivalentBoxed = new HashSet<>();
			equivalentPrimitive.forEach(equivalentBoxed::add);
			return equivalentBoxed;
		}
		return Set.of();
	}

	public Set<Long> getSuperConcepts(long id) {
		if (nsoOntology.getConcept(id) != null) {
			// Convert primitive MutableLongSet to Set<Long>
			MutableLongSet superConceptsPrimitive = nsoReasoner.getSuperConcepts(id);
			Set<Long> superConceptsBoxed = new HashSet<>();
			superConceptsPrimitive.forEach(superConceptsBoxed::add);
			return superConceptsBoxed;
		}
		// Convert from SnomedIsa (returns MutableLongSet) to Set<Long>
		ImmutableLongSet parents = isas.getParents(id);
		Set<Long> parentsBoxed = new HashSet<>();
		parents.forEach(parentsBoxed::add);
		return parentsBoxed;
	}

	public HashMap<Long, Set<Long>> getSuperConcepts() {
		HashMap<Long, Set<Long>> superConcepts = new HashMap<>();
		for (Concept concept : ontology.getConcepts()) {
			long id = concept.getId();
			superConcepts.put(id, getSuperConcepts(id));
		}
		return superConcepts;
	}

	public Set<Long> getSubConcepts(long id) {
//		if (id == swecIds.swec_parent) {
//			Set<Long> ret = new HashSet<>(nsoReasoner.getSubConcepts(id));
//			ret.add(swecIds.swec);
//			return ret;
//		}
		if (id == swecIds.swec) {
			// Convert from SnomedIsa (returns MutableLongSet) to Set<Long>
			ImmutableLongSet children = isas.getChildren(id);
			Set<Long> childrenBoxed = new HashSet<>();
			children.forEach(childrenBoxed::add);
			return childrenBoxed;
		}
		if (nsoOntology.getConcept(id) != null) {
			// Convert primitive MutableLongSet to Set<Long>
			MutableLongSet subConceptsPrimitive = nsoReasoner.getSubConcepts(id);
			Set<Long> subConceptsBoxed = new HashSet<>();
			subConceptsPrimitive.forEach(subConceptsBoxed::add);
			return subConceptsBoxed;
		}
		// Convert from SnomedIsa (returns MutableLongSet) to Set<Long>
		ImmutableLongSet children = isas.getChildren(id);
		Set<Long> childrenBoxed = new HashSet<>();
		children.forEach(childrenBoxed::add);
		return childrenBoxed;
	}

	public HashMap<Long, Set<Long>> getSuperRoleTypes(boolean direct) {
		// Convert primitive map to boxed HashMap
		MutableLongObjectMap<MutableLongSet> superRoleTypesPrimitive = nsoReasoner.getSuperRoleTypes(direct);
		HashMap<Long, Set<Long>> superRoleTypesBoxed = new HashMap<>();
		superRoleTypesPrimitive.forEachKeyValue((key, primitiveSet) -> {
			Set<Long> boxedSet = new HashSet<>();
			primitiveSet.forEach(boxedSet::add);
			superRoleTypesBoxed.put(key, boxedSet);
		});
		return superRoleTypesBoxed;
	}

}
