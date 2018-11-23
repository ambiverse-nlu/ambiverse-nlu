package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import edu.stanford.nlp.ling.IndexedWord;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import static java.lang.System.in;

/** Options handles the ClausIe settings which should be loaded out of a configuration file.
 *
 * @date $LastChangedDate: 2013-04-24 11:35:23 +0200 (Wed, 24 Apr 2013) $
 * @version $LastChangedRevision: 739 $ */
public class Options {

  // informatin
  public static Dictionary dictCopular;

  public static Dictionary dictExtCopular;

  public static Dictionary dictNotExtCopular;

  public static Dictionary dictComplexTransitive;

  public static Dictionary dictAdverbsConj;

  public static Dictionary dictAdverbsIgnore;

  public static Dictionary dictAdverbsInclude;

  public static Dictionary dictReverbPartial;

  public static Dictionary dictReverbFull;

  public static boolean conservativeSVA;

  public static boolean conservativeSVOA;

  /**
   * Process coordinating conjunctions with common components. All other verbal coordinating
   * conjunctions will always been processed.
   *
   * Example: some sentence
   * Option on: ...
   * Option off:
   *
   * defaulot value
   *
   * Example sentance that is not affected
   */
  public static boolean processCcAllVerbs;

  public static boolean processCcNonVerbs;

  public static boolean processAppositions;

  public static boolean processPossessives;

  public static boolean processPartmods;
  //	public boolean processPassive = false; // NOT SUPPORTED FOR NOW (collapsed semantic graph needed but less stable)
  //add for possesive

  // representation
  public static boolean nary;

  public static int minOptionalArgs; // only when nary=false

  public static int maxOptionalArgs; // only when nary=false

  public static boolean lemmatize;

  public static String appositionVerb;

  public static String possessiveVerb;

  public static boolean includeOptionalAdverbs;

  public static boolean reverbRelationStyle;

  public static boolean keepOnlyLongest;

  //helpds
  private static Properties properties;
  private static String resourceFile;

  /**Constructs the set of options out of a conf file (clausie.conf)*/
  public Options() throws IOException, URISyntaxException {
    this("clausieResources/clausie.conf");
  }

  /**Constructs the set of options out of a conf file (fileOrResourceName)*/
  public Options(String fileOrResourceName) throws IOException {
    if(resourceFile == null || !fileOrResourceName.equals(resourceFile)) {
      resourceFile = fileOrResourceName;
      Properties prop = ClassPathUtils.getPropertiesFromClasspath(fileOrResourceName);
      try {
        if (properties == null || !properties.equals(prop)) {
          properties = prop;
          setOptions(properties);
        }
      } catch(URISyntaxException e){
        e.printStackTrace();
      }
    }
  }

  private InputStream openFileOrResource(String name) throws IOException {
    try {
      File file = new File(name);
      return new FileInputStream(file);
    } catch (FileNotFoundException e) {
    }
    URL url = getClass().getResource(name);
    if (url == null) {
      throw new IOException("File or resource '" + name + "' not found.");
    }
    return url.openStream();
  }

  /** Load options from the configuration file*/
  public void setOptions(Properties prop) throws IOException, URISyntaxException {
    // load the required options
    conservativeSVA = Boolean.parseBoolean(getProperty(prop, "conservativeSVA"));
    conservativeSVOA = Boolean.parseBoolean(getProperty(prop, "conservativeSVOA"));
    processCcAllVerbs = Boolean.parseBoolean(getProperty(prop, "processCcAllVerbs"));
    processCcNonVerbs = Boolean.parseBoolean(getProperty(prop, "processCcNonVerbs"));
    processAppositions = Boolean.parseBoolean(getProperty(prop, "processAppositions"));
    appositionVerb = getProperty(prop, "appositionVerb");
    processPossessives = Boolean.parseBoolean(getProperty(prop, "processPossessives"));
    possessiveVerb = getProperty(prop, "possessiveVerb");
    processPartmods = Boolean.parseBoolean(getProperty(prop, "processPartmods"));
    lemmatize = Boolean.parseBoolean(getProperty(prop, "lemmatize"));
    includeOptionalAdverbs = Boolean.parseBoolean(getProperty(prop, "includeOptionalAdverbs"));
    reverbRelationStyle = Boolean.parseBoolean(getProperty(prop, "reverbRelationStyle"));
    keepOnlyLongest = Boolean.parseBoolean(getProperty(prop, "keepOnlyLongest"));
    nary = Boolean.parseBoolean(getProperty(prop, "nary"));
    minOptionalArgs = Integer.parseInt(getProperty(prop, "minOptionalArgs"));
    maxOptionalArgs = Integer.parseInt(getProperty(prop, "maxOptionalArgs"));


    // get dictionaries
    dictCopular = getDictionary(prop, "dictCopular");
    dictExtCopular = getDictionary(prop, "dictExtCopular");
    dictNotExtCopular = getDictionary(prop, "dictNotExtCopular");
    dictComplexTransitive = getDictionary(prop, "dictComplexTransitive");
    dictAdverbsConj = getDictionary(prop, "dictAdverbsConj");
    dictAdverbsIgnore = getDictionary(prop, "dictAdverbsIgnore");
    dictAdverbsInclude = getDictionary(prop, "dictAdverbsInclude");
    dictReverbPartial = getDictionary(prop, "dictReverbPartial");
    dictReverbFull = getDictionary(prop, "dictReverbFull");

    // check for unused properties
    if (!prop.isEmpty()) {
      System.err.println("Unknown option(s): " + Arrays.toString(prop.keySet().toArray()));
    }
  }

