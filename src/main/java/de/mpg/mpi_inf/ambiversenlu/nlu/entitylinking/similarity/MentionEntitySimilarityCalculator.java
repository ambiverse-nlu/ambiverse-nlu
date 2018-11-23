package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.similarity;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.LanguageSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EnsembleMentionEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.MentionEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContextSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.MentionEntitySimilarityMeasure;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A generic calculator for MentionEntitySimilarity which 
 * calculates the scores for each mention in a given collection using
 * the given {@link de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.MentionEntitySimilarityMeasure
 * MentionEntitySimilarityMeasure} and 
 * {@link de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContext EntitiesContext}
 * and writes them into '.score' files.
 */
public class MentionEntitySimilarityCalculator extends ScoreCalculator {

  private static final Logger logger = LoggerFactory.getLogger(MentionEntitySimilarityCalculator.class);

  /** MentionEntitySimilarity id */
  String mesId;

  /** Constructor for the MentionEntitySimilarityMeasure */
  Constructor<?> mesmConstructor;

  /** Constructor for the EntitiesContext */
  Constructor<?> contextConstructor;

  EntitiesContextSettings entitiesContextSettings;

  boolean useDistanceDiscount;

  /**
   * @param documents The list of documents.
   * @param outputDirectory The directory where the '.score' file should be saved.
   * @param mesmConstructor The constructor for the MentionEntitySimilarityMeasure.
   * @param contextConstructor The constructor for the EntitiesContext.
   * @param includeIncorrectEntityCandidates True to use all entities, false to use only the matching ones.
   * @param doDocumentNormalization True to normalize the scores over documents, false for no normalisation.
   * @param useDistanceDiscount {@link de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.MentionEntitySimilarityMeasure#setUseDistanceDiscount(boolean) MentionEntitySimilarityMeasure#setUseDistanceDiscount()}
   * @param tracer The tracer that should be used.
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws InstantiationException
   */
  public MentionEntitySimilarityCalculator(LanguageSettings languageSettings, List<ProcessedDocument> documents, File outputDirectory,
                                           Constructor<?> mesmConstructor, Constructor<?> contextConstructor, EntitiesContextSettings entitiesContextSettings,
                                           boolean includeIncorrectEntityCandidates, boolean doDocumentNormalization, 
                                           boolean useDistanceDiscount, Tracer tracer, boolean isNamedEntity)
      throws IllegalAccessException, InvocationTargetException, InstantiationException, EntityLinkingDataAccessException,
      AidaUnsupportedLanguageException {
    super(languageSettings, documents, outputDirectory, includeIncorrectEntityCandidates, doDocumentNormalization, tracer, isNamedEntity);
    if (!MentionEntitySimilarityMeasure.class.isAssignableFrom(mesmConstructor.getDeclaringClass()))
      throw new IllegalArgumentException("mesmConstructor is not a MentionEntitySimilarityMeasure constructor");
    if (!EntitiesContext.class.isAssignableFrom(contextConstructor.getDeclaringClass()))
      throw new IllegalArgumentException("contextConstructor is not a EntitiesContext constructor");
    this.mesmConstructor = mesmConstructor;
    this.contextConstructor = contextConstructor;
    this.entitiesContextSettings = entitiesContextSettings;
    this.useDistanceDiscount = useDistanceDiscount;

    mesId = new MentionEntitySimilarity((MentionEntitySimilarityMeasure) mesmConstructor.newInstance(tracer),
        (EntitiesContext) contextConstructor.newInstance(new Entities(), new ExternalEntitiesContext(), null)).getIdentifier();
  }

  @Override public void run() {
    try (BufferedWriter writer = createFile()) {
      if (writer == null) return;

      ExecutorService es = Executors.newFixedThreadPool(8);

      for (ProcessedDocument input : documents) {
        String docId = input.getDocId();
        Calculator calc = new Calculator(input, writer);
        es.execute(calc);
      }

      es.shutdown();
      es.awaitTermination(6, TimeUnit.DAYS);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  private BufferedWriter createFile() throws IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
    File outFile = new File(outputDirectory, mesId2FileName(mesId));

    if (!outFile.createNewFile()) {
      logger.info(mesId + " exists, not creating ...");
      return null;
    } else {
      return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
    }
  }

  public static String mesId2FileName(String mesId) {
    return mesId.replace(":", "-") + ".score";
  }

  @Override public boolean equals(Object o) {
    return o.getClass() == this.getClass() && mesId.equals(((MentionEntitySimilarityCalculator) o).mesId);
  }

  @Override public int hashCode() {
    return mesId.hashCode();
  }

  private class Calculator implements Runnable {

    private ProcessedDocument document;

    private final BufferedWriter writer;

    public Calculator(ProcessedDocument document, BufferedWriter writer) {
      this.writer = writer;
      this.document = document;
    }

    @Override public void run() {
      try {
        long docBeginTime = System.currentTimeMillis();

        Mentions mentions = document.getMentions();
        logger.info("Mentions size: " + mentions.getMentions().size());
        Entities entities = getAllEntities(mentions);
        EntitiesContext entitiesContext = (EntitiesContext) contextConstructor
            .newInstance(entities, new ExternalEntitiesContext(), entitiesContextSettings);
        MentionEntitySimilarityMeasure mentionEntitySimilarityMeasure = (MentionEntitySimilarityMeasure) mesmConstructor.newInstance(tracer);
        mentionEntitySimilarityMeasure.setUseDistanceDiscount(useDistanceDiscount);
        MentionEntitySimilarity mentionEntitySimilarity = new MentionEntitySimilarity(mentionEntitySimilarityMeasure, entitiesContext);

        Map<Mention, Map<Entity, Double>> precomputedScores = new HashMap<>();

        Context context = new Context(document.getTokens());
        for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
          for (Mention m : innerMap.values()) {
            Map<Entity, Double> mentionScores = new HashMap<>();
            precomputedScores.put(m, mentionScores);
            for (Entity e : getEntitiesForMention(m)) {
              double sim = mentionEntitySimilarity.calcSimilarity(m, context, e);
              mentionScores.put(e, sim);
            }
          }
        }

        double[] minMax = null;
        if (doDocumentNormalization) {
          minMax = new double[] { Double.MAX_VALUE, 0.0 };
          for (Map<Entity, Double> mentionScores : precomputedScores.values()) {
            for (Double score : mentionScores.values()) {
              minMax[0] = Math.min(minMax[0], score);
              minMax[1] = Math.max(minMax[1], score);
            }
          }
        }

        for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
          for (Mention m : innerMap.values()) {
            Map<Entity, Double> scoresForEntities = precomputedScores.get(m);
            for (Entity e : getEntitiesForMention(m)) {
              double sim = scoresForEntities.get(e);
              if (minMax != null) { // doDocumentNormalization
                sim = EnsembleMentionEntitySimilarity.rescale(sim, minMax[0], minMax[1]);
              }
              String instance = getInstanceString(document.getDocId(), m, e, sim);
              synchronized (writer) {
                writer.write(instance);
                writer.newLine();
              }
            }
          }
        }

        long docTotalTime = (System.currentTimeMillis() - docBeginTime) / 1000;
        logger.info(mesId + " (" + document.getDocId() + ") - " + docTotalTime + "s for this doc");
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      }
    }
  }
}
