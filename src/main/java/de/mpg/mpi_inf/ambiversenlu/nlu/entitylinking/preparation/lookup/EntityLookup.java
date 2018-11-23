package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.lookup;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.LanguageSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.Counter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.MathUtil;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.StringUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.AidaUnsupportedLanguageException;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.lsh.LSH;
import de.mpg.mpi_inf.ambiversenlu.nlu.lsh.LSHStringNgramFeatureExtractor;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * This package-level class retrieves list of candidate entities for given mentions.
 * The actual retrieval depends on the type of instance created during runtime.
 * Based on the runtime type, entities will be retrieved by database lookup or
 * dictionary lookup.
 */
abstract class EntityLookup {

  private Logger logger_ = LoggerFactory.getLogger(EntityLookup.class);

  private static Map<Language, LSH<String>> lshEntityLookup_ = new HashMap<>();

  private LanguageSettings languageSettings;

  protected static final Set<String> malePronouns = new HashSet<String>() {

    private static final long serialVersionUID = 2L;

    {
      add("He");
      add("he");
      add("Him");
      add("him");
      add("His");
      add("his");
    }
  };

  protected static final Set<String> femalePronouns = new HashSet<String>() {

    private static final long serialVersionUID = 3L;

    {
      add("she");
      add("she");
      add("Her");
      add("her");
      add("Hers");
      add("hers");
    }
  };

