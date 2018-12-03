package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.Settings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EntityEntitySimilarityCombinationsIds;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.SimilaritySettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Preconfigured settings for the {@see Disambiguator} using the mention-entity
 * prior, the language model context similarity, and the MilneWitten Wikipedia link
 * based entity coherence.
 *
 * It applies additional heuristics to fix entities before the graph algorithm
 * is run, and a threshold to assign Entity.OOKBE to null mentions.
 *
 * Use this for running on "real world" documents to get the best results.
 */
public class CocktailPartyLangaugeModelDefaultDisambiguationSettings extends DisambiguationSettings {

  private static final long serialVersionUID = -7635330852952398920L;

  public CocktailPartyLangaugeModelDefaultDisambiguationSettings() throws IOException, NoSuchMethodException, ClassNotFoundException {

    setDisambiguationTechnique(Settings.TECHNIQUE.GRAPH);
    setDisambiguationAlgorithm(Settings.ALGORITHM.COCKTAIL_PARTY_SIZE_CONSTRAINED);

    getGraphSettings().setUseExhaustiveSearch(true);
    getGraphSettings().setUseNormalizedObjective(true);

    setTrainingCorpus(EntityLinkingConfig.get(EntityLinkingConfig.TRAINING_CORPUS));
    
    String fileNameC = "SwitchedUnit";//"SwitchedUnit" "WordVecTEMP"
    String fileNameNE = "SwitchedUnit"; 
    String flag = "";

    List<String[]> cohConfigs;
    cohConfigs = EntityEntitySimilarityCombinationsIds.InlinkOverlap.getConfig();
//    cohConfigs = EntityEntitySimilarityCombinationsIds.MilneWitten.getConfig();
//    cohConfigs = EntityEntitySimilarityCombinationsIds.VectorRepresentation.getConfig();
//    cohConfigs = EntityEntitySimilarityCombinationsIds.MilneWittenAndVectorRepresentation.getConfig(); //0.5 weights
    
    Properties switchedUnitPropNE = ClassPathUtils.getPropertiesFromClasspath("similarity/" + getTrainingCorpus() + "/" + fileNameNE + flag + "_NE.properties");
    SimilaritySettings switchedUnitSettingsNE = new SimilaritySettings(switchedUnitPropNE, "SimilarityNE");
    switchedUnitSettingsNE.setEntityEntitySimilarities(cohConfigs);
    setSimilaritySettingsNE(switchedUnitSettingsNE);
    
    Properties switchedUnitPropC = ClassPathUtils.getPropertiesFromClasspath("similarity/" + getTrainingCorpus() + "/" + fileNameC +  flag + "_C.properties");
    SimilaritySettings switchedUnitSettingsC = new SimilaritySettings(switchedUnitPropC, "SimilarityC");
    switchedUnitSettingsC.setEntityEntitySimilarities(cohConfigs);
    setSimilaritySettingsC(switchedUnitSettingsC);

    Properties cohRobPropNE = ClassPathUtils.getPropertiesFromClasspath("similarity/" + getTrainingCorpus() +"/" + fileNameNE + flag + "_cohrob_NE.properties");
    SimilaritySettings unnormalizedKPsettingsNE = new SimilaritySettings(cohRobPropNE, "CoherenceRobustnessTestNE");
    getGraphSettings().setCoherenceSimilaritySettingNE(unnormalizedKPsettingsNE);

    setComputeConfidence(true);
    
    setMaxCandidatesPerEntityByPrior(500);

    // Default hyperparameters are trained on the AIDA-CoNLL dataset.
    double alpha = 0.7125;
    double cohRobThreshNE = 1.075;
    double cohRobThreshC = 0; //not used
    double confTestThresh = 0.635;
    double nullThresh = 0.00833;

    switch (getTrainingCorpus()) {
      case "spiegel":
        alpha = 1.0;
        cohRobThreshNE = 0.91;
        confTestThresh = 0.65;
        nullThresh = 0.073;
    }

    getGraphSettings().setAlpha(alpha);
    getGraphSettings().setUseCoherenceRobustnessTestNE(true);
    getGraphSettings().setCohRobustnessThresholdNE(cohRobThreshNE);
    getGraphSettings().setUseCoherenceRobustnessTestC(false);
    getGraphSettings().setCohRobustnessThresholdC(cohRobThreshC);
    getGraphSettings().setUseEasyMentionsTest(true);
    getGraphSettings().setEasyMentionsTestThreshold(5);
    getGraphSettings().setUseConfidenceThresholdTest(true);
    getGraphSettings().setConfidenceTestThreshold(confTestThresh);
    getGraphSettings().setPruneCandidateEntities(true);
    getGraphSettings().setPruneCandidateThreshold(25);
    setNullMappingThreshold(nullThresh);
  }
}
