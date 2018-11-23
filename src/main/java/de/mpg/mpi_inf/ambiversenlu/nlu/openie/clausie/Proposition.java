package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import edu.stanford.nlp.ling.IndexedWord;

import java.util.*;

/**
 * Stores a proposition.
 *
 * @date $LastChangedDate: 2014-05-20 15:17:22 +0200 (Tue, 20 May 2014) $
 * @version $LastChangedRevision: 1601 $
 */
public class Proposition {



  /** Constituents of the proposition */

  Map<Integer, String> constituents = new TreeMap<>();

  /** Position of optional constituents */
  Set<Integer> optional = new HashSet<Integer>();

  /** Tokens belonging to this proposition per constituent*/
  private Multimap<Integer, IndexedWord> tokensPerConstituent = ArrayListMultimap.create();

  private Map<Integer, IndexedWord> constituentHeads = new HashMap<>();

  // TODO: types of constituents (e.g., optionality)
  // sentence ID etc.

  private String dictRelation;

  public Proposition() {
  }



  /** Returns the subject of the proposition */
  public String subject() {
    return constituents.get(0);
  }

  /** Returns the relation of the proposition */
  public String relation() {
    return constituents.get(1);
  }

  /**
   * Returns a constituent in a given position <br>
   * SVO => i=0 is O
   */
  public String argument(int i) {
    return constituents.get(i + 2);
  }

  /** Returns the number of arguments */
  public int noArguments() {
    return constituents.size() - 2;
  }

  /** Checks if an argument is optional */
  public boolean isOptionalArgument(int i) {
    return optional.contains(i + 2);
  }

  public void addTokens(int contituentIndex, IndexedWord iw) {
    tokensPerConstituent.put(contituentIndex, iw);
  }

  public void addTokens(int contituentIndex, Collection<IndexedWord> words) {
    ((ArrayListMultimap) tokensPerConstituent).putAll(contituentIndex, words);
  }

  public List<IndexedWord> getTokensForConstituent(int contituentIndex) {
    return (List<IndexedWord>) ((ArrayListMultimap) tokensPerConstituent).get(contituentIndex);
  }

  public void addHead(int constituentIndex, IndexedWord head) {
    constituentHeads.put(constituentIndex, head);
  }

  public IndexedWord getHead(int constituentIndex) {
    return constituentHeads.get(constituentIndex);
  }

  public void removeTokensForConstituent(int contituentIndex) {
    ((ArrayListMultimap) tokensPerConstituent).removeAll(contituentIndex);
  }


  public String getDictRelation() {
      return dictRelation;
  }

  public void setDictRelation(String dictRelation) {
    this.dictRelation = dictRelation;
  }
  /**
   * SVO => i=0 is S, i=1 is V, ...
   * @return
   */
  public Map<Integer, String> getConstituents() {
    return constituents;
  }

  public void setConstituents(Map<Integer, String> constituents) {
    this.constituents = constituents;
  }

  @Override public String toString() {
    StringBuffer sb = new StringBuffer();
    String sep = "(";
    for (int i = 0; i < constituents.size(); i++) {
      String constituent = constituents.get(i);
      sb.append(sep);
      sep = ", ";
      sb.append("\"");
      sb.append(constituent);
      sb.append("\"");
      if (optional.contains(i)) {
        sb.append("?");
      }
    }
    sb.append(")");
    return sb.toString();
  }


  @Override public Proposition clone() {
    Proposition clone = new Proposition();
    clone.constituents = new HashMap<>(this.constituents);
    clone.optional = new HashSet<Integer>(this.optional);
    clone.tokensPerConstituent = ArrayListMultimap.create(this.tokensPerConstituent);
    return clone;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Proposition that = (Proposition) o;

    if (constituents != null ? !constituents.equals(that.constituents) : that.constituents != null) return false;
    if (optional != null ? !optional.equals(that.optional) : that.optional != null) return false;
    if (tokensPerConstituent != null ? !tokensPerConstituent.equals(that.tokensPerConstituent) : that.tokensPerConstituent != null) return false;
    return constituentHeads != null ? constituentHeads.equals(that.constituentHeads) : that.constituentHeads == null;

  }

  @Override public int hashCode() {
    int result = constituents != null ? constituents.hashCode() : 0;
    result = 31 * result + (optional != null ? optional.hashCode() : 0);
    result = 31 * result + (tokensPerConstituent != null ? tokensPerConstituent.hashCode() : 0);
    result = 31 * result + (constituentHeads != null ? constituentHeads.hashCode() : 0);
    return result;
  }

}
