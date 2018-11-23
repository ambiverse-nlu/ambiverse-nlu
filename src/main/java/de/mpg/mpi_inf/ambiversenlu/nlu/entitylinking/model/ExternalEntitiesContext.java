package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.Utils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.UimaTokenizer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import org.apache.uima.UIMAException;

import java.io.IOException;
import java.util.*;

public class ExternalEntitiesContext {

  private TIntObjectHashMap<int[]> entityKeyphrases_ = new TIntObjectHashMap<>();

  private TIntObjectHashMap<int[]> keyphraseTokens_ = new TIntObjectHashMap<>();

  private TIntIntHashMap entityCounts_ = new TIntIntHashMap();

  private TIntIntHashMap keyphraseCounts_ = new TIntIntHashMap();

  private TIntIntHashMap keywordCounts_ = new TIntIntHashMap();

  private int totalCount_;

  private TIntObjectHashMap<TIntIntHashMap> entity2keyword2count_ = new TIntObjectHashMap<>();

  private TIntObjectHashMap<TIntIntHashMap> entity2keyphrase2count_ = new TIntObjectHashMap<>();

  private TIntObjectHashMap<String> transientId2token_ = new TIntObjectHashMap<>();

  private TIntIntHashMap transientWordExpansions_ = new TIntIntHashMap();

  private TObjectIntHashMap<String> transientToken2Id_ = new TObjectIntHashMap<>();

  private TObjectIntHashMap<String> existingToken2Id_ = new TObjectIntHashMap<>();

  private CandidateDictionary dictionary_;

  private Set<KBIdentifiedEntity> blacklistedEntities_ = new HashSet<>();

  private Language language;

  public ExternalEntitiesContext() {
    // Placeholder when no external context is present.
  }

  /**
   * Use this if you don't need MI weights.
   *
   * @param mentionEntityDictionary
   * @param entityKeyphrases
   */
  public ExternalEntitiesContext(
      Map<String, List<KBIdentifiedEntity>> mentionEntityDictionary,
      Map<KBIdentifiedEntity, List<Keyphrase>> entityKeyphrases, Language language, boolean isNamedEntity)
      throws EntityLinkingDataAccessException, UIMAException, ClassNotFoundException, NoSuchMethodException, IOException,
      MissingSettingException, UnprocessableDocumentException {
    this(mentionEntityDictionary, entityKeyphrases, null, null, 0, null, language, isNamedEntity);
  }

  /**
   * Use this for MI weight computation.
   *
   * @param mentionEntityDictionary
   * @param entityKeyphrases
   * @param entityCounts
   * @param keyphraseCounts
   * @param totalCount
   * @param blacklistedEntities
   */
  public ExternalEntitiesContext(
      Map<String, List<KBIdentifiedEntity>> mentionEntityDictionary,
      Map<KBIdentifiedEntity, List<Keyphrase>> entityKeyphrases,
      Map<KBIdentifiedEntity, Integer> entityCounts,
      Set<Keyphrase> keyphraseCounts,
      Integer totalCount,
      Set<KBIdentifiedEntity> blacklistedEntities,
      Language language,
      boolean isNamedEntity)
      throws EntityLinkingDataAccessException, UIMAException, ClassNotFoundException, NoSuchMethodException,
      IOException, MissingSettingException, UnprocessableDocumentException {
    this.language = language;
    dictionary_ = new CandidateDictionary(isNamedEntity);
    if (mentionEntityDictionary != null && entityKeyphrases != null) {
      dictionary_ = new CandidateDictionary(mentionEntityDictionary, isNamedEntity);
      keyphraseTokens_ = buildKeyphraseTokens(entityKeyphrases);
      entityKeyphrases_ = buildEntityKeyphrases(entityKeyphrases);

      // Check if counts for MI weights are present.
      if (entityCounts != null && keyphraseCounts != null && totalCount != null) {
        countKeyphrasesKeywords(entityKeyphrases, entityCounts, keyphraseCounts);
        totalCount_ = totalCount;
      }
    }
    blacklistedEntities_ = blacklistedEntities;
  }

  public TIntObjectHashMap<int[]> getEntityKeyphrases() {
    return entityKeyphrases_;
  }

  public TIntObjectHashMap<int[]> getKeyphraseTokens() {
    return keyphraseTokens_;
  }

  public TIntIntHashMap getEntityCounts() {
    return entityCounts_;
  }

