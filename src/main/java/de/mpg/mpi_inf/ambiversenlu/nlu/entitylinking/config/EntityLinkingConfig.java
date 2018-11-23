package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.EntityLookupSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main configuration path for global settings.
 */
public class EntityLinkingConfig {

  private static final Logger logger = LoggerFactory.getLogger(EntityLinkingConfig.class);

  public static final String DATAACCESS = "dataaccess";

  public static final String DATAACCESS_CACHE_ENABLE = "dataaccess.cache.enable";

  public static final String DATAACCESS_CACHE_KEYPHRASES_ENABLE = "dataaccess.cache.keyphrases.enable";

  public static final String SYSTEM_LANGUAGE = "languages";

  public static final String GRAPH_ENTITIES_MAXCOUNT = "graph.entities.maxcount";

  public static final String GRAPH_ENTITIES_COMP_THREADS = "graph.entities.comp.threads";

  // Number of Entity contexts to cache (across documents).
  public static final String ENTITIES_CACHE_SIZE = "entities.cache.size";

  // Format is [knowledgebase:typename,knowledgebase:typename, ...etc].
  public static final String ENTITIES_FILTER_TYPES = "entities.filter.types";

  public static final String ENTITIES_CANDIDATE_LOOKUP_SOURCE = "entities.candidate.lookup.source";

  public static final String ENTITIES_CANDIADATE_LOOKUP_MENTION_ISPREFIX = "entities.candiadate.lookup.mention.isprefix";

  /** Maximal number of sentences per chunk. */
  public static final String DOCUMENT_CHUNKING_STRATEGY_FIXEDLENGTH_SIZE = "document.chunking.strategy.fixedlength.size";

  public static final String DICTIONARY_FUZZY_POSTGRES_ENABLE = "dictionary.fuzzy.postgres.enable";

  public static final String DICTIONARY_FUZZY_POSTGRES_MINSIM = "dictionary.fuzzy.postgres.minsim";

  public static final String DICTIONARY_FUZZY_LSH_MINSIM = "dictionary.fuzzy.lsh.minsim";

  public static final String TRAINING_CORPUS = "training.corpus";

  public static final String DATAACESS_CACHE_PRELOAD = "dataacess.cache.preload";

  public static final String LANGUAGE_DETECTOR = "language.detector";

  public static final String LANGUAGE_DETECTOR_PRELOAD = "language.detector.preload";

  public static final String DOCUMENT_PARALLEL_COUNT = "document.parallel.count";

  public static final String WEBSERVICE_NER = "webservice.ner";

  public static String confDir;

  private Properties properties;

  private static EntityLinkingConfig config = null;

  private EntityLinkingConfig() {
    try {
      properties = ConfigUtils.loadProperties("aida.properties");
    } catch (IOException e) {
      throw new RuntimeException("Could not load EntityLinkingConfig: " + e.getLocalizedMessage());
    }
  }

  public static EntityLinkingConfig getInstance() {
    if (config == null) {
      config = new EntityLinkingConfig();
    }
    return config;
  }

  private String getValue(String key) {
    return properties.getProperty(key);
  }

  private void setValue(String key, String value) {
    properties.setProperty(key, value);
  }

  public static String get(String key) {
    String value = EntityLinkingConfig.getInstance().getValue(key);
    if (value == null) {
      logger.error("Missing key in properties file with no default value: " + key);
    }
    return value;
  }

  public static int getAsInt(String key) {
    String value = get(key);
    return Integer.parseInt(value);
  }

  public static Set<String> getLanguages() {
    String prop = EntityLinkingConfig.getInstance().getValue(SYSTEM_LANGUAGE);
    if(prop == null) {
      throw new IllegalArgumentException("Please set the " + SYSTEM_LANGUAGE + " property in the aida.properties config file");
    } else {
      Set<String> languages = Arrays.stream(prop.split(",")).collect(Collectors.toSet());
      return languages;
    }
  }

  public static int getFixedChunkSize() {
    return Integer.parseInt(get(DOCUMENT_CHUNKING_STRATEGY_FIXEDLENGTH_SIZE));
  }

  public static EntityLookupSettings.LOOKUP_SOURCE getEntityLookupSource() {
    return EntityLookupSettings.LOOKUP_SOURCE.valueOf(get(ENTITIES_CANDIDATE_LOOKUP_SOURCE));
  }

  public static boolean getBoolean(String key) {
    return Boolean.parseBoolean(get(key));
  }

  public static double getDouble(String key) {
    return Double.parseDouble(get(key));
  }

  public static void set(String key, String value) {
    EntityLinkingConfig.getInstance().setValue(key, value);
  }

  public static Type[] getEntitiesFilterTypes() {
    String filteringTypesStr = get(ENTITIES_FILTER_TYPES);
    if (filteringTypesStr == null || filteringTypesStr.equals("")) {
      return null;
    }
    String[] filteringTypeStrList = filteringTypesStr.split(",");
    Type[] filteringTypes = new Type[filteringTypeStrList.length];
    int i = 0;
    for (String filteringTypeStr : filteringTypeStrList) {
      int colonIndex = filteringTypeStr.indexOf(":");
      if (colonIndex < 0) {
        logger.error("Wrong filtering types string format in AIDA config file");
      }
      String knowledgebase = filteringTypeStr.substring(0, colonIndex);
      String typename = filteringTypeStr.substring(colonIndex + 1);
      filteringTypes[i++] = new Type(knowledgebase, typename);
    }
    return filteringTypes;
  }

  public static void shutdown() {
    config = null;
  }
}
