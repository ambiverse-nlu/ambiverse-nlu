package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EvaluationStats {

  public static ObjectMapper om = new ObjectMapper();

  private Stats nerStats;
  private Stats nedStats;
  private Stats entityNERStats;
  private Stats entityNEDStats;
  private Stats conceptNERStats;
  private Stats conceptNEDStats;

  public Stats getEntityNERStats() {
    return entityNERStats;
  }

  public void setEntityNERStats(Stats entityNERStats) {
    this.entityNERStats = entityNERStats;
  }

  public Stats getEntityNEDStats() {
    return entityNEDStats;
  }

  public void setEntityNEDStats(Stats entityNEDStats) {
    this.entityNEDStats = entityNEDStats;
  }

  public Stats getNerStats() {
    return nerStats;
  }

  public void setNerStats(Stats nerStats) {
    this.nerStats = nerStats;
  }

  public Stats getNedStats() {
    return nedStats;
  }

  public void setNedStats(Stats nedStats) {
    this.nedStats = nedStats;
  }

  public Stats getConceptNERStats() {
    return conceptNERStats;
  }

  public void setConceptNERStats(Stats conceptNERStats) {
    this.conceptNERStats = conceptNERStats;
  }

  public Stats getConceptNEDStats() {
    return conceptNEDStats;
  }

  public void setConceptNEDStats(Stats conceptNEDStats) {
    this.conceptNEDStats = conceptNEDStats;
  }

  public String toJSONString() throws JsonProcessingException {
    return om.writeValueAsString(this);
  }

}
