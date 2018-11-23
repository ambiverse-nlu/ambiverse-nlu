package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.ConfidenceSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.Graph;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.GraphNode;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CollectionUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * Estimates the confidence in the mention-entity mapping by randomly
 * switching entity mappings. Keeps track of how often each the candidate
 * entities of other mentions are chosen.
 * Single-candidate mentions are assigned a default confidence of 0.9.
 */
public class GraphConfidenceEstimator {

  public static final double OUT_OF_DICTIONARY_CONFIDENCE = 1.0;

  public static final double SINGLE_CANDIDATE_CONFIDENCE = 1.0;

  private static final int MAX_ITERATIONS = 10000;

  private int outOfGraphEntityId = -1;

  private Logger logger_ = LoggerFactory.getLogger(GraphConfidenceEstimator.class);

  public class Configuration {

    Map<Integer, Integer> mapping_;

    TIntSet presentGraphNodes_;

    TIntSet flippedMentions_;

    public Configuration(Map<Integer, Integer> mapping) {
      mapping_ = mapping;
      presentGraphNodes_ = new TIntHashSet();
      flippedMentions_ = new TIntHashSet();

      for (Entry<Integer, Integer> entry : mapping_.entrySet()) {
        presentGraphNodes_.add(entry.getKey());
        presentGraphNodes_.add(entry.getValue());
      }
    }

    public Configuration() {
      // Nothing.
    }

    public boolean isPresent(int nodeId) {
      return presentGraphNodes_.contains(nodeId);
    }
  }

  private Graph g_;

  private Map<Integer, Integer> solution_;

  private Random random_;

  private TIntObjectHashMap<TIntIntHashMap> mentionEntityCounts_ = new TIntObjectHashMap<TIntIntHashMap>();

  public GraphConfidenceEstimator(final Graph g, final Map<Integer, Integer> solution) {
    g_ = g;
    solution_ = new HashMap<Integer, Integer>(solution);
    random_ = new Random(1337);
  }

  /**
   * Estimates the confidence of the solution_ on the graph g_ by randomly
   * flipping mention-entity mappings. It handles solutions with no mention or 
   * a single mention with a default value.
   *
   * The confidence for mention-entity pairs where none of the candidate entities
   * have a coherence edge are computed using the normalized scores (same as
   * the LocalDisambiguation). They are removed before the random sampling step
   * as they have no influence on the coherence.
   * TODO: Think about entities that have no coherence but are connected to two mentions - should this be treated differently?
   * @return Confidences in the solution (and all other candidates) in a
   *          mention-entity-confidence map. 
   */
  public Map<Integer, Map<Integer, Double>> estimate(ConfidenceSettings confSettings) {
    Integer timerId = RunningTimer.recordStartTime("GraphConfidenceEstimator");

    Map<Integer, Map<Integer, Double>> localConfidences = new HashMap<Integer, Map<Integer, Double>>();

    // Compute the local confidence for all mentions. Where no entity candidate
    // has a coherence edge, remove from the solution.
    for (int mentionId : g_.getMentionNodesIds().values()) {
      Map<Integer, Double> scores = null;
      switch (confSettings.getScoreType()) {
        case LOCAL:
          scores = getMentionEntityLocalScores(g_, mentionId);
          break;
        case WEIGHTED_DEGREE:
          scores = getMentionEntityWeightedDegreeScores(g_, new Configuration(solution_), mentionId);
          break;
        default:
          scores = getMentionEntityLocalScores(g_, mentionId);
          break;
      }
      Map<Integer, Double> normalizedScores = new HashMap<Integer, Double>();
      if (g_.isLocalMention(mentionId)) {
        if (scores.size() == 0) {
          // There was is no candidate at all, use out of dictionary default
          // confidence
          normalizedScores.put(-1, OUT_OF_DICTIONARY_CONFIDENCE);
        } else if (scores.size() == 1) {
          // Use single candidate default value.
          normalizedScores.put(scores.keySet().iterator().next(), SINGLE_CANDIDATE_CONFIDENCE);
        } else {
          normalizedScores = CollectionUtils.normalizeValuesToSum(scores);
        }
        // No need to work on this mention anymore.
        solution_.remove(mentionId);
      } else {
        normalizedScores = CollectionUtils.normalizeValuesToSum(scores);
      }
      cleanNegativeKeys(normalizedScores);
      localConfidences.put(mentionId, normalizedScores);
    }

    Map<Integer, Map<Integer, Double>> confidences = null;
    double confidenceBalance = confSettings.getConfidenceBalance();
    if (confidenceBalance >= 1.0) {
      // Use only local similarity. Skip coherence confidence computation
      // for faster running time.
      logger_.debug("Computing only local confidence scores by normalization");
      confidences = localConfidences;
    } else {
      // Get the numebr of iterations, bounded by MAX_ITERATIONS
      int totalIterations = Math.min(confSettings.getIterationsPerMention() * solution_.size(), MAX_ITERATIONS);
      logger_.debug("Estimating confidence with " + totalIterations + " iterations.");
      for (int i = 0; i < totalIterations; ++i) {
        // Create configuration with switched mappings - mentions without
        // coherence candidates are ignored.
        Configuration configuration = getRandomConfiguration(g_, solution_, confSettings.getMentionFlipPercentage());
        // Increment counts for non-switched mentions and their chosen entities.
        Map<Integer, Integer> best = getBestCandidates(g_, configuration);
        incrementCounts(best, mentionEntityCounts_);
      }

      Map<Integer, Map<Integer, Double>> coherenceConfidences = computeConfidence(g_, mentionEntityCounts_);
      confidences = mergeCoherenceConfidences(localConfidences, coherenceConfidences, confSettings.getConfidenceBalance());
    }
    RunningTimer.recordEndTime("GraphConfidenceEstimator", timerId);
    return confidences;
  }

