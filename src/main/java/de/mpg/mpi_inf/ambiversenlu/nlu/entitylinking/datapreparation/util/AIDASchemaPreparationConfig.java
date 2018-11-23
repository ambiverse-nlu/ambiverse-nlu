package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DatabaseKeyValueStore;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class AIDASchemaPreparationConfig {

  private static final Logger logger = LoggerFactory.getLogger(AIDASchemaPreparationConfig.class);

  public static final String KB_NAME = "kb.name";

  // Can be any DataPrepConf.
  public static final String PREPARATION_CONFIG = "preparation.config";

  public static final String YAGO3_FILE = "yago3.file";

  public static final String GENERIC_FILE = "generic.file";

  public static final String DATABASE_CREATE_KEYWORDS = "database.create.keywords";

  public static final String DATABASE_CREATE_BIGRAMS = "database.create.bigrams";

  public static final String DATABASE_UNIT_CREATION_THRESHOLD_TOPK = "database.unit.creation.threshold.topk";

  public static final String DATABASE_UNIT_CREATION_THRESHOLD_MIN_WEIGHT = "database.unit.creation.threshold.min.weight";

  /**
   * Minimum count an anchor has to link to an entity to be included in the dictionary.
   */
  public static final String DICTIONARY_ANCHORS_MINOCCURRENCE = "dictionary.anchors.minoccurrence";

  public static final String KNOWNER_CREATENEW = "knowner.createnew";

  private Properties properties;

  private static AIDASchemaPreparationConfig config = null;

  private AIDASchemaPreparationConfig() {
    try {
      properties = ConfigUtils.loadProperties("preparation.properties");
    } catch (IOException e) {
      logger.error("Could not load EntityLinkingConfig: " + e.getLocalizedMessage());
    }
  }

  private static AIDASchemaPreparationConfig getInstance() {
    if (config == null) {
      config = new AIDASchemaPreparationConfig();
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
    String value = getInstance().getValue(key);
    if (value == null) {
      logger.error("Missing key in properties file with no default value: " + key);
    }
    return value;
  }

  public static boolean getBoolean(String key) {
    if (key == null) {
      throw new NullPointerException();
    }
    return Boolean.parseBoolean(get(key));
  }

  public static Integer getInteger(String key) {
    return Integer.parseInt(get(key));
  }

  public static Double getDouble(String key) {
    return Double.parseDouble(get(key));
  }

  public static void set(String key, String value) {
    AIDASchemaPreparationConfig.getInstance().setValue(key, value);
  }

  public static String getConfigurationName() {
    return get(PREPARATION_CONFIG);
  }

  public static String getKBName() {
    return get(KB_NAME);
  }
}
