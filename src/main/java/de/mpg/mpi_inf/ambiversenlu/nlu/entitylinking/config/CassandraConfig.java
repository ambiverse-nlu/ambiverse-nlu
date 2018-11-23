package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CassandraConfig {

  private static final Logger logger = LoggerFactory.getLogger(CassandraConfig.class);

  public static final String FILE_NAME = "cassandra.properties";

  public static final String NER_FILE_NAME = "ner_cassandra.properties";

  public static final String PAGE_SIZE = "page.size";

  public static final String HOST = "host";

  public static final String LOAD_BALANCING_POLICY = "load.balancing.policy";

  public static final String REPLICATION = "replication.factor";

  public static final String KEYSPACE = "keyspace";

  public static final String GENERIC_PRIMARY_KEY = "k";

  public static final String GENERIC_VALUE = "v";

  private static final Map<String, CassandraConfig> configs = new HashMap<>();
  private static final String[] configNames = new String[] {CassandraConfig.FILE_NAME, CassandraConfig.NER_FILE_NAME};

  private Properties properties;

  private static CassandraConfig instance(String fileName) {
//    loading all CassandraConfigs at once
    for (String configName : configNames) {
      configs.put(configName, new CassandraConfig(configName));
    }
    return configs.get(fileName);
  }

  private CassandraConfig(String fileName) {
    try {
      properties = ConfigUtils.loadProperties(fileName);
    } catch (IOException e) {
      throw new RuntimeException("Could not load CassandraConfig: " + e.getLocalizedMessage());
    }
  }

  private String getValue(String key) {
    return properties.getProperty(key);
  }

  private void setValue(String key, String value) {
    properties.setProperty(key, value);
  }

  private boolean hasValue(String key) {
    return properties.containsKey(key);
  }

  public static String get(String key) {
    return get(FILE_NAME, key);
  }

  public static String get(String fileName, String key) {
    String value = instance(fileName).getValue(key);
    if (value == null) {
      logger.error("Missing key in properties file with no default value: " + key);
    }
    return value;
  }

  public static void set(String key, String value) {
    instance(FILE_NAME).setValue(key, value);
  }

  public static void set(String fileName, String key, String value) {
    instance(fileName).setValue(key, value);
  }

  public static int getInt(String key) {
    return Integer.parseInt(get(key));
  }

  public static boolean getBoolean(String key) {
    return Boolean.parseBoolean(get(key));
  }

  public static double getDouble(String key) {
    return Double.parseDouble(get(key));
  }
}