  public TIntIntHashMap getKeyphraseCounts() {
    return keyphraseCounts_;
  }

  public TIntIntHashMap getKeywordCounts() {
    return keywordCounts_;
  }

  public int getTotalCount() {
    return totalCount_;
  }

  public TIntObjectHashMap<TIntIntHashMap> getEntity2keyword2count() {
    return entity2keyword2count_;
  }

  public TIntObjectHashMap<TIntIntHashMap> getEntity2keyphrase2count() {
    return entity2keyphrase2count_;
  }

  /**
   * @return All ids of words that have been created transiently.
   */
  public TIntSet getTransientWordIds() {
    return transientId2token_.keySet();
  }

  public TObjectIntHashMap<String> getTransientTokenIds() {
    return transientToken2Id_;
  }

  public int getIdForWord(String word) {
    return getWordId(word);
  }

  public TIntIntMap getTransientWordExpansions() {
    return transientWordExpansions_;
  }

  private TIntObjectHashMap<int[]> buildKeyphraseTokens(Map<KBIdentifiedEntity, List<Keyphrase>> entityKeyphrases)
      throws EntityLinkingDataAccessException, UIMAException, ClassNotFoundException, IOException, NoSuchMethodException,
          MissingSettingException, UnprocessableDocumentException {
    // Collect all strings to map them to IDs.
    Set<String> allKeyphrases = new HashSet<>();
    Set<String> allKeywords = new HashSet<>();
    Map<String, List<String>> keyphraseTokenStrings = new HashMap<>();
    for (List<Keyphrase> keyphrases : entityKeyphrases.values()) {
      for (Keyphrase keyphrase : keyphrases) {
        if (keyphrase.getKeyphrase() == null || keyphrase.getKeyphrase().isEmpty()) {
          continue;
        }

        allKeyphrases.add(keyphrase.getKeyphrase());
        // Create term expansions for matching upper case tokens.
        allKeyphrases.add(DataAccess.expandTerm(keyphrase.getKeyphrase()));
        Tokens tokens = UimaTokenizer.tokenize(language, keyphrase.getKeyphrase());
        if(!Utils.shouldKeepTokens(tokens)) {
          continue;
        }
        List<String> keywords = new ArrayList<>(tokens.size());
        for (Token token : tokens.getTokens()) {
          allKeywords.add(token.getOriginal());
          allKeywords.add(DataAccess.expandTerm(token.getOriginal()));
          keywords.add(token.getOriginal());
        }

        keyphraseTokenStrings.put(keyphrase.getKeyphrase(), keywords);
      }
    }

    // Map to IDs, assign transient IDs to tokens that are not in the main database.
    Set<String> allWords = new HashSet<>(allKeyphrases);
    allWords.addAll(allKeywords);
    existingToken2Id_ = DataAccess.getIdsForWords(allWords);
    int nextWordId = DataAccess.getMaximumWordId() + 1;

    // Create ID-based keyphraseTokens
    TIntObjectHashMap<int[]> keyphraseTokens = new TIntObjectHashMap<>();
    for (Map.Entry<String, List<String>> entry : keyphraseTokenStrings.entrySet()) {
      String keyphrase = entry.getKey();
      List<String> keywords = entry.getValue();
      int kpId = existingToken2Id_.get(keyphrase);
      if (kpId == existingToken2Id_.getNoEntryValue()) {
        kpId = transientToken2Id_.get(keyphrase);
        if (kpId == transientToken2Id_.getNoEntryValue()) {
          kpId = nextWordId;
          nextWordId = createTransientWord(keyphrase, kpId);
        }
      }
      int[] kwIds = new int[keywords.size()];
      for (int i = 0; i < keywords.size(); i++) {
        String keyword = keywords.get(i);
        int kwId = existingToken2Id_.get(keyword);
        if (kwId == existingToken2Id_.getNoEntryValue()) {
          kwId = transientToken2Id_.get(keyword);
          if (kwId == transientToken2Id_.getNoEntryValue()) {
            kwId = nextWordId;
            nextWordId = createTransientWord(keyword, kwId);
          }
        }
        kwIds[i] = kwId;
      }
      keyphraseTokens.put(kpId, kwIds);
    }
    return keyphraseTokens;
  }

