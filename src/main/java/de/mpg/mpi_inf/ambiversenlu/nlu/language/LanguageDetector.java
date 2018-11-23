package de.mpg.mpi_inf.ambiversenlu.nlu.language;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig.LANGUAGE_DETECTOR;

public interface LanguageDetector {

  public Language detectLanguage(String text) throws AidaUnsupportedLanguageException;

  public static class LanguageDetectorHolder {

    private static LanguageDetector instance = null;

    private static Logger slogger_ = LoggerFactory.getLogger(LanguageDetectorHolder.class);

    public static void init() {
      getLanguageDetector();
    }

    public static LanguageDetector getLanguageDetector() {
      if (instance == null) {
        slogger_.info("Loading language detection library");
        instance = LanguageDetectorFactory.getLanguageDetector();
      }
      return instance;
    }
  }

  public static class LanguageDetectorFactory {

    public static LanguageDetector getLanguageDetector() {
      if (EntityLinkingConfig.get(LANGUAGE_DETECTOR).equalsIgnoreCase("OPTIMAIZE")) {
        return new OptimaizeLanguageDetector();
      } else {
        return new OptimaizeLanguageDetector();
      }
    }
  }

}
