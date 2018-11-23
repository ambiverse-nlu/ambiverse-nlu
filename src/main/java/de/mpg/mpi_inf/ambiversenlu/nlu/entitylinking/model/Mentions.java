package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.mentionrecognition.HybridFilter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMention;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.uima.jcas.JCas;

import java.io.Serializable;
import java.util.*;

import static org.apache.uima.fit.util.JCasUtil.select;

public class Mentions implements Serializable {

  private static final long serialVersionUID = -383105468450056989L;

//  private List<Mention> mentions = null;
  
  private Map<Integer, Map<Integer, Mention>> mentions = null;

  private HashMap<Integer, Integer> subStrings = null;

  private static HybridFilter mentionFilter = new HybridFilter();

  /**
   * The expected types for entities to which those mentions will be disambiguated
   */
  private Set<Type> entitiesTypes = null;

  public Mentions() {
    mentions = new TreeMap<Integer, Map<Integer,Mention>>();
  }
  
  public boolean containsOffsetAndLength(int offset, int length) {
    if (mentions.containsKey(offset) && mentions.get(offset).containsKey(length)) {
      return true;
    }
    return false;
  }

  public boolean containsOffset(int offset) {
    if (mentions.containsKey(offset) &&  mentions.get(offset) != null && mentions.get(offset).size() != 0) {
      return true;
    }
    return false;
  }
  
  public Mention getMentionForOffsetAndLength(int offset, int length) {
    if  (containsOffsetAndLength(offset, length)) {
      return mentions.get(offset).get(length);
    }
    return null;
  }
  
  public Mentions getMentionsForOffset(int offset) {
    Mentions ret_mentions = new Mentions();
    if (containsOffset(offset)) {
      for (Mention mention : mentions.get(offset).values()) {
        ret_mentions.addMention(mention);
      }
    }
    return ret_mentions;
  }
  
  public void setMentionForOffset(int offset, Mention m) {
    Map<Integer, Mention> temp = mentions.get(offset);
    if (temp == null) {
      temp = new TreeMap<>();
      mentions.put(offset, temp);
    }
    temp.put(m.getCharLength(), m);
    return;
  }

  public int size() {
    int size = 0;
    for (Map.Entry<Integer, Map<Integer, Mention>> offsetMentions : mentions.entrySet()) {
      size += offsetMentions.getValue().size();
    }
    return size;
  }

  public void addMention(Mention mention) {
    Map<Integer, Mention> temp = mentions.get(mention.getCharOffset());
    if (temp == null) {
      temp = new TreeMap<>();
      mentions.put(mention.getCharOffset(), temp);
    }
    temp.put(mention.getCharLength(), mention);
  }
  
  public void addMentions(Mentions addMentions) {
    for (int offset : addMentions.getMentions().keySet()) {
      for (int len : addMentions.getMentions().get(offset).keySet()) {
        addMention(addMentions.getMentions().get(offset).get(len));
      }
    }
  }

  public Map<Integer, Map<Integer, Mention>> getMentions() {
    return mentions;
  }

  public List<String> getMentionNames() {
    List<String> names = new ArrayList<String>(mentions.size());
    for (int offset : mentions.keySet()) {
      for (Mention m : mentions.get(offset).values()) {
        names.add(m.getMention());
      }
    }
    return names;
  }

  public ArrayList<Integer> getMentionTokenStanfordIndices() {
    ArrayList<Integer> mentionTokenIndices = new ArrayList<Integer>();
    // there's just one
    for (int offset : mentions.keySet()) {
      for (Mention mention : mentions.get(offset).values()) {
        for (int i = mention.getStartStanford(); i <= mention.getEndStanford(); i++)
          mentionTokenIndices.add(i);
      }
        
    }
    return mentionTokenIndices;
  }

  public boolean remove(Mention mention) {
    boolean removed = false;
    Map<Integer, Mention> temp = mentions.get(mention.getCharOffset());
    if (temp != null) {
      removed = temp.remove(mention.getCharLength()) == null? false : true;

      // Make sure to also remove the inner map if the last mention was removed.
      if (temp.isEmpty()) {
        mentions.remove(mention.getCharOffset());
      }
    }
    return removed;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer(200);
    for (int i = 0; i < mentions.size(); i++) {
      sb.append(mentions.get(i).toString()).append('\n');
    }
    return sb.toString();
  }

  public void setSubstring(HashMap<Integer, Integer> subStrings) {
    this.subStrings = subStrings;
  }

  public HashMap<Integer, Integer> getSubstrings() {
    return subStrings;
  }

  public Set<Type> getEntitiesTypes() {
    return entitiesTypes;
  }

  public void setEntitiesTypes(Set<Type> entitiesTypes) {
    this.entitiesTypes = entitiesTypes;
  }

  public Entities getAllCandidateEntities() {
    Entities candidates = new Entities();
    for (Map<Integer, Mention> temp : mentions.values()) {
      for (Mention m : temp.values()) {
        candidates.addAll(m.getCandidateEntities());
      }
    }
    return candidates;
  }

  public static Mentions getNeMentionsFromJCas(JCas jCas) {
    List<Mentions> mentions = new ArrayList<>();
    mentions.add(new Mentions());
    mentions.add(new Mentions());
    mentions.add(new Mentions());
    mentions.add(new Mentions());
    for (NamedEntity ne : select(jCas, NamedEntity.class)) {
      Mention mention = new Mention(ne.getCoveredText(), ne.getBegin(), ne.getEnd(), ne.getBegin(), ne.getEnd(), 0);
      mention.setCharOffset(ne.getBegin());
      mention.setCharLength(ne.getEnd() - ne.getBegin());
      if (ne.getValue() != null && (ne.getValue().equals("manual"))) {
        mentions.get(0).addMention(mention);
      } else if (ne.getValue() != null && ne.getValue().equals("pn")) {
        mention.setNerType("UNKNOWN");
        mentions.get(3).addMention(mention);
//        KnowNER
      } else if (ne.getValue() != null && ne.getValue().contains("KnowNER-")) {
        mentions.get(1).addMention(mention);
        mention.setNerType(ne.getValue().substring(ne.getValue().indexOf("-")+1));
//        the rest mentions
      } else {
        mentions.get(2).addMention(mention);
      }
    }
    Mentions result = mentions.get(0);
    for (int i = 0; i < mentions.size() - 1; i++) {
      result = mentionFilter.parse(result, mentions.get(i + 1));
    }
    return result;
  }
  
  public static Mentions getConceptMentionsFromJCas(JCas jCas) {
    Mentions mentions = new Mentions();
    for(ConceptMention c: select(jCas, ConceptMention.class)) {
      Mention mention = new Mention(c.getConcept(), c.getBegin(), c.getEnd(), c.getBegin(), c.getEnd(), 0); //c.getCoveredText() replaced by getConcept(), to allow fuzzy concepts
      mention.setCharOffset(c.getBegin());
      mention.setCharLength(c.getEnd() - c.getBegin());
      
      mentions.addMention(mention);
    }
    return mentions;
  }

}
