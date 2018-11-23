/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

import java.util.Collection;

@TypeCapability(
        inputs = {"org.dkpro.tc.api.type.TextClassificationOutcome"},
        outputs = {"de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity"})

public class KnowNERNamedEntityPostClassificationBMEOWAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

        Collection<TextClassificationOutcome> outcomes = JCasUtil.select(jCas, TextClassificationOutcome.class);

        NamedEntity namedEntity = null;
        for (TextClassificationOutcome outcome : outcomes) {
            String value = outcome.getOutcome();
            if (value.equals("OTH") || value.startsWith("M-")) continue;

            if (value.startsWith("B-") || value.startsWith("W-")) {
                namedEntity = new NamedEntity(jCas, outcome.getBegin(), outcome.getEnd());
                namedEntity.setValue("KnowNER-" + value.substring(2));
                namedEntity.addToIndexes();

            } else if (value.startsWith("E-")) {
              if(namedEntity != null) {
                namedEntity.setEnd(outcome.getEnd());
              }
            }
        }
        jCas.removeAllExcludingSubtypes(TextClassificationOutcome.typeIndexID);
        jCas.removeAllExcludingSubtypes(TextClassificationTarget.typeIndexID);
        jCas.removeAllExcludingSubtypes(TextClassificationSequence.typeIndexID);
        jCas.removeAllExcludingSubtypes(AidaEntity.typeIndexID);
	}

}
