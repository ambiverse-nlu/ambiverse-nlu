package de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3;

import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative.D;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.FinalMap;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.Char17;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.DateParser;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.NumberParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class FactComponent
 *
 * This code is part of the YAGO project at the Max Planck Institute for
 * Informatics and the Telecom ParisTech University. It is licensed under a
 * Creative Commons Attribution License by the YAGO team:
 * https://creativecommons.org/licenses/by/3.0/
 *
 * @author Fabian M. Suchanek
 *
 *         This class formats a component for a YAGO fact.
 */
public class FactComponent {

  /** YAGO namespace */
  public static final String YAGONAMESPACE = "http://yago-knowledge.org/resource/";

  /** Standard namespace prefixes that this N4Reader will assume */
  public static final Map<String, String> standardPrefixes = new FinalMap<String, String>("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
      "rdfs:", "http://www.w3.org/2000/01/rdf-schema#", "xsd:", "http://www.w3.org/2001/XMLSchema#", "owl:", "http://www.w3.org/2002/07/owl#",
      "skos:", "http://www.w3.org/2004/02/skos/core#", "dbp:", "http://dbpedia.org/ontology/");

  /** Creates a fact component for a URI */
  public static String forUri(String s) {
    if (s.startsWith("<") && s.endsWith(">")) return (s);
    if (s.startsWith(YAGONAMESPACE)) {
      return ('<' + s.substring(YAGONAMESPACE.length()) + '>');
    }
    if (s.startsWith("http://")) {
      for (Entry<String, String> entry : standardPrefixes.entrySet()) {
        if (s.startsWith(entry.getValue())) {
          return (forQname(entry.getKey(), s.substring(entry.getValue().length())));
        }
      }
    }
    return ('<' + Char17.encodeBackslash(s, turtleUri) + '>');
  }

  /** Creates a fact component for number */
  public static String forNumber(int i) {
    return (forStringWithDatatype(i + "", forQname("xsd:", "integer")));
  }

  /** Creates a fact component for number */
  public static String forNumber(float i) {
    return (forStringWithDatatype(i + "", forQname("xsd:", "decimal")));
  }

  /** Creates a fact component for number. We don't do any syntax checks here. */
  public static String forNumber(String s) {
    if (DateParser.isDate(s)) return (forStringWithDatatype(s, forQname("xsd:", "date")));
    if (s.indexOf('.') == -1 && s.indexOf("e") == -1 && s.indexOf("E") == -1) return (forStringWithDatatype(s, forQname("xsd:", "integer")));
    return (forStringWithDatatype(s, forQname("xsd:", "decimal")));
  }

  /**
   * Creates a fact component for a Qname. We don't do any syntax checks here.
   */
  public static String forQname(String prefixWithColon, String name) {
    if (prefixWithColon.equals("y:")) return (forUri(name));
    return (prefixWithColon + name);
  }

  /** Creates a fact component for a date. No checks done. */
  public static String forDate(String date) {
    return (forStringWithDatatype(date, "xsd:date"));
  }

  /** Creates a fact component for a year. No checks done. */
  public static String forYear(String year) {
    return (forStringWithDatatype(year + "-00-00", "xsd:date"));
  }

  /** Creates a fact component for a YAGO entity */
  public static String forYagoEntity(String name) {
    return (forUri(name.replace(' ', '_')));
  }

  /** Reverses the transformation from Wikipedia title to YAGO entity */
  public static String unYagoEntity(String entity) {
    return Char17.decodeBackslash(stripBracketsAndLanguage(entity).replace('_', ' '));
  }

  /** Creates a fact component for a YAGO entity from another language */
  public static String forForeignYagoEntity(String name, String lan) {
    if (isEnglish(lan)) return (forYagoEntity(name));
    return (forYagoEntity(lan + "/" + name));
  }

  /** Returns the language of a foreign entity (or null) */
  public static String getLanguageOfEntity(String name) {
    if (!name.startsWith("<")) return (null);
    int pos = name.indexOf('/');
    if (pos == -1 || pos > 4) return (null);
    return (name.substring(1, pos));
  }

  /** Returns the pure entity name */
  public static String stripBracketsAndLanguage(String entity) {
    entity = stripBrackets(entity);
    if (entity.indexOf('/') != -1) {
      String language = entity.substring(0, entity.indexOf('/'));
      if (Language.isActiveLanguage(language)) { //This still does a wrong formating when an entity has a prefix similar to a langauge. Best solution is just get urls directly from YAGO
        return (entity.substring(entity.indexOf('/') + 1));
      } else {
        return entity;
      }
    } else {
      return entity;
    }
  }

