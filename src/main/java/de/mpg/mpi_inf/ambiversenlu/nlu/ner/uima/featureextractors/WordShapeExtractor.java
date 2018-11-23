/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.dkpro.tc.features.window.WindowFeatureExtractor;
import org.dkpro.tc.features.window.WordShapeClassifier;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Extracts the word shape of the current token and surrounding
 * word shapes
 * 
 * REQUIREMENTS: You need to set the parameters
 * WindowFeatureExtractor.PARAM_NUM_PRECEEDING and
 * WindowFeatureExtractor.PARAM_NUM_FOLLOWING, default is 3
 */
public class WordShapeExtractor extends WindowFeatureExtractor<Token> {
	
	@Override
	protected Class<Token> getTargetType() {
		return Token.class;
	}

	@Override
	protected String getFeatureName() {		
		return "WordShape";
	}

	@Override
	protected String getFeatureValue(Token a) {
		String text = a.getCoveredText();		
		 
		CharsetEncoder isoEncoder = Charset.forName("ISO-8859-1").newEncoder(); 

		text = text.replace("“", "\"").replace("„", "\"").replace("–", "-").replace("−", "-").replace("…", "...").replace("—", "-").replace("’", "'").replace("’", "'");

		// here you can choose which word shape classifier you'd like to use
		String shape = WordShapeClassifier.wordShape(text, WordShapeClassifier.WORDSHAPECHRIS4);
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0, n = shape.length(); i < n; i++) {
		    char c = shape.charAt(i);
		    
		    if(!isoEncoder.canEncode(c)) {
				sb.append("-NON-ISO-");
			} else {
				sb.append(c);
			}
		}
		

		return sb.toString();
	}

}
