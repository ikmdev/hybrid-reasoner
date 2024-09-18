package dev.ikm.reasoner.hybrid;

import org.semanticweb.elk.owlapi.ElkReasonerConfiguration;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HybridReasonerFactory implements OWLReasonerFactory {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ElkReasonerFactory.class);

	private HybridReasonerProperties properties;

	public HybridReasonerFactory(HybridReasonerProperties properties) {
		super();
		this.properties = properties;
	}

	@Override
	public String getReasonerName() {
		return HybridReasoner.class.getSimpleName();
	}

	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
		return createElkReasoner(ontology, false, null);
	}

	@Override
	public OWLReasoner createReasoner(OWLOntology ontology) {
		return createElkReasoner(ontology, true, null);
	}

	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config)
			throws IllegalConfigurationException {
		return createElkReasoner(ontology, false, config);
	}

	@Override
	public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config)
			throws IllegalConfigurationException {
		return createElkReasoner(ontology, true, config);
	}

	private OWLReasoner createElkReasoner(OWLOntology ontology, boolean isBufferingMode,
			OWLReasonerConfiguration config) throws IllegalConfigurationException {
		/*
		 * Allowing only buffering reasoner, since non-buffering reasoner may cause
		 * errors when the ontology is changed while the hybrid reasoner is running. May
		 * wish to further test this, since some other changes were since made to the
		 * hybrid reasoner algorithm that may now allow a non-buffering reasoner to work
		 * without causing these errors. This has not yet been tested.
		 */
		if (!isBufferingMode)
			throw new RuntimeException(
					"Cannot create non-buffering " + HybridReasoner.class.getSimpleName() + " reasoner");
		ElkReasonerConfiguration elkReasonerConfig;
		if (config != null) {
			if (config instanceof ElkReasonerConfiguration) {
				elkReasonerConfig = (ElkReasonerConfiguration) config;
			} else {
				elkReasonerConfig = new ElkReasonerConfiguration(config);
			}
		} else {
			elkReasonerConfig = new ElkReasonerConfiguration();
		}
		return new HybridReasoner(ontology, elkReasonerConfig, properties);
	}

}
