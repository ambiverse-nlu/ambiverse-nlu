package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.SimilaritySettings;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GraphSettings implements Serializable {

  /**
   * Balances the mention-entity edge weights (alpha) 
   * and the entity-entity edge weights (1-alpha)
   */
  private double alpha = 0;

  /**
   * Set to true to use exhaustive search in the final solving stage of
   * ALGORITHM.COCKTAIL_PARTY. Set to false to do a hill-climbing search
   * from a random starting point.
   */
  private boolean useExhaustiveSearch = false;

  /**
   * Set to true to normalize the minimum weighted degree in the 
   * ALGORITHM.COCKTAIL_PARTY by the number of graph nodes. This prefers
   * smaller solutions.
   */
  private boolean useNormalizedObjective = false;

  /**
   * TrainingSettings to compute the initial mention-entity edge weights when
   * using coherence robustness.
   */
  private SimilaritySettings coherenceSimilaritySettingNE = null;
//  private SimilaritySettings coherenceSimilaritySettingC = null;

  /**
   * Number of candidates to keep for for 
   * ALGORITHM.COCKTAIL_PARTY_SIZE_CONSTRAINED.
   */
  private int entitiesPerMentionConstraint = 5;

  /**
   * Set to true to enable the coherence robustness test, fixing mentions
   * with highly similar prior and similarity distribution to the most
   * promising candidate before running the graph algorithm.
   */
  private boolean useCoherenceRobustnessTestNE = true;
  private boolean useCoherenceRobustnessTestC = true;

  /**
   * Threshold of the robustness test, below which the the L1-norm between
   * prior and sim results in the fixing of the entity candidate.
   */
  private double cohRobustnessThresholdNE = 0;
  private double cohRobustnessThresholdC = 0;
  
  /**
   * Set to true to enable the easy mentions test, fixing mentions to the
   * best candidate by local sim only for &lt; easyMentionsTestThreshold
   * candidates.
   */
  private boolean useEasyMentionsTest = false;

  /**
   * Threshold to use for easyMentionsTest;
   */
  private int easyMentionsTestThreshold = Integer.MAX_VALUE;

  /**
   * Set to true to enable the confidence threshold test, fixing mentions to
   * the best candidate if the confidence for the local sim is higher than
   * confidenceTestThreshold.
   */
  private boolean useConfidenceThresholdTest = false;

  /**
   * Threshold to use for the confidenceThresholdTest;
   */
  private double confidenceTestThreshold = 1.0;

  /**
   * Set to true to keep only the top K candidates for each mention, set by
   * pruneCandidateThreshold.
   */
  private boolean pruneCandidateEntities = false;

  /**
   * Set the number of candidates to keep when pruning.
   */
  private int pruneCandidateThreshold = Integer.MAX_VALUE;

  /**
   * Set to true to discover null mentions based on thresholding BEFORE the
   * graph algorithm is run. This should mitigate spurious effects of 
   * having to consider entities that are actually out of knowledge base.
   * Sets mentions to nil where max normalized local score &lt;
   *
   */
  private boolean preCoherenceNullMappingDiscovery = false;

  /**
   * Threshold to use for the usePreCoherenceNullMappingDiscovery;
   */
  private double preCoherenceNullMappingDiscoveryThreshold = 1.0;

  public boolean shouldUseExhaustiveSearch() {
    return useExhaustiveSearch;
  }

  public void setUseExhaustiveSearch(boolean useExhaustiveSearch) {
    this.useExhaustiveSearch = useExhaustiveSearch;
  }

  public double getAlpha() {
    return alpha;
  }

  public void setAlpha(double alpha) {
    this.alpha = alpha;
  }
  
  public SimilaritySettings getCoherenceSimilaritySettingNE() {
    return coherenceSimilaritySettingNE;
  }

  public void setCoherenceSimilaritySettingNE(SimilaritySettings similaritySettings) {
    this.coherenceSimilaritySettingNE = similaritySettings;
  }
  
//  public SimilaritySettings getCoherenceSimilaritySettingC() {
//    return coherenceSimilaritySettingC;
//  }
//
//  public void setCoherenceSimilaritySettingC(SimilaritySettings similaritySettings) {
//    this.coherenceSimilaritySettingC = similaritySettings;
//  }

  public int getEntitiesPerMentionConstraint() {
    return entitiesPerMentionConstraint;
  }

  public void setEntitiesPerMentionConstraint(int entitiesPerMentionConstraint) {
    this.entitiesPerMentionConstraint = entitiesPerMentionConstraint;
  }

  public double getCohRobustnessThresholdNE() {
    return cohRobustnessThresholdNE;
  }
  
  public double getCohRobustnessThresholdC() {
    return cohRobustnessThresholdC;
  }

  public void setCohRobustnessThresholdNE(double cohRobustnessThreshold) {
    this.cohRobustnessThresholdNE = cohRobustnessThreshold;
  }
  
  public void setCohRobustnessThresholdC(double cohRobustnessThreshold) {
    this.cohRobustnessThresholdC = cohRobustnessThreshold;
  }

  public boolean shouldUseNormalizedObjective() {
    return useNormalizedObjective;
  }

  public void setUseNormalizedObjective(boolean useNormalizedObjective) {
    this.useNormalizedObjective = useNormalizedObjective;
  }
  
  public boolean shouldUseCoherenceRobustnessTestNE() {
    return useCoherenceRobustnessTestNE;
  }

  public void setUseCoherenceRobustnessTestNE(boolean useCoherenceRobustnessTest) {
    this.useCoherenceRobustnessTestNE = useCoherenceRobustnessTest;
  }
  
  public boolean shouldUseCoherenceRobustnessTestC() {
    return useCoherenceRobustnessTestC;
  }

  public void setUseCoherenceRobustnessTestC(boolean useCoherenceRobustnessTest) {
    this.useCoherenceRobustnessTestC = useCoherenceRobustnessTest;
  }

  public boolean shouldUseEasyMentionsTest() {
    return useEasyMentionsTest;
  }

  public void setUseEasyMentionsTest(boolean useEasyMentionsTest) {
    this.useEasyMentionsTest = useEasyMentionsTest;
  }

  public int getEasyMentionsTestThreshold() {
    return easyMentionsTestThreshold;
  }

  public void setEasyMentionsTestThreshold(int easyMentionsTestThreshold) {
    this.easyMentionsTestThreshold = easyMentionsTestThreshold;
  }

  public boolean shouldUseConfidenceThresholdTest() {
    return useConfidenceThresholdTest;
  }

  public void setUseConfidenceThresholdTest(boolean useConfidenceThresholdTest) {
    this.useConfidenceThresholdTest = useConfidenceThresholdTest;
  }

  public double getConfidenceTestThreshold() {
    return confidenceTestThreshold;
  }

  public void setConfidenceTestThreshold(double confidenceTestThreshold) {
    this.confidenceTestThreshold = confidenceTestThreshold;
  }

  public boolean shouldPruneCandidateEntities() {
    return pruneCandidateEntities;
  }

  public void setPruneCandidateEntities(boolean pruneCandidateEntities) {
    this.pruneCandidateEntities = pruneCandidateEntities;
  }

  public int getPruneCandidateThreshold() {
    return pruneCandidateThreshold;
  }

  public void setPruneCandidateThreshold(int pruneCandidateThreshold) {
    this.pruneCandidateThreshold = pruneCandidateThreshold;
  }

  public boolean isPreCoherenceNullMappingDiscovery() {
    return preCoherenceNullMappingDiscovery;
  }

  public void setPreCoherenceNullMappingDiscovery(boolean preCoherenceNullMappingDiscovery) {
    this.preCoherenceNullMappingDiscovery = preCoherenceNullMappingDiscovery;
  }

  public double getPreCoherenceNullMappingDiscoveryThreshold() {
    return preCoherenceNullMappingDiscoveryThreshold;
  }

  public void setPreCoherenceNullMappingDiscoveryThreshold(double preCoherenceNullMappingDiscoveryThreshold) {
    this.preCoherenceNullMappingDiscoveryThreshold = preCoherenceNullMappingDiscoveryThreshold;
  }

  public Map<String, Object> getAsMap() {
    Map<String, Object> s = new HashMap<String, Object>();
    s.put("alpha", String.valueOf(alpha));
    s.put("useExhaustiveSearch", String.valueOf(useExhaustiveSearch));
    s.put("useNormalizedObjective", String.valueOf(useNormalizedObjective));
    if (coherenceSimilaritySettingNE != null) {
      s.put("coherenceSimilaritySettingNE", coherenceSimilaritySettingNE.getAsMap());
    }
//    if (coherenceSimilaritySettingC != null) {
//      s.put("coherenceSimilaritySettingC", coherenceSimilaritySettingC.getAsMap());
//    }
    s.put("entitiesPerMentionConstraint", String.valueOf(entitiesPerMentionConstraint));
    s.put("useCoherenceRobustnessTestNE", String.valueOf(useCoherenceRobustnessTestNE));
    s.put("useCoherenceRobustnessTestC", String.valueOf(useCoherenceRobustnessTestC));
    s.put("cohRobustnessThresholdNE", String.valueOf(cohRobustnessThresholdNE));
    s.put("cohRobustnessThresholdC", String.valueOf(cohRobustnessThresholdC));
    s.put("useEasyMentionsTest", String.valueOf(useEasyMentionsTest));
    s.put("easyMentionsTestThreshold", String.valueOf(easyMentionsTestThreshold));
    s.put("useConfidenceThresholdTest", String.valueOf(useConfidenceThresholdTest));
    s.put("confidenceTestThreshold", String.valueOf(confidenceTestThreshold));
    s.put("pruneCandidateEntities", String.valueOf(pruneCandidateEntities));
    s.put("preCoherenceNullMappingDiscovery", String.valueOf(preCoherenceNullMappingDiscovery));
    s.put("preCoherenceNullMappingDiscoveryThreshold", String.valueOf(preCoherenceNullMappingDiscoveryThreshold));

    return s;
  }

  public boolean shouldUseCoherenceRobustnessTest(boolean isNamedEntity) {
    if (isNamedEntity) {
      return shouldUseCoherenceRobustnessTestNE();
    }
    else {
      return shouldUseCoherenceRobustnessTestC();
    }
  }

  public double getCohRobustnessThreshold(boolean isNamedEntity) {
    if (isNamedEntity) {
      return getCohRobustnessThresholdNE();
    }
    else {
      return getCohRobustnessThresholdC();
    }
  }
}
