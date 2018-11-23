package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3RelationNames;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.Yago3Util;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Yago3ResourceDictionary {

  private Map<String, String> toEnglishMap;

  private Map<String, String> fromEnglishMap;

  private final String relation = YAGO3RelationNames.hasTranslation;

  //True if this dictionary is translating en to en, and hence no actual
  //translation takes place
  private boolean isEnglishDictionary = false;

  public Yago3ResourceDictionary(YAGO3Reader reader, String language) throws IOException {
    if (language.equals("en")) {
      isEnglishDictionary = true;
    } else {
      toEnglishMap = new HashMap<>();
      fromEnglishMap = new HashMap<>();
      List<Fact> facts = reader.getFacts(relation);
      for (Fact f : facts) {
        if (!isInterestingFact(f)) {
          continue;
        }
        String uSubject = f.getSubject();
        if (Yago3Util.getEntityLanguage(uSubject).equals(language)) {
          String subject = normalize(uSubject);
          String object = normalize(f.getObject());
          toEnglishMap.put(subject, object);
          fromEnglishMap.put(object, subject);
        }
      }
    }
  }

  public String toEnglish(String resource) {
    if (isEnglishDictionary) {
      return resource;
    }
    return toEnglishMap.get(resource);
  }

  public String fromEnglish(String resource) {
    if (isEnglishDictionary) {
      return resource;
    }
    return fromEnglishMap.get(resource);
  }

  public Map<String, String> getAllEntriesFromEnglish() {
    return fromEnglishMap;
  }

  protected abstract String normalize(String resource);

  protected abstract boolean isInterestingFact(Fact fact);
}
