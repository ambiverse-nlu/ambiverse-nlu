package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.lookup;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.LanguageSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class DbLookup extends EntityLookup {

  public DbLookup(LanguageSettings languageSettings) throws EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    super(languageSettings);
  }

  @Override
  public Entities getEntitiesForMention(Mention mention, double maxEntityRank, int topByPrior, boolean mentionIsPrefix, boolean isNamedEntity)
      throws EntityLinkingDataAccessException {
    if (mentionIsPrefix) {
      throw new IllegalArgumentException(
          DbLookup.class + " does not support prefix-based candidate lookup. " + "Use DictionaryLookup if this functionality is needed.");
    }
    int id = RunningTimer.recordStartTime("dbLookup:Entity");
    Set<String> normalizedMentions = mention.getNormalizedMention();
//    normalizedMentions.add(mention.getMention());
    Entities entities = new Entities();
    Map<String, Entities> entitiesMap = DataAccess.getEntitiesForMentions(normalizedMentions, maxEntityRank, topByPrior, isNamedEntity);
    for(Entry<String, Entities> entry : entitiesMap.entrySet()) {
      entities.addAll(entry.getValue());
    }
    RunningTimer.recordEndTime("dbLookup:Entity", id);
    return entities;
  }
}
