/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;

import java.util.Set;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * Extracts a binary feature that indicates whether the
 * current token is at the beginning of a sentence.
 *
 */
public class BeginOfSentence extends SynchronizedTcuLookUpTable {
	
	public static final String FEATURE_NAME = "beginOfSentence";
	
	@Override
	public Set<Feature> extract(JCas jCas, TextClassificationTarget unit)
			throws TextClassificationException {
		super.extract(jCas, unit);
		
		Integer idx = unitBegin2IdxTL.get().get(unit.getBegin());
		
		boolean result = isBeginOfSentence(idx);
		
		return new Feature(FEATURE_NAME, result).asSet();
	}

	private boolean isBeginOfSentence(Integer idx) {
		if(idx2SequenceBeginTL.get().get(idx) != null){
			return true;
		} else {
			return false;
		}
	}

}
