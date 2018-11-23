/**
 *
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.ClassProbabilityDistributionBmeow;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators.LocalFeaturesTcAnnotator;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers.FileLines;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import org.apache.log4j.Logger;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * Extracts the probability that the current token belong to a certain
 * class type (PERS, LOC, ORG, MISC) and BMEOW (also BILOU) tag.
 *
 * REQUIREMENTS: Needs the file 'tokenClassProbs-bmeow.tsv'. The path to
 * the file can be specified in variable CLASS_PROB_FILE.
 * To generate tokenClassProbs-bmeow.tsv run class
 * mpi.ner.task.YagoLabelsToClassProbabilitiesBmeow
 *
 */
public class ClassTypeProbabilityBmeow extends FeatureExtractorResource_ImplBase
		implements FeatureExtractor {

	public static final String FEATURE_NAME = "ClassTypeProbabilityBmeow";

	private static final String[] DIMENSIONS = {"B-PER","M-PER","E-PER","W-PER",
			"B-ORG","M-ORG","E-ORG","W-ORG",
			"B-LOC","M-LOC","E-LOC","W-LOC",
			"B-MISC","M-MISC","E-MISC","W-MISC"};

	private static final Map<String, Map<String, ClassProbabilityDistributionBmeow>> classTypeProbabilities = new HashMap<>();

	private static final String CLASS_PROB_FILE = "tokenClassProbs-bmeow.tsv";

	private static Logger logger = Logger.getLogger(ClassTypeProbabilityBmeow.class);

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
		boolean init = super.initialize(aSpecifier, aAdditionalParams);

		try {
			String language = (String) aAdditionalParams.get(LocalFeaturesTcAnnotator.PARAM_LANGUAGE);
			populateLanguage(language);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}

		return init;
	}

	private void populateLanguage(String language) throws IOException {
		logger.debug("Populating '" + language + "' class type probability map...");
		FileLines reader = new FileLines(new File(KnowNERSettings.getLangResourcesPath(language) + KnowNERSettings.getDumpName() + "_" + CLASS_PROB_FILE));
		HashMap<String, ClassProbabilityDistributionBmeow> classTypeProbabilitiesForLanguage = new HashMap<>();
		while(reader.hasNext()){
			String line = reader.next();
			if(line.startsWith("#")){
				continue;
			}
			String[] split = line.split("\t");
			String token = split[0];
			double[] pers = new double[4];
			pers[0] = Double.valueOf(split[1]);
			pers[1] = Double.valueOf(split[2]);
			pers[2] = Double.valueOf(split[3]);
			pers[3] = Double.valueOf(split[4]);

			double[] org = new double[4];
			org[0] = Double.valueOf(split[5]);
			org[1] = Double.valueOf(split[6]);
			org[2] = Double.valueOf(split[7]);
			org[3] = Double.valueOf(split[8]);

			double[] loc = new double[4];
			loc[0] = Double.valueOf(split[9]);
			loc[1] = Double.valueOf(split[10]);
			loc[2] = Double.valueOf(split[11]);
			loc[3] = Double.valueOf(split[12]);

			double[] misc = new double[4];
			misc[0] = Double.valueOf(split[13]);
			misc[1] = Double.valueOf(split[14]);
			misc[2] = Double.valueOf(split[15]);
			misc[3] = Double.valueOf(split[16]);

			classTypeProbabilitiesForLanguage.put(token, new ClassProbabilityDistributionBmeow(token, pers, org, loc, misc));
		}

		reader.close();
		classTypeProbabilities.put(language, classTypeProbabilitiesForLanguage);
	}

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget unit) throws TextClassificationException {
		Set<Feature> features = new HashSet<>();

		String language = jcas.getDocumentLanguage();
		String token = KnowNERLanguage.requiresLemma(language)?
				JCasUtil.selectCovered(jcas, Lemma.class, unit).get(0).getValue() :
				unit.getCoveredText();

		Map<String, ClassProbabilityDistributionBmeow> classTypeProbability = classTypeProbabilities.get(language);
		if(classTypeProbability.containsKey(token)){
			ClassProbabilityDistributionBmeow distribution = classTypeProbability.get(token);
			for (int i = 0; i < 4; i++) {
				features.add(new Feature(FEATURE_NAME + "_" + DIMENSIONS[i], distribution.getPers()[i]));
				features.add(new Feature(FEATURE_NAME + "_" + DIMENSIONS[i+4], distribution.getOrg()[i]));
				features.add(new Feature(FEATURE_NAME + "_" + DIMENSIONS[i+8], distribution.getLoc()[i]));
				features.add(new Feature(FEATURE_NAME + "_" + DIMENSIONS[i+12], distribution.getMisc()[i]));
			}
		} else {
			for (int i = 0; i < 4; i++) {
				features.add(new Feature(FEATURE_NAME + "_" + DIMENSIONS[i], 0.0));
				features.add(new Feature(FEATURE_NAME + "_" + DIMENSIONS[i+4], 0.0));
				features.add(new Feature(FEATURE_NAME + "_" + DIMENSIONS[i+8], 0.0));
				features.add(new Feature(FEATURE_NAME + "_" + DIMENSIONS[i+12], 0.0));
			}
		}

		return features;
	}

}
