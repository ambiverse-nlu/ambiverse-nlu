package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;

class DataAccessSQLCache {

  private static long entityKeyphrasesCacheHits;

  private static long entityKeyphrasesCacheMisses;

  private static long entityKeywordsCacheHits;

  private static long entityKeywordsCacheMisses;

  private Logger logger_ = LoggerFactory.getLogger(DataAccessSQLCache.class);

  private static class DataAccessSQLCacheHolder {

    public static DataAccessSQLCache cache = new DataAccessSQLCache();
  }

  public static DataAccessSQLCache singleton() {
    return DataAccessSQLCacheHolder.cache;
  }

  class EntityKeyphraseData {

    final int keyphrase;

    final double weight;

    final int source;

    public EntityKeyphraseData(int keyphrase, double weight, int source) {
      super();
      this.keyphrase = keyphrase;
      this.weight = weight;
      this.source = source;
    }

    public EntityKeyphraseData(EntityKeyphraseData ekd) {
      super();
      this.keyphrase = ekd.keyphrase;
      this.weight = ekd.weight;
      this.source = ekd.source;
    }
  }

  class EntityKeywordsData {

    final int keyword;

    final double weight;

    public EntityKeywordsData(int keyword, double weight) {
      super();
      this.keyword = keyword;
      this.weight = weight;
    }

    public EntityKeywordsData(EntityKeywordsData ekd) {
      super();
      this.keyword = ekd.keyword;
      this.weight = ekd.weight;
    }
  }

  class CachingHashMap<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 4693725887215865592L;

    private int maxEntities_;

    public CachingHashMap(int maxEntities) {
      // Initialize with half the maximum capacity.
      super(maxEntities / 2, 0.75f, true);
      maxEntities_ = maxEntities;
      if (maxEntities > 0) {
        logger_.info("Caching up to " + maxEntities + " entities per query type");
      }
    }

