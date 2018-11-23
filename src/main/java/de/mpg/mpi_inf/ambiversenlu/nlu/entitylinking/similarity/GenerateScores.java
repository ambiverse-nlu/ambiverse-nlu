package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.similarity;

import com.google.common.collect.Iterables;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.LanguageSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.SimilaritySettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Generates scores for use by 
 * {@link EstimateParameterWeightsFromScores
 * EstimateParameterWeightsFromScores}, which creates WEKA formatted input.
 * It will generate scores for all mentions in the input collection and
 * write one file per measure method in the settings files.
 */
public class GenerateScores {

  private Collection<SimilaritySettings> settings;

  public GenerateScores(Collection<SimilaritySettings> settings) {
    this.settings = settings;
  }

  /**
   * Generates the scores for each measure method given in a given settings file
   * and writes them into '.score' files.
   *
   * @param languageSettings settings for the language of the documents
   * @param documents A set of documents
   * @param outputDirectory The directory the '.score' files should go.
   * @param includeIncorrectEntities True to use all entities, false to use only the matching ones.
   * @param doDocumentNormalization True to normalize the scores over documents, false for no normalisation.
   * @param useDistanceDiscount {@link de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.MentionEntitySimilarityMeasure#setUseDistanceDiscount(boolean) MentionEntitySimilarityMeasure#setUseDistanceDiscount()}
   * @param tracer The tracer that should be used.
   * @throws Exception
   */
  protected void generate(LanguageSettings languageSettings, List<ProcessedDocument> documents, File outputDirectory, boolean includeIncorrectEntities,
                          boolean doDocumentNormalization, boolean useDistanceDiscount, Tracer tracer, boolean isNamedEntity) throws Exception {
    
    if (!outputDirectory.exists() && !outputDirectory.mkdir()) {
      return;
    }

    Set<Runnable> calcs = new HashSet<>();

    for (SimilaritySettings setting : settings) {
      for (SimilaritySettings.MentionEntitySimilarityRaw mentionEntitySimilarityRaw : Iterables
          .concat(setting.getMentionEntitySimilaritiesNoPrior(), setting.getMentionEntitySimilaritiesWithPrior())) {
        calcs.add(new MentionEntitySimilarityCalculator(languageSettings, documents, outputDirectory, mentionEntitySimilarityRaw.getSimConstructor(),
          mentionEntitySimilarityRaw.getContextConstructor(), setting.getEntitiesContextSettings(false), 
          includeIncorrectEntities, doDocumentNormalization, useDistanceDiscount, tracer, isNamedEntity));
      }

      for (SimilaritySettings.EntityImportancesRaw entityImportancesRaw : Iterables
          .concat(setting.getEntityImportancesNoPrior(), setting.getEntityImportancesWithPrior())) {
        calcs.add(new EntityImportanceCalculator(languageSettings, documents, outputDirectory, entityImportancesRaw.getImpConstructor(),
          includeIncorrectEntities, tracer, isNamedEntity));
      }

      if (EstimateParameterWeightsFromScores.settingNeedsPrior(setting)) {
        calcs.add(new PriorCalculator(languageSettings, documents, outputDirectory, includeIncorrectEntities, tracer,
          setting.getIdentifier(), setting.shouldPriorTakeLog(), setting.getPriorDampingFactor(), isNamedEntity));
      }
    }
    
    calcs.add(new TrueFalseCalculator(languageSettings, documents, outputDirectory, includeIncorrectEntities, tracer, isNamedEntity));
//    calcs.add(new NoMatchingEntityCalculator(cr, dir, includeIncorrectEntities, tracer));
    
    ExecutorService es = Executors.newFixedThreadPool(2);

    calcs.forEach(es::execute);

    es.shutdown();
    es.awaitTermination(6, TimeUnit.DAYS);
  }
}