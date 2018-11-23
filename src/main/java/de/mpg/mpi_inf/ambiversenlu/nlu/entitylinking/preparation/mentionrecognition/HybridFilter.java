package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.mentionrecognition;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HybridFilter {

  public Mentions parse(Mentions manual, Mentions ner) {
    if (manual.size() == 0 && ner.size() != 0) {
      return ner;
    } else if (manual.size() != 0 && ner.size() == 0) {
      return manual;
    }

    int from = 0;
    List<Mention> toAdd = new LinkedList<Mention>();
    for (Map<Integer, Mention> innerMap : ner.getMentions().values()) {
      for (Mention nerMention : innerMap.values()) {
        boolean ok = true;
        int nerStart = nerMention.getStartToken();
        int nerEnd = nerMention.getEndToken();
        for (Map<Integer, Mention> innerMap2 : manual.getMentions().values()) {
          for (Mention manMention : innerMap2.values()) {
            int manStart = manMention.getStartToken();
            int manEnd = manMention.getEndToken();
            if (nerEnd >= manStart && nerEnd <= manEnd) {
              ok = false;
            } else if (nerStart >= manStart && nerStart <= manEnd) {
              ok = false;
            } else if (nerStart <= manStart && nerEnd >= manEnd) {
              ok = false;
            }
          }
        }
        if (ok) {
          toAdd.add(nerMention);
        }
      }
    }
    for (int i = 0; i < toAdd.size(); i++) {
      manual.addMention(toAdd.get(i));
    }
//    Collections.sort(manual.getMentions()); Again now we have them in a map
    return manual;
  }
}
