package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ChunkDisambiguationResults {

  private List<ResultMention> mentions = new ArrayList<>();

  private String gTracerHtml;

  public ChunkDisambiguationResults() {
    // Does nothing.
  }

  public ChunkDisambiguationResults(Map<ResultMention, List<ResultEntity>> mentionMappings, String gTracerHtml) {
    super();
    mentions = new ArrayList<>();
    // Transform the old MAP style of results to store result entities directly in the result mentions.
    for (Map.Entry<ResultMention, List<ResultEntity>> entry : mentionMappings.entrySet()) {
      ResultMention rm = entry.getKey();
      List<ResultEntity> res = entry.getValue();
      rm.setResultEntities(res);
      mentions.add(rm);
    }
    Collections.sort(mentions);
    this.gTracerHtml = gTracerHtml;
  }

  public List<ResultMention> getResultMentions() {
    return mentions;
  }

  public String getgTracerHtml() {
    return gTracerHtml;
  }

  public void setgTracerHtml(String gTracerHtml) {
    this.gTracerHtml = gTracerHtml;
  }

  public String toString() {
    return mentions.toString();
  }
}