  /** Returns the pure string of a Wikipedia category */
  public static String stripCat(String cat) {
    cat = stripBracketsAndLanguage(cat);
    if (cat.startsWith("wikicat_")) cat = cat.substring("wikicat_".length());
    cat = cat.replace('_', ' ');
    cat = Char17.decodeBackslash(cat);
    return (cat);
  }

  /** Creates a fact component for a Wikipedia title */
  public static String forWikipediaTitle(String name) {
    name = Char17.decodeAmpersand(name).trim();
    return (forYagoEntity(name));
  }

  /** Creates a fact component for a String with language. We check the syntax */
  public static String forStringWithLanguage(String string, String language) {
    if (language != null && language.length() > 1) return ('"' + Char17.encodeBackslash(string, turtleString) + "\"@" + language);
    return ('"' + Char17.encodeBackslash(string, turtleString) + "\"");
  }

  /** Creates a fact component for a String with datatype. We check the syntax */
  public static String forStringWithDatatype(String string, String datatype) {
    if (datatype != null && !datatype.equals(YAGO.string)) return ('"' + Char17.encodeBackslash(string, turtleString) + "\"^^" + datatype);
    return (forStringWithLanguage(string, null));
  }

  /** Creates a fact component for a String. We check the syntax */
  public static String forString(String string) {
    return (forStringWithLanguage(string, null));
  }

  /** Creates a fact component for a wordnet entity */
  public static String forWordnetEntity(String word, String id) {
    return (forYagoEntity("wordnet_" + word + "_" + id));
  }

  /** Creates a fact component for a Wikipedia category */
  public static String forWikiCategory(String word) {
    // Capitalize the first letter for consistency
    word = word.substring(0, 1).toUpperCase() + word.substring(1);
    // Remove subsequent spaces. This happens for categories
    word = word.replaceAll("[ _]+", "_");
    return (forYagoEntity("wikicat_" + word));
  }

  /** Creates a fact component for a Wikipedia category */
  public static String forForeignWikiCategory(String word, String lan) {
    if (isEnglish(lan)) return (forWikiCategory(word));
    // Capitalize the first letter for consistency
    word = word.substring(0, 1).toUpperCase() + word.substring(1);
    // Remove subsequent spaces. This happens for categories
    word = word.replaceAll("[ _]+", "_");
    return (forYagoEntity(lan + "/wikicat_" + word));
  }

  /** Creates a fact component for a GeoNames class */
  public static String forGeoNamesClass(String word) {
    return (forYagoEntity("geoclass_" + word));
  }

  /** Creates a fact component for a degree */
  public static String forDegree(double deg) {
    return (forStringWithDatatype(deg + "", "<degrees>"));
  }

  /** Translates anything into a FactComponent */
  public static String forAny(String s) {
    if (s == null || s.length() == 0) return (null);
    if (s.startsWith("\"")) {
      String split[] = literalAndDatatypeAndLanguage(s);
      if (split != null && (split[1] != null || split[2] != null)) return (s);
      return (forString(stripQuotes(s.substring(1))));
    }
    if (s.startsWith("http://")) {
      return (forUri(s));
    }
    if (s.startsWith("<")) {
      return (s);
    }
    if (DateParser.isDate(s)) {
      return (forDate(s));
    }
    if (Character.isDigit(s.charAt(0)) || s.charAt(0) == '-' || s.charAt(0) == '+' || s.charAt(0) == '.') {
      return (forNumber(s));
    }
    if (s.indexOf(':') != -1) {
      return (forQname(s.substring(0, s.indexOf(':') + 1), s.substring(s.indexOf(':') + 1)));
    }
    return (forUri(s));
  }

  /** Turtle valid string characters */
  public static Char17.Legal turtleString = new Char17.Legal() {

    public boolean isLegal(char c) {
      if (c == '"') return (false);
      if (c == '\\') return (false);
      if (c < 0x20) return (false);
      return (true);
    }
  };

  /** Turtle valid URI characters. As per http://www.w3.org/TR/turtle/#grammar-production-IRIREF */
  public static Char17.Legal turtleUri = new Char17.Legal() {

    public boolean isLegal(char c) {
      if (Char17.in(c, "<>\"{}|^`\\")) return (false);
      if (c <= 0x20) return (false);
      return (true);
    }
  };

