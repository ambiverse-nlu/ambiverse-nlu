package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker.KnowNERResourceChecker;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker.KnowNERResourceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**This class performs configuration of KnowNER: either adding a new language or updating the resources.
 * For out of the box usage just use KnowNERLanguageConfiguratorBuilder with default checkers, however it is possible to run it in custom mode.
 * If there are any failures, returns a non-empty list of error messages.
  */
public class KnowNERLanguageConfigurator {

  private Logger logger = LoggerFactory.getLogger(KnowNERLanguageConfigurator.class);
  private final String language;
  private final List<KnowNERResourceChecker> checkerList;

  public KnowNERLanguageConfigurator(String language,
                   List<KnowNERResourceChecker> checkerList) {
    this.language = language;
    if (language == null) {
      throw new IllegalStateException();
    }
    this.checkerList = Collections.unmodifiableList(checkerList);
  }


  public List<String> run() throws KnowNERLanguageConfiguratorException {
    List<String> errorMessages = new ArrayList<>();
    for (KnowNERResourceChecker checker : checkerList) {
      logger.info("Creating '" + language + "' KnowNER resources for " + checker.getClass().getSimpleName());
      KnowNERResourceResult result = checker.check();
      if (!result.isSuccess()) {
        errorMessages.add(result.getMessage());
      }
    }
    return errorMessages;
  }
}
