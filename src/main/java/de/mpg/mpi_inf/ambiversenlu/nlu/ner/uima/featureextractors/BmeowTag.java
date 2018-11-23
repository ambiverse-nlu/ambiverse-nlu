package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.BmeowTypePair;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type.BmeowType;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 * 
 * Feature extractor for the Document Dictionary feature (see Radford et al. 2015)
 * 
 * Extracts binary features for all permutations of BMEOW (also called BILOU) 
 * and type tags.
 * 
 * REQUIREMENTS: To use this extractor you need to run BmeowTypeAnnotator or
 * BmeowTypeGoldAnnotator as part of you pre-processing pipeline
 * 
 * Example: "Barack" -> B-PERS
 *
 */
public class BmeowTag extends FeatureExtractorResource_ImplBase implements FeatureExtractor {

	/**
	 * the feature's name
	 */
	public static final String FEATURE_NAME = "BmeowTag";

	public static final Set<String> allPairs = BmeowTypePair.getAllPossiblePairCombinations().stream()
			.map(BmeowTypePair::getFeatureString).collect(Collectors.toSet());

	/**
	 * This feature extractor returns all bmeow type tag of the current
	 * TextClassifiactionUnit
	 */
	public Set<Feature> extract(JCas view, TextClassificationTarget classificationUnit)
			throws TextClassificationException {
		Set<Feature> features = new HashSet<>();

		List<BmeowType> bmeowTypes = JCasUtil.selectCovered(view, BmeowType.class, classificationUnit);

		List<String> bmeowStrings = bmeowTypes.stream().map(BmeowType::getBmeowType).collect(Collectors.toList());
		for (String pair : allPairs) {
			if (bmeowStrings.contains(pair)) {
				features.add(new Feature(FEATURE_NAME + "_" + pair, true));
			} else {
				features.add(new Feature(FEATURE_NAME + "_" + pair, false));
			}
		}
		
		if(bmeowStrings.get(0).equals("OTHER")){
			features.add(new Feature(FEATURE_NAME + "_OTHER", true));
		} else {
			features.add(new Feature(FEATURE_NAME + "_OTHER", false));
		}
		
//		features.stream().forEach(f -> System.out
//				.println(classificationUnit.getCoveredText() + ": " + f.getName() + ", " + f.getValue()));

		return features;
	}

}
