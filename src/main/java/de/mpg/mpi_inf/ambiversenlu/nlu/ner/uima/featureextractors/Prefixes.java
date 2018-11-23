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
 * Extracts the prefixes of size 3 and 4 from the current word.
 * Adds null if the word length is smaller than 3.
 * 
 * See: Zhang and Johnson 2003
 */
public class Prefixes extends FeatureExtractorResource_ImplBase implements FeatureExtractor {
	
	public static final String FEATURE_NAME = "Prefixes";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget unit) throws TextClassificationException {
		Set<Feature> features = new HashSet<>();
		
		String token = unit.getCoveredText();
		
		if(token.length() >= 3){
			String prefix = token.substring(0, 3);
			features.add(new Feature(FEATURE_NAME + "3", prefix));
		} else {
			features.add(new Feature(FEATURE_NAME + "3", Constants.FEATURE_NULL));
		}
		if(token.length() >= 4){
			String prefix = token.substring(0, 4);
			features.add(new Feature(FEATURE_NAME + "4", prefix));
		} else {
			features.add(new Feature(FEATURE_NAME + "4", Constants.FEATURE_NULL));
		}
		
		return features;
	}

}