  private void cleanNegativeKeys(Map<Integer, Double> scores) {
    // Remove all scores with negative keys, as these entities are 
    // no longer present in the graph.
    List<Integer> toRemove = new ArrayList<Integer>();
    for (Integer k : scores.keySet()) {
      if (k < 0) {
        toRemove.add(k);
      }
    }
    for (Integer k : toRemove) {
      scores.remove(k);
    }
  }

  /**
   * Returns all local scores computed during the graph creation. As some
   * entities are dropped (graph coherence), they will be missing from the
   * current graph. These entities will still be present in the returned
   * map with negative scores. This is necessary for proper normalization.
   */
  private Map<Integer, Double> getMentionEntityLocalScores(Graph g, int mentionId) {
    Map<Integer, Double> scores = new HashMap<Integer, Double>();
    // Don't get the local scores from the graph edges, they are incomplete when
    // candidate entities are dropped due to the coherence robustness test.
    // The graph contains all scores as well in a different variable.
    Mention mention = (Mention) g.getNode(mentionId).getNodeData();
    TIntDoubleHashMap entitySims = g.getMentionEntitySims(mention);
    if (entitySims == null) {
      return new HashMap<Integer, Double>();
    }
    TIntIntHashMap entity2id = g.getEntityNodesIds();
    for (TIntDoubleIterator itr = entitySims.iterator(); itr.hasNext(); ) {
      itr.advance();
      // If the entity is not present in the graph anymore, assign a new, 
      // negative one. The negative ids will never be queried, they are
      // just there for the score normalization.
      Integer entityId = 0;
      if (!entity2id.contains(itr.key())) {
        entityId = outOfGraphEntityId;
        --outOfGraphEntityId;
      } else {
        entityId = entity2id.get(itr.key());
      }
      scores.put(entityId, itr.value());
    }
    return scores;
  }

