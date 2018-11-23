package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the results of a disambiguation.
 */
public class DisambiguationResults implements Serializable {

  private static final long serialVersionUID = 8366493180300359941L;

  private List<ResultMention> mentions = new ArrayList<>();

  private transient String gTracerHtml;

  private transient Tracer tracer = null;

  private transient long processingTime = 0l;

  public DisambiguationResults() {
    // bean.
  }

  /**
   * Passes all found mentions (including the corresponding entities).
   *
   * @param mentions  Mentions including entities.
   * @param gTracerHtml     Tracing HTML.
   */
  public DisambiguationResults(List<ResultMention> mentions, String gTracerHtml) {
    super();
    if (mentions != null) {
      this.mentions = mentions;
      Collections.sort(mentions);
      this.gTracerHtml = gTracerHtml;
    }
  }

  public void setMentions(List<ResultMention> mentions) {
    this.mentions = mentions;
    Collections.sort(mentions);
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

  public Tracer getTracer() {
    return tracer;
  }

  public void setTracer(Tracer tracer) {
    this.tracer = tracer;
  }

  public long getProcessingTime() {
    return processingTime;
  }

  public void setProcessingTime(long processingTime) {
    this.processingTime = processingTime;
  }

  public String toString() {
    return mentions.toString();
  }
}