  /** returns the string part of a literal (with quotes) */
  public static String getString(String stringLiteral) {
    String[] split = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    return (split[0]);
  }

  /** returns the datatype part of a literal */
  public static String getDatatype(String stringLiteral) {
    String[] split = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    return (split[1]);
  }

  /** returns the language part of a literal */
  public static String getLanguageOfString(String stringLiteral) {
    String[] split = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    return (split[2]);
  }

  /** removes quotes before and after a string */
  public static String stripQuotes(String string) {
    if (string == null) return (null);
    if (string.startsWith("\"")) string = string.substring(1);
    if (string.endsWith("\"")) string = Char17.cutLast(string);
    return (string);
  }

  /** removes brackets */
  public static String stripBrackets(String result) {
    if (result == null) return (null);
    if (result.startsWith("<")) result = result.substring(1);
    if (result.endsWith(">")) result = Char17.cutLast(result);
    return (result);
  }

  public static String stripPrefix(String entityWithPrefix) {
    int pos = Math.max(entityWithPrefix.lastIndexOf('#'), entityWithPrefix.lastIndexOf('/'));
    return (stripBrackets(entityWithPrefix.substring(pos + 1)));
  }

  /** Returns a Java string for a YAGO string */
  public static String asJavaString(String stringLiteral) {
    return (Char17.decodeBackslash(FactComponent.stripQuotes(getString(stringLiteral))));
  }

  /** Sets data type of a literal */
  public static String setDataType(String stringLiteral, String datatype) {
    String split[] = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    if (D.equal(split[1], datatype)) return (stringLiteral);
    return (FactComponent.forStringWithDatatype(stripQuotes(split[0]), datatype));
  }

  /** Sets language of a literal */
  public static String setLanguage(String stringLiteral, String language) {
    String split[] = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    if (D.equal(split[2], language)) return (stringLiteral);
    return (FactComponent.forStringWithLanguage(stripQuotes(split[0]), language));
  }

  /** TRUE for literals */
  public static boolean isLiteral(String entity) {
    return (entity.startsWith("\""));
  }

  /** TRUE for urls */
  public static boolean isUri(String entity) {
    return (entity.startsWith("<"));
  }

  /** TRUE for categories */
  public static boolean isCategory(String cat) {
    cat = stripBracketsAndLanguage(cat);
    if (cat.startsWith("wikicat_") || cat.startsWith("wordnet_")) return true;
    else return false;
  }

  /**
   * Splits a literal into literal (with quotes) and datatype, followed by the
   * language. Non-existent components are NULL
   */
  public static String[] literalAndDatatypeAndLanguage(String s) {
    if (s == null || !s.startsWith("\"")) return (null);

    // Get the language tag
    int at = s.lastIndexOf('@');
    if (at > 0 && s.indexOf('\"', at) == -1) {
      String language = s.substring(at + 1);
      String string = s.substring(0, at);
      return (new String[] { string, null, language });
    }

    // Get the data type
    int dta = s.lastIndexOf("\"^^");
    if (dta > 0 && s.indexOf('\"', dta + 1) == -1) {
      String datatype = s.substring(dta + 3);
      String string = s.substring(0, dta + 1);
      return (new String[] { string, datatype, null });
    }

    // Otherwise, return just the string
    return (new String[] { s, null, null });
  }

  /** TRUE if the first thing is more specific than the second */
  public static boolean isMoreSpecific(String first, String second) {
    if (first.equals(second)) return (false);
    if (isLiteral(first)) {
      if (!isLiteral(second)) return (false);
      String secondString = asJavaString(second);
      String firstString = asJavaString(first);
      if (DateParser.isDate(firstString) && DateParser.isDate(secondString) && DateParser.includes(secondString, firstString)) return (true);
      if (D.equal(getDatatype(first), getDatatype(second)) && NumberParser.isFloat(firstString) && NumberParser.isFloat(secondString)
          && first.indexOf('.') != -1 && first.startsWith(Char17.cutLast(secondString)) && first.length() > second.length()) return (true);
      return (false);
    }
    return (false);
  }

  /** Returns a hash for a Java String */
  public static String hash(String string) {
    int hash = string.hashCode();
    return (Long.toString((long) hash - (long) Integer.MIN_VALUE, Character.MAX_RADIX));
  }