    @Override protected boolean removeEldestEntry(Entry<K, V> eldest) {
      return size() > maxEntities_;
    }
  }

  private Map<String, CachingHashMap<Integer, List<EntityKeyphraseData>>> entityKeyphrasesCaches = new HashMap<String, CachingHashMap<Integer, List<EntityKeyphraseData>>>();

  private Map<String, CachingHashMap<Integer, List<EntityKeywordsData>>> entityKeywordsCaches = new HashMap<String, CachingHashMap<Integer, List<EntityKeywordsData>>>();

  public KeytermsCache<EntityKeyphraseData> getEntityKeyphrasesCache(Entities entities, TObjectIntHashMap<String> keyphraseSrcName2Id,
      Map<String, Double> keyphraseSourceWeights, double minKeyphraseWeight, int maxEntityKeyphraseCount, boolean useSources) throws EntityLinkingDataAccessException {
    String querySignature = createEntityKeyphrasesQuerySignature(keyphraseSourceWeights, minKeyphraseWeight, maxEntityKeyphraseCount);
    KeytermsCache<EntityKeyphraseData> kpc = new KeytermsCache<EntityKeyphraseData>();
    CachingHashMap<Integer, List<EntityKeyphraseData>> queryCache = null;
    synchronized (entityKeyphrasesCaches) {
      queryCache = entityKeyphrasesCaches.get(querySignature);
      if (queryCache == null) {
        int maxEntities = EntityLinkingConfig.getAsInt(EntityLinkingConfig.ENTITIES_CACHE_SIZE);
        queryCache = new CachingHashMap<Integer, List<EntityKeyphraseData>>(maxEntities);
        entityKeyphrasesCaches.put(querySignature, queryCache);
      }
    }
    Integer id = RunningTimer.recordStartTime("CacheAccess:getEntityKeyphraseCache");
    Set<Integer> missingEntities = new HashSet<Integer>();
    for (int eId : entities.getUniqueIds()) {
      List<EntityKeyphraseData> ekds = queryCache.get(eId);
      if (ekds == null) {
        missingEntities.add(eId);
        ++entityKeyphrasesCacheMisses;
      } else {
        // Cache hit.
        for (EntityKeyphraseData ekd : ekds) {
          kpc.add(eId, ekd);
        }
        ++entityKeyphrasesCacheHits;
      }
    }

    logger_.debug("Keyphrase Cache hits/misses :" + entityKeyphrasesCacheHits + "/" + entityKeyphrasesCacheMisses);
    for (Entry<String, CachingHashMap<Integer, List<EntityKeyphraseData>>> c : entityKeyphrasesCaches.entrySet()) {
      logger_.debug("Keyphrase Cache size (" + c.getKey() + "):" + c.getValue().size());
    }

    if (missingEntities.isEmpty()) {
      // All entities are cached.
      RunningTimer.recordEndTime("CacheAccess:getEntityKeyphraseCache", id);
      return kpc;
    }

    logger_.debug("Accessing DB for missing entities.. ");
    Integer cId = RunningTimer.recordStartTime("LoadMissingEntities");
    Connection con = null;
    Statement statement = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      String entityQueryString = StringUtils.join(missingEntities, ",");
      statement = con.createStatement();

      StringBuilder sql = new StringBuilder();
      sql.append("SELECT entity, keyphrase, weight");
      if (useSources) {
        sql.append(", source");
      }
      sql.append(" FROM ");
      if (maxEntityKeyphraseCount > 0) {
        sql.append("(SELECT ROW_NUMBER() OVER").append(" (PARTITION BY entity ORDER BY weight DESC) AS p,")
            .append(" ek.entity, ek.keyphrase, ek.weight").append(" FROM ").append(DataAccessSQL.ENTITY_KEYPHRASES).append(" ek")
            .append(" WHERE entity IN (").append(entityQueryString).append(")");
      } else {
        sql.append(DataAccessSQL.ENTITY_KEYPHRASES).append(" WHERE entity IN (").append(entityQueryString).append(")");
      }
      if (useSources) {
        for (Entry<String, Double> sourceWeight : keyphraseSourceWeights.entrySet()) {
          if (sourceWeight.getValue() == 0.0) {
            sql.append(" AND source<>").append(keyphraseSrcName2Id.get(sourceWeight.getKey()));
          }
        }
      }
      if (minKeyphraseWeight > 0.0) {
        sql.append(" AND weight > ").append(minKeyphraseWeight);
      }
      // Close nested query when limiting number of keyphrases per entity.
      if (maxEntityKeyphraseCount > 0) {
        sql.append(" ) g WHERE g.p <= ").append(maxEntityKeyphraseCount);
      }

      TIntObjectHashMap<List<EntityKeyphraseData>> entityKeyphrases = new TIntObjectHashMap<List<EntityKeyphraseData>>();
      TIntObjectHashMap<TIntDoubleHashMap> entityKeyphrasesWeights = new TIntObjectHashMap<TIntDoubleHashMap>();
      TIntObjectHashMap<TIntIntHashMap> entityKeyphraseSource = new TIntObjectHashMap<TIntIntHashMap>();
      TIntObjectHashMap<TIntObjectHashMap<List<Integer>>> entityKeyphrasesTokens = new TIntObjectHashMap<TIntObjectHashMap<List<Integer>>>();

      ResultSet rs = statement.executeQuery(sql.toString());
      while (rs.next()) {
        int entityId = rs.getInt("entity");
        List<EntityKeyphraseData> lstEntityKeyphrases = null;
        if (!entityKeyphrases.containsKey(entityId)) {
          lstEntityKeyphrases = new ArrayList<EntityKeyphraseData>();
          entityKeyphrases.put(entityId, lstEntityKeyphrases);
        } else {
          lstEntityKeyphrases = entityKeyphrases.get(entityId);
        }

        // maintain entity-keyphrase-weight relation
        TIntDoubleHashMap keyphraseWeights = null;
        if (entityKeyphrasesWeights.containsKey(entityId)) {
          keyphraseWeights = entityKeyphrasesWeights.get(entityId);
        } else {
          keyphraseWeights = new TIntDoubleHashMap();
          entityKeyphrasesWeights.put(entityId, keyphraseWeights);
        }

        int keyphrase = rs.getInt("keyphrase");
        double keyphraseWeight = rs.getDouble("weight");
        if (!keyphraseWeights.containsKey(keyphrase)) {
          keyphraseWeights.put(keyphrase, keyphraseWeight);
        }

        // maintain entity-keyphrase-tokens relation
        TIntObjectHashMap<List<Integer>> keyphraseTokens = null;
        // DataAccessCache.singleton().getKeyphraseTokens(entityId);
        if (entityKeyphrasesTokens.contains(entityId)) {
          keyphraseTokens = entityKeyphrasesTokens.get(entityId);
        } else {
          keyphraseTokens = new TIntObjectHashMap<List<Integer>>();
          entityKeyphrasesTokens.put(entityId, keyphraseTokens);
        }

        List<Integer> kpTokens = null;
        if (!keyphraseTokens.containsKey(keyphrase)) {
          kpTokens = new ArrayList<Integer>();
          keyphraseTokens.put(keyphrase, kpTokens);
        } else {
          kpTokens = keyphraseTokens.get(keyphrase);
        }

        int[] kpTokensArray = DataAccessCache.singleton().getKeyphraseTokens(keyphrase);

        for (int kpTok : kpTokensArray) {
          kpTokens.add(kpTok);
        }

        int source = 0;
        if (useSources) {
          // maintain entity-keyphrase-source relation
          TIntIntHashMap keyphraseSource = null;
          if (entityKeyphraseSource.contains(entityId)) {
            keyphraseSource = entityKeyphraseSource.get(entityId);
          } else {
            keyphraseSource = new TIntIntHashMap();
            entityKeyphraseSource.put(entityId, keyphraseSource);
          }

          source = rs.getInt("source");
          if (!keyphraseSource.containsKey(keyphrase)) {
            keyphraseSource.put(keyphrase, source);
          }
        }
      } // end while

      for (TIntObjectIterator<TIntDoubleHashMap> outerItr = entityKeyphrasesWeights.iterator(); outerItr.hasNext(); ) {
        outerItr.advance();
        int entityId = outerItr.key();
        TIntDoubleHashMap keyphrasesWeights = outerItr.value();
        for (TIntDoubleIterator innerItr = keyphrasesWeights.iterator(); innerItr.hasNext(); ) {
          innerItr.advance();
          int keyphrase = innerItr.key();
          double keyphraseWeight = innerItr.value();
          //          List<Double> lstTokWeights = entityKeyphrasesTokenWeights.get(entityId).get(keyphrase);
          //          Double[] tokenWeights = lstTokWeights.toArray(new Double[lstTokWeights.size()]);

          int source = 0;
          if (useSources) {
            source = entityKeyphraseSource.get(entityId).get(keyphrase);
          }
          EntityKeyphraseData ekd = new EntityKeyphraseData(keyphrase, keyphraseWeight, source);
          kpc.add(entityId, ekd);

          List<EntityKeyphraseData> entityCache = entityKeyphrases.get(entityId);
          if (entityCache == null) {
            entityCache = new ArrayList<EntityKeyphraseData>();
            entityKeyphrases.put(entityId, entityCache);
          }
          entityCache.add(ekd);
        }
      }

      rs.close();
      statement.close();

      addToEntityKeyphrasesCache(querySignature, entityKeyphrases);

    } catch (Exception e) {
      logger_.error(e.getLocalizedMessage());
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    RunningTimer.recordEndTime("LoadMissingEntities", cId);
    RunningTimer.recordEndTime("CacheAccess:getEntityKeyphraseCache", id);
    return kpc;
  }

  public KeytermsCache<EntityKeywordsData> getEntityKeywordsCache(Entities entities, Map<String, Double> keyphraseSourceWeights,
      TIntObjectHashMap<int[]> entityKeyphrases, TIntObjectHashMap<int[]> keyphraseTokens, double minKeyphraseWeight, int maxEntityKeyphraseCount) throws EntityLinkingDataAccessException {
    String querySignature = createEntityKeyphrasesQuerySignature(keyphraseSourceWeights, minKeyphraseWeight, maxEntityKeyphraseCount);
    KeytermsCache<EntityKeywordsData> kwc = new KeytermsCache<EntityKeywordsData>();
    CachingHashMap<Integer, List<EntityKeywordsData>> queryCache = null;
    synchronized (entityKeywordsCaches) {
      queryCache = entityKeywordsCaches.get(querySignature);
      if (queryCache == null) {
        int maxEntities = EntityLinkingConfig.getAsInt(EntityLinkingConfig.ENTITIES_CACHE_SIZE);
        queryCache = new CachingHashMap<Integer, List<EntityKeywordsData>>(maxEntities);
        entityKeywordsCaches.put(querySignature, queryCache);
      }
    }
    Set<Integer> missingEntities = new HashSet<Integer>();
    for (int eId : entities.getUniqueIds()) {
      List<EntityKeywordsData> ekds = queryCache.get(eId);
      if (ekds == null) {
        missingEntities.add(eId);
        ++entityKeywordsCacheMisses;
      } else {
        // Cache hit.
        for (EntityKeywordsData ekd : ekds) {
          kwc.add(eId, ekd);
        }
        // Move element to the front so that CachingHashMap can keep track.        
        queryCache.put(eId, ekds);
        ++entityKeywordsCacheHits;
      }
    }

    logger_.debug("Keyword Cache hits/misses :" + entityKeywordsCacheHits + "/" + entityKeywordsCacheMisses);
    for (Entry<String, CachingHashMap<Integer, List<EntityKeywordsData>>> c : entityKeywordsCaches.entrySet()) {
      logger_.debug("Keyword Cache size (" + c.getKey() + "):" + c.getValue().size());
    }

    if (missingEntities.isEmpty()) {
      // All entities are cached.
      return kwc;
    }

    boolean needKeywordCleaning = false;
    if (minKeyphraseWeight > 0.0 || maxEntityKeyphraseCount > 0) {
      needKeywordCleaning = true;
    }

    TIntObjectHashMap<TIntSet> entityKeyphraseTokens = new TIntObjectHashMap<TIntSet>();
    if (needKeywordCleaning) {
      for (TIntObjectIterator<int[]> itr = entityKeyphrases.iterator(); itr.hasNext(); ) {
        itr.advance();
        int entityId = itr.key();
        int[] keyphrases = itr.value();
        TIntSet tokSet = new TIntHashSet();
        entityKeyphraseTokens.put(entityId, tokSet);
        for (int keyphrase : keyphrases) {
          tokSet.addAll(keyphraseTokens.get(keyphrase));
        }
      }
    }

    Integer id = RunningTimer.recordStartTime("KeywordRetr");
    Connection con = null;
    Statement statement = null;
    try {
      con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      String entityQueryString = StringUtils.join(missingEntities, ",");
      statement = con.createStatement();

      StringBuilder sql = new StringBuilder();
      sql.append("SELECT entity, keyword, weight");
      sql.append(" FROM ");
      sql.append(DataAccessSQL.ENTITY_KEYWORDS).append(" WHERE entity IN (").append(entityQueryString).append(")");

      TIntObjectHashMap<List<EntityKeywordsData>> entityKeywords = new TIntObjectHashMap<List<EntityKeywordsData>>();
      ResultSet rs = statement.executeQuery(sql.toString());

      while (rs.next()) {
        int entityId = rs.getInt("entity");
        int keyword = rs.getInt("keyword");

        if (needKeywordCleaning) {
          TIntSet entityTokens = entityKeyphraseTokens.get(entityId);
          if (!entityTokens.contains(keyword)) {
            continue;
          }
        }

        double keywordWeight = rs.getDouble("weight");
        kwc.add(entityId, new EntityKeywordsData(keyword, keywordWeight));
        List<EntityKeywordsData> entityCache = entityKeywords.get(entityId);
        if (entityCache == null) {
          entityCache = new ArrayList<EntityKeywordsData>();
          entityKeywords.put(entityId, entityCache);
        }
        entityCache.add(new EntityKeywordsData(keyword, keywordWeight));
      } // end while

      addToEntityKeywordsCache(querySignature, entityKeywords);

    } catch (Exception e) {
      logger_.error(e.getLocalizedMessage());
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(con);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    RunningTimer.recordEndTime("KeywordRetr", id);
    return kwc;
  }

  private synchronized void addToEntityKeyphrasesCache(String querySignature, TIntObjectHashMap<List<EntityKeyphraseData>> entityKeyphrases) {
    CachingHashMap<Integer, List<EntityKeyphraseData>> queryCache = entityKeyphrasesCaches.get(querySignature);
    if (queryCache == null) {
      int maxEntities = EntityLinkingConfig.getAsInt(EntityLinkingConfig.ENTITIES_CACHE_SIZE);
      queryCache = new CachingHashMap<Integer, List<EntityKeyphraseData>>(maxEntities);
      entityKeyphrasesCaches.put(querySignature, queryCache);
    }

    for (TIntObjectIterator<List<EntityKeyphraseData>> itr = entityKeyphrases.iterator(); itr.hasNext(); ) {
      itr.advance();
      int entityId = itr.key();
      List<EntityKeyphraseData> keyphrases = itr.value();
      queryCache.put(entityId, keyphrases);
    }
  }

  private synchronized void addToEntityKeywordsCache(String querySignature, TIntObjectHashMap<List<EntityKeywordsData>> entityKeywords) {
    CachingHashMap<Integer, List<EntityKeywordsData>> queryCache = entityKeywordsCaches.get(querySignature);
    if (queryCache == null) {
      int maxEntities = EntityLinkingConfig.getAsInt(EntityLinkingConfig.ENTITIES_CACHE_SIZE);
      queryCache = new CachingHashMap<Integer, List<EntityKeywordsData>>(maxEntities);
      entityKeywordsCaches.put(querySignature, queryCache);
    }

    for (TIntObjectIterator<List<EntityKeywordsData>> itr = entityKeywords.iterator(); itr.hasNext(); ) {
      itr.advance();
      int entityId = itr.key();
      List<EntityKeywordsData> keyphrases = itr.value();
      queryCache.put(entityId, keyphrases);
    }
  }

  private String createEntityKeyphrasesQuerySignature(Map<String, Double> keyphraseSourceWeights, double minKeyphraseWeight,
      int maxEntityKeyphraseCount) {
    StringBuilder sb = new StringBuilder();
    if (keyphraseSourceWeights != null) {
      for (Entry<String, Double> e : keyphraseSourceWeights.entrySet()) {
        sb.append(e.getKey());
        sb.append(":");
        sb.append(e.getValue());
        sb.append("_");
      }
    }
    sb.append(minKeyphraseWeight);
    sb.append("_");
    sb.append(maxEntityKeyphraseCount);
    return sb.toString();
  }
}
