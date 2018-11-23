package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Utility methods for managing configuration through properties.
 */
public class ConfigUtils {

  private static Logger logger_ = LoggerFactory.getLogger(ConfigUtils.class);

  public static final String ENV_CONF_FLAG = "AIDA_CONF";

  public static final String CONF_FLAG = "aida.conf";

  public static final String CONF_PATH = "aida_conf.properties";

  public static final String DEFAULT_CONF = "default";

  public static final String DEFAULT_EVALUATION = "evaluation";

  private static boolean loadedOnce = false;

  private static String CONF_OVERRIDE;

  public static void setConfOverride(String override) {
    CONF_OVERRIDE = override;
  }

  public static Properties loadProperties(String fileName) throws IOException {

    // Load the defaults.
    Properties defaultProperties;
    try {
      String defaultPath = DEFAULT_CONF + "/" + fileName;
      defaultProperties = ClassPathUtils.getPropertiesFromClasspath(defaultPath);
    } catch (IOException e) {
      throw new IOException("Default settings file missing. This should not happen!");
    }

    // Conf can override defaults.
    String conf = null;

    // Try if aida_env.properties specifies non-default aida.conf
    Properties envProperties = ClassPathUtils.getPropertiesFromClasspath(CONF_PATH);
    if (envProperties != null && envProperties.containsKey(CONF_FLAG)) {
      String env = envProperties.getProperty(CONF_FLAG);
      if (!env.equals(DEFAULT_CONF)) {
        conf = env;
        if (!loadedOnce) {
          logger_.info("Configuration '" + conf + "' [read from " + CONF_PATH + "].");
        }
      }
    }

    // Override by Environment
    if (System.getenv().containsKey(ENV_CONF_FLAG)) {
      conf = System.getenv(ENV_CONF_FLAG);
      if (!loadedOnce) {
        logger_.info("Configuration '" + conf + "' [set by environment " + ENV_CONF_FLAG + "].");
      }
    }

    // Override by System property.
    if (System.getProperty(CONF_FLAG) != null) {
      conf = System.getProperty(CONF_FLAG);
      if (!loadedOnce) {
        logger_.info("Configuration '" + conf + "' [set by -D" + CONF_FLAG + "].");
      }
    }

    // Override by CONF_OVERRIDE private variable.
    if (CONF_OVERRIDE != null) {
      conf = CONF_OVERRIDE;
    }

    if (conf == null) {
      throw new RuntimeException("AIDA configuration should be specified as enviromental variable (AIDA_CONF) or as a system property aida.conf");
    }

    EntityLinkingConfig.confDir = conf;

    // Check if the configuration specified actually exists (i.e. the aida.properties is there)
    String aidaProperties = conf + "/aida.properties";
    if (!ClassPathUtils.checkResource(aidaProperties)) {
      throw new RuntimeException("The specified configuration '" + conf + "' does not exist. "
          + "Make sure the directory is present on the classpath and contains the 'aida.properties' file.");
    }

    Properties confProperties;
    try {
      String confPath = conf + "/" + fileName;
      confProperties = ClassPathUtils.getPropertiesFromClasspath(confPath, defaultProperties);
      if (confProperties == null) {
        // No specific overwrites found, use defaults.
        logger_.debug("Config '" + conf + "' was specified but did not contain overwrites for '" + fileName + "'.");
        return defaultProperties;
      }
    } catch (IOException e) {
      throw new IOException("Could not read '" + fileName + "' from configuration '" + conf + "'.");
    }

    loadedOnce = true;

    return confProperties;
  }
}
