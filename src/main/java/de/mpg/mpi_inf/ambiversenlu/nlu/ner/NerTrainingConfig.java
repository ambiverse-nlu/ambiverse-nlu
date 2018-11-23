/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.uima.resource.Resource;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * This class can be used to train a model based on the features
 * you provide in the config file.
 *
 * In addition to the config file, you need to specify which annotators should
 * be run as part of the pre-processing. See method 'getPreprocessing()' in class
 * AmbiverseNerTrainer for more information.
 *
 * ===============================================================================
 * WHAT A CONFIGURATION FILE SHOULD LOOK LIKE:
 * (see more examples at src/main/config/train/)
 * ------------------------------------------------------------------------------
 *
 * # The title for each model. This helps to name a set of features.
 * ner.model.title = Agnostic
 *
 * # The output path of the model.
 * ner.model.outputPath = /var/tmp/dseyler/model/agnostic/
 *
 * # Here you have to provide the full class including the package name of the
 * # feature extractors you want to use for this model.
 * ner.features = org.dkpro.tc.features.tcu.CurrentUnit
 * ner.features = org.dkpro.tc.features.ngram.LuceneCharacterNGramUFE
 * ner.features = mpi.ner.features.PosTag
 * ner.features = mpi.ner.features.PosTagSequence
 * ner.features = mpi.ner.features.CurrentWordShape
 * ner.features = mpi.ner.features.WordShapeSequence
 * ner.features = mpi.ner.features.PresenceInLeftWindow
 * ner.features = mpi.ner.features.PresenceInRightWindow
 *
 * # The parameters that are required for the features
 * ner.parameters = charNgramMinN, 1
 * ner.parameters = charNgramMaxN, 6
 * ner.parameters = charNgramUseTopK, 50
 * ner.parameters = windowSize, 4
 *
 * ------------------------------------------------------------------------------
 */
public class NerTrainingConfig {

	Logger logger = Logger.getLogger(getClass());

	private Configuration configuration = null;

	/**
	 * @param file
	 * 			Path to config file
	 */
	public NerTrainingConfig(String file) {
		try {
			configuration = new PropertiesConfiguration(file);
		} catch (ConfigurationException e) {
			logger.error("Configuration file could not be loaded: " + file);
			e.printStackTrace();
		}
	}

	public List<String> getFeatures(){
		return Arrays.asList(configuration.getStringArray("ner.features"));
	}

	public TcFeatureSet getFeatureSet(){
		TcFeatureSet fs = new TcFeatureSet(
				Arrays
						.stream(configuration.getStringArray("ner.features"))
						.map(s -> {
							try {
								return TcFeatureFactory.create((Class<? extends Resource>)Class.forName(s));
							} catch (ClassNotFoundException e) {
								return null;
							}
						}).toArray(TcFeature[]::new));
		return fs;
	}
	
	public Object[] getPipelineParameters(){
		String[] strings = configuration.getStringArray("ner.parameters");
		Object[] parameters = new Object[strings.length]; 
		for (int i = 0; i < strings.length; i++) {
			// every second parameter is an integer or boolean
			if(i % 2 != 0){
				if ("true".equals(strings[i]) || "false".equals(strings[i])) {
					parameters[i] = Boolean.parseBoolean(strings[i]);
				} else {
					try {
						parameters[i] = Integer.parseInt(strings[i]);
					} catch (NumberFormatException nfe) {
						parameters[i] = strings[i];
					}
				}
			} else {
				parameters[i] = strings[i];
			}
		}
		return parameters;
	}
//
//	public String getModelOutputPath(){
//		if (configuration.containsKey("ner.model.outputPath")) {
//			return configuration.getString("ner.model.outputPath");
//		}
//		else {
//			return Settings.MODELS_PATH;
//		}
//	}
	
	public String getModelTitle(){
		return configuration.getString("ner.model.title");
	}
//
//	public boolean useLemmatizer(){
//		return configuration.getBoolean("ner.useLemmatizer");
//	}
//
//	public boolean usePOSTagger(){
//		return configuration.getBoolean("ner.usePOSTagger");
//	}

}
