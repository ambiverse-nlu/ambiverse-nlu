package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.nlp;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Utility methods for dealing with language-specific proper nouns.
 */
public class ProperNounManager {

  Logger logger_ = LoggerFactory.getLogger(ProperNounManager.class);

  /**
   * Singleton stuff.
   */
  private static class ProperNounManagerHolder {

    public static ProperNounManager pnm = new ProperNounManager();
  }

  /**
   * Singleton stuff.
   */
  public static ProperNounManager singleton() {
    return ProperNounManagerHolder.pnm;
  }

  private static final String basePath_ = "tokens/pos/propernoun/";

  private Map<Language, Set<String>> languageProperNounTags_ = new HashMap<>();

  private Set<String> getLanguageTags(Language language) {
    // Cache loading of language tags.
    Set<String> properNounTags = languageProperNounTags_.get(language);
    if (properNounTags == null) {
      synchronized (languageProperNounTags_) {
        if (!languageProperNounTags_.containsKey(language)) {
          String path = basePath_ + language.name() + ".txt";
          try {
            List<String> properNouns = ClassPathUtils.getContent(path);
            properNounTags = new HashSet<>(properNouns);
            languageProperNounTags_.put(language, properNounTags);
          } catch (IOException e) {
            throw new RuntimeException("Could not load proper noun tags for language '" + language.name() + "' from '" + path + "'.");
          }
        }
      }
    }

    return properNounTags;
  }

  public boolean isProperNounTag(String tag, Language language) {
    Set<String> properNounTags = getLanguageTags(language);
    return properNounTags.contains(tag);
  }
}
