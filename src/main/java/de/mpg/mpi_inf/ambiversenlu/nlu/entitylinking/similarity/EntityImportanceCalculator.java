package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.similarity;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.LanguageSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.importance.EntityImportance;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * A generic calculator for EntityImportance which 
 * calculates the scores for each mention in a given collection using
 * the given {@link de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.importance.EntityImportance
 * EntityImportance} and writes them into '.score' files.
 */
public class EntityImportanceCalculator extends ScoreCalculator {

  private static final Logger logger = LoggerFactory.getLogger(EntityImportanceCalculator.class);

  /** EntityImportance id */
  String impId;

  /** Constructor for the EntityImportance */
  Constructor<?> impConstructor;

  /**
   * A generic calculator for entity importance.
   *
   * @param cr The collection reader that should be used.
   * @param outputDirectory The directory where the '.score' file should be saved.
   * @param impConstructor The constructor for the EntityImportance.
   * @param includeIncorrectEntities
   * @param tracer The tracer that should be used.
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws InstantiationException
   */
  public EntityImportanceCalculator(LanguageSettings languageSettings, List<ProcessedDocument> documents, File outputDirectory,
                                    Constructor<?> impConstructor,
                                    boolean includeIncorrectEntities, Tracer tracer, boolean isNamedEntity)
      throws IllegalAccessException, InvocationTargetException, InstantiationException, EntityLinkingDataAccessException,
      AidaUnsupportedLanguageException {
    super(languageSettings, documents, outputDirectory, includeIncorrectEntities, false, tracer, isNamedEntity);
    if (!EntityImportance.class.isAssignableFrom(impConstructor.getDeclaringClass()))
      throw new IllegalArgumentException("impConstructor is not a EntityImportance constructor");
    this.impConstructor = impConstructor;
    impId = ((EntityImportance) impConstructor.newInstance(new Entities())).getIdentifier();
  }

  @Override public void run() {

    File eiFile = new File(outputDirectory, getFileName(impId));

    BufferedWriter eiWriter = null;

    try {
      if (!eiFile.createNewFile()) {
        logger.info(impId + " exists, not creating ...");
        return;
      } else {
        eiWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(eiFile), "UTF-8"));
      }

      int current = 0;

      for (ProcessedDocument input : documents) {
        current++;

        Mentions mentions = input.getMentions();
        Entities entities = getAllEntities(mentions);

        EntityImportance ei = (EntityImportance) impConstructor.newInstance(entities);

        for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
          for (Mention m : innerMap.values()) {
            for (Entity e : getEntitiesForMention(m)) {
              double imp = ei.getImportance(e);
  
              String impInstance = getInstanceString(input.getDocId(), m, e, imp);
              eiWriter.write(impInstance);
              eiWriter.newLine();
            }
          }
        }

        logger.info(impId + " (" + current + "/" + documents.size() + ")");

        eiWriter.flush();
      }

      eiWriter.close();
    } catch (Exception e) {
      logger.error("Couldn't create EntityImportance, aborting!\n");
      e.printStackTrace();
    }
  }

  public static String getFileName(String impId) {
    return impId + ".score";
  }
}