package de.mpg.mpi_inf.ambiversenlu.nlu.language;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

import java.io.IOException;
import java.util.List;

public class OptimaizeLanguageDetector implements LanguageDetector {

  private static List<LanguageProfile> languageProfiles;

  private static com.optimaize.langdetect.LanguageDetector languageDetector;

  private static TextObjectFactory textObjectFactory;

  static {
    try {
      languageProfiles = new LanguageProfileReader().readAllBuiltIn();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard()).withProfiles(languageProfiles).build();
    textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
  }

  public Language detectLanguage(String text) throws AidaUnsupportedLanguageException {
    TextObject textObject = textObjectFactory.forText(text);
    Optional<LdLocale> lang = languageDetector.detect(textObject);
    if (!lang.isPresent()) {
      throw new AidaUnsupportedLanguageException(
          "Language could not be detected. Please specify it as a parameter in ISO 639-1 two-letter format, e.g. 'cs', 'en', 'de', 'es', or 'zh'.");
    }
    String language = lang.get().getLanguage();
    Language result;
    try {
      result = Language.getLanguageForString(language);
    } catch (IllegalArgumentException e) {
      throw new AidaUnsupportedLanguageException("Language '" + language + "' has been detected but it is not supported. "
          + "If this is not the language of your document, please specify the correct language as a parameter in ISO 639-1 two-letter format, e.g. 'cs', 'en', 'de', 'es', or 'zh'.");
    }
    return result;
  }

}
