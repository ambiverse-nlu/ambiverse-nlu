package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.CassandraConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class DataAccessCache {

  private static final Logger logger = LoggerFactory.getLogger(DataAccessCache.class);

  private static final String DATABASE_AIDA_CONFIG_CACHE = "database_aida.cache";

  private static final String CASSANDRA_CONFIG_CACHE = "cassandra_config.cache";

  private DataAccessIntIntCacheTarget wordExpansion;

  private DataAccessIntIntCacheTarget wordContraction;

  private DataAccessIntIntCacheTarget[] unitCounts;

  private DataAccessKeyphraseTokensCacheTarget keyphraseTokens;

  private DataAccessKeyphraseSourcesCacheTarget keyphraseSources;
  
  private DataAccessEntitiesCacheTarget entities;

  private static DataAccessCache cache;

  private static synchronized void createCache() throws EntityLinkingDataAccessException {
    logger.debug("Creating caches...");
    if (cache == null) {
      cache = new DataAccessCache();
    }
  }

  public static DataAccessCache singleton() throws EntityLinkingDataAccessException {
    logger.debug("Creating singleton...");
    if (cache == null) {
      logger.debug("Loading caches");
      createCache();
    }
    return cache;
  }

  private  DataAccessCache() throws EntityLinkingDataAccessException {
    List<DataAccessCacheTarget> cacheTargets = new ArrayList<>();
    wordExpansion = new DataAccessWordExpansionCacheTarget();
    cacheTargets.add(wordExpansion);
    wordContraction = new DataAccessWordContractionCacheTarget();
    cacheTargets.add(wordContraction);
    unitCounts = new DataAccessUnitCountCacheTarget[UnitType.values().length];
    for (UnitType unitType : UnitType.values()) {
      DataAccessUnitCountCacheTarget target = new DataAccessUnitCountCacheTarget(unitType);
      unitCounts[unitType.ordinal()] = target;
      cacheTargets.add(target);
    }
    keyphraseTokens = new DataAccessKeyphraseTokensCacheTarget();
    keyphraseSources = new DataAccessKeyphraseSourcesCacheTarget();
    if (EntityLinkingConfig.getBoolean(EntityLinkingConfig.DATAACCESS_CACHE_KEYPHRASES_ENABLE)) {
      cacheTargets.add(keyphraseTokens);
      cacheTargets.add(keyphraseSources);
    }
    entities = new DataAccessEntitiesCacheTarget();
    cacheTargets.add(entities);
    
    logger.info("Loading local db caches.");

    long start = System.currentTimeMillis();

    if (EntityLinkingConfig.getBoolean(EntityLinkingConfig.DATAACCESS_CACHE_ENABLE)) {
      // Determine cache state.
      boolean needsCacheCreation = true;
      try {
        needsCacheCreation = determineCacheCreation();
      } catch (FileNotFoundException e1) {
        logger.error("Did not find file: " + e1.getLocalizedMessage());
        e1.printStackTrace();
      } catch (IOException e1) {
        logger.error("Exception reading file: " + e1.getLocalizedMessage());
        e1.printStackTrace();
      }
      for (DataAccessCacheTarget target : cacheTargets) {
        try {
          target.createAndLoadCache(needsCacheCreation);
        } catch (IOException e) {
          target.loadFromDb();
          logger.warn("Could not read cache file, reading from DB.", e);
        }
      }
      if (needsCacheCreation) {
        try {
          Properties currentConfig = null;
          File cachedConfigFile = null;
          switch (DataAccess.getAccessType()) {
            case testing:
            case sql:
              currentConfig = ConfigUtils.loadProperties(EntityLinkingManager.databaseAidaConfig);
              cachedConfigFile = new File(DATABASE_AIDA_CONFIG_CACHE);
              break;
            case cassandra:
              currentConfig = ConfigUtils.loadProperties(CassandraConfig.FILE_NAME);
              cachedConfigFile = new File(CASSANDRA_CONFIG_CACHE);
              break;

          }
          currentConfig.store(new BufferedOutputStream(new FileOutputStream(cachedConfigFile)), "cached aida data config");
        } catch (IOException e) {
          logger.error("Could not write config: " + e.getLocalizedMessage());
          e.printStackTrace();
        }
      }
    } else {
      logger.info("Loading data for local caching from DB.");
      for (DataAccessCacheTarget target : cacheTargets) {
        target.loadFromDb();
      }
    }

    long dur = System.currentTimeMillis() - start;

    logger.info("Loaded local db caches in " + dur/1_000 + "s.");
  }

  private boolean determineCacheCreation() throws IOException {
    File cachedDBConfigFile, cachedCassandraConfigFile;

    List<File> allCacheFiles = Arrays
        .asList(cachedDBConfigFile = new File(DATABASE_AIDA_CONFIG_CACHE),
                cachedCassandraConfigFile = new File(CASSANDRA_CONFIG_CACHE));

    logger.debug("Determining the cache creation...");
    logger.debug("Cassandra Config file {}.", cachedCassandraConfigFile.getName());
    File curConfigFile;
    switch (DataAccess.getAccessType()) {
      case testing:
      case sql:
        curConfigFile = cachedDBConfigFile;
        break;
      case cassandra:
        curConfigFile = cachedCassandraConfigFile;
        break;
      default:
        curConfigFile = null;
    }

    if (curConfigFile == null || !curConfigFile.exists()) {
      logger.info("Cache files not found or database system has been changed; caches will be created.");
      allCacheFiles.forEach(File::delete);
      return true;
    }

    Properties currentConfig = null;
    switch (DataAccess.getAccessType()) {
      case testing:
      case sql:
        currentConfig = ConfigUtils.loadProperties(EntityLinkingManager.databaseAidaConfig);
        break;
      case cassandra:
        currentConfig = ConfigUtils.loadProperties(CassandraConfig.FILE_NAME);
        break;
    }

    Properties cachedConfig = new Properties();
    cachedConfig.load(new BufferedInputStream(new FileInputStream(curConfigFile)));

    if (!currentConfig.equals(cachedConfig)) {
      logger.info("Cache files exist, but config has been changed since it was created; data access is unavoidable!");
      //there is a change in the DB config
      // do a clean up and require a DB access
      allCacheFiles.forEach(File::delete);
      return true;
    }
    return false;
  }

  public int expandTerm(int wordId) {
    try {
      return wordExpansion.getData(wordId);
    } catch (IllegalArgumentException e) {
      logger.warn("Returning same id for word id without expansions " + wordId);
      return wordId;
    }
  }

  public int contractTerm(int wordId) {
    return wordContraction.getData(wordId);
  }

  public int getKeywordCount(int wordId) {
    try {
      return unitCounts[UnitType.KEYWORD.ordinal()].getData(wordId);
    } catch (IllegalArgumentException e) {
      // For whatever reason it seems that the AIDA db is  inconsistent.
      // There are keywords that are part of keyphrases NOT assigned to any entity -
      // which in effect cannot have a count.
      // This creates a problem when ExternalEntitiesContext tries to
      // get a count for existing words (which then do not have any).
      // As a very hacky workaround, return 1 for now.
      logger.warn("Returning count 1 for word id without count " + wordId);
      return 1;
    }
  }

  public int getUnitCount(int unitId, UnitType unitType) {
    return unitCounts[unitType.ordinal()].getData(unitId);
  }

  public int[] getKeyphraseTokens(int wordId) {
    return keyphraseTokens.getData(wordId);
  }

  public int getKeyphraseSourceId(String source) {
    return keyphraseSources.getData(source);
  }

  public TIntObjectHashMap<int[]> getAllKeyphraseTokens() {
    return keyphraseTokens.getAllData();
  }

  public TObjectIntHashMap<String> getAllKeyphraseSources() {
    return keyphraseSources.getAllData();
  }
  
  public TIntObjectHashMap<EntityType> getAllEntityClasses() {
    return entities.getAllData();
  }
  
  public TIntObjectHashMap<EntityType> getEntityClasses(int[] entityIds) {
    TIntObjectHashMap<EntityType> results = new TIntObjectHashMap<>();
    for (int entityId : entityIds) {
      if (entities.getAllData().containsKey(entityId)) {
        results.put(entityId, entities.getAllData().get(entityId));
      }
    }
    return results;
  }
}