  private void countKeyphrasesKeywords(Map<KBIdentifiedEntity, List<Keyphrase>> entityKeyphrases, Map<KBIdentifiedEntity, Integer> entityCounts,
      Set<Keyphrase> keyphraseCounts) {
    for (Map.Entry<KBIdentifiedEntity, List<Keyphrase>> e : entityKeyphrases.entrySet()) {
      int entity = dictionary_.getEntityId(e.getKey());

      // Update co-occurrence counts.
      List<Keyphrase> kps = e.getValue();
      TIntIntHashMap kpCounts = entity2keyphrase2count_.get(entity);
      if (kpCounts == null) {
        kpCounts = new TIntIntHashMap();
        entity2keyphrase2count_.put(entity, kpCounts);
      }
      TIntIntHashMap kwCounts = entity2keyword2count_.get(entity);
      if (kwCounts == null) {
        kwCounts = new TIntIntHashMap();
        entity2keyword2count_.put(entity, kwCounts);
      }

      for (Keyphrase kp : kps) {
        int kpId = getWordId(kp.getKeyphrase());
        int kpCount = kp.getCount();
        kpCounts.adjustOrPutValue(kpId, kpCount, kpCount);
        // For each token increment the co-occurrence count by
        // the keyphrase count.
        for (int kwId : keyphraseTokens_.get(kpId)) {
          kwCounts.adjustOrPutValue(kwId, kpCount, kpCount);
        }
      }

      // Transform occurrence counts.
      entityCounts_.put(entity, entityCounts.get(e.getKey()));
      for (Keyphrase kp : keyphraseCounts) {
        int kpId = getWordId(kp.getKeyphrase());
        keyphraseCounts_.put(kpId, kp.getCount());
        for (int kwId : keyphraseTokens_.get(kpId)) {
          keywordCounts_.adjustOrPutValue(kwId, 1, 1);
        }
      }
    }
  }

  /**
   * Assumes the word does NOT exist in the database.
   *
   * @param word
   * @param wordId
   * @return ID to assign to the next word.
   */
  private int createTransientWord(String word, int wordId) {
    int newWordId = wordId;
    transientId2token_.put(wordId, word);
    transientToken2Id_.put(word, wordId);
    String expanded = DataAccess.expandTerm(word);
    int expandedId = existingToken2Id_.get(expanded);
    if (expandedId == existingToken2Id_.getNoEntryValue()) {
      expandedId = transientToken2Id_.get(expanded);
      if (expandedId == transientToken2Id_.getNoEntryValue()) {
        expandedId = ++newWordId;
        transientId2token_.put(expandedId, expanded);
        transientToken2Id_.put(expanded, expandedId);
      }
    }
    transientWordExpansions_.put(wordId, expandedId);
    return ++newWordId;
  }

  /**
   * Looks in both transient and existing word id map for the word id.
   *
   * @param word Word to look up.
   * @return Id of the word.
   */
  private int getWordId(String word) {
    int id = transientToken2Id_.get(word);
    if (id == transientToken2Id_.getNoEntryValue()) {
      id = existingToken2Id_.get(word);
    }
    return id;
  }

  private TIntObjectHashMap<int[]> buildEntityKeyphrases(Map<KBIdentifiedEntity, List<Keyphrase>> entityKeyphrases) {
    TIntObjectHashMap<int[]> entityKeyphrasesIds = new TIntObjectHashMap<>();
    for (Map.Entry<KBIdentifiedEntity, List<Keyphrase>> entry : entityKeyphrases.entrySet()) {
      KBIdentifiedEntity entityKbId = entry.getKey();
      List<Keyphrase> keyphrases = entry.getValue();
      int[] keyphraseIds = new int[keyphrases.size()];
      for (int i = 0; i < keyphrases.size(); i++) {
        Keyphrase keyphrase = keyphrases.get(i);
        int kpId = getWordId(keyphrase.getKeyphrase());
        keyphraseIds[i] = kpId;
      }
      int entityId = dictionary_.getEntityId(entityKbId);
      entityKeyphrasesIds.put(entityId, keyphraseIds);
    }
    return entityKeyphrasesIds;
  }

  public CandidateDictionary getDictionary() {
    return dictionary_;
  }

  public Set<KBIdentifiedEntity> getBlacklistedEntities() {
    return blacklistedEntities_;
  }

  public boolean contains(Entity entity) {
    return dictionary_.contains(entity);
  }
}
