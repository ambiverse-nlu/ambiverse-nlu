package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.FactComponent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Yago3Util {

  private final static Pattern WIKICAT_PATTERN = Pattern.compile("<(.{2,3}\\/)?wikicat_([^>]*)>");

  private final static Pattern CLEAN_P_1 = Pattern.compile("\\(.*?\\)");
  private final static Pattern CLEAN_P_2 = Pattern.compile("[\\s\\x00-\\x1F]+");
  private final static Pattern CLEAN_P_3 = Pattern.compile("^[\\s\\x00-\\x1F]+");
  private final static Pattern CLEAN_P_4 = Pattern.compile("[\\s\\x00-\\x1F]+$");

  public static boolean isNamedEntity(String entity) {
    if (entity.startsWith("<wikicat_")) {
      return false;
    }
    if (entity.startsWith("<geoclass_")) {
      return false;
    }
    if (entity.startsWith("<geoentity_")) {
      return false;
    }
    if (entity.startsWith("<wordnet_")) {
      return false;
    }

    if (entity.equals("<male>")) {
      return false;
    }
    if (entity.equals("<female>")) {
      return false;
    }
    return true;
  }

  public static String getEntityLanguage(String entity) {
    int pos = entity.indexOf('/');
    if (pos == -1 || pos > 4) return "en";
    else {
      String lang = FactComponent.getLanguageOfEntity(entity);
      if (lang.length() != 2) { //e.g. for the entity <M/A-COM_Technology_Solutions>
        return "en";
      } else {
        return lang;
      }
    }
  }

  /**
   * Returns the name of the wiki category (or null)
   */
  public static String unWikiCategory(String wn) {
    Matcher matcher = WIKICAT_PATTERN.matcher(wn);
    if (matcher.find()) {
      String entity = "<" + (matcher.group(1) == null ? "" : matcher.group(1)) + matcher.group(2) + ">";
      return (FactComponent.unYagoEntity(entity));
    }
    return null;
  }

  /**
   * Creates a url-encoded part from the entity id (without KB-prefix)
   * @param entity  Entity ID without KB prefix in YAGO3 format.
   * @return entity id as url-encoded part.
   */
  public static String getEntityAsUrlPart(String entity) throws UnsupportedEncodingException {
    return URLEncoder.encode(FactComponent.unYagoEntity(entity), "UTF-8").replace("+", "%20");
  }

  /**
   * Applies cleaning patterns to an entity name.
   *
   * @param entityMention Name to clean.
   * @return  Cleaned name.
   */
  public static String cleanEntityMention(String entityMention) {
    Matcher m = CLEAN_P_1.matcher(entityMention);
    entityMention = m.replaceAll("");

    m = CLEAN_P_2.matcher(entityMention);
    entityMention = m.replaceAll(" ");

    m = CLEAN_P_3.matcher(entityMention);
    entityMention = m.replaceAll("");

    m = CLEAN_P_4.matcher(entityMention);
    entityMention = m.replaceAll("");

    return entityMention;
  }
}
