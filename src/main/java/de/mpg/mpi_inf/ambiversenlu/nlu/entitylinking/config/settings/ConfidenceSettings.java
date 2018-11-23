package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ConfidenceSettings implements Serializable {

  /**
   * Score type to use for the non-coherence score. LOCAL is the regular
   * mention-entity similarity (prior + keyphrasesim), WEIGHTED_DEGREE
   * is the weighted degree of an entity.
   */
  public enum SCORE_TYPE {
    LOCAL, WEIGHTED_DEGREE,
  }

  /**
   * Use this to balance the LOCAL vs. the COHERENCE confidence in the graph 
   * mode. LOCAL is the normalized score, COHERENCE is the randomized counts. 
   * The final confidence for is 
   * (confidenceBalance * LOCAL) + (( 1 - confidenceBalance) * COHERENCE). 
   */
  private float confidenceBalance = 1.0f;

  /**
   * Percentage of mentions to flip in the COHERENCE confidence estiamtion.
   * This is the maximum value, real flip percentages per iteration will be
   * uniformly drawn from [0.0f, mentionFlipPercentage].
   *
   * The default is set so that on average 1/3 of the mentions are flipped. 
   */
  private float mentionFlipPercentage = 0.66f;

  /**
   * Number of random flips to do per mention in the input graph.
   */
  private int iterationsPerMention = 1000;

  /**
   * Score type to use for the non-coherence score. LOCAL is the regular
   * mention-entity similarity (prior + keyphrasesim), WEIGHTED_DEGREE
   * is the weighted degree of an entity.
   */
  private SCORE_TYPE scoreType = SCORE_TYPE.WEIGHTED_DEGREE;

  /**
   * Set to true to combine re-run confidence with local scores.
   */
  private boolean combineReRunConfidence;

  public int getIterationsPerMention() {
    return iterationsPerMention;
  }

  public void setIterationsPerMention(int iterationsPerMention) {
    this.iterationsPerMention = iterationsPerMention;
  }

  public float getConfidenceBalance() {
    return confidenceBalance;
  }

  public void setConfidenceBalance(float confidenceBalance) {
    this.confidenceBalance = confidenceBalance;
  }

  public float getMentionFlipPercentage() {
    return mentionFlipPercentage;
  }

  public void setMentionFlipPercentage(float mentionFlipPercentage) {
    this.mentionFlipPercentage = mentionFlipPercentage;
  }

  public SCORE_TYPE getScoreType() {
    return scoreType;
  }

  public void setScoreType(SCORE_TYPE scoreType) {
    this.scoreType = scoreType;
  }

  public boolean isCombineReRunConfidence() {
    return combineReRunConfidence;
  }

  public void setCombineReRunConfidence(boolean combineReRunConfidence) {
    this.combineReRunConfidence = combineReRunConfidence;
  }

  public Map<String, Object> getAsMap() {
    Map<String, Object> s = new HashMap<String, Object>();
    s.put("confidenceBalance", String.valueOf(confidenceBalance));
    s.put("mentionFlipPercentage", String.valueOf(mentionFlipPercentage));
    s.put("iterationsPerMention", String.valueOf(iterationsPerMention));
    s.put("scoreType", scoreType.toString());
    return s;
  }
}