  private Map<Integer, Double> getMentionEntityWeightedDegreeScores(Graph g, Configuration configuration, int mentionId) {
    Map<Integer, Double> scores = new HashMap<Integer, Double>();
    // When coherence robustness is enabled, all but one candidate entities
    // are dropped and the (normalized) score is always 1 (and thus useless).
    // Use the original scores if they are available.
    GraphNode mentionNode = g.getNode(mentionId);
    TIntDoubleHashMap successors = mentionNode.getSuccessors();
    if (successors.size() > 1) {
      for (TIntDoubleIterator itr = g.getNode(mentionId).getSuccessors().iterator(); itr.hasNext(); ) {
        itr.advance();
        int entity = itr.key();
        scores.put(entity, computeWeightedDegree(g, configuration, entity));
      }
    } else {
      // Use local scores.
      scores = getMentionEntityLocalScores(g, mentionId);
    }
    return scores;
  }

  private Configuration getRandomConfiguration(Graph g, Map<Integer, Integer> solution, float mentionFlipPercentage) {
    Configuration flippedConfiguration = new Configuration();

    // Solution has at least 2 mentions, other case is handled in estimate().
    // Decide number of mentions to switch - at least 1, at most 20%.
    int mentionSize = Math.round(solution.size() * mentionFlipPercentage);
    mentionSize = Math.max(1, mentionSize);
    int numFlips = Math.max(1, random_.nextInt(mentionSize));
    TIntSet flipCandidates = getFlipCandidates(g, solution);
    TIntSet flippedMentions = getRandomElements(flipCandidates, numFlips);
    flippedConfiguration.flippedMentions_ = flippedMentions;
    Map<Integer, Integer> flippedSolution = new HashMap<Integer, Integer>(solution);
    for (TIntIterator itr = flippedMentions.iterator(); itr.hasNext(); ) {
      int mentionId = itr.next();
      TIntDoubleHashMap entityCandidates = new TIntDoubleHashMap(getConnectedEntitiesWithScores(g_, mentionId));
      // Remove correct solution from candidates - it should not be chosen
      // when flipping.
      entityCandidates.remove(solution.get(mentionId));
      // Put placeholder if resembling a missing entity (will not contribute
      // to coherence at all).
      Integer flippedEntity = -1;
      if (entityCandidates.size() > 0) {
        TIntDoubleHashMap entityCandidateProbabilities = CollectionUtils.normalizeValuesToSum(entityCandidates);
        flippedEntity = getRandomEntity(mentionId, entityCandidateProbabilities, random_);
      }
      flippedSolution.put(mentionId, flippedEntity);
    }
    flippedConfiguration.mapping_ = flippedSolution;
    // Store active nodes in graph for faster lookup.
    flippedConfiguration.presentGraphNodes_ = new TIntHashSet();
    for (Entry<Integer, Integer> entry : flippedSolution.entrySet()) {
      flippedConfiguration.presentGraphNodes_.add(entry.getKey());
      flippedConfiguration.presentGraphNodes_.add(entry.getValue());
    }
    //    logger_.debug("Flipped " + flippedMentions.size() + " mentions: " +
    //                  flippedMentions);
    return flippedConfiguration;
  }

  /**
   * Selects one candidate key in the entityProbabilities map based on the
   * probability of the entity given by its value.
   *
   * @param mention Mention to choose the entity for.
   * @param entityCandidates A map of entity-id to probability.
   * @return A key from the entityProbabilities selected randomly based on the probability.
   */
  private Integer getRandomEntity(int mention, TIntDoubleHashMap entityCandidates, Random rand) {
    return CollectionUtils.getConditionalElement(entityCandidates, rand);
  }

  /**
   * Return all mention node ids with more than candidate entity. Others
   * cannot be flipped. 
   *
   * @param g Input graph.
   * @param solution  Disambiguation solution.
   * @return Mention node flipping candidates.
   */
  private TIntSet getFlipCandidates(Graph g, Map<Integer, Integer> solution) {
    TIntSet candidates = new TIntHashSet();
    for (int mention : solution.keySet()) {
      if (g.getNode(mention).getSuccessors().size() > 1) {
        candidates.add(mention);
      }
    }
    return candidates;
  }