  /** Returns a hash for an entity */
  public static String hashEntity(String entity) {
    return (hash(stripBrackets(entity)));
  }

  /** Returns a hash for a literal */
  public static String hashLiteral(String literal) {
    return (hash(asJavaString(literal)));
  }

  /** Returns a hash for a literal */
  public static String hashLiteralOrEntity(String s) {
    return (isLiteral(s) ? hashLiteral(s) : hashEntity(s));
  }

  /** Returns a hash for a literal */
  public static String hashRelation(String relation) {
    String hash = hashEntity(relation);
    return (hash.substring(0, Math.min(3, hash.length())));
  }

  /** TRUE if the argument is a fact id */
  public static boolean isFactId(String arg) {
    return arg.startsWith("<id_");
  }

  /** Makes a Wikipedia URL for an entity coming from the English Wikipedia */
  public static String wikipediaURL(String entityName) {
    return wikipediaURL(entityName, "en");
  }

  /** Makes a Wikipedia URL for an entity coming from the LANGUAGE Wikipedia */
  public static String wikipediaURL(String entityName, String wikipediaLanguageCode) {
    entityName = unYagoEntity(entityName);
    String url = null;
    try {
      url = "<http://" + wikipediaLanguageCode + ".wikipedia.org/wiki/" + URLEncoder.encode(entityName, "UTF-8").replace("+", "%20") + ">";
    } catch (UnsupportedEncodingException e) {
      // Should never happen, we are dealing with UTF-8.
      e.printStackTrace();
    }
    return url;
  }

  /** Parses out the Wordnet name */
  public static String wordnetWord(String wordnetEntity) {
    if (!wordnetEntity.startsWith("<wordnet_") || wordnetEntity.length() < 8 + 9) return (null);
    wordnetEntity = wordnetEntity.substring("<wordnet_".length(), wordnetEntity.length() - 11);
    return (wordnetEntity);
  }

  /** DBpedia class prefix */
  public static final String dbpediaPrefix = "http://dbpedia.org/class/yago/";

  /** Returns the DBpedia class name for a YAGO class name */
  public static String dbpediaClassForYagoClass(String arg) {
    arg = FactComponent.stripBrackets(arg);
    if (arg.startsWith("wordnet_")) arg = arg.substring("wordnet_".length());
    if (arg.startsWith("wikicategory_")) arg = arg.substring("wikicategory_".length());
    StringBuilder result = new StringBuilder(dbpediaPrefix);
    boolean upcase = true;
    for (int i = 0; i < arg.length(); i++) {
      if (arg.charAt(i) == '_') {
        upcase = true;
        continue;
      }
      result.append(upcase ? Character.toUpperCase(arg.charAt(i)) : arg.charAt(i));
      upcase = false;
    }
    return FactComponent.forUri(result.toString());
  }

  public static String forInfoboxAttribute(String language, String attribute) {
    // return relation;
    return "<infobox/" + language + "/" + attribute.toLowerCase() + ">";
  }

  public static String forInfoboxTypeRelation(String language) {
    return (forInfoboxAttribute(language, "type"));
  }

  public static String forInfoboxTemplate(String cls, String lan) {
    cls = cls.toLowerCase().replace('_', ' ').trim();
    if (Character.isDigit(Char17.last(cls))) cls = Char17.cutLast(cls);
    return forStringWithLanguage(cls, lan);
  }

  /** TRUE if the entity is a wordnet class or a wikipedia category */
  public static boolean isClass(String entity) {
    return entity.startsWith("<wordnet_") || entity.startsWith("<wikicat_");
  }

  public static String stripClass(String cls) {
    String strippedCls = stripBrackets(cls);
    if (strippedCls.startsWith("wordnet_")) {
      strippedCls = strippedCls.substring("wordnet_".length(), strippedCls.lastIndexOf("_"));
    } else if (strippedCls.startsWith("wikicat_")) {
      strippedCls = strippedCls.substring("wikicat_".length());
    }
    strippedCls = strippedCls.replace('_', ' ');
    strippedCls = Char17.decodeBackslash(strippedCls);
    return strippedCls;
  }

  /** TRUE if the language is english */
  public static boolean isEnglish(String lan) {
    return (lan.equals("en") || lan.equals("eng"));
  }

  /** Testing */
  public static void main(String[] args) throws Exception {
    D.p(isMoreSpecific("\"1898-03-05\"^^xsd:date", "\"1898-##-##\"^^xsd:date"));
  }
}
