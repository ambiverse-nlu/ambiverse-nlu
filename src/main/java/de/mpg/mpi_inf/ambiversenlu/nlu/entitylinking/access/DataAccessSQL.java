package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQLCache.EntityKeyphraseData;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQLCache.EntityKeywordsData;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;
import edu.stanford.nlp.util.StringUtils;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class DataAccessSQL implements DataAccessInterface {

  private static final Logger logger = LoggerFactory.getLogger(DataAccessSQL.class);

  public static final String ENTITY_KEYPHRASES = "entity_keyphrases";

  public static final String ENTITY_BIGRAMS = "entity_bigrams";

  public static final String ENTITY_IDS = "entity_ids";

  public static final String TYPE_IDS = "type_ids";

  public static final String CATEGORY_IDS = "category_ids";

  public static final String WORD_IDS = "word_ids";

  public static final String WORD_EXPANSION = "word_expansion";

  public static final String KEYPHRASE_COUNTS = "keyphrase_counts";

  public static final String KEYPHRASES_SOURCES_WEIGHTS = "keyphrases_sources_weights";

  public static final String KEYPHRASES_SOURCE = "keyphrase_sources";

  public static final String KEYPHRASES_TOKENS = "keyphrase_tokens";

  public static final String KEYWORD_COUNTS = "keyword_counts";

  public static final String BIGRAM_COUNTS = "bigram_counts";

  public static final String ENTITY_COUNTS = "entity_counts";

  public static final String ENTITY_KEYWORDS = "entity_keywords";

  public static final String ENTITY_INLINKS = "entity_inlinks";

  public static final String ENTITY_TYPES = "entity_types";

  public static final String TYPE_ENTITIES = "type_entities";

  public static final String CONCEPT_CATEGORIES = "concept_categories";

  public static final String CATEGORY_CONCEPTS = "category_concepts";

  public static final String TYPE_TAXONOMY = "type_taxonomy";

  public static final String ENTITY_RANK = "entity_rank";

  public static final String METADATA = "meta";

  public static final String LANGUAGES = "languages";

  public static final String ENTITY_METADATA = "entity_metadata";

  public static final String IMPORTANCE_COMPONENTS_INFO = "importance_components_info";

  public static final String DICTIONARY = "dictionary";

  public static final String ENTITIES = "entities";

  public static final String ENTITY_CLASSES = "entity_classes";

  public static final String ENTITY_LANGUAGES = "entity_languages";

  @Override public DataAccess.type getAccessType() {
    return DataAccess.type.sql;
  }

  @Override
  public Map<String, Entities> getEntitiesForMentions(Collection<String> mentions, double maxEntityRank, int topByPrior, boolean isNamedEntity)
      throws EntityLinkingDataAccessException {
    Integer id = RunningTimer.recordStartTime("DataAccess:getEntitiesForMention");
    Map<String, Entities> candidates = new HashMap<String, Entities>(mentions.size(), 1.0f);

    EntityType entityType;
    if (isNamedEntity) {
      entityType = EntityType.NAMED_ENTITY;
    } else {
      entityType = EntityType.CONCEPT;
    }

    if (mentions.size() == 0) {
      return candidates;
    }
    List<String> queryMentions = new ArrayList<String>(mentions.size());
    for (String m : mentions) {
      queryMentions.add(EntityLinkingManager.conflateToken(m, isNamedEntity));
      // Add an emtpy candidate set as default.
      candidates.put(m, new Entities());
    }
    Connection mentionEntityCon = null;
    Statement statement = null;
    Map<String, Map<Integer, Double>> queryMentionCandidates = new HashMap<>();
    try {
      mentionEntityCon = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = mentionEntityCon.createStatement();
      String sql = null;
      String query = PostgresUtil.getPostgresEscapedConcatenatedQuery(queryMentions);
      if (maxEntityRank < 1.0) {
        sql = "SELECT " + DICTIONARY + ".mention, " +
            DICTIONARY + ".entity, " + DICTIONARY + ".prior FROM " + DICTIONARY +
            ", " + ENTITY_RANK + ", " +
            " WHERE " + DICTIONARY + ".entity=" + ENTITY_RANK + ".entity" +
            " AND mention IN (" + query + ")" +
            " AND rank<" + maxEntityRank +
            " AND " + DICTIONARY + ".entitytype =" + entityType.getDBId();
      } else {
        sql = "SELECT mention, " + DICTIONARY + ".entity, prior FROM " + DICTIONARY +
            " WHERE mention IN (" + query + ")" +
            " AND " + DICTIONARY + ".entitytype =" + entityType.getDBId();
      }
      ResultSet r = statement.executeQuery(sql);
      while (r.next()) {
        String mention = r.getString(1);
        int entity = r.getInt(2);
        if (entity == 0) continue; // workaround for issue in db. Remove later.
        double prior = r.getDouble(3);
        Map<Integer, Double> entities = queryMentionCandidates.get(mention);
        if (entities == null) {
          entities = new HashMap<>();
          queryMentionCandidates.put(mention, entities);
        }
        // The result contain duplicate entities with different priors, keep the max
        if (entities.containsKey(entity)) {
          entities.put(entity, Math.max(entities.get(entity), prior));
        }
        else {
          entities.put(entity, prior);
        }
      }
      r.close();
      statement.close();
      EntityLinkingManager.releaseConnection(mentionEntityCon);

      // Get the candidates for the original Strings.
      for (Entry<String, Entities> entry: candidates.entrySet()) {
        String queryMention = EntityLinkingManager.conflateToken(entry.getKey(), isNamedEntity);
        Map<Integer, Double> entityPriors = queryMentionCandidates.get(queryMention);
        if (entityPriors != null) {
          Integer[] ids;
          if (topByPrior > 0) {
            List<Integer> topIds = CollectionUtils.getTopKeys(entityPriors, topByPrior);
            int droppedByPrior = entityPriors.size() - topIds.size();
            Counter.incrementCountByValue("CANDIDATES_DROPPED_BY_PRIOR", droppedByPrior);
            ids = topIds.toArray(new Integer[topIds.size()]);
          } else {
            ids = entityPriors.keySet().toArray(new Integer[entityPriors.size()]);
          }
          TIntObjectHashMap<KBIdentifiedEntity> yagoEntityIds = getKnowlegebaseEntitiesForInternalIds(ArrayUtils.toPrimitive(ids));
          Entities entities = entry.getValue();
          for (int i = 0; i < ids.length; ++i) {
            entities.add(new Entity(yagoEntityIds.get(ids[i]), ids[i]));
          }
        }
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    RunningTimer.recordEndTime("DataAccess:getEntitiesForMention", id);
    return candidates;
  }

  @Override public TIntObjectHashMap<List<MentionObject>> getMentionsForEntities(Entities entities) {
    Integer id = RunningTimer.recordStartTime("DataAccess:getMentionsForEntities");
    TIntObjectHashMap<List<MentionObject>> mentions = new TIntObjectHashMap<>(entities.size(), 1.0f);
    if (entities.size() == 0) {
      return mentions;
    }
    int[] queryEntityIds = new int[entities.size()];
    int i = 0;
    for (Entity e : entities) {
      queryEntityIds[i] = e.getId();
      i++;
    }

    Connection entityMentionCon = null;
    Statement statement = null;

    try {
      entityMentionCon = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = entityMentionCon.createStatement();
      String query = PostgresUtil.getIdQuery(queryEntityIds);
      String sql = "SELECT DISTINCT mention, entity, prior FROM " + DICTIONARY + " WHERE entity IN (" + query + ")";
      ResultSet r = statement.executeQuery(sql);
      while (r.next()) {
        String mention = r.getString(1);
        int entity = r.getInt(2);
        double prior = r.getDouble(3);
        List<MentionObject> tempMentions = mentions.get(entity);
        MentionObject mentionObject = new MentionObject(mention, prior);
        if (tempMentions == null) {
          mentions.put(entity, new ArrayList<>(Arrays.asList(mentionObject)));
        } else {
          tempMentions.add(mentionObject);
          mentions.put(entity, tempMentions);
        }

      }
      r.close();
      statement.close();
      EntityLinkingManager.releaseConnection(entityMentionCon);

    } catch (Exception e) {
      logger.error(e.getLocalizedMessage());
      e.printStackTrace();
    }
    RunningTimer.recordEndTime("DataAccess:getMentionsForEntities", id);
    return mentions;
  }

  @Override
  public Entities getEntitiesForMentionByFuzzyMatching(String mention, double minSimilarity, boolean isNamedEntity) throws EntityLinkingDataAccessException {
    Integer id = RunningTimer.recordStartTime("FuzzyEntitiesForMention");
    String conflatedMention = EntityLinkingManager.conflateToken(mention, isNamedEntity);
    TIntHashSet entitiesIds = new TIntHashSet();
    EntityType entityType;
    if (isNamedEntity) {
      entityType = EntityType.NAMED_ENTITY;
    } else {
      entityType = EntityType.CONCEPT;
    }
    Connection mentionEntityCon = null;
    Statement statement = null;
    try {
      mentionEntityCon = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = mentionEntityCon.createStatement();
      String sql = null;
      String query = PostgresUtil.getPostgresEscapedString(conflatedMention);
        sql = "SELECT mention, entity  FROM " + DICTIONARY + ", " +
              " WHERE mention % '" + query + "'" +
              " AND similarity(mention, '" +  query + "') >= " + minSimilarity +
              " AND " + DataAccessSQL.DICTIONARY + ".entitytype=" + entityType.getDBId();
      System.out.println(sql);
      ResultSet r = statement.executeQuery(sql);
      while (r.next()) {
        int entity = r.getInt(2);
        entitiesIds.add(entity);
      }
      r.close();
      statement.close();
      EntityLinkingManager.releaseConnection(mentionEntityCon);
    } catch (Exception e) {
      logger.error(e.getLocalizedMessage());
      throw new EntityLinkingDataAccessException(e);
    }
    int[] ids = entitiesIds.toArray();
    TIntObjectHashMap<KBIdentifiedEntity> yagoEntityIds = getKnowlegebaseEntitiesForInternalIds(ids);

    Entities entities = new Entities();
    for (int i = 0; i < ids.length; ++i) {
      entities.add(new Entity(yagoEntityIds.get(ids[i]), ids[i]));
    }
    RunningTimer.recordEndTime("FuzzyEntitiesForMention", id);
    return entities;
  }

  @Override public Keyphrases getEntityKeyphrases(Entities entities, Map<String, Double> keyphraseSourceWeights, double minKeyphraseWeight,
      int maxEntityKeyphraseCount) throws EntityLinkingDataAccessException {
    Integer runId = RunningTimer.recordStartTime("DataAccess:getEntityKeyPhrases");
    boolean useSources = keyphraseSourceWeights != null && !keyphraseSourceWeights.isEmpty();

    TObjectIntHashMap<String> keyphraseSrcName2Id = DataAccessCache.singleton().getAllKeyphraseSources();

    KeytermsCache<EntityKeyphraseData> kpc = DataAccessSQLCache.singleton().
        getEntityKeyphrasesCache(entities, keyphraseSrcName2Id, keyphraseSourceWeights, minKeyphraseWeight, maxEntityKeyphraseCount, useSources);

    // Create and fill return object with empty maps.
    Keyphrases keyphrases = new Keyphrases();

    TIntObjectHashMap<int[]> entityKeyphrases = new TIntObjectHashMap<int[]>();
    TIntObjectHashMap<TIntDoubleHashMap> entity2keyphrase2mi = new TIntObjectHashMap<TIntDoubleHashMap>();
    TIntObjectHashMap<TIntDoubleHashMap> entity2keyword2mi = new TIntObjectHashMap<TIntDoubleHashMap>();

    // Fill the keyphrases object with all data.
    keyphrases.setEntityKeyphrases(entityKeyphrases);
    keyphrases.setEntityKeyphraseWeights(entity2keyphrase2mi);
    keyphrases.setEntityKeywordWeights(entity2keyword2mi);

    // All keyphrase tokens are preloaded, just use the full data
    keyphrases.setKeyphraseTokens(DataAccessCache.singleton().getAllKeyphraseTokens());
    if (useSources) {
      TIntObjectHashMap<TIntIntHashMap> entity2keyphrase2source = new TIntObjectHashMap<TIntIntHashMap>();
      keyphrases.setEntityKeyphraseSources(entity2keyphrase2source);
      keyphrases.setKeyphraseSource2id(keyphraseSrcName2Id);
      TIntDoubleHashMap keyphraseSourceId2weight = new TIntDoubleHashMap();
      keyphrases.setKeyphraseSourceWeights(keyphraseSourceId2weight);
    }

    if (entities == null || entities.size() == 0) {
      return keyphrases;
    }

    TIntObjectHashMap<TIntHashSet> eKps = new TIntObjectHashMap<TIntHashSet>();
    for (Entity e : entities) {
      eKps.put(e.getId(), new TIntHashSet());
    }

    for (Pair<Integer, EntityKeyphraseData> p : kpc) {
      int entity = p.first;
      EntityKeyphraseData ekd = p.second;
      int keyphrase = ekd.keyphrase;
      double keyphraseWeight = ekd.weight;

      // Add keyphrase.
      TIntHashSet kps = eKps.get(entity);
      if (kps == null) {
        kps = new TIntHashSet();
        eKps.put(entity, kps);
      }
      kps.add(keyphrase);

      // Add keyphrase weight.
      TIntDoubleHashMap keyphrase2mi = entity2keyphrase2mi.get(entity);
      if (keyphrase2mi == null) {
        keyphrase2mi = new TIntDoubleHashMap();
        entity2keyphrase2mi.put(entity, keyphrase2mi);
      }
      keyphrase2mi.put(keyphrase, keyphraseWeight);

      if (useSources) {
        int source = ekd.source;
        TIntIntHashMap keyphraseSources = keyphrases.getEntityKeyphraseSources().get(entity);
        if (keyphraseSources == null) {
          keyphraseSources = new TIntIntHashMap();
          keyphrases.getEntityKeyphraseSources().put(entity, keyphraseSources);
        }
        keyphraseSources.put(keyphrase, source);
      }
    }

    // Transform eKps to entityKeyphrases.
    for (Entity e : entities) {
      entityKeyphrases.put(e.getId(), eKps.get(e.getId()).toArray());
    }

    // Retrieve entity keywords and weights.
    KeytermsCache<EntityKeywordsData> kwc = DataAccessSQLCache.singleton()
        .getEntityKeywordsCache(entities, keyphraseSourceWeights, keyphrases.getEntityKeyphrases(), keyphrases.getKeyphraseTokens(),
            minKeyphraseWeight, maxEntityKeyphraseCount);

    for (Pair<Integer, EntityKeywordsData> p : kwc) {
      int entity = p.first;
      EntityKeywordsData ewd = p.second;
      int keyword = ewd.keyword;
      double keywordWeight = ewd.weight;

      // Add keywords and weights.
      TIntDoubleHashMap keyword2mi = entity2keyword2mi.get(entity);
      if (keyword2mi == null) {
        keyword2mi = new TIntDoubleHashMap();
        entity2keyword2mi.put(entity, keyword2mi);
      }
      keyword2mi.put(keyword, keywordWeight);
    }

    RunningTimer.recordEndTime("DataAccess:getEntityKeyPhrases", runId);
    return keyphrases;
  }

  @Override public void getEntityKeyphraseTokens(Entities entities, TIntObjectHashMap<int[]> entityKeyphrases,
      TIntObjectHashMap<int[]> keyphraseTokens) throws EntityLinkingDataAccessException {
    if (entities == null || entities.size() == 0) {
      return;
    }

    Connection con = null;
    Statement statement = null;

    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      String entityQueryString = StringUtils.join(entities.getUniqueIds(), ",");
      statement = con.createStatement();

      TIntObjectHashMap<int[]> allKeyphraseTokens = getAllKeyphraseTokens();

      String sql = "SELECT entity,keyphrase " + " FROM " + ENTITY_KEYPHRASES + " WHERE entity IN (" + entityQueryString + ")";
      ResultSet rs = statement.executeQuery(sql);
      TIntObjectHashMap<TIntHashSet> eKps = new TIntObjectHashMap<TIntHashSet>();
      for (Entity e : entities) {
        eKps.put(e.getId(), new TIntHashSet());
      }
      while (rs.next()) {
        int entity = rs.getInt("entity");
        int keyphrase = rs.getInt("keyphrase");
        TIntHashSet kps = eKps.get(entity);
        if (kps == null) {
          kps = new TIntHashSet();
          eKps.put(entity, kps);
        }
        kps.add(keyphrase);

        if (!keyphraseTokens.containsKey(keyphrase)) {
          int[] tokenIds = allKeyphraseTokens.get(keyphrase);
          keyphraseTokens.put(keyphrase, tokenIds);
        }
      }
      rs.close();
      statement.close();

      // Transform eKps to entityKeyphrases.
      for (Entity e : entities) {
        entityKeyphrases.put(e.getId(), eKps.get(e.getId()).toArray());
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
  }

  @Override public TIntIntHashMap getEntitySuperdocSize(Entities entities) throws EntityLinkingDataAccessException {
    TIntIntHashMap entitySuperDocSizes = new TIntIntHashMap();

    if (entities == null || entities.size() == 0) {
      return entitySuperDocSizes;
    }

    Connection con = null;
    Statement statement = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = con.createStatement();
      String entitiesQuery = StringUtils.join(entities.getUniqueIds(), ",");
      String sql = "SELECT entity, count" + " FROM " + ENTITY_COUNTS + " WHERE entity IN (" + entitiesQuery + ")";
      ResultSet r = statement.executeQuery(sql);
      while (r.next()) {
        int entity = r.getInt("entity");
        int entityDocCount = r.getInt("count");
        entitySuperDocSizes.put(entity, entityDocCount);
      }
      r.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

    return entitySuperDocSizes;
  }

  @Override public TIntObjectHashMap<TIntIntHashMap> getEntityKeywordIntersectionCount(Entities entities) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<TIntIntHashMap> entityKeywordIC = new TIntObjectHashMap<TIntIntHashMap>();
    for (Entity entity : entities) {
      TIntIntHashMap keywordsIC = new TIntIntHashMap();
      entityKeywordIC.put(entity.getId(), keywordsIC);
    }

    if (entities == null || entities.size() == 0) {
      return entityKeywordIC;
    }

    Connection con = null;
    Statement statement = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = con.createStatement();
      String entitiesQuery = StringUtils.join(entities.getUniqueIds(), ",");
      String sql = "SELECT entity, keyword, count" + " FROM " + ENTITY_KEYWORDS + " WHERE entity IN (" + entitiesQuery + ")";
      ResultSet r = statement.executeQuery(sql);
      while (r.next()) {
        int entity = r.getInt("entity");
        int keyword = r.getInt("keyword");
        int keywordCount = r.getInt("count");
        entityKeywordIC.get(entity).put(keyword, keywordCount);
      }
      r.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

    return entityKeywordIC;
  }

  @Override public TIntObjectHashMap<TIntIntHashMap> getEntityUnitIntersectionCount(Entities entities, UnitType unitType)
      throws EntityLinkingDataAccessException {
    int timer = RunningTimer.recordStartTime("DataAccessSQL#getEntityUnitIntersectionCount()");
    TIntObjectHashMap<TIntIntHashMap> entityKeywordIC = new TIntObjectHashMap<TIntIntHashMap>();
    for (Entity entity : entities) {
      TIntIntHashMap keywordsIC = new TIntIntHashMap();
      entityKeywordIC.put(entity.getId(), keywordsIC);
    }

    if (entities == null || entities.size() == 0) {
      return entityKeywordIC;
    }

    Connection con = null;
    Statement statement = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = con.createStatement();
      String entitiesQuery = StringUtils.join(entities.getUniqueIds(), ",");
      String sql =
          "SELECT entity, " + unitType.getUnitName() + ", count" + " FROM " + unitType.getEntityUnitCooccurrenceTableName() + " WHERE entity IN ("
              + entitiesQuery + ")";
      ResultSet r = statement.executeQuery(sql);
      while (r.next()) {
        int entity = r.getInt(1);
        int keyword = r.getInt(2);
        int keywordCount = r.getInt(3);
        entityKeywordIC.get(entity).put(keyword, keywordCount);
      }
      r.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    RunningTimer.recordEndTime("DataAccessSQL#getEntityUnitIntersectionCount()", timer);
    return entityKeywordIC;
  }

  @Override public TIntObjectHashMap<int[]> getInlinkNeighbors(Entities entities) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<int[]> neighbors = new TIntObjectHashMap<int[]>();
    if (entities.isEmpty()) {
      return neighbors;
    }

    for (int entityId : entities.getUniqueIds()) {
      neighbors.put(entityId, new int[0]);
    }

    Connection con = null;
    Statement statement = null;

    String entitiesQuery = StringUtils.join(entities.getUniqueIds(), ",");
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      statement = con.createStatement();
      statement.setFetchSize(1_000_000);
      String sql = "SELECT entity, inlinks FROM " + DataAccessSQL.ENTITY_INLINKS + " WHERE entity IN (" + entitiesQuery + ")";
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        Integer[] neigbors = (Integer[]) rs.getArray("inlinks").getArray();
        int entity = rs.getInt("entity");
        neighbors.put(entity, ArrayUtils.toPrimitive(neigbors));
      }
      rs.close();
      statement.close();
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return neighbors;
  }

  @Override public TIntIntHashMap getKeyphraseDocumentFrequencies(TIntHashSet keyphrases) throws EntityLinkingDataAccessException {
    TIntIntHashMap keyphraseCounts = new TIntIntHashMap();

    if (keyphrases == null || keyphrases.size() == 0) {
      return keyphraseCounts;
    }

    Connection con = null;
    Statement statement = null;

    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = con.createStatement();

      String keyphraseQueryString = PostgresUtil.getIdQuery(keyphrases);

      String sql = "SELECT keyphrase,count " + "FROM " + KEYPHRASE_COUNTS + " WHERE keyphrase IN (" + keyphraseQueryString + ")";
      ResultSet rs = statement.executeQuery(sql);

      while (rs.next()) {
        int keyphrase = rs.getInt("keyphrase");
        int count = rs.getInt("count");

        keyphraseCounts.put(keyphrase, count);
      }

      rs.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

    return keyphraseCounts;
  }

  public TIntDoubleHashMap getEntityPriors(String mention, boolean isNamedEntity) throws EntityLinkingDataAccessException {
    mention = EntityLinkingManager.conflateToken(mention, isNamedEntity);
    TIntDoubleHashMap entityPriors = new TIntDoubleHashMap();
    EntityType entityType;
    if (isNamedEntity) {
      entityType = EntityType.NAMED_ENTITY;
    } else {
      entityType = EntityType.CONCEPT;
    }
    Connection con = null;
    Statement statement = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = con.createStatement();
      String sql = "SELECT " + DICTIONARY + ".entity, prior FROM " + DICTIONARY +
          " WHERE " + DataAccessSQL.DICTIONARY + ".entitytype=" + entityType.getDBId() +
          " AND mention=E'" +
           PostgresUtil.getPostgresEscapedString(
               mention) + "'";
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        int entity = rs.getInt("entity");
        double prior = rs.getDouble("prior");
        //The result contain duplicate entities with different priors, keep the max
        if (entityPriors.containsKey(entity)) {
          entityPriors.put(entity, Math.max(entityPriors.get(entity), prior));
        }
        else {
          entityPriors.put(entity, prior);
        }
      }
      rs.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return entityPriors;
  }

  @Override public TIntObjectHashMap<KBIdentifiedEntity> getKnowlegebaseEntitiesForInternalIds(int[] ids) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<KBIdentifiedEntity> entityIds = new TIntObjectHashMap<KBIdentifiedEntity>();
    if (ids.length == 0) {
      return entityIds;
    }
    Connection con = null;
    Statement stmt = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String idQuery = PostgresUtil.getIdQuery(ids);
      String sql = "SELECT entity, id, knowledgebase FROM " + DataAccessSQL.ENTITY_IDS + " WHERE id IN (" + idQuery + ")";
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String entity = rs.getString("entity");
        String kb = rs.getString("knowledgebase");
        int id = rs.getInt("id");
        entityIds.put(id, KBIdentifiedEntity.getKBIdentifiedEntity(entity, kb));

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " entity ids.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return entityIds;
  }

  @Override public TObjectIntHashMap<KBIdentifiedEntity> getInternalIdsForKBEntities(Collection<KBIdentifiedEntity> kbEntities)
      throws EntityLinkingDataAccessException {
    //for performance, query by entities names only, and filter later by kb
    TObjectIntHashMap<KBIdentifiedEntity> allEntities = new TObjectIntHashMap<KBIdentifiedEntity>();
    TObjectIntHashMap<KBIdentifiedEntity> filteredEntities = new TObjectIntHashMap<KBIdentifiedEntity>();
    if (kbEntities.size() == 0) {
      return filteredEntities;
    }

    Set<String> entities = new HashSet<String>();
    for (KBIdentifiedEntity e : kbEntities) {
      entities.add(e.getIdentifier());
    }

    Connection con = null;
    Statement stmt = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String idQuery = PostgresUtil.getPostgresEscapedConcatenatedQuery(entities);
      String sql = "SELECT entity, id, knowledgebase FROM " + DataAccessSQL.ENTITY_IDS + " WHERE entity IN (" + idQuery + ")";
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String entity = rs.getString("entity");
        String kb = rs.getString("knowledgebase");
        int id = rs.getInt("id");
        allEntities.put(KBIdentifiedEntity.getKBIdentifiedEntity(entity, kb), id);

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " entity ids.");
        }
      }
      con.setAutoCommit(true);

      for (KBIdentifiedEntity e : kbEntities) {
        filteredEntities.put(e, allEntities.get(e));
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return filteredEntities;
  }

  @Override public TIntObjectHashMap<String> getWordsForIds(int[] ids) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<String> wordIds = new TIntObjectHashMap<String>();
    if (ids.length == 0) {
      return wordIds;
    }
    Connection con = null;
    Statement stmt = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String idQuery = PostgresUtil.getIdQuery(ids);
      String sql = "SELECT word, id FROM " + DataAccessSQL.WORD_IDS + " WHERE id IN (" + idQuery + ")";
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String word = rs.getString("word");
        int id = rs.getInt("id");
        wordIds.put(id, word);

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " word ids.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return wordIds;
  }

  @Override
  public TIntObjectHashMap<String> getWordsForIdsLowerCase(int[] ids) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<String> wordIds = new TIntObjectHashMap<String>();
    if (ids.length == 0) {
      return wordIds;
    }
    Connection con = null;
    Statement stmt = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String idQuery = PostgresUtil.getIdQuery(ids);
      String sql = "SELECT word, id FROM " + DataAccessSQL.WORD_IDS +
                   " WHERE id IN (" + idQuery + ")";
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String word = rs.getString("word").toLowerCase();
        int id = rs.getInt("id");
        wordIds.put(id, word);

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " word ids.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return wordIds;
  }

  @Override public TObjectIntHashMap<String> getIdsForWords(Collection<String> keywords) throws EntityLinkingDataAccessException {
    TObjectIntHashMap<String> wordIds = new TObjectIntHashMap<String>();
    if (keywords.isEmpty()) {
      return wordIds;
    }
    Connection con = null;
    Statement stmt = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String idQuery = PostgresUtil.getPostgresEscapedConcatenatedQuery(keywords);
      String sql = "SELECT word, id FROM " + DataAccessSQL.WORD_IDS + " WHERE word IN (" + idQuery + ")";
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String word = rs.getString("word");
        int id = rs.getInt("id");
        wordIds.put(word, id);

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " word ids.");
        }
      }
      stmt.close();
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return wordIds;
  }

  @Override public TObjectIntHashMap<String> getAllKeyphraseSources() throws EntityLinkingDataAccessException {
    Integer kpSrcTime = RunningTimer.recordStartTime("DataAccessSQL:getAllKPSrc");
    Connection con = null;
    Statement stmt = null;
    TObjectIntHashMap<String> keyphraseSources = new TObjectIntHashMap<String>();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      String sql = "SELECT source, source_id FROM " + DataAccessSQL.KEYPHRASES_SOURCE;
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        int sourceId = rs.getInt("source_id");
        String sourceName = rs.getString("source");
        keyphraseSources.put(sourceName, sourceId);
      }
      stmt.close();
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    RunningTimer.recordEndTime("DataAccessSQL:getAllKPSrc", kpSrcTime);
    return keyphraseSources;
  }

  @Override public TIntObjectHashMap<int[]> getAllKeyphraseTokens() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TIntObjectHashMap<int[]> keyphraseTokens = new TIntObjectHashMap<int[]>();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      stmt = con.createStatement();
      con.setAutoCommit(false);
      stmt.setFetchSize(1000000);
      String sql = "SELECT keyphrase, token FROM " + DataAccessSQL.KEYPHRASES_TOKENS + " ORDER BY keyphrase, position";
      ResultSet rs = stmt.executeQuery(sql);

      int prevKp = -1;
      int currentKp = -1;
      TIntArrayList currentTokens = new TIntArrayList();
      int read = 0;
      while (rs.next()) {
        currentKp = rs.getInt(1);
        if (prevKp != -1 && currentKp != prevKp) {
          keyphraseTokens.put(prevKp, currentTokens.toArray());
          currentTokens.clear();
          if (++read % 1000000 == 0) {
            logger.debug("Read " + read + " keyphrase tokens.");
          }
        }
        currentTokens.add(rs.getInt(2));
        prevKp = currentKp;
      }
      // Put in last keyphrase.
      keyphraseTokens.put(currentKp, currentTokens.toArray());
      stmt.close();
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

    return keyphraseTokens;
  }

  @Override public TObjectIntHashMap<KBIdentifiedEntity> getAllEntityIds() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TObjectIntHashMap<KBIdentifiedEntity> entityIds = new TObjectIntHashMap<>();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String sql = "SELECT entity, id, knowledgebase FROM " + DataAccessSQL.ENTITY_IDS + " WHERE knowledgebase <> 'MENTION'";
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String entity = rs.getString("entity");
        String kb = rs.getString("knowledgebase");
        int id = rs.getInt("id");
        entityIds.put(KBIdentifiedEntity.getKBIdentifiedEntity(entity, kb), id);

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " entity ids.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return entityIds;
  }

  @Override
  public TObjectIntHashMap<KBIdentifiedEntity> getAllEntityIds(boolean isNamedEntity) throws EntityLinkingDataAccessException {
    EntityType entityType;
    if (isNamedEntity) {
      entityType = EntityType.NAMED_ENTITY;
    } else {
      entityType = EntityType.CONCEPT;
    }
    Connection con = null;
    Statement stmt = null;
    TObjectIntHashMap<KBIdentifiedEntity> entityIds = new TObjectIntHashMap<KBIdentifiedEntity>();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String sql = "SELECT " +
          DataAccessSQL.ENTITY_IDS + ".entity as entity, " +
          DataAccessSQL.ENTITY_IDS + ".id as id, " +
          DataAccessSQL.ENTITY_IDS + ".knowledgebase as knowledgebase" +
          " FROM " + DataAccessSQL.ENTITY_IDS + "," + DataAccessSQL.DICTIONARY +
          " WHERE knowledgebase <> 'MENTION'" +
          " AND " + DataAccessSQL.ENTITY_IDS + ".id=" + DataAccessSQL.DICTIONARY + ".entity" +
          " AND " +  DataAccessSQL.DICTIONARY + ".entitytype =" + entityType.getDBId();
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String entity = rs.getString("entity");
        String kb = rs.getString("knowledgebase");
        int id = rs.getInt("id");
        entityIds.put(KBIdentifiedEntity.getKBIdentifiedEntity(entity, kb), id);

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " entity ids.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      };
    }
    return entityIds;
  }

  @Override
  public TObjectIntHashMap<Type> getAllTypeIds() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TObjectIntHashMap<Type> typeIds = new TObjectIntHashMap<Type>();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String sql = "SELECT type, id, knowledgebase FROM " + DataAccessSQL.TYPE_IDS;
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String type = rs.getString("type");
        String kb = rs.getString("knowledgebase");
        int id = rs.getInt("id");
        typeIds.put(new Type(kb, type), id);

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " type ids.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return typeIds;
  }

  @Override public TIntDoubleHashMap getAllEntityRanks() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TIntDoubleHashMap entityRanks = new TIntDoubleHashMap();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String sql = "SELECT entity, rank FROM " + DataAccessSQL.ENTITY_RANK;
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        int entity = rs.getInt("entity");
        double rank = rs.getDouble("rank");
        entityRanks.put(entity, rank);

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " entity ranks.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return entityRanks;
  }

  @Override public TIntObjectHashMap<int[]> getAllEntityTypes() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TIntObjectHashMap<int[]> entityTypes = new TIntObjectHashMap<>();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String sql = "SELECT entity, types FROM " + DataAccessSQL.ENTITY_TYPES;
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        int entity = rs.getInt(1);
        Integer[] types = (Integer[]) rs.getArray(2).getArray();
        entityTypes.put(entity, ArrayUtils.toPrimitive(types));
        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " entity types.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return entityTypes;
  }

  @Override public TIntObjectHashMap<int[]> getTaxonomy() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TIntObjectHashMap<int[]> taxonomy = new TIntObjectHashMap<>();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String sql = "SELECT type, parents FROM " + DataAccessSQL.TYPE_TAXONOMY;
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        int type = rs.getInt(1);
        Integer[] parents = (Integer[]) rs.getArray(2).getArray();
        taxonomy.put(type, ArrayUtils.toPrimitive(parents));
        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " type parents.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return taxonomy;
  }

  @Override public TObjectIntHashMap<String> getAllWordIds() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TObjectIntHashMap<String> wordIds = new TObjectIntHashMap<String>(10000000);
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String sql = "SELECT word, id FROM " + DataAccessSQL.WORD_IDS;
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String word = rs.getString("word");
        int id = rs.getInt("id");
        wordIds.put(word, id);

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " word ids.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return wordIds;
  }

  @Override public Entities getAllEntities() throws EntityLinkingDataAccessException {
    TObjectIntHashMap<KBIdentifiedEntity> entityIds = getAllEntityIds();
    Entities entities = new Entities();
    for (TObjectIntIterator<KBIdentifiedEntity> itr = entityIds.iterator(); itr.hasNext(); ) {
      itr.advance();
      entities.add(new Entity(itr.key(), itr.value()));
    }
    return entities;
  }

  @Override
  public Entities getAllEntities(boolean isNamedEntity) throws EntityLinkingDataAccessException {
    TObjectIntHashMap<KBIdentifiedEntity> entityIds = getAllEntityIds(isNamedEntity);
    Entities entities = new Entities();
    for (TObjectIntIterator<KBIdentifiedEntity> itr = entityIds.iterator();
        itr.hasNext(); ) {
      itr.advance();
      entities.add(new Entity(itr.key(), itr.value()));
    }
    return entities;
  }

  @Override
  public int[] getAllWordExpansions() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TIntIntHashMap wordExpansions = new TIntIntHashMap();
    int maxId = -1;
    try {
      logger.info("Reading word expansions.");
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(1000000);
      String sql = "SELECT word, expansion FROM " + WORD_EXPANSION;
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        int word = rs.getInt("word");
        int expansion = rs.getInt("expansion");
        wordExpansions.put(word, expansion);
        if (word > maxId) {
          maxId = word;
        }

        if (++read % 1000000 == 0) {
          logger.debug("Read " + read + " word expansions.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

    // Transform hash to int array.
    int[] expansions = new int[maxId + 1];
    for (TIntIntIterator itr = wordExpansions.iterator(); itr.hasNext(); ) {
      itr.advance();
      assert itr.key() < expansions.length && itr.key() > 0;  // Ids start at 1.
      expansions[itr.key()] = itr.value();
    }
    return expansions;
  }

  @Override public int[] getAllWordContractions() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TIntIntHashMap wordContraction = new TIntIntHashMap();
    int maxId = -1;
    try {
      logger.info("Reading word contractions.");
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(1000000);
      String sql = "SELECT word, expansion FROM " + WORD_EXPANSION;
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        int word = rs.getInt("word");
        int expansion = rs.getInt("expansion");
        wordContraction.put(expansion, word);
        if (expansion > maxId) {
          maxId = expansion;
        }

        if (++read % 1000000 == 0) {
          logger.debug("Read " + read + " word expansions.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

    // Transform hash to int array.
    int[] contractions = new int[maxId + 1];
    for (TIntIntIterator itr = wordContraction.iterator(); itr.hasNext(); ) {
      itr.advance();
      assert itr.key() < contractions.length && itr.key() >= 0 : "Word contraction was out of range: " + itr.key();  // Ids start at 0.
      contractions[itr.key()] = itr.value();
    }
    return contractions;
  }

  @Override public int getWordExpansion(int wordId) throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    int wordExpansion = 0;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      stmt = con.createStatement();
      String sql = "SELECT expansion FROM " + WORD_EXPANSION + " WHERE word=" + wordId;
      ResultSet rs = stmt.executeQuery(sql);
      if (rs.next()) {
        wordExpansion = rs.getInt("expansion");
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return wordExpansion;
  }

  @Override
  public List<Integer> getWordsForExpansion(int expansionId) throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    List<Integer> result = new ArrayList<>();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      stmt = con.createStatement();
      String sql = "SELECT word FROM " + WORD_EXPANSION +
                   " WHERE expansion=" + expansionId;
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        result.add(rs.getInt("word"));
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return result;
  }

  @Override
  public TIntObjectHashMap<TIntArrayList> getWordsForExpansions(int[] expansionIds) throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TIntObjectHashMap<TIntArrayList> result = new TIntObjectHashMap<>();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      stmt = con.createStatement();
      List<Integer> temp = new ArrayList<Integer>();
      for (int t:expansionIds) {
        temp.add(t);
      }
      String ids = StringUtils.join(temp, ", ");
      String sql = "SELECT expansion, word FROM " + WORD_EXPANSION +
                   " WHERE expansion IN (" + ids + ")";
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        int key = rs.getInt(1);
        if (!result.contains(key)) {
          result.put(key, new TIntArrayList());
        }
        TIntArrayList t = result.get(key);
        t.add(rs.getInt(2));
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

    return result;
  }

  @Override public TIntObjectHashMap<int[]> getAllInlinks() throws EntityLinkingDataAccessException {
    TIntObjectHashMap<int[]> inlinks = new TIntObjectHashMap<int[]>();
    Connection con = null;
    Statement statement = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      statement = con.createStatement();
      statement.setFetchSize(1_000_000);
      int read = 0;
      String sql = "SELECT entity, inlinks FROM " + DataAccessSQL.ENTITY_INLINKS;
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        Integer[] neigbors = (Integer[]) rs.getArray("inlinks").getArray();
        int entity = rs.getInt("entity");
        inlinks.put(entity, ArrayUtils.toPrimitive(neigbors));

        if (++read % 1_000_000 == 0) {
          logger.info("Read " + read + " entity inlinks.");
        }
      }
      rs.close();
      statement.close();
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return inlinks;
  }

  @Override public int getCollectionSize() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    int collectionSize = 0;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      stmt = con.createStatement();
      String sql = "SELECT value FROM " + METADATA + " WHERE key='collection_size'";
      ResultSet rs = stmt.executeQuery(sql);

      // if there is a result, it means it is a YAGO entity
      if (rs.next()) {
        String sizeString = rs.getString("value");
        collectionSize = Integer.parseInt(sizeString);
      }
      rs.close();
      stmt.close();
    } catch (Exception e) {
      logger.error("You might have an outdated entity repository, please " + "download the latest version from the AIDA website. Also check "
          + "above for other error messages, maybe the connection to the " + "Postgres database is not working properly.");
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return collectionSize;
  }

  @Override public String getDumpVersion() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    String dumpVersion = "";
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      stmt = con.createStatement();
      String sql = "SELECT value FROM " + METADATA + " WHERE key like 'KB:\"WikipediaSource_%'";
      ResultSet rs = stmt.executeQuery(sql);

      // if there is a result, it means it is a YAGO entity
      if (rs.next()) {
        String dumpVersionString = rs.getString("value");
        //Extract only the numbers. This will be the year
        String currVersion = dumpVersionString.replaceAll("\\D+", "");
        if (currVersion.compareTo(dumpVersion) >= 1) {
          dumpVersion = currVersion;
        }
      }
      rs.close();
      stmt.close();
    } catch (Exception e) {
      logger.error("You might have an outdated entity repository, please " + "download the latest version from the AIDA website. Also check "
          + "above for other error messages, maybe the connection to the " + "Postgres database is not working properly.");
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return dumpVersion;
  }

  @Override public Date getDumpCreationDate() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    Date dumpCreationDate = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      stmt = con.createStatement();
      String sql = "SELECT value FROM " + METADATA + " WHERE key='creationDate'";
      ResultSet rs = stmt.executeQuery(sql);

      // if there is a result, it means it is a YAGO entity
      if (rs.next()) {
        String dumpCreationString = rs.getString("value");
        //Extract the creation date from string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM d HH:mm:ss z yyyy");
        LocalDateTime dateTime = LocalDateTime.parse(dumpCreationString, formatter);
        dumpCreationDate = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
      }
      rs.close();
      stmt.close();
    } catch (Exception e) {
      logger.error("You might have an outdated entity repository, please " + "download the latest version from the AIDA website. Also check "
          + "above for other error messages, maybe the connection to the " + "Postgres database is not working properly.");
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return dumpCreationDate;
  }

  @Override public int getTypeCollectionSize() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    int count = 0;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      stmt = con.createStatement();
      String sql = "SELECT count(*) as cnt from " + TYPE_IDS;
      ResultSet rs = stmt.executeQuery(sql);

      // if there is a result, it means it is a YAGO entity
      if (rs.next()) {
        count = rs.getInt("cnt");
      }
      rs.close();
      stmt.close();
    } catch (Exception e) {
      logger.error("You might have an outdated entity repository, please " + "download the latest version from the AIDA website. Also check "
          + "above for other error messages, maybe the connection to the " + "Postgres database is not working properly.");
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return count;
  }

  @Override public int getMaximumEntityId() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    int maxId = 0;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      stmt = con.createStatement();
      String sql = "SELECT max(id) FROM " + ENTITY_IDS;
      ResultSet rs = stmt.executeQuery(sql);

      if (rs.next()) {
        maxId = rs.getInt(1);
      }
      rs.close();
      stmt.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return maxId;
  }

  @Override public int getMaximumWordId() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    int maxId = 0;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      stmt = con.createStatement();
      String sql = "SELECT max(id) FROM " + WORD_IDS;
      ResultSet rs = stmt.executeQuery(sql);

      if (rs.next()) {
        maxId = rs.getInt(1);
      }
      rs.close();
      stmt.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return maxId;
  }

  @Override public TIntObjectHashMap<Type> getTypesForIds(int[] ids) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<Type> typeNames = new TIntObjectHashMap<Type>();
    if (ids.length == 0) {
      return typeNames;
    }
    Connection con = null;
    Statement stmt = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String idQuery = PostgresUtil.getIdQuery(ids);
      String sql = "SELECT type, knowledgeBase, id FROM " + DataAccessSQL.TYPE_IDS + " WHERE id IN (" + idQuery + ")";
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String typeName = rs.getString("type");
        String knowledgeBase = rs.getString("knowledgeBase");
        int id = rs.getInt("id");
        typeNames.put(id, new Type(knowledgeBase, typeName));

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " type names.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return typeNames;
  }

  @Override public TObjectIntHashMap<String> getIdsForTypeNames(Collection<String> typeNames) throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TObjectIntHashMap<String> typesIds = new TObjectIntHashMap<String>();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String idQuery = PostgresUtil.getPostgresEscapedConcatenatedQuery(typeNames);
      String sql = "SELECT type, id FROM " + DataAccessSQL.TYPE_IDS + " WHERE type IN (" + idQuery + ")";
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String type = rs.getString("type");
        int id = rs.getInt("id");
        typesIds.put(type, id);

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " types ids.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return typesIds;
  }

  @Override public TIntObjectHashMap<int[]> getTypesIdsForEntitiesIds(int[] entitiesIds) throws EntityLinkingDataAccessException {
    Integer id = RunningTimer.recordStartTime("DataAccess:getTypesIdsForEntitiesIds");
    TIntObjectHashMap<int[]> typesIds = new TIntObjectHashMap<int[]>();
    if (entitiesIds.length == 0) {
      return typesIds;
    }

    for (int entityId : entitiesIds) {
      typesIds.put(entityId, new int[0]);
    }

    Connection con = null;
    Statement statement = null;

    String entitiesQuery = StringUtils.join(Util.asIntegerList(entitiesIds), ",");
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = con.createStatement();
      String sql = "SELECT entity, types FROM " + DataAccessSQL.ENTITY_TYPES + " WHERE entity IN (" + entitiesQuery + ")";
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        Integer[] types = (Integer[]) rs.getArray("types").getArray();
        int entity = rs.getInt("entity");
        typesIds.put(entity, ArrayUtils.toPrimitive(types));
      }
      rs.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
      RunningTimer.recordEndTime("DataAccess:getTypesIdsForEntitiesIds", id);
    }
    return typesIds;
  }

  @Override public TIntObjectHashMap<int[]> getEntitiesIdsForTypesIds(int[] typesIds) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<int[]> entitiesIds = new TIntObjectHashMap<int[]>();
    for (int typeId : typesIds) {
      entitiesIds.put(typeId, new int[0]);
    }

    Connection con = null;
    Statement statement = null;

    String typesQuery = StringUtils.join(Util.asIntegerList(typesIds), ",");
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = con.createStatement();
      String sql = "SELECT type, entities FROM " + DataAccessSQL.TYPE_ENTITIES + " WHERE type IN (" + typesQuery + ")";
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        Integer[] entities = (Integer[]) rs.getArray("entities").getArray();
        int type = rs.getInt("type");
        entitiesIds.put(type, ArrayUtils.toPrimitive(entities));
      }
      rs.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return entitiesIds;
  }

  @Override public Map<String, List<String>> getAllEntitiesMetaData(String startingWith) throws EntityLinkingDataAccessException {
    Map<String, List<String>> entitiesMetaData = new TreeMap<String, List<String>>();

    Connection con = null;
    Statement statement = null;

    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = con.createStatement();
      StringBuilder sb = new StringBuilder();
      boolean useFilteringTypes = EntityLinkingConfig.getEntitiesFilterTypes() != null;
      sb.append("SELECT em.humanreadablererpresentation, em.url ");
      if (useFilteringTypes) {
        sb.append(", ti.type ");
      }
      sb.append("FROM entity_metadata em ");
      if (useFilteringTypes) {
        sb.append(", entity_types et, type_ids ti ");
      }
      sb.append("WHERE em.humanreadablererpresentation ILIKE '" + startingWith + "%' ");
      if (useFilteringTypes) {
        sb.append("AND em.entity = et.entity AND ti.id=ANY(et.types) ");
        sb.append("AND ti.type IN ( ");
        sb.append(
            Arrays.stream(EntityLinkingConfig.getEntitiesFilterTypes()).map(Type::getName).map(n -> "'" + n + "'").collect(Collectors.joining(",")));
        sb.deleteCharAt(sb.length() - 1);
        sb.append(") ");
      }
      sb.append("ORDER BY em.humanreadablererpresentation");
      ResultSet rs = statement.executeQuery(sb.toString());
      while (rs.next()) {
        String humanReadableRepresentation = rs.getString("humanreadablererpresentation");
        String url = rs.getString("url");
        if (entitiesMetaData.containsKey(humanReadableRepresentation)) {
          entitiesMetaData.get(humanReadableRepresentation).add(url);
        } else {
          List<String> newList = new ArrayList<String>();
          newList.add(url);
          entitiesMetaData.put(humanReadableRepresentation, newList);
        }
      }
      rs.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return entitiesMetaData;
  }

  @Override public TIntObjectHashMap<EntityMetaData> getEntitiesMetaData(int[] entitiesIds) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<EntityMetaData> entitiesMetaData = new TIntObjectHashMap<EntityMetaData>();
    if (entitiesIds == null || entitiesIds.length == 0) {
      return entitiesMetaData;
    }

    Connection con = null;
    Statement statement = null;

    String entitiesQuery = StringUtils.join(Util.asIntegerList(entitiesIds), ",");
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = con.createStatement();
      //String sql = "SELECT entity, humanreadablererpresentation, url, "
      //    + "knowledgebase, depictionurl, license, description, wikidataid FROM " +
      //             DataAccessSQL.ENTITY_METADATA +
      //             " WHERE entity IN (" + entitiesQuery + ")";
      String sql =
          "SELECT entity, humanreadablererpresentation, url, " + "knowledgebase, description, wikidataid FROM " + DataAccessSQL.ENTITY_METADATA
              + " WHERE entity IN (" + entitiesQuery + ")";
      logger.debug("Getting metadata for " + entitiesIds.length + " entities");
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        int entity = rs.getInt("entity");
        String humanReadableRepresentation = rs.getString("humanreadablererpresentation");
        String url = rs.getString("url");
        String knowledgebase = rs.getString("knowledgebase");
        //String depictionurl = rs.getString("depictionurl");
        //String license = rs.getString("license");
        String depictionurl = null;
        String license = null;

        String description = rs.getString("description");
        String wikiData = rs.getString("wikidataid");

        entitiesMetaData
            .put(entity, new EntityMetaData(entity, humanReadableRepresentation, url, knowledgebase, depictionurl, license, description, wikiData));
      }
      logger.debug("Getting metadata for " + entitiesIds.length + " entities DONE");
      rs.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return entitiesMetaData;
  }

  @Override public TIntDoubleHashMap getEntitiesImportances(int[] entitiesIds) throws EntityLinkingDataAccessException {
    TIntDoubleHashMap entitiesImportances = new TIntDoubleHashMap();
    if (entitiesIds == null || entitiesIds.length == 0) {
      return entitiesImportances;
    }

    Connection con = null;
    Statement statement = null;

    String entitiesQuery = StringUtils.join(Util.asIntegerList(entitiesIds), ",");
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = con.createStatement();
      String sql = "SELECT entity, rank FROM " + DataAccessSQL.ENTITY_RANK + " WHERE entity IN (" + entitiesQuery + ")";
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        int entity = rs.getInt("entity");
        double rank = rs.getDouble("rank");
        entitiesImportances.put(entity, rank);
      }
      rs.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return entitiesImportances;
  }

  @Override public Map<String, Double> getKeyphraseSourceWeights() throws EntityLinkingDataAccessException {
    Connection sourceWeightCon = null;
    Statement statement = null;
    Map<String, Double> querySourceWeights = new HashMap<String, Double>();
    try {
      sourceWeightCon = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = sourceWeightCon.createStatement();
      String sql = "SELECT " + KEYPHRASES_SOURCE + ".source, " + KEYPHRASES_SOURCES_WEIGHTS + ".weight FROM " + KEYPHRASES_SOURCE + ","
          + KEYPHRASES_SOURCES_WEIGHTS + " WHERE " + KEYPHRASES_SOURCE + ".source_id = " + KEYPHRASES_SOURCES_WEIGHTS + ".source";

      ResultSet r = statement.executeQuery(sql);
      while (r.next()) {
        String source = r.getString(1);
        double weight = r.getDouble(2);
        querySourceWeights.put(source, weight);
      }
      r.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(sourceWeightCon);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return querySourceWeights;
  }

  @Override public String getConfigurationName() throws EntityLinkingDataAccessException {
    String confName = "";
    Connection confNameConn = null;
    Statement statement = null;
    try {
      confNameConn = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = confNameConn.createStatement();
      String sql = "SELECT " + METADATA + ".value FROM " + METADATA + " WHERE " + METADATA + ".key = 'confName'";

      ResultSet r = statement.executeQuery(sql);
      if (r.next()) {
        confName = r.getString(1);
      }
      r.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(confNameConn);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return confName;
  }

  @Override public String getLanguages() throws EntityLinkingDataAccessException {
    String language = "";
    Connection confNameConn = null;
    Statement statement = null;
    try {
      confNameConn = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = confNameConn.createStatement();
      String sql = "SELECT " + METADATA + ".value FROM " + METADATA + " WHERE " + METADATA + ".key = 'language'";

      ResultSet r = statement.executeQuery(sql);
      if (r.next()) {
        language = r.getString(1);
      }
      r.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(confNameConn);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return language;
  }

  @Override public Map<String, Integer> getInternalIdsfromWikidataIds(List<String> ids) throws EntityLinkingDataAccessException {
    Map<String, Integer> result = new HashMap<>();
    Connection con = null;
    Statement stmt = null;
    try {
      logger.info("Reading Internal Ids for Wikidata Ids.");
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      String entitiesQuery = StringUtils.join(ids.toArray(new String[ids.size()]), "','");
      String sql = "SELECT wikidataid, entity FROM " + DataAccessSQL.ENTITY_METADATA + " where wikidataid IN ('" + entitiesQuery + "')";
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        String wikidataid = rs.getString("wikidataid");
        int entity = rs.getInt("entity");
        result.put(wikidataid, entity);
      }
      con.setAutoCommit(true);
      return result;
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
  }

  @Override public Entities getEntitiesForInternalIds(int[] ids) throws EntityLinkingDataAccessException {
    Entities entities = new Entities();
    TIntObjectHashMap<KBIdentifiedEntity> yagoEntityIds = getKnowlegebaseEntitiesForInternalIds(ids);
    if (yagoEntityIds.isEmpty()) {
      return entities;
    }
    for (int id : ids) {
      KBIdentifiedEntity kbi = yagoEntityIds.get(id);
      entities.add(new Entity(kbi, id));
    }
    return entities;
  }

  @Override public Set<String> getMentionsforLanguage(Language language, Boolean isNamedEntity, int limit) throws EntityLinkingDataAccessException {
    Set<String> result = new HashSet<>();
    EntityType entityType;
    if (isNamedEntity) {
      entityType = EntityType.NAMED_ENTITY;
    } else {
      entityType = EntityType.CONCEPT;
    }
    Connection con = null;
    Statement stmt = null;
    try {
      logger.debug("Reading mentions for '" +language.name() + "'.");
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(1000000);
      String sql;
      if (isNamedEntity == null) {
        sql = "SELECT DISTINCT mention FROM " + DataAccessSQL.ENTITY_LANGUAGES + " where language=" + language.getID()
              + (limit != 0? " limit " + limit : "");
      } else {
        sql = "SELECT DISTINCT tmp.mention FROM " + DataAccessSQL.ENTITY_LANGUAGES  +
            ", (SELECT DISTINCT entity, mention FROM " + DataAccessSQL.DICTIONARY +
            " WHERE entitytype=" + entityType.getDBId()
            + (limit != 0? " limit " + limit : "") + ") as tmp "
            + "WHERE " +DataAccessSQL.ENTITY_LANGUAGES+".entity=tmp.entity"
            + " AND language=" + language.getID();
      }
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      if (!rs.isBeforeFirst()) {
        return result;
      }
      while (rs.next()) {
        String mention = rs.getString("mention");
        result.add(mention);
        if (++read % 1000000 == 0) {
          logger.debug("Read " + read + " keyword counts.");
        }
      }
      con.setAutoCommit(true);
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
  }



  @Override public Map<String, int[]> getEntityMentionsforLanguageAndEntities(int[] ids, Language language)
      throws EntityLinkingDataAccessException {
    Map<String, int[]> result = new HashMap<>();
    Connection con = null;
    Statement stmt = null;
    try {
      logger.info("Reading keyword counts.");
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(1000000);
      StringJoiner sj = new StringJoiner(",");
      for (int id : ids) {
        sj.add(Integer.toString(id));
      }
      String sql =
          "SELECT entity, mention FROM " + DataAccessSQL.ENTITY_LANGUAGES + " where language=" + language.getID() + " AND entity IN ( " + sj.toString()
              + " ) ORDER BY MENTION;";
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      String mention = null;
      Set<Integer> idsSet = new HashSet<>();
      while (rs.next()) {
        if (mention != null && !mention.equals(rs.getString("mention"))) {
          result.put(mention, idsSet.stream().mapToInt(i -> i).toArray());
          idsSet.clear();
        }
        mention = rs.getString("mention");
        idsSet.add(rs.getInt("entity"));
        if (++read % 1000000 == 0) {
          logger.debug("Read " + read + " keyword counts.");
        }
      }
      con.setAutoCommit(true);
      return result;
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
  }

  @Override public int[] getAllKeywordDocumentFrequencies() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TIntIntHashMap keywordCounts = new TIntIntHashMap();
    int maxId = -1;
    try {
      logger.info("Reading keyword counts.");
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(1000000);
      String sql = "SELECT keyword, count FROM " + KEYWORD_COUNTS;
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        int keyword = rs.getInt("keyword");
        int count = rs.getInt("count");
        keywordCounts.put(keyword, count);
        if (keyword > maxId) {
          maxId = keyword;
        }

        if (++read % 1000000 == 0) {
          logger.debug("Read " + read + " keyword counts.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

    // Transform hash to int array. This will contain a lot of zeroes as 
    // the keyphrase ids are not part of this (but need to be considered).
    int[] counts = new int[maxId + 1];
    for (TIntIntIterator itr = keywordCounts.iterator(); itr.hasNext(); ) {
      itr.advance();
      int keywordId = itr.key();
      // assert keywordId < counts.length && keywordId > 0 : "Failed for " + keywordId;  // Ids start at 1.
      // TODO: actually, keywords should not contain a 0 id, but they do.
      assert keywordId < counts.length : "Failed for " + keywordId;  // Ids start at 1.
      counts[keywordId] = itr.value();
    }
    return counts;
  }

  @Override public int[] getAllUnitDocumentFrequencies(UnitType unitType) throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TIntIntHashMap unitCounts = new TIntIntHashMap();
    int maxId = -1;
    try {
      logger.info("Reading " + unitType.getUnitName() + " counts.");
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(1000000);
      StringBuilder sql = new StringBuilder();
      sql.append("SELECT ").append(unitType.getUnitName()).append(", count FROM ").append(unitType.getUnitCountsTableName());
      ResultSet rs = stmt.executeQuery(sql.toString());
      int read = 0;
      while (rs.next()) {
        int unit = rs.getInt(1);
        int count = rs.getInt(2);
        unitCounts.put(unit, count);
        if (unit > maxId) {
          maxId = unit;
        }

        if (++read % 1000000 == 0) {
          logger.debug("Read " + read + " " + unitType.getUnitName() + " counts.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

    if (maxId == -1) return null;

    // Transform hash to int array. This will contain a lot of zeroes as 
    // the keyphrase ids are not part of this (but need to be considered).
    int[] counts = new int[maxId + 1];
    for (TIntIntIterator itr = unitCounts.iterator(); itr.hasNext(); ) {
      itr.advance();
      int unitId = itr.key();
      // assert unitId < counts.length && unitId > 0 : "Failed for " + unitId;  // Ids start at 1.
      // TODO: actually, units should not contain a 0 id, but they do.
      assert unitId < counts.length : "Failed for " + unitId;  // Ids start at 1.
      counts[unitId] = itr.value();
    }
    return counts;
  }

  private TIntIntHashMap getEntityImportanceComponentValue(Entities entities, String dbTableName) throws EntityLinkingDataAccessException {
    TIntIntHashMap values = new TIntIntHashMap();
    if (entities.size() == 0) {
      return values;
    }

    Connection con = null;
    Statement statement = null;

    String entitiesQuery = StringUtils.join(entities.getUniqueIds(), ",");
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = con.createStatement();
      String sql = "SELECT entity, value FROM " + dbTableName + " WHERE entity IN (" + entitiesQuery + ")";
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        int value = rs.getInt("value");
        int entity = rs.getInt("entity");
        values.put(entity, value);
      }
      rs.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return values;
  }

  @Override public Pair<Integer, Integer> getImportanceComponentMinMax(String importanceComponentId) throws EntityLinkingDataAccessException {
    Pair<Integer, Integer> minMax = null;

    Connection con = null;
    Statement statement = null;

    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = con.createStatement();
      String sql = "SELECT min, max FROM " + DataAccessSQL.IMPORTANCE_COMPONENTS_INFO + " WHERE component='" + importanceComponentId + "'";
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        int min = rs.getInt("min");
        int max = rs.getInt("max");
        minMax = new Pair<Integer, Integer>(min, max);
      }
      rs.close();
      statement.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return minMax;
  }

  @Override public Map<String, int[]> getDictionary() throws EntityLinkingDataAccessException {
    Map<String, int[]> candidates = new HashMap<String, int[]>();
    Connection con = null;
    Statement statement = null;
    Map<String, TIntList> tempCandidates = new HashMap<String, TIntList>(DataAccess.getCollectionSize(), 1.0f);
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      statement = con.createStatement();
      statement.setFetchSize(100000);
      String sql = "SELECT mention, entity FROM " + DICTIONARY;
      ResultSet r = statement.executeQuery(sql);
      int read = 0;
      while (r.next()) {
        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " dictionary entries.");
        }
        String mention = r.getString(1);
        int entity = r.getInt(2);
        TIntList entities = tempCandidates.get(mention);
        if (entities == null) {
          entities = new TIntArrayList();
          tempCandidates.put(mention, entities);
        }
        entities.add(entity);
      }
      r.close();
      statement.close();
      con.setAutoCommit(true);
      EntityLinkingManager.releaseConnection(con);

      // Transform to arrays.
      candidates = new HashMap<String, int[]>(tempCandidates.size(), 1.0f);
      for (Entry<String, TIntList> entry : tempCandidates.entrySet()) {
        candidates.put(entry.getKey(), entry.getValue().toArray());
      }
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    }
    return candidates;
  }

  @Override
  public double getGlobalEntityPrior(Entity entity) throws EntityLinkingDataAccessException {
    // Query the database for the entity-mention prior
    Connection conn = null;
    PreparedStatement pstmt = null;

    // Mind that 1.0 is the worst value!
    double prior = 1.0;

    try {
      conn = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);

      // Prepare the query.
      pstmt = conn.prepareStatement("SELECT rank FROM entity_rank WHERE entity=?");

      pstmt.setInt(1, entity.getId());

      // Execute the query.
      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        prior = rs.getDouble("rank");
      }
      rs.close();
      pstmt.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(conn);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }

    return prior;
  }


    @Override
    public List<String> getMentionsInLanguageForEnglishMentions(Collection<String> mentions, Language language, boolean isNamedEntity) {
    Integer id = RunningTimer.recordStartTime("DataAccess:getAllMentionsInAllLanguagesForMentions");
    List<String> resultMentions = new ArrayList<>();

    Connection entityMentionCon = null;
    Statement statement = null;

    try {
      entityMentionCon =
          EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = entityMentionCon.createStatement();

      List<String> queryMentions = new ArrayList<String>(mentions.size());
      for (String m : mentions) {
        queryMentions.add(EntityLinkingManager.conflateToken(m, isNamedEntity));
      }

      String query = PostgresUtil.getPostgresEscapedConcatenatedQuery(queryMentions);
      String sql = "SELECT DISTINCT mention"
          + " FROM " + DataAccessSQL.ENTITY_LANGUAGES
          + " WHERE language=" + language.getID()
          + " AND entity IN"
          + " (SELECT DISTINCT entity"
          + " FROM " + DataAccessSQL.ENTITY_LANGUAGES
          + " WHERE mention IN (" + query + ")"
          + " AND language=0"
          + ")";
      ResultSet r = statement.executeQuery(sql);
      while (r.next()) {
        resultMentions.add(r.getString(1));
      }
      r.close();
      statement.close();
      EntityLinkingManager.releaseConnection(entityMentionCon);

    } catch (Exception e) {
      logger.error(e.getLocalizedMessage());
      e.printStackTrace();
    }
    RunningTimer.recordEndTime("DataAccess:getAllMentionsInAllLanguagesForMention", id);
    return resultMentions;
  }

  @Override
  public Map<String, int[]> getCategoryIdsForMentions(Set<String> mentions, Language language, boolean isNamedEntity) {
    Map<String, Set<Integer>> results = new HashMap<>();

    if (mentions.isEmpty()) {
      return new HashMap<>();
    }
    List<String> queryMentions = new ArrayList<String>(mentions.size());
    for (String m : mentions) {
      queryMentions.add(EntityLinkingManager.conflateToken(m, isNamedEntity));
    }

    EntityType entityType;
    if (isNamedEntity) {
      entityType = EntityType.NAMED_ENTITY;
    } else {
      entityType = EntityType.CONCEPT;
    }

    Connection entityMentionCon = null;
    Statement statement = null;
    String sql = null;
    try {
      entityMentionCon =
          EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = entityMentionCon.createStatement();
      String mentionsQuery = PostgresUtil.getPostgresEscapedConcatenatedQuery(queryMentions);

      sql = "SELECT "+ DICTIONARY + ".mention, types "
          + " FROM " + DICTIONARY + ", " + CONCEPT_CATEGORIES + ", entity_languages"
          + " WHERE " + DICTIONARY + ".mention IN (" + mentionsQuery + ") "
          + " AND " + CONCEPT_CATEGORIES + ".entity=" + DICTIONARY + ".entity "
          + " AND " + DICTIONARY + ".entitytype= " + entityType.getDBId() + " "
          + " AND entity_languages.language=" + language.getID() + " "
          + " AND " + DICTIONARY + ".entity=" + "entity_languages.entity" + " "
          + " AND " + DICTIONARY + ".mention=entity_languages.mention" ;

      ResultSet r = statement.executeQuery(sql);
      while (r.next()) {
        Integer[] types = (Integer[]) r.getArray("types").getArray();
        if (results.containsKey(r.getString("mention"))) {
          results.get(r.getString("mention")).addAll(Arrays.asList(types));
        }
        else {
          results.put(r.getString("mention"),new HashSet<>(Arrays.asList(types)));
        }
      }
      r.close();
      statement.close();
      EntityLinkingManager.releaseConnection(entityMentionCon);


    } catch (Exception e) {
      System.out.println(sql);
      logger.error(e.getLocalizedMessage());
      e.printStackTrace();
    }

    Map<String, int[]> ret = new HashMap<>();
    for (String key:results.keySet()) {
      Integer[] temp = results.get(key).toArray(new Integer[0]);
      ret.put(key, ArrayUtils.toPrimitive(temp));
    }

    return ret;
  }

  @Override
  public TIntObjectHashMap<Type> getAllWikiCategoryTypes() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    TIntObjectHashMap<Type> typeNames = new TIntObjectHashMap<>();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);

      String sql = "SELECT type, id, knowledgebase"
          + " FROM " + DataAccessSQL.CATEGORY_IDS;
//          + " WHERE type LIKE '<wikicat%'"; //This table does not have other yago types (<wordnet_...)
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String type = rs.getString("type");
        String kb = rs.getString("knowledgebase");
        int id = rs.getInt("id");

        typeNames.put(id, new Type(kb, type));

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " type ids.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return typeNames;
  }

  @Override
  public Set<String> getEntityURL(String namedEntity) throws EntityLinkingDataAccessException {
    Set<String> result = new HashSet<>();
    Connection con = null;
    Statement stmt = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      String sql = "select em.url from entity_metadata em, entity_ids e " +
              "where em.entity = e.id and e.entity = '" + namedEntity.replace("'", "''") + "'";
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        String url = rs.getString("url");
        result.add(url);
      }
      con.setAutoCommit(true);
      return result;
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
  }

  @Override
  public Map<String, String> getWikidataIdsForEntities(Set<String> entities) {
    Map<String, String> results = new HashMap<>();

    if (entities.isEmpty()) {
      return results;
    }

    Connection entityMentionCon = null;
    Statement statement = null;
    String sql = null;
    try {
      entityMentionCon =
          EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      statement = entityMentionCon.createStatement();
      String entitiesQuery = PostgresUtil.getPostgresEscapedConcatenatedQuery(entities);
      sql = "SELECT entity_ids.entity, entity_metadata.wikidataid "
          + "FROM entity_metadata "
          + "JOIN entity_ids ON entity_ids.id=entity_metadata.entity "
          + "WHERE entity_ids.entity IN (" + entitiesQuery + ")";

      ResultSet r = statement.executeQuery(sql);
      while (r.next()) {
        results.put(r.getString(1), r.getString(2));
      }
      r.close();
      statement.close();
      EntityLinkingManager.releaseConnection(entityMentionCon);
    } catch (Exception e) {
      System.out.println(sql);
      logger.error(e.getLocalizedMessage());
      e.printStackTrace();
    }

    return results;
  }

  @Override
  public List<String> getAllWikidataIds() throws EntityLinkingDataAccessException {
    Connection con = null;
    Statement stmt = null;
    List<String> wikidataIds = new ArrayList<String>();
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.setFetchSize(100000);
      String sql = "SELECT DISTINCT wikidataid FROM " + DataAccessSQL.ENTITY_METADATA;
      ResultSet rs = stmt.executeQuery(sql);
      int read = 0;
      while (rs.next()) {
        String id = rs.getString("wikidataid");
        wikidataIds.add(id);

        if (++read % 1000000 == 0) {
          logger.info("Read " + read + " entity ids.");
        }
      }
      con.setAutoCommit(true);
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    return wikidataIds;
  }

  @Override
  public LinkedHashMap<Integer, String> getTopEntitiesByRank(int start, int entityNumber, String language) throws EntityLinkingDataAccessException {
    LinkedHashMap<Integer, String> result = new LinkedHashMap<>();
    Connection con = null;
    Statement stmt = null;
    int end = start + entityNumber;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      String languageCondition = "";
      if (language != null && !"en".equals(language)) {
        languageCondition = " where e.entity like '<" + language + "/%'";
      }
      String sql = "select id, entity from (select er.entity as id, e.entity, er.rank from entity_rank er join entity_ids e" +
              " on (er.entity = e.id)" + languageCondition + " order by rank limit " + end + ") as t order by rank desc limit " + entityNumber;
      logger.info("getTopEntitiesByRank: " + sql);
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        String entity = rs.getString("entity");
        int id = rs.getInt("id");
        result.put(id, entity);
      }
      con.setAutoCommit(true);
      return result;
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
  }

  @Override
  public TIntObjectHashMap<EntityType> getEntityClasses(Entities entities) throws EntityLinkingDataAccessException {
    TIntObjectHashMap<EntityType> entityClasses = new TIntObjectHashMap<EntityType>();
    Connection con = null;
    Statement stmt = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      String entitiesQuery = PostgresUtil.getIdQuery(new TIntHashSet(entities.getUniqueIds()));
      String sql = "SELECT entity, entitytype from " + DataAccessSQL.DICTIONARY +
              " WHERE entity IN ( " + entitiesQuery + ")";
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        int entity = rs.getInt("entity");
        int isNE = rs.getInt("entitytype");

        entityClasses.put(entity, EntityType.getNameforDBId(isNE));
      }
      con.setAutoCommit(true);
      return entityClasses;
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
  }

  @Override
  public TIntObjectHashMap<EntityType> getAllEntityClasses() throws EntityLinkingDataAccessException {
    TIntObjectHashMap<EntityType> entityClasses = new TIntObjectHashMap<EntityType>();
    Connection con = null;
    Statement stmt = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      con.setAutoCommit(false);
      stmt = con.createStatement();
      String sql = "SELECT entity, entitytype from " + DataAccessSQL.DICTIONARY;
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        int entity = rs.getInt("entity");
        int isNE = rs.getInt("entitytype");

        entityClasses.put(entity, EntityType.getNameforDBId(isNE));
      }
      con.setAutoCommit(true);
      return entityClasses;
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
  }
}