  /**
   * Will return numElements integers from the input elements. If numElements
   * is larger than elements.size(), everything will be returned.
   *
   * @param elements    Elements to choose from.
   * @param numElements Number of elements to choose.
   * @return numElement random integers from elements.
   */
  private TIntSet getRandomElements(TIntSet elements, int numElements) {
    TIntList source = new TIntArrayList(elements.toArray());
    TIntSet randomElements = new TIntHashSet();
    for (int i = 0; i < numElements; ++i) {
      if (source.size() == 0) {
        break;
      }
      // TODO: this is not efficient, as deleting from the ArrayList
      // will copy ... make this more efficient when necessary.
      int elementPosition = random_.nextInt(source.size());
      int element = source.get(elementPosition);
      source.remove(element);
      randomElements.add(element);
    }
    return randomElements;
  }

  private Map<Integer, Integer> getBestCandidates(Graph g, Configuration conf) {
    Map<Integer, Integer> bestCandidates = new HashMap<Integer, Integer>();
    TIntDoubleHashMap entityWeightedDegrees = new TIntDoubleHashMap();
    for (int mentionId : conf.mapping_.keySet()) {
      // Ignore mentions that were switched, only work on the stable ones.
      if (!conf.flippedMentions_.contains(mentionId)) {
        // Find the maximum weighted degree first, then select the entities.
        double maxWeightedDegree = -Double.MAX_VALUE;
        int[] connectedEntities = getConnectedEntities(g, mentionId).toArray();
        for (int entityId : connectedEntities) {
          // Cache for entities - do not recompute when connected to
          // multiple mentions.
          double weightedDegree = entityWeightedDegrees.get(entityId);
          if (weightedDegree == entityWeightedDegrees.getNoEntryValue()) {
            weightedDegree = computeWeightedDegree(g, conf, entityId);
            entityWeightedDegrees.put(entityId, weightedDegree);
          }
          //          logger_.trace("Weighted degree for " + entityId + ": " + weightedDegree);
          if (weightedDegree > maxWeightedDegree) {
            maxWeightedDegree = weightedDegree;
          }
        }

        // Get all candidates with the maximum weighted degree, just randomly
        // among them. This happens frequently when all candidates have a 
        // 0.0 weight.
        TIntSet candidates = new TIntHashSet();
        for (int entityId : connectedEntities) {
          if (entityWeightedDegrees.get(entityId) == maxWeightedDegree) {
            candidates.add(entityId);
          }
        }
        TIntSet candidate = getRandomElements(candidates, 1);
        assert candidate.size() == 1 : "No candidate found";
        bestCandidates.put(mentionId, candidate.iterator().next());
        //        logger_.trace("Best for " + mentionId + ": " + bestEntityId);
      }
    }
    return bestCandidates;
  }

  private double computeWeightedDegree(Graph g, Configuration conf, int entityId) {
    double weightedDegree = 0;
    for (TIntDoubleIterator itr = g.getNode(entityId).getSuccessors().iterator(); itr.hasNext(); ) {
      itr.advance();
      int nodeId = itr.key();
      if (conf.isPresent(nodeId)) {
        weightedDegree += itr.value();
      }
    }
    return weightedDegree;
  }

  private void incrementCounts(Map<Integer, Integer> best, TIntObjectHashMap<TIntIntHashMap> mentionEntityCounts) {
    for (Entry<Integer, Integer> entry : best.entrySet()) {
      TIntIntHashMap entityCounts = mentionEntityCounts.get(entry.getKey());
      if (entityCounts == null) {
        entityCounts = new TIntIntHashMap();
        mentionEntityCounts.put(entry.getKey(), entityCounts);
      }
      entityCounts.adjustOrPutValue(entry.getValue(), 1, 1);
    }
  }

