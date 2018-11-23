package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.Graph;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.GraphNode;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.GraphTracer;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GreedyHillClimbing {

  private static final Logger logger = LoggerFactory.getLogger(GreedyHillClimbing.class);

  // the mentions and the entities that are part of the current solution.
  private Set<Integer> mentionNodes, entityNodes;

  private Graph inputGraph;

  private double currentSolution;

  private HashMap<Integer, Integer> finalSolution;

  private HashMap<Integer, Integer> currentChoice;

  // for each entity, maintain the number of times it has been chosen for a
  // mention node
  // private HashMap<Integer, Integer> currentNumberOfMentions;

  // The maximum number of combinations that we accept to check exhaustively
  private int maxCombinationsExhaustive;

  private HashMap<Integer, Integer> actualMentionDegrees;

  private HashMap<Integer, Integer[]> actualMentionSuccessors;

  public GreedyHillClimbing(Graph graph, Set<Integer> mnodes, Set<Integer> enodes, boolean[] rem, int maxCombinations) {
    this.inputGraph = graph;
    this.mentionNodes = mnodes;
    this.entityNodes = enodes;
    this.maxCombinationsExhaustive = maxCombinations;
    this.actualMentionDegrees = new HashMap<Integer, Integer>();
    this.actualMentionSuccessors = new HashMap<Integer, Integer[]>();
    this.finalSolution = new HashMap<Integer, Integer>();

  }

  public double computeWeight(Map<Integer, Integer> currentSolution) {

    double totalWeight = 0.0;

    Set<Integer> entities = new HashSet<Integer>();

    // mentions
    for (int mentionNodeId : currentSolution.keySet()) {

      if (inputGraph.isRemoved(mentionNodeId)) continue;
      GraphNode mentionNode = inputGraph.getNode(mentionNodeId);

      TIntDoubleHashMap successorsMap = mentionNode.getSuccessors();
      TIntDoubleIterator successorsIterator = successorsMap.iterator();
      for (int i = successorsMap.size(); i-- > 0; ) {
        successorsIterator.advance();

        int successorId = successorsIterator.key();
        if (inputGraph.isRemoved(successorId)) continue;
        double weight = successorsIterator.value();

        if (successorId == currentSolution.get(mentionNodeId)) {
          totalWeight += weight;
          entities.add(successorId);
        }

      }

    }

    // entities
    for (int entityId : entities) {

      if (inputGraph.isRemoved(entityId)) continue;
      GraphNode entityNode = inputGraph.getNode(entityId);

      TIntDoubleHashMap successorsMap = entityNode.getSuccessors();
      TIntDoubleIterator successorsIterator = successorsMap.iterator();
      for (int i = successorsMap.size(); i-- > 0; ) {
        successorsIterator.advance();

        int successorId = successorsIterator.key();
        if (inputGraph.isRemoved(successorId)) continue;
        double weight = successorsIterator.value();

        if (entities.contains(successorId) && entityId < successorId) {
          totalWeight += weight;
        }

      }

    }

    // Normalize by number of entities. Otherwise adding one more entity (if
    // possible) will always increase the weight.
    totalWeight = totalWeight / entities.size();
    return totalWeight;
  }

  public Map<Integer, Integer> runExhaustive() throws Exception {
    return runExhaustive("tempGraph");
  }

  /**
   * Exhaustive enumeration of all the possible combinations
   *
   * @param graphName
   */

  public Map<Integer, Integer> runExhaustive(String graphName) throws Exception {
    long possibleCombinations = 1;
    logger.debug("Computing the initial solution...");
    Map<Integer, Integer> bestChoice = new HashMap<Integer, Integer>();

    double bestTotalWeight = 0.0;
    for (int mentionNodeId : mentionNodes) {
      // This should be needed, as we cannot remove mentions
      if (inputGraph.isRemoved(mentionNodeId)) continue;

      GraphNode mentionNode = inputGraph.getNode(mentionNodeId);
      int actualOutdegree = 0;
      List<Integer> actualSuccessors = new LinkedList<Integer>();

      TIntDoubleHashMap successorsMap = mentionNode.getSuccessors();
      TIntDoubleIterator successorsIterator = successorsMap.iterator();
      for (int i = successorsMap.size(); i-- > 0; ) {
        successorsIterator.advance();

        int successorId = successorsIterator.key();
        if (inputGraph.isRemoved(successorId)) continue;

        actualOutdegree++;
        actualSuccessors.add(successorId);

      }

      if (actualOutdegree > 0) {
        actualMentionDegrees.put(mentionNodeId, actualOutdegree);
        actualMentionSuccessors.put(mentionNodeId, actualSuccessors.toArray(new Integer[actualOutdegree]));

        possibleCombinations *= actualOutdegree;

        if (possibleCombinations < 0) { // overflow
          possibleCombinations = Long.MAX_VALUE;
        }
      }
    } // end choosing first entity for every mention
    if (possibleCombinations > this.maxCombinationsExhaustive) {

      logger.debug("The combinations to check are " + possibleCombinations);
      logger.debug("Applying local search");

      GraphTracer.gTracer.addStat(graphName, "Exhaustive search not applied, too many combinations", Long.toString(possibleCombinations));

      return null;
    }

    logger.debug("Number of possible combinations that need to be checked: " + possibleCombinations);

    GraphTracer.gTracer.addStat(graphName, "Exhaustively searching number of combinations", Long.toString(possibleCombinations));

    Integer[] mentionIds = actualMentionDegrees.keySet().toArray(new Integer[actualMentionDegrees.keySet().size()]);
    if (possibleCombinations > 0) {
      Map<Integer, Integer> currentConfiguration = new HashMap<Integer, Integer>();
      GraphConfiguration gc = permuteRecursive(mentionIds, 0, currentConfiguration);
      bestChoice = gc.getMapping();
      bestTotalWeight = gc.getWeight();

    }

    logger.debug("Checked " + possibleCombinations + " combinations");

    logger.debug("The final solution has total weight " + bestTotalWeight);

    GraphTracer.gTracer.addStat(graphName, "Objective value after Exhaustive Search", Double.toString(bestTotalWeight));

    return bestChoice;
  }

  private GraphConfiguration permuteRecursive(Integer[] mentionIds, int currentMentionIndex, Map<Integer, Integer> currentConfiguration) {
    if (currentMentionIndex < mentionIds.length) {
      int currentMention = mentionIds[currentMentionIndex];
      GraphConfiguration gc = new GraphConfiguration(null, Double.MIN_VALUE);
      currentMentionIndex++;

      for (int candidate : actualMentionSuccessors.get(currentMention)) {
        currentConfiguration.put(currentMention, candidate);
        GraphConfiguration newGc = permuteRecursive(mentionIds, currentMentionIndex, currentConfiguration);

        if (newGc.getWeight() > gc.getWeight()) {
          gc = newGc;
        }
      }
      return gc;
    } else {
      double weight = computeWeight(currentConfiguration);
      GraphConfiguration gc = new GraphConfiguration(new HashMap<Integer, Integer>(currentConfiguration), weight);
      return gc;
    }
  }

  public HashMap<Integer, Integer> localSearch(int numberOfMoves) throws Exception {
    return localSearch("tempGraph", numberOfMoves);
  }

  /**
   * In this method we apply one-swap moves to explore the neighborhood of a
   * given solution: given the current solution, we must check just one of it.
   *
   * @param graphName
   * */
  public HashMap<Integer, Integer> localSearch(String graphName, int numberOfMoves) throws Exception {

    logger.debug("The starting graph has " + inputGraph.getNodesCount() + " nodes and " + inputGraph.getEdgesCount() + " arcs");
    logger.debug("Number of mentions: " + mentionNodes.size());
    logger.debug("Number of entities: " + entityNodes.size());

    HashMap<HashMap<Integer, Integer>, Integer> checkedCombinations = new HashMap<HashMap<Integer, Integer>, Integer>();

    logger.debug("Computing the initial solution...");

    currentChoice = new HashMap<Integer, Integer>();
    currentSolution = 0.0;

    long possibleCombinations = 1;

    ArrayList<Integer> mentionList = new ArrayList<Integer>();

    for (int mentionNodeId : mentionNodes) {

      mentionList.add(mentionNodeId);
      // find the candidate with the highest mention/entity similarity
      GraphNode mentionNode = inputGraph.getNode(mentionNodeId);

      double max = Double.NEGATIVE_INFINITY;
      int outdegree = inputGraph.getNodeOutdegree(mentionNodeId);
      int actualOutdegree = 0;
      Integer[] actualSuccessors = new Integer[outdegree];

      TIntDoubleHashMap successorsMap = mentionNode.getSuccessors();
      TIntDoubleIterator successorsIterator = successorsMap.iterator();

      int selected = -1;
      int readIndex = -1;
      for (int i = successorsMap.size(); i-- > 0; ) {
        successorsIterator.advance();

        int successor = successorsIterator.key();
        if (inputGraph.isRemoved(successor)) continue;

        actualOutdegree++;
        actualSuccessors[++readIndex] = successor;

        double mentionEntitySimilarity = successorsIterator.value();

        if (mentionEntitySimilarity > max) {

          max = mentionEntitySimilarity;
          selected = successor;

        }
      }

      currentChoice.put(mentionNodeId, selected);

      if (actualOutdegree > 0) {
        possibleCombinations *= actualOutdegree;

        if (possibleCombinations < 0) { // overflow
          possibleCombinations = Long.MAX_VALUE;
        }
      }

      actualMentionDegrees.put(mentionNodeId, actualOutdegree);
      actualMentionSuccessors.put(mentionNodeId, actualSuccessors);

    } // end choosing first entity for every mention

    currentSolution = computeWeight(currentChoice);
    checkedCombinations.put(currentChoice, 1);
    Random generator = new Random(1337);

    logger.debug("Initial solution has weight: " + currentSolution + ";\tnumber of possible combinations: " + possibleCombinations
        + ";\tapplying random moves...");

    GraphTracer.gTracer.addStat(graphName, "Starting HillClimbing for total combinations", Long.toString(possibleCombinations));

    GraphTracer.gTracer.addStat(graphName, "HillClimbing for total number of moves", Integer.toString(numberOfMoves));

    if (possibleCombinations > 0) {

      while (checkedCombinations.keySet().size() < numberOfMoves && checkedCombinations.keySet().size() < possibleCombinations) {

        HashMap<Integer, Integer> randomChoice = new HashMap<Integer, Integer>();

        for (int chosenMention : mentionList) {
          if (actualMentionDegrees.get(chosenMention) == 0) {
            // nothing to do here
            continue;
          }

          int chosenEntityIndex = generator.nextInt(actualMentionDegrees.get(chosenMention));
          int chosenEntity = actualMentionSuccessors.get(chosenMention)[chosenEntityIndex];

          randomChoice.put(chosenMention, chosenEntity);

        }

        // Compute the total weight of this subgraph

        double randomWeight = computeWeight(randomChoice);

        if (randomWeight > currentSolution) {

          currentSolution = randomWeight;
          currentChoice = randomChoice;

        }

        checkedCombinations.put(randomChoice, 1);

      }

    }

    logger.debug("Checked " + checkedCombinations.size() + " feasibleSolutions");

    logger.debug("The final solution has total weight " + currentSolution);

    GraphTracer.gTracer.addStat(graphName, "HillClimbing checked number of combinations", Integer.toString(checkedCombinations.size()));

    GraphTracer.gTracer.addStat(graphName, "HillClimbing got final objective score", Double.toString(currentSolution));

    for (int mnode : currentChoice.keySet()) {
      int enode = currentChoice.get(mnode);

      finalSolution.put(mnode, enode);

    }

    // pw.close();
    return finalSolution;

  }

  class GraphConfiguration {

    private Map<Integer, Integer> mapping;

    private Double weight;

    public GraphConfiguration(Map<Integer, Integer> mapping, Double weight) {
      super();
      this.mapping = mapping;
      this.weight = weight;
    }

    public Map<Integer, Integer> getMapping() {
      return mapping;
    }

    public Double getWeight() {
      return weight;
    }
  }
}
