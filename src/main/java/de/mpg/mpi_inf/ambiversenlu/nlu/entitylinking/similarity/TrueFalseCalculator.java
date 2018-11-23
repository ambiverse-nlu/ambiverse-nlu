package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.similarity;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.LanguageSettings;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TrueFalseCalculator extends ScoreCalculator {

  private static final Logger logger = LoggerFactory.getLogger(TrueFalseCalculator.class);

  public static final String FILE_NAME = "truefalse.gt";

  public static final String TRUE_STRING = "TRUE";

  public static final String FALSE_STRING = "FALSE";
  
  public TrueFalseCalculator(LanguageSettings languageSettings, List<ProcessedDocument> documents, File outputDirectory, boolean includeIncorrectEntities, Tracer tracer, boolean isNamedEntity)
      throws EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    super(languageSettings, documents, outputDirectory, includeIncorrectEntities, false, tracer, isNamedEntity);
  }

  @Override public void run() {

    File gtFile = new File(outputDirectory, FILE_NAME);

    BufferedWriter gtWriter = null;

    try {
      if (!gtFile.createNewFile()) {
        logger.info("TrueFalse exists, not creating ...");
        return;
      } else {
        gtWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(gtFile), "UTF-8"));
      }

      int current = 0;

      for (ProcessedDocument input : documents) {
        String docId = input.getDocId();
        current++;

        Mentions mentions = input.getMentions();

        for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
          for (Mention m : innerMap.values()) {
            for (Entity e : getEntitiesForMention(m)) {
              String candidateTarget = e.getIdentifierInKb();
              Set<String> correctTarget = m.getGroundTruthResult();
  
              String tfInstance;
  
              if (correctTarget.contains(candidateTarget)) {
                tfInstance = getInstanceString(docId, m, e, TRUE_STRING);
              } else {
                tfInstance = getInstanceString(docId, m, e, FALSE_STRING);
              }
  
              gtWriter.write(tfInstance);
              gtWriter.newLine();
            }
          }
        }

        logger.info("TrueFalse (" + current + "/" + documents.size() + ")");

        gtWriter.flush();
      }

      gtWriter.close();
    } catch (Exception e) {
      logger.error("Couldn't create TrueFalse, aborting!\n");
      e.printStackTrace();
    }
  }
}