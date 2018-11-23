package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DataAccess {

  private static final Logger logger = LoggerFactory.getLogger(DataAccess.class);

  protected static DataAccessInterface dataAccess = null;

  /* Keyphrase sources */
  public static final String KPSOURCE_LINKANCHOR = "linkAnchor";

  public static final String KPSOURCE_INLINKTITLE = "inlinkTitle";

  public static final String KPSOURCE_CATEGORY = "wikipediaCategory";

  public static final String KPSOURCE_CITATION = "citationTitle";

  /** which type of data access*/
  public static enum type {
    sql, testing, cassandra
  }

  private static synchronized void initDataAccess() {
    if (dataAccess != null) {
      return;
    }
    if (DataAccess.type.sql.toString().equalsIgnoreCase(EntityLinkingConfig.get(EntityLinkingConfig.DATAACCESS))) {
      dataAccess = new DataAccessSQL();
    } else if (DataAccess.type.testing.toString().equalsIgnoreCase(EntityLinkingConfig.get(EntityLinkingConfig.DATAACCESS))) {
      dataAccess = new DataAccessForTesting();
    } else if (type.cassandra.toString().equalsIgnoreCase(EntityLinkingConfig.get(EntityLinkingConfig.DATAACCESS))) {
      dataAccess = new DataAccessKeyValueStore(type.cassandra);
    } else {
      // Default is sql.
      logger.info("No dataAccess given in 'settings/aida.properties', " + "using 'sql' as default.");
      dataAccess = new DataAccessSQL();
    }

    // Read dataaccess_aggregate.properties to create DataAccessConfig/DataAccesses
    //    dataAccess = new DataAccessKeyValueStoreAggregation(new DataAccessConfig());
  }

  private static DataAccessInterface getInstance() {
    if (dataAccess == null) {
      initDataAccess();
    }
    return dataAccess;
  }

  public static void init() {
    DataAccess.getInstance();
  }

  public static DataAccess.type getAccessType() {
    return DataAccess.getInstance().getAccessType();
  }

  public static List<String> getMentionsInLanguageForEnglishMentions(Collection<String> mentions, Language language, boolean isNamedEntity) throws EntityLinkingDataAccessException{
    return DataAccess.getInstance().getMentionsInLanguageForEnglishMentions(mentions, language, isNamedEntity);
  }

  /**
   * Returns candidate entities for the given mention.
   * The candidate space can be restricted globally by the maxEntityRank, and on a per mention
   * basis only the topK according to the prior can be returned.
   *
   * @param mention Mention to get candidates for
   * @param maxEntityRank Maximum rank of the candidate entity (according to global rank in [0.0,1.0] where 0.0 is the best rank.
   * @param topByPrior  How many candidates to return, according to the ranking by prior. Set to 0 to return all.
   * @return Candidate entities for mention.
   */
  public static Entities getEntitiesForMention(String mention, double maxEntityRank, int topByPrior, boolean isNamedEntity) throws EntityLinkingDataAccessException {
    List<String> mentionAsList = new ArrayList<String>(1);
    mentionAsList.add(mention);
    Map<String, Entities> candidates = getEntitiesForMentions(mentionAsList, maxEntityRank, topByPrior, isNamedEntity);
    return candidates.get(mention);
  }

  public static Map<String, Entities> getEntitiesForMentions(Collection<String> mention, double maxEntityRank, int topByPrior, boolean isNamedEntity)
      throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getEntitiesForMentions(mention, maxEntityRank, topByPrior, isNamedEntity);
  }


  public static Entities getEntitiesForMentionByFuzzyMatcyhing(String mention, double minSimilarity, boolean isNamedEntity) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getEntitiesForMentionByFuzzyMatching(mention, minSimilarity, isNamedEntity);
  }

  public static TIntObjectHashMap<List<MentionObject>> getMentionsForEntities(Entities entities)
      throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getMentionsForEntities(entities);
  }

  /**
   * @return The complete mention-entity dictionary.
   */
  public static Map<String, int[]> getDictionary() throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getDictionary();
  }

  /**
   * Retrieves all the Keyphrases for the given entities. Does not return
   * keyphrases when the source has a weight of 0.0.
   *
   * If keyphraseSourceWeights is not null, the return object will also
   * contain all keyphrase sources. This will increase the data transfer,
   * so use wisely.
   *
   * If minKeyphraseWeight &gt; 0.0, keyphrases with a weight lower than
   * minKeyphraseWeight will not be returned.
   *
   * If maxEntityKeyphraseCount &gt; 0, at max maxEntityKeyphraseCount will
   * be returned for each entity.
   *
   * @param entities
   * @param keyphraseSourceWeights
   * @param minKeyphraseWeight
   * @param maxEntityKeyphraseCount
   * @return
   */
  public static Keyphrases getEntityKeyphrases(Entities entities, Map<String, Double> keyphraseSourceWeights, double minKeyphraseWeight,
      int maxEntityKeyphraseCount) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().
        getEntityKeyphrases(entities, keyphraseSourceWeights, minKeyphraseWeight, maxEntityKeyphraseCount);
  }

  public static void getEntityKeyphraseTokens(Entities entities, TIntObjectHashMap<int[]> entityKeyphrases, TIntObjectHashMap<int[]> keyphraseTokens)
      throws EntityLinkingDataAccessException {
    DataAccess.getInstance().getEntityKeyphraseTokens(entities, entityKeyphrases, keyphraseTokens);
  }

  public static int[] getInlinkNeighbors(Entity e) throws EntityLinkingDataAccessException {
    Entities entities = new Entities();
    entities.add(e);
    TIntObjectHashMap<int[]> neighbors = getInlinkNeighbors(entities);
    return neighbors.get(e.getId());
  }

  public static TIntObjectHashMap<int[]> getInlinkNeighbors(Entities entities) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getInlinkNeighbors(entities);
  }

  public static int getInternalIdForKBEntity(KBIdentifiedEntity entity) throws EntityLinkingDataAccessException {
    List<KBIdentifiedEntity> entities = new ArrayList<KBIdentifiedEntity>(1);
    entities.add(entity);
    return getInternalIdsForKBEntities(entities).get(entity);
  }

  public static TObjectIntHashMap<KBIdentifiedEntity> getInternalIdsForKBEntities(Collection<KBIdentifiedEntity> entities)
      throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getInternalIdsForKBEntities(entities);
  }

  public static KBIdentifiedEntity getKnowlegebaseEntityForInternalId(int entity) throws EntityLinkingDataAccessException {
    int[] entities = new int[1];
    entities[0] = entity;
    return getKnowlegebaseEntitiesForInternalIds(entities).get(entity);
  }

  public static TIntObjectHashMap<KBIdentifiedEntity> getKnowlegebaseEntitiesForInternalIds(int[] ids) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getKnowlegebaseEntitiesForInternalIds(ids);
  }

  public static Entities getAidaEntitiesForInternalIds(int[] internalIds) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<KBIdentifiedEntity> kbEntities = DataAccess.getKnowlegebaseEntitiesForInternalIds(internalIds);
    Entities entities = new Entities();
    for (TIntObjectIterator<KBIdentifiedEntity> itr = kbEntities.iterator(); itr.hasNext(); ) {
      itr.advance();
      entities.add(new Entity(itr.value(), itr.key()));
    }
    return entities;
  }

  public static Entities getAidaEntitiesForKBEntities(Set<KBIdentifiedEntity> entities) throws EntityLinkingDataAccessException {
    TObjectIntHashMap<KBIdentifiedEntity> kbEntities = DataAccess.getInternalIdsForKBEntities(entities);
    Entities aidaEntities = new Entities();
    for (TObjectIntIterator<KBIdentifiedEntity> itr = kbEntities.iterator(); itr.hasNext(); ) {
      itr.advance();
      aidaEntities.add(new Entity(itr.key(), itr.value()));
    }
    return aidaEntities;
  }

  public static int getIdForTypeName(String typeName) throws EntityLinkingDataAccessException {
    List<String> typeNames = new ArrayList<String>(1);
    typeNames.add(typeName);
    return getIdsForTypeNames(typeNames).get(typeName);
  }

  public static TObjectIntHashMap<String> getIdsForTypeNames(Collection<String> typeNames) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getIdsForTypeNames(typeNames);
  }

  public static Type getTypeForId(int typeId) throws EntityLinkingDataAccessException {
    int[] types = new int[1];
    types[0] = typeId;
    return getTypesForIds(types).get(typeId);
  }

  public static TIntObjectHashMap<Type> getTypesForIds(int[] ids) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getTypesForIds(ids);
  }

  public static int[] getTypeIdsForEntityId(int entityId) throws EntityLinkingDataAccessException {
    int[] entities = new int[1];
    entities[0] = entityId;
    return getTypesIdsForEntitiesIds(entities).get(entityId);
  }

  public static TIntObjectHashMap<int[]> getTypesIdsForEntitiesIds(int[] ids) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getTypesIdsForEntitiesIds(ids);
  }

  public static int[] getEntitiesIdsForTypeId(int typeId) throws EntityLinkingDataAccessException {
    int[] types = new int[1];
    types[0] = typeId;
    return getEntitiesIdsForTypesIds(types).get(typeId);
  }

  public static TIntObjectHashMap<int[]> getEntitiesIdsForTypesIds(int[] ids) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getEntitiesIdsForTypesIds(ids);
  }

  public static String getWordForId(int wordId) throws EntityLinkingDataAccessException {
    int[] words = new int[1];
    words[0] = wordId;
    return getWordsForIds(words).get(wordId);
  }

  public static int getIdForWord(String word) throws EntityLinkingDataAccessException {
    List<String> words = new ArrayList<String>(1);
    words.add(word);
    return getIdsForWords(words).get(word);
  }

  public static TIntObjectHashMap<String> getWordsForIds(int[] wordIds) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getWordsForIds(wordIds);
  }

  public static TIntObjectHashMap<String> getWordsForIdsLowerCase(int[] wordIds) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getWordsForIdsLowerCase(wordIds);
  }

  /**
   * Transform words into the token ids. Unknown words will be assigned
   * the id -1
   *
   * @param   words
   * @return Ids for words ranging from 1 to MAX, -1 for missing.
   */
  public static TObjectIntHashMap<String> getIdsForWords(Collection<String> words) throws EntityLinkingDataAccessException {
    logger.debug("Getting ids for words.");
    return DataAccess.getInstance().getIdsForWords(words);
  }

  public static TIntObjectHashMap<Set<Type>> getTypes(Entities entities) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<Set<Type>> entityTypes = new TIntObjectHashMap<Set<Type>>();
    TIntObjectHashMap<int[]> entitiesTypesIds = getTypesIdsForEntitiesIds(entities.getUniqueIdsAsArray());
    TIntSet allTypesIds = new TIntHashSet();
    for (TIntObjectIterator<int[]> itr = entitiesTypesIds.iterator(); itr.hasNext(); ) {
      itr.advance();
      int[] types = itr.value();
      allTypesIds.addAll(types);
    }

    TIntObjectHashMap<Type> typeNamesMap = getTypesForIds(allTypesIds.toArray());

    if(typeNamesMap != null) {
      for (Entity entity : entities) {
        int[] typesIds = entitiesTypesIds.get(entity.getId());
        Set<Type> types = new HashSet<Type>(typesIds.length, 1.0f);
        for (int typeId : typesIds) {
          if (typeId == 0) continue; // workaround for db issue (allEntities): type id 0 does not exists, but is in entity_types
          types.add(typeNamesMap.get(typeId));
        }
        entityTypes.put(entity.getId(), types);
      }
    }
    return entityTypes;
  }

  public static Set<Type> getTypes(Entity entity) throws EntityLinkingDataAccessException {
    Entities entities = new Entities();
    entities.add(entity);
    TIntObjectHashMap<Set<Type>> entityTypes = getTypes(entities);
    return entityTypes.get(entity.getId());
  }

  @Deprecated public static Map<String, List<String>> getAllEntitiesMetaData(String startingWith) throws EntityLinkingDataAccessException {
    return getInstance().getAllEntitiesMetaData(startingWith);
  }

  public static TIntObjectHashMap<EntityMetaData> getEntitiesMetaData(int[] entitiesIds) throws EntityLinkingDataAccessException {
    return getInstance().getEntitiesMetaData(entitiesIds);
  }

  public static EntityMetaData getEntityMetaData(int entityId) throws EntityLinkingDataAccessException {
    int[] entitiesIds = new int[1];
    entitiesIds[0] = entityId;
    TIntObjectHashMap<EntityMetaData> results = getEntitiesMetaData(entitiesIds);
    return results.get(entityId);
  }

  public static Map<String, Integer> getInternalIdsfromWikidataIds(List<String> ids) throws EntityLinkingDataAccessException {
    return getInstance().getInternalIdsfromWikidataIds(ids);
  }

  public static Integer getInternalIdfromWikidataId(String id) throws EntityLinkingDataAccessException {
    List<String> ids = new ArrayList<>();
    ids.add(id);
    Map<String, Integer> result = getInternalIdsfromWikidataIds(ids);
    return result.get(id);
  }

  public static TIntDoubleHashMap getEntitiesImportances(int[] entitiesIds) throws EntityLinkingDataAccessException {
    return getInstance().getEntitiesImportances(entitiesIds);
  }

  public static double getEntityImportance(int entityId) throws EntityLinkingDataAccessException {
    int[] entitiesIds = new int[1];
    entitiesIds[0] = entityId;
    TIntDoubleHashMap results = getEntitiesImportances(entitiesIds);
    return results.get(entityId);
  }

  public static TIntDoubleHashMap getEntityPriors(String mention, boolean isNamedEntity) throws EntityLinkingDataAccessException {
    return getInstance().getEntityPriors(mention, isNamedEntity);
  }

  public static TIntIntHashMap getKeywordDocumentFrequencies(TIntSet keywords) throws EntityLinkingDataAccessException {
    logger.debug("Get keyword-document frequencies.");
    Integer runId = RunningTimer.recordStartTime("DataAccess:KWDocFreq");
    TIntIntHashMap keywordCounts = new TIntIntHashMap((int) (keywords.size() / Constants.DEFAULT_LOAD_FACTOR));
    for (TIntIterator itr = keywords.iterator(); itr.hasNext(); ) {
      int keywordId = itr.next();
      int count = DataAccessCache.singleton().getKeywordCount(keywordId);
      keywordCounts.put(keywordId, count);
    }
    RunningTimer.recordEndTime("DataAccess:KWDocFreq", runId);
    return keywordCounts;
  }

  public static TIntIntHashMap getUnitDocumentFrequencies(TIntSet keywords, UnitType unitType) throws EntityLinkingDataAccessException {
    logger.debug("Get Unit-document frequencies.");
    Integer runId = RunningTimer.recordStartTime("DataAccess:KWDocFreq");
    TIntIntHashMap keywordCounts = new TIntIntHashMap((int) (keywords.size() / Constants.DEFAULT_LOAD_FACTOR));
    for (TIntIterator itr = keywords.iterator(); itr.hasNext(); ) {
      int keywordId = itr.next();
      int count = DataAccessCache.singleton().getUnitCount(keywordId, unitType);
      keywordCounts.put(keywordId, count);
    }
    RunningTimer.recordEndTime("DataAccess:KWDocFreq", runId);
    return keywordCounts;
  }

  public static int getUnitDocumentFrequency(int unit, UnitType unitType) {
    logger.debug("Get unit-document frequency for unit {} and unit type {}.", unit, unitType.getUnitName());
    int frequency = 0;
    try {
      frequency = DataAccessCache.singleton().getUnitCount(unit, unitType);
    } catch (Exception e) {
      logger.debug("Could not get frequency of " + unitType.getUnitName() + ": " + unit);
    }
    return frequency;
  }

  public static TIntIntHashMap getEntitySuperdocSize(Entities entities) throws EntityLinkingDataAccessException {
    return getInstance().getEntitySuperdocSize(entities);
  }

  public TIntObjectHashMap<TIntIntHashMap> getEntityKeywordIntersectionCount(Entities entities) throws EntityLinkingDataAccessException {
    throw new NotImplementedException("getEntityKeywordIntersectionCount method not implemented");
  }

  public static TIntObjectHashMap<TIntIntHashMap> getEntityUnitIntersectionCount(Entities entities, UnitType unitType)
      throws EntityLinkingDataAccessException {
    return getInstance().getEntityUnitIntersectionCount(entities, unitType);
  }

  public static TObjectIntHashMap<KBIdentifiedEntity> getAllEntityIds() throws EntityLinkingDataAccessException {
    return getInstance().getAllEntityIds();
  }

  public static TObjectIntHashMap<KBIdentifiedEntity> getAllEntityIds(boolean isNamedEntity) throws EntityLinkingDataAccessException {
    return getInstance().getAllEntityIds(isNamedEntity);
  }

  public static TObjectIntHashMap<Type> getAllTypeIds() throws EntityLinkingDataAccessException {
    return getInstance().getAllTypeIds();
  }

  public static TIntObjectHashMap<Type> getAllWikiCategoryTypes() throws EntityLinkingDataAccessException {
    return getInstance().getAllWikiCategoryTypes();
  }

  public static TIntObjectHashMap<int[]> getAllEntityTypes() throws EntityLinkingDataAccessException {
    return getInstance().getAllEntityTypes();
  }

  public static TIntObjectHashMap<int[]> getTaxonomy() throws EntityLinkingDataAccessException {
    return getInstance().getTaxonomy();
  }

  public static TIntDoubleHashMap getAllEntityRanks() throws EntityLinkingDataAccessException {
    return getInstance().getAllEntityRanks();
  }

  public Entities getEntitiesForMentionByFuzzyMatching(String mention, double minSimilarity) throws EntityLinkingDataAccessException {
    throw new NotImplementedException("getEntitiesForMentionByFuzzyMatching not implemented");
  }

  public static TObjectIntHashMap<String> getAllKeyphraseSources() throws EntityLinkingDataAccessException {
    return getInstance().getAllKeyphraseSources();
  }

  public static TIntObjectHashMap<int[]> getAllKeyphraseTokens() throws EntityLinkingDataAccessException {
    return getInstance().getAllKeyphraseTokens();
  }

  public static Entities getAllEntities() throws EntityLinkingDataAccessException {
    return getInstance().getAllEntities();
  }

  public static Entities getAllEntities(boolean isNE) throws EntityLinkingDataAccessException {
    return getInstance().getAllEntities(isNE);
  }

  // Will return an array where the index is the id of the word to be expanded
  // and the value is the id of the expanded word. Expansion is done by
  // fully uppercasing the original word behind the id.
  public static int[] getAllWordExpansions() throws EntityLinkingDataAccessException {
    return getInstance().getAllWordExpansions();
  }

  public static int[] getAllWordContractions() throws EntityLinkingDataAccessException {
    return getInstance().getAllWordContractions();
  }

  public static TIntObjectHashMap<int[]> getAllInlinks() throws EntityLinkingDataAccessException {
    return getInstance().getAllInlinks();
  }

  /**
   * Computes all entity occurrence probabilities based on their incoming links.
   *
   * @return Map of Entity->Probability.
   * @throws EntityLinkingDataAccessException
   */
  public static TIntDoubleHashMap getAllEntityProbabilities() throws EntityLinkingDataAccessException {
    TIntObjectHashMap<int[]> entityInlinks = getAllInlinks();

    TIntDoubleHashMap entityProbabilities = new TIntDoubleHashMap(entityInlinks.size(), 0.5f);

    // Get the total number of links.
    long totalLinkCount = 0;

    TIntObjectIterator<int[]> itr = entityInlinks.iterator();

    while (itr.hasNext()) {
      itr.advance();
      totalLinkCount += itr.value().length;
    }

    // Derive probabilities from counts.
    itr = entityInlinks.iterator();

    while (itr.hasNext()) {
      itr.advance();
      double probability = (double) itr.value().length / (double) totalLinkCount;
      entityProbabilities.put(itr.key(), probability);
    }

    return entityProbabilities;
  }

  public static TObjectIntHashMap<String> getAllWordIds() throws EntityLinkingDataAccessException {
    return getInstance().getAllWordIds();
  }

  public static List<Integer> getWordsForExpansion(int expansionId) throws EntityLinkingDataAccessException {
    return getInstance().getWordsForExpansion(expansionId);
  }

  public static TIntObjectHashMap<TIntArrayList> getWordsForExpansions(int[] expansionIds) throws EntityLinkingDataAccessException {
    return getInstance().getWordsForExpansions(expansionIds);
  }

  /**
   * Used for the weight computation. This returns the total number of 
   * documents in the collection for the computation of the keyword IDF weights.
   * In the original AIDA setting with YAGO-entities this is the number of 
   * Wikipedia entities.
   * @return Collection Size.
   */
  public static int getCollectionSize() throws EntityLinkingDataAccessException {
    return getInstance().getCollectionSize();
  }

  public static String getDumpVersion() throws EntityLinkingDataAccessException {
    return getInstance().getDumpVersion();
  }

  public static Date getDumpCreationDate() throws EntityLinkingDataAccessException {
    return getInstance().getDumpCreationDate();
  }

  public static int getTypeCollectionSize() throws EntityLinkingDataAccessException {
    return getInstance().getTypeCollectionSize();
  }

  public static int getMaximumEntityId() throws EntityLinkingDataAccessException {
    return getInstance().getMaximumEntityId();
  }

  public static int getMaximumWordId() throws EntityLinkingDataAccessException {
    return getInstance().getMaximumWordId();
  }

  public static int getWordExpansion(int wordId) throws EntityLinkingDataAccessException {
    return getInstance().getWordExpansion(wordId);
  }

  public static String getConfigurationName() throws EntityLinkingDataAccessException {
    return getInstance().getConfigurationName();
  }

  public static int expandTerm(int wordId) throws EntityLinkingDataAccessException {
    logger.debug("Expand term for word {}.", wordId);
    return DataAccessCache.singleton().expandTerm(wordId);
  }

  public static String expandTerm(String term) {
    return term.toUpperCase(Locale.ENGLISH);
  }

  public static int contractTerm(int wordId) throws EntityLinkingDataAccessException {
    logger.debug("ContractTerm for word "+wordId);
    return DataAccessCache.singleton().contractTerm(wordId);
  }

  public static int[] getAllKeywordDocumentFrequencies() throws EntityLinkingDataAccessException {
    return getInstance().getAllKeywordDocumentFrequencies();
  }

  public static int[] getAllUnitDocumentFrequencies(UnitType unitType) throws EntityLinkingDataAccessException {
    return getInstance().getAllUnitDocumentFrequencies(unitType);
  }

  public static Pair<Integer, Integer> getImportanceComponentMinMax(String importanceId) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getImportanceComponentMinMax(importanceId);
  }

  public static Map<String, Double> getKeyphraseSourceWeights() throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getKeyphraseSourceWeights();
  }

  public static Map<KBIdentifiedEntity, EntityMetaData> getEntitiesMetaData(Set<KBIdentifiedEntity> entities)
      throws EntityLinkingDataAccessException {
    TObjectIntHashMap<KBIdentifiedEntity> ids = getInternalIdsForKBEntities(entities);
    TIntObjectHashMap<EntityMetaData> metadata = getEntitiesMetaData(ids.values());
    Map<KBIdentifiedEntity, EntityMetaData> result = new HashMap<KBIdentifiedEntity, EntityMetaData>();
    for (TObjectIntIterator<KBIdentifiedEntity> itr = ids.iterator(); itr.hasNext(); ) {
      itr.advance();
      int id = itr.value();
      result.put(itr.key(), metadata.get(id));
    }
    return result;
  }

  public static Language[] getLanguages() throws EntityLinkingDataAccessException {
    String languageStr = getInstance().getLanguages();
    String[] languagesStr = languageStr.split(",");
    List<Language> languages = new ArrayList<>();
    for (String l : languagesStr) {
      if (!Language.isActiveLanguage(l)) {
        logger.warn("Language is not supported " + l);
      } else {
        languages.add(Language.getLanguageForString(l));
      }
    }
    return languages.toArray(new Language[languages.size()]);
  }

  public static Set<String> getMentionsforLanguage(Language language, boolean isNamedEntity) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getMentionsforLanguage(language, isNamedEntity, 0);
  }

  public static Set<String> getMentionsforLanguage(Language language, boolean isNamedEntity, Integer mentionTokenFrequencyCountsLimit) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getMentionsforLanguage(language, isNamedEntity, mentionTokenFrequencyCountsLimit);
  }

  public static Map<String, int[]> getEntityMentions(int[] ids, Language language) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getEntityMentionsforLanguageAndEntities(ids, language);
  }

  public static int[] getCategoryIdsForMention(String mention, Language language, boolean isNamedEntity) throws EntityLinkingDataAccessException {
    Set<String> mentions = new HashSet<String>();
    mentions.add(mention);
    return getCategoryIdsForMentions(mentions, language, isNamedEntity).get(mention);
  }

  public static Map<String, int[]> getCategoryIdsForMentions(Set<String> mentions, Language language, boolean isNamedEntity) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getCategoryIdsForMentions(mentions, language, isNamedEntity);
  }

  public static Map<String, String> getWikidataIdForEntities(Set<String> entities) {
    return DataAccess.getInstance().getWikidataIdsForEntities(entities);
  }

  public static List<String> getAllWikidataIds() throws EntityLinkingDataAccessException {
    return getInstance().getAllWikidataIds();
  }

  public static Set<String> getEntityURLs(String namedEntity) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getEntityURL(namedEntity);
  }

  public static LinkedHashMap<Integer, String> getTopEntitiesByRank(int start, int entityNumber, String language) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getTopEntitiesByRank(start, entityNumber, language);
  }

  public static Map<String, int[]> getEntityMentionsforLanguageAndEntities(int[] entityIDs, Language language) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getEntityMentionsforLanguageAndEntities(entityIDs, language);
  }

  public static TIntObjectHashMap<EntityType> getEntityClasses(Entity e) throws EntityLinkingDataAccessException {
    Entities es = new Entities();
    es.add(e);
    return DataAccess.getEntityClasses(es);
  }

  public static TIntObjectHashMap<EntityType> getEntityClasses(Entities entities) throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getEntityClasses(entities);
  }

  public static TIntObjectHashMap<EntityType> getAllEntityClasses() throws EntityLinkingDataAccessException {
    return DataAccess.getInstance().getAllEntityClasses();
  }

}
