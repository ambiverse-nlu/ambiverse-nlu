package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.similarity;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.LanguageSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.lookup.EntityLookupManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ScoreCalculator implements Runnable {

  public static final String VALUE_SEPARATOR = "\t";

  protected List<ProcessedDocument> documents;

  protected File outputDirectory;

  private boolean includeIncorrectEntityCandidates;

  protected boolean doDocumentNormalization;

  protected Tracer tracer = null;

  private EntityLookupManager entityLookupMgr;

  protected boolean isNamedEntity;

  public ScoreCalculator(LanguageSettings languageSettings, List<ProcessedDocument> documents, File outputDirectory, Tracer tracer, boolean isNamedEntity)
      throws EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    this(languageSettings, documents, outputDirectory, true, false, tracer, isNamedEntity);
  }

  public ScoreCalculator(LanguageSettings languageSettings, List<ProcessedDocument> documents, File outputDirectory,
      boolean includeIncorrectEntityCandidates, boolean doDocumentNormalization, Tracer tracer, boolean isNamedEntity)
      throws EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    this.documents = documents;
    this.outputDirectory = outputDirectory;
    this.includeIncorrectEntityCandidates = includeIncorrectEntityCandidates;
    this.doDocumentNormalization = doDocumentNormalization;
    this.tracer = tracer;
    this.entityLookupMgr = new EntityLookupManager(languageSettings);
    this.isNamedEntity = isNamedEntity;
  }

  protected Entities getAllEntities(Mentions mentions) throws SQLException, IOException, EntityLinkingDataAccessException {
    Set<Entity> entityMap = new HashSet<Entity>();
    for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
      for (Mention mention : innerMap.values()) {
        for (Entity entity : entityLookupMgr.getEntitiesForMention(mention, isNamedEntity)) {
          if (includeIncorrectEntityCandidates || mention.getGroundTruthResult().equals(entity.getIdentifierInKb())) {
            entityMap.add(entity);
          }
        }
      }
    }
    return new Entities(entityMap);
  }

  protected Entities getEntitiesForMention(Mention mention) throws IOException, EntityLinkingDataAccessException {
    Entities entities = entityLookupMgr.getEntitiesForMention(mention, 1.0, isNamedEntity);

    if (!includeIncorrectEntityCandidates) {
      Entities correctEntities = new Entities();

      Set<String> correctTarget = mention.getGroundTruthResult();

      for (Entity e : entities) {
        String candidateTarget = e.getIdentifierInKb();

        if (correctTarget.contains(candidateTarget)) {
          correctEntities.add(e);
        }
      }

      entities = correctEntities;
    }

    return entities;
  }

  protected String getInstanceString(String docId, Mention m, Entity e, double sim) {
    return docId + ":" + m.getId() + ":" + m.getMention() + VALUE_SEPARATOR + e.getIdentifierInKb() + VALUE_SEPARATOR + sim;
  }

  protected String getInstanceString(String docId, Mention m, Entity e, String cls) {
    return docId + ":" + m.getId() + ":" + m.getMention() + VALUE_SEPARATOR + e.getIdentifierInKb() + VALUE_SEPARATOR + cls;
  }
}
