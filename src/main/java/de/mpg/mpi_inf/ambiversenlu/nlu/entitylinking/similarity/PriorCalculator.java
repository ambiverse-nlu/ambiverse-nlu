package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.similarity;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.LanguageSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.MaterializedPriorProbability;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.PriorProbability;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.MathUtil;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculates the priors for each mention-entity pair in a
 * given collection and writes them into the 'prior.score'
 * file as well as the best prior into th 'bestPrior.score' file.
 */
public class PriorCalculator extends ScoreCalculator {

  private static final Logger logger = LoggerFactory.getLogger(PriorCalculator.class);

  private static final String PRIOR_LABEL = "prior";

  private static final String BEST_PRIOR_LABEL = "bestPrior";

  private static final String FILE_NAME_POSTFIX = "." + PRIOR_LABEL + ".score";

  private static final String FILE_NAME_BEST_POSTFIX = "." + BEST_PRIOR_LABEL + ".score";

  private String settingsId;

  private boolean shouldPriorTakeLog;

  private double dampingFactor;
  
  public PriorCalculator(LanguageSettings languageSettings, List<ProcessedDocument> documents, File outputDirectory, boolean includeIncorrectEntities, Tracer tracer,
                         String settingsId, boolean shouldPriorTakeLog, double dampingFactor, boolean isNamedEntity)
      throws EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    super(languageSettings, documents, outputDirectory, includeIncorrectEntities, false, tracer, isNamedEntity);
    this.shouldPriorTakeLog = shouldPriorTakeLog;
    this.dampingFactor = dampingFactor;
    this.settingsId = settingsId;
  }

  @Override public void run() {
    File priorFile = new File(outputDirectory, getFileName(settingsId));
    File bestPriorFile = new File(outputDirectory, getFileNameBest(settingsId));

    BufferedWriter priorWriter = null;
    BufferedWriter bestPriorWriter = null;

    try {
      if (!priorFile.createNewFile() && !bestPriorFile.createNewFile()) {
        logger.info(settingsId + " prior exists, not creating ...");
        return;
      } else {
        priorWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(priorFile), "UTF-8"));
        bestPriorWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bestPriorFile), "UTF-8"));
      }

      int current = 0;

      for (ProcessedDocument input : documents) {
        current++;

        Mentions mentions = input.getMentions();
        String docId = input.getDocId();

        Set<Mention> mentionObjects = new HashSet<>();
        for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
          mentionObjects.addAll(innerMap.values());
        }

        PriorProbability pp = new MaterializedPriorProbability(mentionObjects, isNamedEntity);

        for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
          for (Mention m : innerMap.values()) {
            for (Entity e : getEntitiesForMention(m)) {
              double prior = pp.getPriorProbability(m, e);
              double bestPrior = pp.getBestPrior(m);
  
              if (shouldPriorTakeLog) {
                prior = MathUtil.logDamping(prior, dampingFactor);
                bestPrior = MathUtil.logDamping(bestPrior, dampingFactor);
              }
  
              String priorInstance = getInstanceString(docId, m, e, prior);
              priorWriter.write(priorInstance);
              priorWriter.newLine();
  
              String bestPriorInstance = getInstanceString(docId, m, e, bestPrior);
              bestPriorWriter.write(bestPriorInstance);
              bestPriorWriter.newLine();
            }
          }
        }

        logger.info(settingsId + " prior (" + current + "/" + documents.size() + ")");

        priorWriter.flush();
        bestPriorWriter.flush();
      }

      priorWriter.close();
      bestPriorWriter.close();
    } catch (Exception e) {
      logger.error("Couldn't create Prior, aborting!\n");
      e.printStackTrace();
    }
  }

  public static String getFileName(String settingsId) {
    return settingsId + FILE_NAME_POSTFIX;
  }

  public static String getFileNameBest(String settingsId) {
    return settingsId + FILE_NAME_BEST_POSTFIX;
  }

  public static String getLabel(String settingsId) {
    return settingsId + "." + PRIOR_LABEL;
  }

  public static String getLabelBest(String settingsId) {
    return settingsId + "." + BEST_PRIOR_LABEL;
  }
}