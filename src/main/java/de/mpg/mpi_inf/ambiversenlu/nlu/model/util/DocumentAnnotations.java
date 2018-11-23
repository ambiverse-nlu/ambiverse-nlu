package de.mpg.mpi_inf.ambiversenlu.nlu.model.util;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.uima.jcas.JCas;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DocumentAnnotations {

  private List<Map.Entry<Integer, Integer>> mentions = new ArrayList<>();

  public boolean addMention(int offset, int length) {
    return mentions.add(new AbstractMap.SimpleEntry<Integer, Integer>(offset, length));
  }

  public void addMentionsToJCas(JCas jCas) {
    for (Entry<Integer, Integer> mention : mentions) {
      NamedEntity ne = new NamedEntity(jCas, mention.getKey(), mention.getKey() + mention.getValue());
      ne.setValue("manual");
      ne.addToIndexes();
    }
  }

}
