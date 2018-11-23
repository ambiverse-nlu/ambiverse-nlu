/**
 *
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * This "annotator" is needed when the data that is being classified already
 * has the classification outcomes annotated. This happens if you have the gold
 * standard already but want to classify the data with your own classifier.
 */
public class RemoveNamedEntityAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		jCas.removeAllExcludingSubtypes(NamedEntity.typeIndexID);
	}

}
