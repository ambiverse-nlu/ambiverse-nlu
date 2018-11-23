package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.*;


public interface DataAccessInterface {

  public DataAccess.type getAccessType();

  public Map<String, Entities> getEntitiesForMentions(Collection<String> mention, double maxEntityRank, int topByPrior, boolean isNamedentity)
      throws EntityLinkingDataAccessException;

  public Keyphrases getEntityKeyphrases(Entities entities, Map<String, Double> keyphraseSourceWeights, double minKeyphraseWeight,
      int maxEntityKeyphraseCount) throws EntityLinkingDataAccessException;

  public void getEntityKeyphraseTokens(Entities entities, TIntObjectHashMap<int[]> entityKeyphrases, TIntObjectHashMap<int[]> keyphraseTokens)
      throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<int[]> getInlinkNeighbors(Entities entities) throws EntityLinkingDataAccessException;

  public TObjectIntHashMap<KBIdentifiedEntity> getInternalIdsForKBEntities(Collection<KBIdentifiedEntity> entities)
      throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<KBIdentifiedEntity> getKnowlegebaseEntitiesForInternalIds(int[] ids) throws EntityLinkingDataAccessException;

  public TObjectIntHashMap<String> getIdsForTypeNames(Collection<String> typeNames) throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<Type> getTypesForIds(int[] ids) throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<int[]> getTypesIdsForEntitiesIds(int[] ids) throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<int[]> getEntitiesIdsForTypesIds(int[] ids) throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<String> getWordsForIds(int[] wordIds) throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<String> getWordsForIdsLowerCase(int[] wordIds) throws EntityLinkingDataAccessException;

  public TObjectIntHashMap<String> getIdsForWords(Collection<String> words) throws EntityLinkingDataAccessException;

  public Map<String, List<String>> getAllEntitiesMetaData(String startingWith) throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<EntityMetaData> getEntitiesMetaData(int[] entitiesIds) throws EntityLinkingDataAccessException;

  public TIntDoubleHashMap getEntitiesImportances(int[] entitiesIds) throws EntityLinkingDataAccessException;

  public TIntIntHashMap getKeyphraseDocumentFrequencies(TIntHashSet keyphrases) throws EntityLinkingDataAccessException;

  public TIntDoubleHashMap getEntityPriors(String mention, boolean isNamedentity) throws EntityLinkingDataAccessException;

  public double getGlobalEntityPrior(Entity entity) throws EntityLinkingDataAccessException;

  public TIntIntHashMap getEntitySuperdocSize(Entities entities) throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<TIntIntHashMap> getEntityKeywordIntersectionCount(Entities entities) throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<TIntIntHashMap> getEntityUnitIntersectionCount(Entities entities, UnitType unitType)
      throws EntityLinkingDataAccessException;

  public TObjectIntHashMap<KBIdentifiedEntity> getAllEntityIds() throws EntityLinkingDataAccessException;

  public TObjectIntHashMap<Type> getAllTypeIds() throws EntityLinkingDataAccessException;

  public Entities getAllEntities() throws EntityLinkingDataAccessException;

  public Entities getAllEntities(boolean isNE) throws EntityLinkingDataAccessException;

  //respect positions
  public int[] getAllWordExpansions() throws EntityLinkingDataAccessException;

  //respect positions
  public int[] getAllWordContractions() throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<int[]> getAllInlinks() throws EntityLinkingDataAccessException;

  public TObjectIntHashMap<String> getAllWordIds() throws EntityLinkingDataAccessException;

  public int getCollectionSize() throws EntityLinkingDataAccessException;

  public String getDumpVersion() throws EntityLinkingDataAccessException;

  public Date getDumpCreationDate() throws EntityLinkingDataAccessException;

  public int getTypeCollectionSize() throws EntityLinkingDataAccessException;

  public int getWordExpansion(int wordId) throws EntityLinkingDataAccessException;

  public String getConfigurationName() throws EntityLinkingDataAccessException;

  //count of keyword id
  public int[] getAllKeywordDocumentFrequencies() throws EntityLinkingDataAccessException;

  //count for doc freq
  public int[] getAllUnitDocumentFrequencies(UnitType unitType) throws EntityLinkingDataAccessException;

  public Pair<Integer, Integer> getImportanceComponentMinMax(String importanceId) throws EntityLinkingDataAccessException;

  public Map<String, Double> getKeyphraseSourceWeights() throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<int[]> getAllEntityTypes() throws EntityLinkingDataAccessException;

  public TIntDoubleHashMap getAllEntityRanks() throws EntityLinkingDataAccessException;

  public Entities getEntitiesForMentionByFuzzyMatching(String mention, double minSimilarity, boolean isNamedEntity) throws EntityLinkingDataAccessException;

  public TObjectIntHashMap<String> getAllKeyphraseSources() throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<int[]> getAllKeyphraseTokens() throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<int[]> getTaxonomy() throws EntityLinkingDataAccessException;

  public Map<String, int[]> getDictionary() throws EntityLinkingDataAccessException;

  public int getMaximumEntityId() throws EntityLinkingDataAccessException;

  public int getMaximumWordId() throws EntityLinkingDataAccessException;

  TIntObjectHashMap<List<MentionObject>> getMentionsForEntities(Entities entities)
      throws EntityLinkingDataAccessException;

  public String getLanguages() throws EntityLinkingDataAccessException;

  public Set<String> getMentionsforLanguage(Language lang, Boolean isNamedEntity, int limit) throws EntityLinkingDataAccessException;

  public Map<String, int[]> getEntityMentionsforLanguageAndEntities(int[] ids, Language language)
      throws EntityLinkingDataAccessException;

  public Map<String, Integer> getInternalIdsfromWikidataIds(List<String> ids) throws EntityLinkingDataAccessException;

  public Entities getEntitiesForInternalIds(int[] ids) throws EntityLinkingDataAccessException;

  public List<String> getMentionsInLanguageForEnglishMentions(Collection<String> mentions, Language language, boolean isNamedEntity) throws EntityLinkingDataAccessException;

  public Map<String, int[]> getCategoryIdsForMentions(Set<String> mentions, Language language, boolean isNamedEntity) throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<Type> getAllWikiCategoryTypes() throws EntityLinkingDataAccessException;

  public Map<String, String> getWikidataIdsForEntities(Set<String> entities);

  TObjectIntHashMap<KBIdentifiedEntity> getAllEntityIds(boolean isNamedEntity) throws EntityLinkingDataAccessException;

  List<Integer> getWordsForExpansion(int expansionId) throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<TIntArrayList> getWordsForExpansions(int[] expansionIds) throws EntityLinkingDataAccessException;

  public List<String> getAllWikidataIds() throws EntityLinkingDataAccessException;

  Set<String> getEntityURL(String namedEntity) throws EntityLinkingDataAccessException;

  LinkedHashMap<Integer, String> getTopEntitiesByRank(int start, int entityNumber, String language) throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<EntityType> getEntityClasses(Entities entities) throws EntityLinkingDataAccessException;

  public TIntObjectHashMap<EntityType> getAllEntityClasses() throws EntityLinkingDataAccessException;
}