  /** Returns a required option (key) */
  private String getProperty(Properties prop, String key) throws IOException {
    String result = prop.getProperty(key);
    if (result == null) {
      throw new IOException("Missing option: " + key);
    }
    return result;
  }

  /**Loads a dictionary (key) */
  private Dictionary getDictionary(Properties prop, String key) throws IOException, URISyntaxException {
    String name = getProperty(prop, key);
    File file = ClassPathUtils.getFileFromClassPath(name);
    Dictionary dict = new Dictionary();
    dict.load(file);
    in.close();
    return dict;
  }

  /**Checks if the copular dictionary contains a given word*/
  public boolean isCop(IndexedWord word) {
    return dictCopular.contains(word);
  }

  /**Checks if the extended copular dictionary contains a given word*/
  public boolean isExtCop(IndexedWord word) {
    return dictExtCopular.contains(word);
  }

  /**Checks if the non-extended copular dictionary contains a given word*/
  public boolean isNotExtCop(IndexedWord word) {
    return dictNotExtCopular.contains(word);
  }

  /**Checks if the complex transitive dictionary contains a given word*/
  public boolean isComTran(IndexedWord word) {
    return dictComplexTransitive.contains(word);
  }

  public boolean isReverbPartial(IndexedWord word) {
    return dictReverbPartial.contains(word);
  }

  public boolean isReverbFull(IndexedWord word) {
    return dictReverbFull.contains(word);
  }

  /**Returns a string with some initial words of a given dictionary*/
  private String someWords(Set<String> dict) {
    if (dict.isEmpty()) return "";
    StringBuffer result = new StringBuffer();
    Iterator<String> it = dict.iterator();
    String sep = "";
    result.append(" (");
    for (int i = 0; i < 3 && it.hasNext(); i++) {
      result.append(sep);
      result.append(it.next());
      sep = ", ";
    }
    if (it.hasNext()) result.append(", ...");
    result.append(")");
    return result.toString();
  }

  public void print(OutputStream out) {
    print(out, "");
  }

  /**Print settings*/
  public void print(OutputStream out, String prefix) {
    PrintStream pout = new PrintStream(out);

    pout.println(prefix + "CLAUSE DETECTION");
    pout.println(prefix + "  Dict. copular        : " + dictCopular.size() + someWords(dictCopular.words));
    pout.println(prefix + "  Dict. ext-copular    : " + dictExtCopular.size() + someWords(dictExtCopular.words));
    pout.println(prefix + "  Dict. not ext.-cop.  : " + dictNotExtCopular.size() + someWords(dictNotExtCopular.words));
    pout.println(prefix + "  Dict. complex trans. : " + dictComplexTransitive.size() + someWords(dictComplexTransitive.words));
    pout.println(prefix + "  Dict. ignored adverb : " + dictAdverbsIgnore.size() + someWords(dictAdverbsIgnore.words));
    pout.println(prefix + "  Dict. included adverb: " + dictAdverbsInclude.size() + someWords(dictAdverbsInclude.words));
    pout.println(prefix + "  Dict. conj adverbs   : " + dictAdverbsConj.size() + someWords(dictAdverbsConj.words));
    pout.println(prefix + "  Conservative SVA     : " + conservativeSVA);
    pout.println(prefix + "  Conservative SVOA    : " + conservativeSVOA);
    pout.println(prefix + "  Process all verb CCs : " + processCcAllVerbs);
    pout.println(prefix + "  Process non-verb CCs : " + processCcNonVerbs);
    pout.println(prefix + "  Process appositions  : " + processAppositions);
    pout.println(prefix + "  Process possessives  : " + processPossessives);
    pout.println(prefix + "  Process partmods     : " + processPartmods);

    pout.println(prefix + "");
    pout.println(prefix + "REPRESENTATION");
    pout.println(prefix + "  n-ary propositions  : " + nary);
    pout.println(prefix + "  Min. opt. args      : " + minOptionalArgs);
    pout.println(prefix + "  Max. opt. args      : " + maxOptionalArgs);
    pout.println(prefix + "  Lemmatize           : " + lemmatize);
    pout.println(prefix + "  Appositions verb    : \"" + appositionVerb + "\"");
    pout.println(prefix + "  Possessive verb     : \"" + possessiveVerb + "\"");
  }
}
