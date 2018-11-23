package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative.Announce;

import java.util.*;

/**
 * Class representing human languages 
 *
 * @author Gerard de Melo
 */
public class Language implements Comparable<Language> {

  protected String id;

  Map<String, List<String>> pronounPositiveTypes = new HashMap<String, List<String>>();

  Map<String, List<String>> pronounNegativeTypes = new HashMap<String, List<String>>();

  protected static final Map<String, String> supported = new HashMap<String, String>();

  static {
    supported.put("en", "English");
    supported.put("de", "German");
    supported.put("fr", "French");
    supported.put("es", "Spanish");
    supported.put("it", "Italian");
  }

  // ---------------------------------------------------------------------
  //           Initiation
  // ---------------------------------------------------------------------

  /**
   * Constructor
   * @param id  ISO 639-1 language code
   */
  public Language(String id) throws LanguageNotSupportedException {
    if (!supportedLanguage(id)) throw new LanguageNotSupportedException();
    this.id = id;
    initPronouns();
  }

  /** Sets pronoun properties for the current language **/
  protected void initPronouns() {
    for (Map.Entry<String, String> prTypes : getPronounTypes(id).entrySet()) {
      String split[] = prTypes.getValue().split("\\+:-");
      if (split[0] != null) pronounPositiveTypes.put(prTypes.getKey(), Arrays.asList((split[0].split(","))));
      if (split.length > 1) if (split[1] != null) pronounNegativeTypes.put(prTypes.getKey(), Arrays.asList((split[0].split(","))));
    }
  }

  /** Pronoun lists for different languages;  
   * (TODO: if it gets more involved, move into a PronounML class and/or load from file)*/
  protected static final Map<String, String> getPronounTypes(String id) {
    Map<String, String> prons = new HashMap<String, String>();
    //map layout: pronoun -> [positiveTypes]+:-[negativeTypes]
    //every pronoun needs at least one positive type, negative types are optional; types in each group can be separated by ','
    //a reference candidate is supposed to satisfy all positive types while not satisfying all negative types
    if (id.equals("en")) {
      prons.put("my", "wordnet_person_100007846");
      prons.put("he", "wordnet_person_100007846");
      prons.put("his", "wordnet_person_100007846");
      prons.put("she", "wordnet_person_100007846");
      prons.put("her", "wordnet_person_100007846");
      prons.put("it", "wordnet_entity_100001740+:-wordnet_person_100007846");
    }
    return prons;
  }

  protected static final Language generateLanguage(String id) {
    Language lang = null;
    try {
      lang = new Language(id);
    } catch (LanguageNotSupportedException ex) {
      Announce.error(ex);
    }
    return lang;
  }

  // ---------------------------------------------------------------------
  //           Provided functionality
  // ---------------------------------------------------------------------

  /** checks whether a string could be a pronoun of the language ignoring cases */
  public boolean isPronoun(String candidate) {
    return pronounPositiveTypes.keySet().contains(candidate.toLowerCase());
  }

  /** provides a list of all pronouns in this language */
  public Set<String> getPronouns() {
    return pronounPositiveTypes.keySet();
  }

  /** returns the entity type associated with the pronoun (e.g. Person, Female Person etc.) */
  public List<String> getPronounEntityTypes(String pronoun) {
    return pronounPositiveTypes.get(pronoun.toLowerCase());
  }

  /** returns the entity type associated with the pronoun (e.g. Person, Female Person etc.) */
  public List<String> getPronounCounterEntityTypes(String pronoun) {
    return pronounNegativeTypes.get(pronoun.toLowerCase());
  }

  public final boolean supportedLanguage(String lang) {
    for (String supLang : supported.keySet())
      if (supLang.equals(lang)) return true;
    return false;
  }

  public final String getLongForm() {
    return supported.get(id);
  }

  /**
   * @return the id
   */
  public final String getId() {
    return id;
  }

  @Override public final boolean equals(Object other) {
    if (this == other)  // same object
      return true;
    if (other == null) return false;
    if (!(other instanceof Language)) return false;
    // now check contents
    Language otherLanguage = (Language) other;
    return (this.id.equals(otherLanguage.id));
  }

  @Override public final int hashCode() {
    if (id == null) return 0;
    else return id.hashCode();
  }

  public final int compareTo(Language other) {
    return id.compareTo(other.id);
  }

  /**
   * Modern English
   */
  public static final Language ENGLISH = generateLanguage("en");

  /**
   * German
   */
  public static final Language GERMAN = generateLanguage("de");

  /**
   * French
   */
  public static final Language FRENCH = generateLanguage("fr");

  /**
   * Spanish
   */
  public static final Language SPANISH = generateLanguage("es");

  /**
   * Italian
   */
  public static final Language ITALIAN = generateLanguage("it");

  /** Test routine */
  public static void main(String[] argv) throws Exception {
    for (String langID : supported.keySet()) {
      Language lang = new Language(langID);
      Announce.message("Language ID:'" + langID + "' full name:'" + lang.getLongForm() + "'");
      assert (lang.getLongForm().equals(supported.get(langID)));
      Announce.message("Pronouns: " + lang.getPronouns());
      for (String pronoun : lang.getPronouns()) {
        assert lang.isPronoun(pronoun);

      }
    }
  }

  // ---------------------------------------------------------------------
  //           Exceptions
  // ---------------------------------------------------------------------

}
