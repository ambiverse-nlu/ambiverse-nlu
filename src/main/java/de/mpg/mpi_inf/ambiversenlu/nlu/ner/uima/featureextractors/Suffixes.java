/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.consts.Constants;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * Extracts the suffixes of size 1 to 4 from the current word.
 * Adds null for all suffixes that are larger than the word itself.
 * 
 * See: Zhang and Johnson 2003
 */
public class Suffixes extends FeatureExtractorResource_ImplBase implements FeatureExtractor {

	public static final String FEATURE_NAME = "Suffixes";
	
	private static final int SUFFIX_MAX_LENGTH = 4;
	
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget unit) throws TextClassificationException {
		Set<Feature> features = new HashSet<>();
		
		String token = unit.getCoveredText();
		
		if(token.length() == 0){
			return features;
		}
		
		for (int i = 1; i <= SUFFIX_MAX_LENGTH; i++) {
			if(i <= token.length()){
				String suffix = token.substring(token.length()-i, token.length());
				features.add(new Feature(FEATURE_NAME + i, suffix));
			} else {
				features.add(new Feature(FEATURE_NAME + i, Constants.FEATURE_NULL));
			}
		}
		
		return features;
	}

}
