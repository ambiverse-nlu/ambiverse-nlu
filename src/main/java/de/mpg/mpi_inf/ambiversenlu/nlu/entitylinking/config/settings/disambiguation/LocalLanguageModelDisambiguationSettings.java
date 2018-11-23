package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.Settings.TECHNIQUE;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.SimilaritySettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * Preconfigured settings for the {@see Disambiguator} using only the 
 * language model similarity.
 */
public class LocalLanguageModelDisambiguationSettings extends DisambiguationSettings {

  private static final long serialVersionUID = -6391627336407534940L;

  public LocalLanguageModelDisambiguationSettings() throws MissingSettingException, NoSuchMethodException, ClassNotFoundException, IOException {
    setDisambiguationTechnique(TECHNIQUE.LOCAL);
    
    String trainingCorpus = EntityLinkingConfig.get(EntityLinkingConfig.TRAINING_CORPUS);
    System.out.println("Training Corpus: " + trainingCorpus);
    
    String fileNameC = "SwitchedUnit";//"SwitchedUnit" "WordVecTEMP"
    String fileNameNE = "SwitchedUnit";
    
    Properties propertiesNE = ClassPathUtils.getPropertiesFromClasspath("similarity/" + trainingCorpus + "/" + fileNameNE + "_NE.properties");
    SimilaritySettings similaritySettingsNE = new SimilaritySettings(propertiesNE, fileNameNE);
    setSimilaritySettingsNE(similaritySettingsNE);
    
    Properties propertiesC = ClassPathUtils.getPropertiesFromClasspath("similarity/" + trainingCorpus + "/" + fileNameC + "_C.properties");
    SimilaritySettings similaritySettingsC = new SimilaritySettings(propertiesC, fileNameNE);
    setSimilaritySettingsC(similaritySettingsC);
  }
}
