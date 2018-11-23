package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.io.IOException;
import java.util.Properties;

public class CollectionSettings {

  public boolean isOneFile() {
    return isOneFile;
  }

  public Language getLanguage() {
    return language;
  }

  public void setIsOneFile(boolean isOneFile) {
    this.isOneFile = isOneFile;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  private boolean isOneFile = true;

  private Language language;

  public static CollectionSettings readFromFile(String path) throws IOException {
    CollectionSettings es = new CollectionSettings();
    Properties prop = ClassPathUtils.getPropertiesFromClasspath(path);
    if (!prop.containsKey("language")) {
      throw new IllegalArgumentException("Collections settings must specify a language");
    }
    for (String key : prop.stringPropertyNames()) {
      switch (key) {
        case "isOneFile":
          es.setIsOneFile(Boolean.parseBoolean(prop.getProperty(key)));
          break;
        case "language":
          es.setLanguage(Language.getLanguageForString(prop.getProperty(key)));
          break;
        case "train": //es.setLanguage(Language.getLanguageForString(prop.getProperty(key))); break;
        case "test": //es.setLanguage(Language.getLanguageForString(prop.getProperty(key))); break;
        case "dev": //es.setLanguage(Language.getLanguageForString(prop.getProperty(key))); break;
        default:
          throw new IllegalArgumentException("Unknown property " + prop.getProperty(key));
      }
    }
    return es;
  }

}
