package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.lookup;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.LanguageSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

/**
 * Provides entity candidate lookup. Uses a database or ternarytree/dmap backend, the actual implementation
 * is done in a subclass of {@see mpi.aida.preparation.EntityLookup}.
 */
public class EntityLookupManager {

  private Logger logger_ = LoggerFactory.getLogger(EntityLookupManager.class);

  private EntityLookup lookupInst;

  public EntityLookupManager(LanguageSettings languageSettings) throws EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    switch (EntityLinkingConfig.getEntityLookupSource()) {
      case DATABASE:
        lookupInst = new DbLookup(languageSettings);
        break;
    }
  }

  public Entities getEntitiesForMention(Mention mention, boolean isNamedEntity) throws IOException, EntityLinkingDataAccessException {
    return getEntitiesForMention(mention, 1.0, 0, false, isNamedEntity);
  }

  public Entities getEntitiesForMention(Mention mention, double maxEntityRank, boolean isNamedEntity) throws IOException, EntityLinkingDataAccessException {
    return getEntitiesForMention(mention, maxEntityRank, 0, false, isNamedEntity);
  }

  public Entities getEntitiesForMention(Mention mention, double maxEntityRank, int topByPrior, boolean isNamedEntity) throws IOException, EntityLinkingDataAccessException {
    return getEntitiesForMention(mention, maxEntityRank, topByPrior, false, isNamedEntity);
  }

  /**
   * Returns the potential entity candidates for a mention (from AIDA dictionary)
   *
   * @param mention
   *            Mention to get entity candidates for
   * @param maxEntityRank Retrieve entities up to a global rank, where rank is
   * between 0.0 (best) and 1.0 (worst). Setting to 1.0 will retrieve all entities.
   * @param topByPrior  Retrieve only the best entities according to the prior.
   * @param mentionIsPrefix Treat the mention string as prefix, gathering all candidates that have at least one label
   *                        with mention as prefix.
   * @return Candidate entities for this mention.
   *
   */
  public Entities getEntitiesForMention(Mention mention, double maxEntityRank, int topByPrior, boolean mentionIsPrefix, boolean isNamedEntity)
      throws IOException, EntityLinkingDataAccessException {
    return lookupInst.getEntitiesForMention(mention, maxEntityRank, topByPrior, mentionIsPrefix, isNamedEntity);
  }

  public void fillInCandidateEntities(Mentions conceptMentions, Mentions namedEntityMentions) throws SQLException, IOException, EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    fillInCandidateEntities(conceptMentions, namedEntityMentions, null, null, false, false, 1.0, 0, false);
  }

  public void fillInCandidateEntities(Mentions conceptMentions, Mentions namedEntityMentions, boolean includeNullEntityCandidates, boolean includeContextMentions, double maxEntityRank)
      throws SQLException, IOException, EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    fillInCandidateEntities(conceptMentions, namedEntityMentions, null, null, includeNullEntityCandidates, includeContextMentions, maxEntityRank, 0, false);
  }

  /**
   * Retrieves all the candidate entities for the given mentions.
   *
   * @param mentions  All mentions in the input doc.
   * @param externalDictionary Additional mention-entity dictionary for lookups.
   * Can be used to supplement the actual entity repository, pass null to ignore.
   * @param includeNullEntityCandidates Set to true to include mentions flagged
   * as NME in the ground-truth data.
   * @param includeContextMentions  Include mentions as context.
   * @param maxEntityRank Fraction of entities to include. Between 0.0 (none)
   * and 1.0 (all). The ranks are taken from the entity_rank table.
   * @param topByPrior  How many candidates to return, according to the ranking by prior. Set to 0 to return all.
   * @param mentionIsPrefix Treat the mention string as prefix, gathering all candidates that have at least one label
   *                        with mention as prefix.
   * @throws SQLException
   */
  public void fillInCandidateEntities(Mentions conceptMentions, Mentions namedEntityMentions, CandidateDictionary externalDictionary,
                                      Set<KBIdentifiedEntity> blacklistedEntities,
                                      boolean includeNullEntityCandidates, boolean includeContextMentions,
                                      double maxEntityRank, int topByPrior, boolean mentionIsPrefix)
      throws SQLException, IOException, EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    lookupInst.fillInCandidateEntities(
            conceptMentions, namedEntityMentions, externalDictionary, blacklistedEntities,
            includeNullEntityCandidates, includeContextMentions,
            maxEntityRank, topByPrior, mentionIsPrefix);
  }
}