  private Map<Integer, Map<Integer, Double>> computeConfidence(Graph g, TIntObjectHashMap<TIntIntHashMap> mentionEntityCounts) {
    Map<Integer, Map<Integer, Double>> confidences = new HashMap<Integer, Map<Integer, Double>>();
    // Fill with 0 confidences
    for (int mentionId : solution_.keySet()) {
      Map<Integer, Double> mentionConfidences = new HashMap<Integer, Double>();
      confidences.put(mentionId, mentionConfidences);
      int[] candidateEntities = g.getNode(mentionId).getSuccessors().keys();
      for (int entity : candidateEntities) {
        mentionConfidences.put(entity, 0.0);
      }
    }

    for (TIntObjectIterator<TIntIntHashMap> itr = mentionEntityCounts.iterator(); itr.hasNext(); ) {
      itr.advance();
      int mentionId = itr.key();
      // Get total count for mention.
      int totalCount = 0;
      for (TIntIntIterator innerItr = itr.value().iterator(); innerItr.hasNext(); ) {
        innerItr.advance();
        totalCount += innerItr.value();
      }
      // Compute confidences (by normalizing).
      Map<Integer, Double> entityConfidences = confidences.get(mentionId);
      if (entityConfidences.size() == 1) {
        entityConfidences.put(entityConfidences.keySet().iterator().next(), SINGLE_CANDIDATE_CONFIDENCE);
      } else {
        TIntIntHashMap entityCounts = itr.value();
        for (TIntIntIterator innerItr = entityCounts.iterator(); innerItr.hasNext(); ) {
          innerItr.advance();
          int entityId = innerItr.key();
          double entityConfidence = (double) innerItr.value() / (double) totalCount;
          assert entityConfidence <= 1.0001;
          assert entityConfidence >= -0.0001;
          entityConfidences.put(entityId, entityConfidence);
        }
      }
      confidences.put(mentionId, entityConfidences);
    }
    return confidences;
  }

  private Map<Integer, Map<Integer, Double>> mergeCoherenceConfidences(Map<Integer, Map<Integer, Double>> localConfidences,
      Map<Integer, Map<Integer, Double>> coherenceConfidences, double balance) {
    Map<Integer, Map<Integer, Double>> confidences = new HashMap<Integer, Map<Integer, Double>>();
    // Local confidences are present for all mentions. Iterate over them
    // and adjust when coherence confidences are present.
    for (Entry<Integer, Map<Integer, Double>> entry : localConfidences.entrySet()) {
      Integer mentionId = entry.getKey();
      Map<Integer, Double> mentionLocalConfs = entry.getValue();
      Map<Integer, Double> mentionCohConfs = coherenceConfidences.get(mentionId);
      boolean equalSized = true;
      if (mentionLocalConfs.size() != mentionCohConfs.size()) {
        logger_.warn(g_.getName() + " - " + mentionId + ": " + mentionLocalConfs.size() + " local scores vs. " + mentionCohConfs.size()
            + " coherence sampled scores present. " + "This happens when using coherence robustness. " + "Using local confidences.");
        equalSized = false;
      }
      if (mentionCohConfs != null && equalSized) {
        Map<Integer, Double> mentionConfs = new HashMap<Integer, Double>();
        confidences.put(mentionId, mentionConfs);
        for (Integer entityId : mentionLocalConfs.keySet()) {
          // Missing are ignored.
          Double localConf = mentionLocalConfs.get(entityId);
          Double cohConf = mentionCohConfs.get(entityId);
          assert cohConf != null : "Entity " + entityId + " missing from coherence confidences.";
          Double conf = (balance * localConf) + ((1 - balance) * cohConf);
          mentionConfs.put(entityId, conf);
        }
      } else {
        // Pass on the local confidences.
        confidences.put(mentionId, mentionLocalConfs);
      }
    }
    return confidences;
  }

  private TIntSet getConnectedEntities(Graph g, int mentionId) {
    // Mentions can only have connected entities, no need to check.
    TIntSet connectedEntities = g.getNode(mentionId).getSuccessors().keySet();
    return connectedEntities;
  }

  private TIntDoubleHashMap getConnectedEntitiesWithScores(Graph g, int mentionId) {
    // Mentions can only have connected entities, no need to check.
    return g.getNode(mentionId).getSuccessors();
  }
}