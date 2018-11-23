package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import java.util.Collection;

public class SingleLabelAnnotator extends JCasAnnotator_ImplBase {
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		Collection<NamedEntity> namedEntities = JCasUtil.select(aJCas, NamedEntity.class);
		for (NamedEntity entity : namedEntities) {
			entity.setValue(entity.getValue().replace("PERSON", "ENTITY")
					.replace("PER", "ENTITY")
					.replace("LOCATION", "ENTITY")
					.replace("LOC", "ENTITY")
					.replace("ORGANIZATION", "ENTITY")
					.replace("ORG", "ENTITY")
					.replace("MISC", "ENTITY"));
		}
		Collection<TextClassificationOutcome> outcomes = JCasUtil.select(aJCas, TextClassificationOutcome.class);
		for (TextClassificationOutcome outcome : outcomes) {
			if (outcome.getOutcome().equals("OTH")) {
				continue;
			}
			outcome.setOutcome("ENTITY");
		}
	}
}