  public EntityLookup(LanguageSettings languageSettings) throws EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    // If LSH matching should be done, initialize the LSH datastructures.
    this.languageSettings = languageSettings;
    if (languageSettings.getFuzzyMatching() && !lshEntityLookup_.containsKey(languageSettings.getLanguage())) {
      lshEntityLookup_.put(languageSettings.getLanguage(), createMentionLsh(languageSettings.getLanguage()));
    }
  }

  private LSH<String> createMentionLsh(Language language) throws EntityLinkingDataAccessException {
    logger_.info("Reading all entity names to create LSH representation.");
    Set<String> names = DataAccess.getMentionsforLanguage(language, true);
    logger_.info("Loaded " + names.size() + " names for " + language.name());
    double threshold = languageSettings.getFuzzyLshMinsim();
    logger_.info("Creating LSH representation with threshold " + threshold + ".");
    LSH<String> lsh = null;
    try {
      lsh = LSH.createLSH(names, new LSHStringNgramFeatureExtractor(), 4, 6, 1);
    } catch (InterruptedException e) {
      logger_.warn("Could not finish the creation of the mention LSH.");
      Thread.currentThread().interrupt();
    }
    return lsh;
  }

  public abstract Entities getEntitiesForMention(Mention mention, double maxEntityRank, int topByPrior, boolean mentionIsPrefix, boolean isNamedEntity)
      throws IOException, EntityLinkingDataAccessException;

  public void fillInCandidateEntities(Mentions conceptMentions, Mentions namedEntityMentions,
      CandidateDictionary externalDictionary, Set<KBIdentifiedEntity> blacklistedEntities,
      boolean includeNullEntityCandidates, boolean includeContextMentions,
      double maxEntityRank, int topByPrior, boolean mentionIsPrefix) throws SQLException, IOException, EntityLinkingDataAccessException, AidaUnsupportedLanguageException {

    fillInCandidateEntities(conceptMentions, externalDictionary, blacklistedEntities, includeNullEntityCandidates,
        includeContextMentions, maxEntityRank, topByPrior, mentionIsPrefix, false);

    fillInCandidateEntities(namedEntityMentions, externalDictionary, blacklistedEntities, includeNullEntityCandidates,
        includeContextMentions, maxEntityRank, topByPrior, mentionIsPrefix, true);
  }

  public void fillInCandidateEntities(Mentions mentions,
      CandidateDictionary externalDictionary, Set<KBIdentifiedEntity> blacklistedEntities,
      boolean includeNullEntityCandidates, boolean includeContextMentions,
      double maxEntityRank, int topByPrior, boolean mentionIsPrefix, boolean isNamedEntity)
      throws SQLException, IOException, EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    String logMsg = "Retrieving candidates with max global rank of " + maxEntityRank + ".";
    if (topByPrior > 0) {
      logMsg += " Restricting to top " + topByPrior + " candidates per mention.";
    }
    logger_.debug(logMsg);
    //flag to be used when having entities from different knowledge bases
    //and some of them are linked by a sameAs relation
    //currently applicable only for the configuration GND_PLUS_YAGO
    Integer id = RunningTimer.recordStartTime("EntityLookup:fillInCandidates");
    boolean removeDuplicateEntities = false;
    if (DataAccess.getConfigurationName().equals("GND_PLUS_YAGO")) {
      removeDuplicateEntities = true;
    }

    int debug_MAX_num_entities = 0;
    Set<Type> filteringTypes = mentions.getEntitiesTypes();
    //TODO This method shouldn't be doing one DB call per mention!
    for (int offset : mentions.getMentions().keySet()) {
      for (int length : mentions.getMentions().get(offset).keySet()) {
        Mention m = mentions.getMentions().get(offset).get(length);
        Entities mentionCandidateEntities;
        if (malePronouns.contains(m.getMention()) || femalePronouns.contains(m.getMention())) {
          // TODO If we want pronouns, we need to enable this again.
//           setCandiatesFromPreviousMentions(mentions, m.getCharOffset(), m.getCharLength());
          // TODO For now, just set empty candidates.
          m.setCandidateEntities(new Entities());
        } else {
          mentionCandidateEntities = getEntitiesForMention(m, maxEntityRank, topByPrior, mentionIsPrefix, isNamedEntity);
          Entities originalCandidateEntities = m.getCandidateEntities();
          if (originalCandidateEntities != null) {
            mentionCandidateEntities.addAll(originalCandidateEntities);
          }

          // Check for fallback options when no candidate was found using direct lookup.
          if (mentionCandidateEntities.size() == 0) {
            Counter.incrementCount("MENTION_WITHOUT_CANDIDATE");
            mentionCandidateEntities = getEntitiesByFuzzy(m, maxEntityRank, topByPrior, isNamedEntity);
          }

          if (externalDictionary != null) {
            mentionCandidateEntities.addAll(externalDictionary.getEntities(m, isNamedEntity));
          }

          int candidateCount = mentionCandidateEntities.size();
          mentionCandidateEntities = filterEntitiesByType(mentionCandidateEntities, filteringTypes);
          int filteredCandidateCount = mentionCandidateEntities.size();
          if (filteredCandidateCount < candidateCount) {
            Counter.incrementCountByValue("MENTION_CANDIDATE_FILTERED_BY_TYPE", candidateCount - filteredCandidateCount);
          }
          if (blacklistedEntities != null) {
            Entities blacklist = EntityLinkingManager.getEntities(blacklistedEntities);
            for (Entity eb : blacklist) {
              mentionCandidateEntities.remove(eb);
            }
          }

          if (includeNullEntityCandidates) {
            Entity nmeEntity = new OokbEntity(m.getMention());

            // add surrounding mentions as context
            if (includeContextMentions) {
              List<Integer> offsets = Arrays.asList(mentions.getMentions().keySet().toArray(new Integer[0]));
              int i = offsets.indexOf(m.getCharOffset());
              List<String> surroundingMentionsNames = new LinkedList<String>();
              int begin = Math.max(i - 2, 0);
              int end = Math.min(i + 3, mentions.getMentions().size());

              for (int s = begin; s < end; s++) {
                if (s == i) continue; // skip mention itself
                for (Mention contextMentions : mentions.getMentions().get(offsets.get(s)).values()) {
                  surroundingMentionsNames.add(contextMentions.getMention());
                }
              }
              nmeEntity.setSurroundingMentionNames(surroundingMentionsNames);
            }

            mentionCandidateEntities.add(nmeEntity);
          }
          if (removeDuplicateEntities) {
            removeDuplicateEntities(mentionCandidateEntities);
          }
          m.setCandidateEntities(mentionCandidateEntities);
        }
      }
    }
    RunningTimer.recordEndTime("EntityLookup:fillInCandidates", id);
  }


  private Entities getEntitiesByFuzzy(Mention m, double maxEntityRank, int topByPrior, boolean isNamedEntity)
      throws EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    Entities mentionCandidateEntities = new Entities();
    boolean doDictionaryFuzzyMatching = EntityLinkingConfig.getBoolean(EntityLinkingConfig.DICTIONARY_FUZZY_POSTGRES_ENABLE);
    if (doDictionaryFuzzyMatching) {
      double minSim = EntityLinkingConfig.getDouble(EntityLinkingConfig.DICTIONARY_FUZZY_POSTGRES_MINSIM);
      mentionCandidateEntities = DataAccess.getEntitiesForMentionByFuzzyMatcyhing(m.getMention(), minSim, isNamedEntity);
      if (mentionCandidateEntities.size() > 0) {
        Counter.incrementCount("MENTION_CANDIDATE_BY_PG_FUZZY");
      }
    } else if (languageSettings.getFuzzyMatching()) {
      mentionCandidateEntities = getEntitiesByLsh(m.getMention(), maxEntityRank, topByPrior, isNamedEntity);
      if (mentionCandidateEntities.size() > 0) {
        Counter.incrementCount("MENTION_CANDIDATE_BY_LSH_FUZZY");
      }
    }
    return mentionCandidateEntities;
  }

  private Entities getEntitiesByLsh(
      String mention, double maxEntityRank, int topByPrior, boolean isNamedEntity) throws EntityLinkingDataAccessException, AidaUnsupportedLanguageException {
    Entities candidates = new Entities();
    String conflatedMention = EntityLinkingManager.conflateToken(mention, isNamedEntity);
    Set<String> names = lshEntityLookup_.get(languageSettings.getLanguage()).getSimilarItemsForFeature(conflatedMention);

    // Check for a sufficiently high Jaccard overlap to avoid false positives.
    Set<String> similarNames = new HashSet<>();
    Set<String> mentionTrigrams = StringUtils.getNgrams(conflatedMention, 3);
    for (String candidate : names) {
      Set<String> candTrigrams = StringUtils.getNgrams(candidate, 3);
      double jSim = MathUtil.computeJaccardSimilarity(mentionTrigrams, candTrigrams);
      if (jSim > languageSettings.getFuzzyLshMinsim()) {
        similarNames.add(candidate);
      }
    }

    Collection<Entities> mentionCandidates =
        DataAccess.getEntitiesForMentions(similarNames, maxEntityRank, topByPrior, isNamedEntity).values();

    for (Entities cands : mentionCandidates) {
      candidates.addAll(cands);
    }
    return candidates;
  }

  /**
   * Filters the entity candidates against the given list of types
   *
   * @param entities Entities to filter'
   * @param filteringTypes Set of types to filter the entities against
   * @return filtered entities
   */
  private Entities filterEntitiesByType(Entities entities, Set<Type> filteringTypes) throws EntityLinkingDataAccessException {
    if (filteringTypes == null) {
      return entities;
    }
    Entities filteredEntities = new Entities();
    TIntObjectHashMap<Set<Type>> entitiesTypes = DataAccess.getTypes(entities);
    for (TIntObjectIterator<Set<Type>> itr = entitiesTypes.iterator(); itr.hasNext(); ) {
      itr.advance();
      int id = itr.key();
      Set<Type> entityTypes = itr.value();
      for (Type t : entityTypes) {
        if (filteringTypes.contains(t)) {
          filteredEntities.add(entities.getEntityById(id));
          break;
        }
      }
    }
    return filteredEntities;
  }

  private Entities removeDuplicateEntities(Entities mentionCandidateEntities) throws EntityLinkingDataAccessException {
    //for now only remove YAGO entities that are also GND entities
    // GNDYagoIdsMapper gndYagoIdsMapper = GNDYagoIdsMapper.getInstance(true);

    Entities filteredEntities = new Entities();

    //for (Entity entity : mentionCandidateEntities) {
    //try to get a GND id assuming this is a yago Entity
    // int gndId = gndYagoIdsMapper.mapFromYagoId(entity.getId());
    //if there exist a gnd counter part, and it's one of the candidates,
    //ignore this entity
    //  if(gndId > 0 && mentionCandidateEntities.contains(gndId)) {
    //    continue;
    //  }
    //  filteredEntities.add(entity);
    // }
    return filteredEntities;
  }
}