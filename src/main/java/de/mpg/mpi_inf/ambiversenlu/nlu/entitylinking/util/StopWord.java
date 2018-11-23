package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Token;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.UimaTokenizer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.uima.UIMAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class StopWord {

  private static final Logger logger = LoggerFactory.getLogger(StopWord.class);

  private static StopWord stopwords = null;
  private final Map<Language, Set<String>> words = new HashMap<>();
  private final Map<Language, TIntHashSet> wordIds = new HashMap<>();

  private final Map<Language, Set<String>> symbols = new HashMap<>();
  private final Map<Language, TIntHashSet> symbolIds = new HashMap<>();

  //This method is left synchronized intentionally for performance reasons,
  //It doesn't cause problems because the data don't change, in the worst case, 
  //this would be instantiated multiple times
  private static StopWord getInstance() throws EntityLinkingDataAccessException {
    if (stopwords == null) {
      stopwords = new StopWord();
    }
    return stopwords;
  }

  private StopWord() throws EntityLinkingDataAccessException {
    load();
  }

  private void load() throws EntityLinkingDataAccessException {

    try {
      for (Language language : Language.activeLanguages()) {
        Set<String> ws = new HashSet<>();
        Set<String> sy = new HashSet<>();
        List<String> stopwords = ClassPathUtils.getContent(language.getPathStopWords());
        for (String stopword : stopwords) {
          ws.add(stopword.trim());
        }
        List<String> str = ClassPathUtils.getContent(language.getPathSymbols());
        for (String word : str) {
          word = word.trim();
          ws.add(word);
          sy.add(word);
        }
        words.put(language, ws);
        wordIds.put(language, new TIntHashSet(DataAccess.getIdsForWords(ws).values()));

        symbols.put(language, sy);
        symbolIds.put(language, new TIntHashSet(DataAccess.getIdsForWords(sy).values()));
      }
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }

  //This is a convinience method stopwords should be language specific
  @Deprecated private boolean isStopWord(String word) {
    for (Language language : Language.activeLanguages()) {
      if (words.get(language).contains(word)) {
        return true;
      }
    }
    return false;
  }

  private boolean isStopWord(String word, Language language) {
    return words.get(language).contains(word);
  }

  private boolean isStopWord(int wordId, Language language) {
    return wordIds.get(language).contains(wordId);
  }

  //This is a convinience method stopwords should be language specific
  @Deprecated private boolean isStopWord(int wordId) {
    for (Language language : Language.activeLanguages()) {
      if (wordIds.get(language).contains(wordId)) {
        return true;
      }
    }
    return false;
  }

  private boolean isSymbol(String word, Language language) {
    return symbols.get(language).contains(word);
  }

  //This is a convinience method stopwords should be language specific
  @Deprecated private boolean isSymbol(String word) {
    for (Language language : Language.activeLanguages()) {
      if (symbols.get(language).contains(word)) {
        return true;
      }
    }
    return false;
  }

  private boolean isSymbol(int word, Language language) {
    return symbolIds.get(language).contains(word);
  }

  //This is a convinience method stopwords should be language specific
  @Deprecated private boolean isSymbol(int word) {
    for (Language language : Language.activeLanguages()) {
      if (symbolIds.get(language).contains(word)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isStopwordOrSymbol(String word, Language language) throws EntityLinkingDataAccessException {
    StopWord sw = StopWord.getInstance();
    boolean is = sw.isStopWord(word.toLowerCase(), language) || sw.isSymbol(word, language);
    return is;
  }

  public static boolean isStopword(String word, Language language) throws EntityLinkingDataAccessException {
    StopWord sw = StopWord.getInstance();
    boolean is = sw.isStopWord(word.toLowerCase(), language);
    return is;
  }

  //This is a convinience method stopwords should be language specific
  @Deprecated public static boolean isStopwordOrSymbol(String word) throws EntityLinkingDataAccessException {
    StopWord sw = StopWord.getInstance();
    boolean is = sw.isStopWord(word.toLowerCase()) || sw.isSymbol(word);
    return is;
  }

  public static boolean isStopwordOrSymbol(int word, Language language) throws EntityLinkingDataAccessException {
    StopWord sw = StopWord.getInstance();
    boolean is = sw.isStopWord(word, language) || sw.isSymbol(word, language);
    return is;
  }

  //This is a convinience method stopwords should be language specific
  @Deprecated public static boolean isStopwordOrSymbol(int word) throws EntityLinkingDataAccessException {
    StopWord sw = StopWord.getInstance();
    boolean is = sw.isStopWord(word) || sw.isSymbol(word);
    return is;
  }

  public static boolean symbol(char word, Language language) throws EntityLinkingDataAccessException {
    return StopWord.getInstance().isSymbol(word, language);
  }

  //This is a convinience method stopwords should be language specific
  @Deprecated public static boolean symbol(char word) throws EntityLinkingDataAccessException {
    return StopWord.getInstance().isSymbol(word);
  }

  /**
   * Filter text from stop words and symbols
   * @param text
   * @return
   */
  public static String filterStopWordsAndSymbols(String text, Language language)
      throws EntityLinkingDataAccessException, UIMAException, ClassNotFoundException, IOException, NoSuchMethodException,
      MissingSettingException, UnprocessableDocumentException {
    Tokens tokens = UimaTokenizer.tokenize(language, text);
    StringBuilder cleanText = new StringBuilder();
    int i = 0;
    int end = tokens.size();
    for (Token token : tokens) {
      if (!isStopwordOrSymbol(token.getOriginal(), language)) {
        cleanText.append(token.getOriginal());
        if (i < end - 1) {
          cleanText.append(" ");
        }
      }
    }
    return cleanText.toString();
  }

  public static void main(String[] args) throws EntityLinkingDataAccessException {
    String test = "no deberia";
    System.out.println(StopWord.isStopwordOrSymbol(test));
  }

  public static boolean isOnlySymbols(String value, Language language) throws EntityLinkingDataAccessException {
    for (int i = 0; i < value.length(); i++) {
      if (!StopWord.symbol(value.charAt(i), language)) {
        return false;
      }
    }
    return true;
  }

}
