package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.ConfidenceSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.GraphSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.Graph;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.GraphGenerator;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.GraphNode;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.GraphNodeTypes;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.extraction.DegreeComparator;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.GraphTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.NullGraphTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CollectionUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * CocktailParty algorithm that does an initial pruning based on the diameter.
 * FIXME TODO this is broken, the diameter is not computed properly!
 * !!! see getDiameter() !!!
 *
 * This method is not used, @see CocktailPartySizeConstrained.
 */
public class CocktailParty extends DisambiguationAlgorithm {

  private static final Logger logger = LoggerFactory.getLogger(CocktailParty.class);

  protected ShortestPath shortestPath;

  protected Graph graph_;

  private boolean useExhaustiveSearch;

  private Map<ResultMention, List<ResultEntity>> solution;

  protected Map<Integer, Double> entityWeightedDegrees = new HashMap<Integer, Double>();

  private Map<Integer, Double> notRemovableEntityWeightedDegrees = new HashMap<Integer, Double>();

  private PriorityQueue<String> entitySortedDegrees = new PriorityQueue<String>(2000, new DegreeComparator());

  private PriorityQueue<String> notRemovableEntitySortedDegrees = new PriorityQueue<String>(2000, new DegreeComparator());

  protected Map<Integer, Integer> mentionDegrees = new HashMap<Integer, Integer>();

  private Map<Integer, Integer> bestMentionDegrees = new HashMap<Integer, Integer>();

  private Map<Integer, Double> bestWeightedDegrees = new HashMap<Integer, Double>();

  private boolean[] bestRemoved;

  private GraphSettings graphSettings;

  private boolean isTracing = false;

  private boolean computeConfidence = false;

  private ConfidenceSettings confSettings;

  private double distanceThreshold_;

  private Entities allEntities_;

	public CocktailParty(PreparedInputChunk input, DisambiguationSettings settings,                       
                      Tracer tracer) throws Exception {
	  super(input, new ExternalEntitiesContext(), settings, tracer);
    if (!(GraphTracer.gTracer instanceof NullGraphTracer))
      isTracing = true;

    logger.debug("Instantiating CoctailParty algorithm.");

    this.shortestPath = new ShortestPath();
    this.graphSettings = settings.getGraphSettings();
    this.computeConfidence = settings.shouldComputeConfidence();
    this.confSettings = settings.getConfidenceSettings();
    
    GraphGenerator gg = new GraphGenerator(input.getConceptMentions(), input.getNamedEntityMentions(), 
        input.getContext(), input.getChunkId(), input.getResultMentions(), settings, tracer);
    this.graph_ = gg.run();

    // The result entities are already included in the namedentity or concept entities in GraphGenerator.
    allEntities_ = new Entities();
    allEntities_.addAll(input.getNamedEntityMentions().getAllCandidateEntities());
    allEntities_.addAll(input.getConceptMentions().getAllCandidateEntities());
	}

	public void setExhaustiveSearch(boolean es) {
		this.useExhaustiveSearch = es;
	}

  public Map<ResultMention, List<ResultEntity>> disambiguate() throws Exception {
    return disambiguateNamedEntities();//Here we can have different disambiguate functions for Concept/NE/Joint
  }
  

	public Map<ResultMention, List<ResultEntity>> disambiguateNamedEntities()
			throws Exception {
		solution = new HashMap<ResultMention, List<ResultEntity>>();
		long start = System.currentTimeMillis();
		int moduleId = RunningTimer.recordStartTime("CocktailPartyDisambiguation");
		int subModId = RunningTimer.recordStartTime("RemoveDanglingMentions");
		TIntSet nodesToRemove = removeUnconnectedMentionEntityPairs(graph_, solution);
		Graph g = createGraphByRemovingNodes(graph_, nodesToRemove);
    RunningTimer.recordEndTime("RemoveDanglingMentions", subModId);
    bestRemoved = new boolean[g.getNodesCount()];
    int diameter = getDiameter();
		
		// Rescale the distance threshold by the global average distance
		// (derived by the average edge weight)
		double globalAverageWeight = (g.getAverageMEweight() + g
				.getAverageEEweight()) / 2.0;

		distanceThreshold_ = diameter * (1 - globalAverageWeight) * 0.5;
		
		debugAndTraceInitialGraphProperties(
		    g, diameter, distanceThreshold_, globalAverageWeight);
				
		Arrays.fill(bestRemoved, false);

		double initialObjective = firstScanAndCalculateInitialObjective(g);

		if (isTracing) {
			traceIntitialGraphStructure(g);
		}

		Set<Integer> bestNotRemovable = notRemovableEntityWeightedDegrees.keySet();
		Set<Integer> bestRemovable = entityWeightedDegrees.keySet();
		double bestValue = initialObjective;
		boolean noMinRemoved = false;

		logger.debug("Initial minimum weighted degree: " + initialObjective);

		debugAndTraceInitialDismabiguationProblemProperties(g);

		int iterations = 0;
		while (true) {
			iterations++;
			if (iterations == 1) {
				/**
				 * currently, compute the shortest-path distances only in the
				 * first iteration.
				 * */
				removeInitialEntitiesByDistance(g);

				if (isTracing) {
					traceCleanedGraphStructure(g);
				}
			}

			int removableMinimumNode = getRemovableMinimumNode(g);

			if (removableMinimumNode == -1) {
				if (iterations == 1) {
					noMinRemoved = true;
				}
				logger.debug("No node can be removed without violating constraints.");
				break;

			}

			double removableMinimumWeightedDegree = entityWeightedDegrees
					.get(removableMinimumNode);

			entitySortedDegrees.remove(removableMinimumNode + ":::"
					+ removableMinimumWeightedDegree);
			entityWeightedDegrees.remove(removableMinimumNode);
			g.setRemoved(removableMinimumNode);

			updateNeighboringNodes(g, removableMinimumNode);

			if (isTracing) {
				traceEntityRemovalStep(g, removableMinimumNode, removableMinimumWeightedDegree);
			}

			double removableMin = Double.POSITIVE_INFINITY;
			double notRemovableMin = Double.POSITIVE_INFINITY;

			if (!entitySortedDegrees.isEmpty()) {
				removableMin = Double.parseDouble(entitySortedDegrees.peek()
						.split(":::")[1]);
			} else {
				logger.debug("No node can be removed without violating constraints.");
				break;
			}

			if (!notRemovableEntitySortedDegrees.isEmpty()) {
				notRemovableMin = Double.parseDouble(notRemovableEntitySortedDegrees.peek()
						.split(":::")[1]);
			}

			double absoluteMinimumWeightedDegree = Math.min(removableMin,
					notRemovableMin);

			double objective = calculateObjective(
					absoluteMinimumWeightedDegree, entityWeightedDegrees);
			
			if (objective > bestValue) {
				bestValue = objective;
				bestRemovable = new HashSet<Integer>(
						entityWeightedDegrees.keySet());
				bestNotRemovable = new HashSet<Integer>(
						notRemovableEntityWeightedDegrees.keySet());

				bestMentionDegrees = new HashMap<Integer, Integer>();
				for (int men : mentionDegrees.keySet()) {
					bestMentionDegrees.put(men, mentionDegrees.get(men));
				}

				for (int b = 0; b < bestRemoved.length; b++) {
					bestRemoved[b] = g.isRemoved(b);
				}
				
	      if (isTracing) {
	        // keep track of actual weights
	        bestWeightedDegrees = new HashMap<Integer, Double>();
	        for (int men : entityWeightedDegrees.keySet()) {
	          bestWeightedDegrees.put(men,
	              entityWeightedDegrees.get(men));
	        }
	      }
			}

		} // end main loop of the algorithm

		if (noMinRemoved) {
			double removableMin = Double.POSITIVE_INFINITY;
			double notRemovableMin = Double.POSITIVE_INFINITY;

			if (!entitySortedDegrees.isEmpty())
				removableMin = Double.parseDouble(entitySortedDegrees.peek()
						.split(":::")[1]);
			if (!notRemovableEntitySortedDegrees.isEmpty())
				notRemovableMin = Double.parseDouble(notRemovableEntitySortedDegrees.peek()
						.split(":::")[1]);
			double absoluteMinimumWeightedDegree = Math.min(removableMin,
					notRemovableMin);

			double objective = calculateObjective(
					absoluteMinimumWeightedDegree, entityWeightedDegrees);

			if (objective > bestValue) {
				bestValue = objective;
				bestRemovable = new HashSet<Integer>(
						entityWeightedDegrees.keySet());
				bestNotRemovable = new HashSet<Integer>(
						notRemovableEntityWeightedDegrees.keySet());

				bestMentionDegrees = new HashMap<Integer, Integer>();
				for (int men : mentionDegrees.keySet()) {
					bestMentionDegrees.put(men, mentionDegrees.get(men));
				}

				for (int b = 0; b < bestRemoved.length; b++) {
					bestRemoved[b] = g.isRemoved(b);
				}
			}
		}

		if (isTracing) {			
			traceFinalGraphStructure(g);
		}

		logger.debug("Maximizing the minimum weighted degree. Best solution: " + bestValue);

		GraphTracer.gTracer
				.addStat(
						g.getName(),
						"Solution of Objective after Graph Algorithm (max. min-weighted-degree)",
						Double.toString(bestValue));

		HashSet<Integer> finalEntities = new HashSet<Integer>(bestRemovable);
		finalEntities.addAll(bestNotRemovable);

		double[][] allCloseness = 
		    new double[g.getNodesCount()][g.getNodesCount()];

		for (int m : bestMentionDegrees.keySet()) {
			double[] shortest = shortestPath.closeness(m, g, bestRemoved);
			for (int e : finalEntities) {
				allCloseness[e][m] = shortest[e];
			}
		}

		int mentions = bestMentionDegrees.keySet().size();
		int entities = finalEntities.size();
		
		debugAndTraceFinalDismabiguationProblemProperties(g, mentions, entities);

		g.setIsRemovedFlags(bestRemoved);

		// Graph algorithm is done, check if further disambiguation is needed
		boolean extraDisambiguationNeeded = false;
		for (int mention : bestMentionDegrees.keySet()) {
			int mentionDegree = bestMentionDegrees.get(mention);
			if (mentionDegree > 1) { // more than a candidate entity
				extraDisambiguationNeeded = true;
				break;
			}
		}

		String stat = extraDisambiguationNeeded ? "Need final solving"
				: "Solved after graph";
		GraphTracer.gTracer.addStat(g.getName(),
				"Status after graph algorithm", stat);

		Map<Integer, Integer> graphMapping = new HashMap<Integer, Integer>();
		if (!extraDisambiguationNeeded) {
			logger.debug("No need for further disambiguation");
			long end = System.currentTimeMillis();
			double seconds = ((double) (end - start)) / 1000.0;
			GraphTracer.gTracer.addStat(g.getName(), "Runtime",
					String.format("%.4fs", seconds));
			// Fills in solution object, returns mapping in original graph
			// as byproduct.
			graphMapping = fillInSolutionObject(g, finalEntities, allCloseness);
		} else {
			logger.debug("Applying disambiguation");
			GreedyHillClimbing search = new GreedyHillClimbing(g,
					bestMentionDegrees.keySet(), finalEntities, bestRemoved,
					1000);
			String solver = useExhaustiveSearch ? "Using exhaustive search"
					: "Using Random Local Search";

			GraphTracer.gTracer.addStat(g.getName(),
					"Final Solving Technique", solver);

			if (useExhaustiveSearch) {
			  graphMapping = search.runExhaustive(g.getName());
				if (graphMapping == null) {
				  graphMapping = search.localSearch(g.getName(),
							g.getNodesCount());
				}
			} else {
			  graphMapping = search.localSearch(g.getName(),
						g.getNodesCount());
			}

			for (int mentionNodeId : mentionDegrees.keySet()) {
				GraphNode mentionNode = g.getNode(mentionNodeId);
				Mention mention = (Mention) mentionNode.getNodeData();

				ResultMention rm = new ResultMention(
						mention.getMention(), mention.getCharOffset(),
						mention.getCharLength());

				if (graphMapping.containsKey(mentionNodeId)
						&& graphMapping.get(mentionNodeId) != -1) {

					int entityNodeId = graphMapping.get(mentionNodeId);
					GraphNode entityNode = g.getNode(entityNodeId);
					if (entityNode.getType() != GraphNodeTypes.ENTITY) {
					  logger.error("Not entity node!");
					}
					int  entityInternalId = (int) entityNode.getNodeData();

          double mentionEntitySimilarity = mentionNode
              .getSuccessors().get(entityNodeId);
          Entity entity = allEntities_.getEntityById(entityInternalId);
          solution.put(rm, ResultEntity.getResultEntityAsList(new ResultEntity(entity, mentionEntitySimilarity)));
        } else {
          solution.put(rm, ResultEntity.getResultEntityAsList(ResultEntity.getNoMatchingEntity()));
          // -1 is a placeholder for OOKBE.
          graphMapping.put(mentionNodeId, -1);
        }
      }

      long end = System.currentTimeMillis();
      double seconds = ((double) (end - start)) / 1000.0;
      GraphTracer.gTracer.addStat(g.getName(), "Runtime", String.format("%.4fs", seconds));
    }

    if (computeConfidence) {
      Map<Integer, Map<Integer, Double>> entityConfidence = computeConfidence(g, graphMapping, confSettings);
      //TODO probably add the score of the result mentions to the entityConfidence (check more)
      // Parts of the solution might already be filled when the graph
      // was pruned before. Do not overwrite but add.
      solution.putAll(createConfidenceSolution(g, graphMapping, entityConfidence));
    }

    RunningTimer.recordEndTime("CocktailPartyDisambiguation", moduleId);
    return solution;
  }

  /**
   * Removes dangling mentions (where no candidate entity has a coherence edge)
   * from gaph. They will influence the minimum weighted degree but can
   * never be improved. Set the solution to the entity with the highest
   * mention-entity weight.
   *
   * @param solution Solution will be updated, setting the correct entity using
   *                local similarity for dangling mentions.
   * @return Node ids of nodes to remove.
   */
  private TIntSet removeUnconnectedMentionEntityPairs(Graph g, Map<ResultMention, List<ResultEntity>> solution) {
    TIntSet mentionsToRemove = new TIntHashSet();
    for (int mentionId : g.getMentionNodesIds().values()) {
      GraphNode mentionNode = g.getNode(mentionId);
      Mention mention = (Mention) mentionNode.getNodeData();
      TIntDoubleHashMap entityCandidates = mentionNode.getSuccessors();
      if (entityCandidates.size() == 0) {
        continue;
      }
      // Remove all mentions without any entities that have coherence edges.
      if (g.isLocalMention(mentionId)) {
        logger.debug("local mention removed: " + mentionId + " " + mention);
        mentionsToRemove.add(mentionId);
        GraphTracer.gTracer.addMentionToDangling(g.getName(), mention.getMention(), mention.getCharOffset());
        // Set solution to best local candidate.
        Pair<Integer, Double> bestEntityScore = getBestLocalCandidateAndScore(entityCandidates);
        int bestEntity = bestEntityScore.getKey();
        double score = bestEntityScore.getValue();
        updateSolution(solution, g, mention, bestEntity, score);
      }

    }
    TIntSet entitiesToRemove = new TIntHashSet();
    // Remove entities that are only connected to removed mentions.
    for (int entityId : g.getEntityNodesIds().values()) {
      GraphNode entityNode = g.getNode(entityId);
      TIntDoubleHashMap successors = entityNode.getSuccessors();
      int removedCount = 0;
      for (TIntDoubleIterator itr = successors.iterator(); itr.hasNext(); ) {
        itr.advance();
        int neighborId = itr.key();
        if (mentionsToRemove.contains(neighborId)) {
          ++removedCount;
        }
      }
      if (removedCount == successors.size()) {
        entitiesToRemove.add(entityId);
      }
    }
    // Remove mentions + entity candidates from graph, trace.
    TIntSet nodesToRemove = new TIntHashSet(mentionsToRemove.size() + entitiesToRemove.size());
    nodesToRemove.addAll(mentionsToRemove);
    nodesToRemove.addAll(entitiesToRemove);
    return nodesToRemove;
  }

  /**
   * Get the best candidate and (normalized) score from the given entity-score map.
   *
   */
  private Pair<Integer, Double> getBestLocalCandidateAndScore(TIntDoubleHashMap entityCandidates) {
    if (entityCandidates.size() == 0) {
      return new Pair<Integer, Double>(-100, 0.0);
    }
    double bestScore = -1.0;
    int bestCandidate = -10;
    for (TIntDoubleIterator itr = entityCandidates.iterator(); itr.hasNext(); ) {
      itr.advance();
      int entityId = itr.key();
      double score = itr.value();
      if (score > bestScore) {
        bestScore = score;
        bestCandidate = entityId;
      }
    }

    if (computeConfidence) {
      TIntDoubleHashMap normalizedScores = CollectionUtils.normalizeValuesToSum(entityCandidates);
      bestScore = normalizedScores.get(bestCandidate);
    }

    return new Pair<>(new Integer(bestCandidate), new Double(bestScore));
  }

  private void updateSolution(Map<ResultMention, List<ResultEntity>> solution, Graph g, Mention mention, int entityId, double score) {
    ResultMention rm = new ResultMention(mention.getMention(), mention.getCharOffset(), mention.getCharLength());
    if (entityId < 0 || entityId >= g.getNodes().length) {
      logger.error("Entity id:" + entityId);
    }
    int entityInternalId = (int) g.getNode(entityId).getNodeData();
    Entity entity = allEntities_.getEntityById(entityInternalId);
    ResultEntity re = new ResultEntity(entity, score);
    // TODO: If ranking is added to graph, add all candidates here.
    solution.put(rm, ResultEntity.getResultEntityAsList(re));
  }

  private Graph createGraphByRemovingNodes(final Graph g, final TIntSet nodesToRemove) {
    return createGraphByRemovingNodesAndEdges(g, nodesToRemove, new HashSet<Edge>());
  }

  private Graph createGraphByRemovingNodesAndEdges(final Graph g, final TIntSet nodesToRemove, final Set<Edge> edgesToRemove) {
    if (nodesToRemove.isEmpty() && edgesToRemove.isEmpty()) {
      return g;
    }
    Graph pruned = new Graph(g.getName(), g.getNodesCount() - nodesToRemove.size(), graphSettings.getAlpha());
    pruned.setAverageEEweight(g.getAverageEEweight());
    pruned.setAverageMEweight(g.getAverageMEweight());
    // Add all non-removed nodes plus mention-entity edges.
    TIntSet addedEntities = new TIntHashSet();
    for (TObjectIntIterator<Mention> itr = g.getMentionNodesIds().iterator(); itr.hasNext(); ) {
      itr.advance();
      int mentionId = itr.value();
      if (!nodesToRemove.contains(mentionId)) {
        Mention m = itr.key();
        pruned.addMentionNode(m);
        GraphNode mentionNode = g.getNode(mentionId);
        for (TIntDoubleIterator entityItr = mentionNode.getSuccessors().iterator(); entityItr.hasNext(); ) {
          entityItr.advance();
          int entityId = entityItr.key();
          if (!nodesToRemove.contains(entityId)) {
            GraphNode entityNode = g.getNode(entityId);
            if (entityNode.getType() == GraphNodeTypes.MENTION) {
              logger.error("ERROR: It is a mention not entity: " + entityNode.getNodeData() + " " + entityNode.getId());
              continue;
            }
            int entityInternalId = (int) entityNode.getNodeData();
            if (!addedEntities.contains(entityId)) {
              pruned.addEntityNode(entityInternalId);
              addedEntities.add(entityId);
            }
            Edge toAdd = new Edge(mentionId, entityId);
            if (!edgesToRemove.contains(toAdd)) {
              pruned.addEdge(m, entityInternalId, entityItr.value());
              TIntDoubleHashMap temp1 = g.getMentionEntitySims(m);
              double temp = temp1.get(entityInternalId);
              pruned.addMentionEntitySim(
                  m, entityInternalId, 
                  temp);
            }
          }
        }
      } else {
        logger.debug("Not adding node: " + g.getNode(mentionId));
      }
    }
    // Add all edges between entities which have not been removed.
    Set<Edge> addedEdges = new HashSet<Edge>();
    for (TIntIntIterator itr = g.getEntityNodesIds().iterator(); itr.hasNext(); ) {
      itr.advance();
      int entityId = itr.value();
      if (!nodesToRemove.contains(entityId)) {
        int entityInternalId = itr.key();
        GraphNode entityNode = g.getNode(entityId);
        for (TIntDoubleIterator entityItr = entityNode.getSuccessors().iterator(); entityItr.hasNext(); ) {
          entityItr.advance();
          int neighborId = entityItr.key();
          GraphNode neighborNode = g.getNode(neighborId);
          if (neighborNode.getType().equals(GraphNodeTypes.ENTITY) && !nodesToRemove.contains(neighborId)) {
            int neighborInternalId = (int) neighborNode.getNodeData();
            Edge toAdd = new Edge(entityId, neighborId);
            if (!addedEdges.contains(toAdd) && !edgesToRemove.contains(toAdd)) {
              pruned.addEdge(entityInternalId, neighborInternalId, entityItr.value());
              addedEdges.add(toAdd);
            }
          }
        }
      }
    }
    return pruned;
  }

  private Map<ResultMention, List<ResultEntity>> createConfidenceSolution(Graph g, Map<Integer, Integer> graphMapping,
      Map<Integer, Map<Integer, Double>> entityConfidence) {
    Map<ResultMention, List<ResultEntity>> newSolution = new HashMap<ResultMention, List<ResultEntity>>();
    for (Entry<Integer, Integer> entry : graphMapping.entrySet()) {
      Integer mentionId = entry.getKey();
      Integer entityId = entry.getValue();
      Mention mention = (Mention) g.getNode(mentionId).getNodeData();
      ResultMention rm = new ResultMention(mention.getMention(), mention.getCharOffset(), mention.getCharLength());
      // Use OOKBE with 0.95 confidence as default.
      ResultEntity re = ResultEntity.getNoMatchingEntity();
      re.setScore(0.95);
      if (entityId >= 0) {
        int entityInternalId = (int) g.getNode(entityId).getNodeData();
        Entity entity = allEntities_.getEntityById(entityInternalId);
        double confidence = 0.0;
        if(entityConfidence.containsKey(mentionId) && entityConfidence.get(mentionId).containsKey(entityId)) {//Because we have some results from before
          confidence = entityConfidence.get(mentionId).get(entityId);
        }
        else {
          if (!input_.getResultMentions().containsKey(mention.getCharOffset())) {
            System.out.println("CocktailParty 639: was not in entity confidence and was not in result mentions " + mention + " " + mentionId + " " + entity);
          }
        }
        re = new ResultEntity(entity, confidence);
      }
      List<ResultEntity> resultEntities = new ArrayList<ResultEntity>(1);
      resultEntities.add(re);
      newSolution.put(rm, resultEntities);
    }
    return newSolution;
  }

  private Map<Integer, Map<Integer, Double>> computeConfidence(Graph g, Map<Integer, Integer> graphIdMapping, ConfidenceSettings confSettings) {
    GraphConfidenceEstimator estimator = new GraphConfidenceEstimator(g, graphIdMapping);
    return estimator.estimate(confSettings);
  }

  protected int getDiameter() throws IOException {
    throw new IllegalStateException("this is completely broken - always use CocktailPartySizeConstrained!");
  }

  protected double calculateObjective(double absoluteMinimumWeightedDegree, Map<Integer, Double> ewd) {
    if (graphSettings.shouldUseNormalizedObjective()) {
      return absoluteMinimumWeightedDegree / ewd.size();
    } else {
      return absoluteMinimumWeightedDegree;
    }
  }
  
  private TIntLinkedList getEntityMentionsNodesIds(Graph graph, int entityNodeId) {
    TIntLinkedList mentions = new TIntLinkedList();

    GraphNode entityNode = graph.getNode(entityNodeId);
    TIntDoubleHashMap successorsMap = entityNode.getSuccessors();
    TIntDoubleIterator successorsIterator = successorsMap.iterator();
    for (int i = successorsMap.size(); i-- > 0; ) {
      successorsIterator.advance();

      int successorId = successorsIterator.key();
      GraphNode successorNode = graph.getNode(successorId);
      if (successorNode.getType() == GraphNodeTypes.MENTION) {
        mentions.add(successorId);
      }
    }
    return mentions;
  }

  private Map<Integer, Double> getConnectedEntities(Graph graph, int nodeId) {
    Map<Integer, Double> entities = new HashMap<Integer, Double>();

    GraphNode entityNode = graph.getNode(nodeId);

    TIntDoubleHashMap successorsMap = entityNode.getSuccessors();
    TIntDoubleIterator successorsIterator = successorsMap.iterator();
    for (int i = successorsMap.size(); i-- > 0; ) {
      successorsIterator.advance();

      int successorId = successorsIterator.key();
      GraphNode successorNode = graph.getNode(successorId);

      if (successorNode.getType() == GraphNodeTypes.ENTITY) {
        int entity = (int) successorNode.getNodeData();
        double weight = successorsIterator.value();

        entities.put(entity, weight);
      }
    }
    return entities;
  }

  /**
   * Fill in the solution, compute average closeness. Return the mapping
   * in the original graph as byproduct.
   *
   * @param finalEntities
   * @param allCloseness
   * @return mention-entity mapping in the original graph.
   */
  private Map<Integer, Integer> fillInSolutionObject(Graph graph, HashSet<Integer> finalEntities, double[][] allCloseness) {
    Map<Integer, Integer> graphMapping = new HashMap<Integer, Integer>();
    for (int mentionNodeId : bestMentionDegrees.keySet()) {
      GraphNode mentionNode = graph.getNode(mentionNodeId);
      Mention mention = (Mention) mentionNode.getNodeData();

      ResultMention rm = new ResultMention(mention.getMention(), mention.getCharOffset(), mention.getCharLength());

      int mentionOutdegree = graph.getNodeOutdegree(mentionNodeId);
      if (mentionOutdegree == 0) {
        solution.put(rm, ResultEntity.getResultEntityAsList(ResultEntity.getNoMatchingEntity()));
        graphMapping.put(mentionNodeId, -1);
      } else {
        TIntDoubleHashMap successorsMap = mentionNode.getSuccessors();
        TIntDoubleIterator successorsIterator = successorsMap.iterator();
        for (int i = successorsMap.size(); i-- > 0; ) {
          successorsIterator.advance();

          int entityNodeId = successorsIterator.key();
          double mentionEntitySimilarity = successorsIterator.value();
          if (finalEntities.contains(entityNodeId)) {
            double confidence = mentionEntitySimilarity;
            double averageCloseness = 0.0;

            for (int otherMention : bestMentionDegrees.keySet()) {
              if (otherMention == mentionNodeId || allCloseness[entityNodeId][otherMention] == Double.NEGATIVE_INFINITY) {
                continue;
              }
              averageCloseness += allCloseness[entityNodeId][otherMention];
            }

            int numOtherMentions = bestMentionDegrees.keySet().size() - 1;
            if (numOtherMentions > 0) {
              averageCloseness = averageCloseness / numOtherMentions;
            }
            confidence += averageCloseness;

            GraphNode entityNode = graph.getNode(entityNodeId);
            int entityInternalId = (int) entityNode.getNodeData();
            Entity entity = allEntities_.getEntityById(entityInternalId);
            List<ResultEntity> res = new ArrayList<ResultEntity>(1);
            res.add(new ResultEntity(entity, confidence));

            graphMapping.put(mentionNodeId, entityNodeId);
            solution.put(rm, res);
          }
        }
      }
    }
    return graphMapping;
  }

  private void updateNeighboringNodes(Graph graph, int removableMinimumNodeId) {
    GraphNode node = graph.getNode(removableMinimumNodeId);
    TIntDoubleHashMap successorsMap = node.getSuccessors();
    TIntDoubleIterator successorsIterator = successorsMap.iterator();
    for (int i = successorsMap.size(); i-- > 0; ) {
      successorsIterator.advance();

      int successorId = successorsIterator.key();
      double edgeWeight = successorsIterator.value();

      GraphNode successorNode = graph.getNode(successorId);
      if (successorNode.getType() == GraphNodeTypes.MENTION) {
        // successor is a mention node, just update the degree
        int mentionNodeDegree = mentionDegrees.get(successorId);
        mentionDegrees.put(successorId, --mentionNodeDegree);
        if (mentionNodeDegree == 1) {
          // this mention has one remaining candidate
          // Find this remaining candidate
          TIntDoubleHashMap candidatesMap = successorNode.getSuccessors();
          TIntDoubleIterator candidatesIterator = candidatesMap.iterator();
          for (int j = candidatesMap.size(); j-- > 0; ) {
            candidatesIterator.advance();
            int candidateNodeId = candidatesIterator.key();
            if (!graph.isRemoved(candidateNodeId)) {
              // mark this candidate as non removable if not
              // already marked
              if (entityWeightedDegrees.containsKey(candidateNodeId)) {
                double weightedDegree = entityWeightedDegrees.get(candidateNodeId);
                entityWeightedDegrees.remove(candidateNodeId);
                entitySortedDegrees.remove(candidateNodeId + ":::" + weightedDegree);

                notRemovableEntityWeightedDegrees.put(candidateNodeId, weightedDegree);
                notRemovableEntitySortedDegrees.add(candidateNodeId + ":::" + weightedDegree);
              }
              break;
            }
          }

        }

      } else {
        // successor is an entity. update its weighted degree
        if (entityWeightedDegrees.get(successorId) != null) {
          double oldWeightedDegree = entityWeightedDegrees.get(successorId);
          double newWeightedDegree = oldWeightedDegree - edgeWeight;
          entityWeightedDegrees.put(successorId, newWeightedDegree);
          entitySortedDegrees.remove(successorId + ":::" + oldWeightedDegree);
          entitySortedDegrees.add(successorId + ":::" + newWeightedDegree);
        } else if (notRemovableEntityWeightedDegrees.get(successorId) != null) {

          double oldWeightedDegree = notRemovableEntityWeightedDegrees.get(successorId);
          double newWeightedDegree = oldWeightedDegree - edgeWeight;
          notRemovableEntityWeightedDegrees.put(successorId, newWeightedDegree);
        }
      }
    } // end updating all the neighbor nodes
  }

  private int getRemovableMinimumNode(Graph graph) {
    int removableMinimumNode = -1;

    while (removableMinimumNode == -1 && !entitySortedDegrees.isEmpty()) {
      String minimumEntityString = entitySortedDegrees.peek();
      int minimumEntity = Integer.parseInt(minimumEntityString.split(":::")[0]);
      double minimumWeightedDegree = Double.parseDouble(minimumEntityString.split(":::")[1]);

      boolean removable = isNodeRemovable(graph, minimumEntity);
      if (!removable) {
        entityWeightedDegrees.remove(minimumEntity);
        entitySortedDegrees.remove(minimumEntity + ":::" + minimumWeightedDegree);
        notRemovableEntityWeightedDegrees.put(minimumEntity, minimumWeightedDegree);
        notRemovableEntitySortedDegrees.add(minimumEntity + ":::" + minimumWeightedDegree);
      } else {
        // Mark the entity as removable
        removableMinimumNode = minimumEntity;
      }
    }
    return removableMinimumNode;
  }

  private boolean isNodeRemovable(Graph graph, int nodeId) {
    GraphNode node = graph.getNode(nodeId);
    if (node.getType() == GraphNodeTypes.MENTION) // this is a mention node
      return false;
    // Check if the entity is removable

    TIntDoubleHashMap successorsMap = node.getSuccessors();
    TIntDoubleIterator successorsIterator = successorsMap.iterator();
    for (int i = successorsMap.size(); i-- > 0; ) {
      successorsIterator.advance();

      int successorNodeId = successorsIterator.key();
      GraphNode successorNode = graph.getNode(successorNodeId);
      // if mention and mention connected to only one entity
      if (successorNode.getType() == GraphNodeTypes.MENTION && mentionDegrees.get(successorNodeId) == 1) {
        return false;
      }

    }
    return true;
  }

  protected void removeInitialEntitiesByDistance(Graph graph) {
    ArrayList<Integer> toRemove = new ArrayList<Integer>();

    double[][] allDistances = new double[graph.getNodesCount()][graph.getNodesCount()];

    HashMap<Integer, Integer> checkMentionDegree = new HashMap<Integer, Integer>();
    HashMap<Integer, Double> mentionMaxWeightedDegree = new HashMap<Integer, Double>();
    HashMap<Integer, Integer> mentionMaxEntity = new HashMap<Integer, Integer>();

    for (int m : mentionDegrees.keySet()) {
      double[] shortest = shortestPath.run(m, graph);
      for (int e : entityWeightedDegrees.keySet()) {
        allDistances[e][m] = shortest[e];
      }
    } // end distance loop

    for (GraphNode node : graph.getNodes()) {
      int nodeId = node.getId();
      if (graph.isRemoved(nodeId)) continue;
      // If the node is a mention, skip.
      if (node.getType() == GraphNodeTypes.MENTION) {
        continue;
      }

      double entityDistance = calcEntityDistance(allDistances[nodeId]);

      if (entityDistance > distanceThreshold_) {
        TIntDoubleHashMap successorsMap = node.getSuccessors();
        TIntDoubleIterator successorsIterator = successorsMap.iterator();
        for (int i = successorsMap.size(); i-- > 0; ) {
          successorsIterator.advance();

          int successorNodeId = successorsIterator.key();

          if (!graph.isEntityNode(successorNodeId)) {
            if (checkMentionDegree.get(successorNodeId) == null) checkMentionDegree.put(successorNodeId, 1);
            else checkMentionDegree.put(successorNodeId, 1 + checkMentionDegree.get(successorNodeId));
            double weightedDegree = entityWeightedDegrees.get(nodeId);
            if (mentionMaxWeightedDegree.get(successorNodeId) == null) {
              mentionMaxWeightedDegree.put(successorNodeId, weightedDegree);
              mentionMaxEntity.put(successorNodeId, nodeId);
            } else {
              if (weightedDegree > mentionMaxWeightedDegree.get(successorNodeId)) {
                mentionMaxWeightedDegree.put(successorNodeId, weightedDegree);
                mentionMaxEntity.put(successorNodeId, nodeId);
              }
            }
          } // end mention neighbor
        }// end scanning neighbors of the entity selected
        // for
        // removal.
        if (!toRemove.contains(nodeId)) toRemove.add(nodeId);

      }
    }

    removeAndUpdateEntities(graph, toRemove, checkMentionDegree, mentionMaxEntity, mentionMaxWeightedDegree);
  }

  protected void removeAndUpdateEntities(Graph graph, List<Integer> toRemove, Map<Integer, Integer> checkMentionDegree,
      Map<Integer, Integer> mentionMaxEntity, Map<Integer, Double> mentionMaxWeightedDegree) {

    // Filter the list of entities to be removed, saving at least
    // one entity for mention
    for (int mention : checkMentionDegree.keySet()) {

      if (checkMentionDegree.get(mention).intValue() == mentionDegrees.get(mention).intValue()) {
        int maxEntity = mentionMaxEntity.get(mention);
        double maxWeightedDegree = mentionMaxWeightedDegree.get(mention);
        toRemove.remove(new Integer(maxEntity));
        entityWeightedDegrees.remove(maxEntity);
        entitySortedDegrees.remove(maxEntity + ":::" + maxWeightedDegree);

        notRemovableEntityWeightedDegrees.put(maxEntity, maxWeightedDegree);
        notRemovableEntitySortedDegrees.add(maxEntity + ":::" + maxWeightedDegree);
      }
    }

    for (int en : toRemove) {
      GraphNode node = graph.getNode(en);
      TIntDoubleHashMap successorsMap = node.getSuccessors();
      TIntDoubleIterator successorsIterator = successorsMap.iterator();
      for (int i = successorsMap.size(); i-- > 0; ) {
        successorsIterator.advance();

        int successorId = successorsIterator.key();
        double edgeWeight = successorsIterator.value();
        if (graph.isMentionNode(successorId)) {
          // Mention successor
          int oldDegree = mentionDegrees.get(successorId);
          mentionDegrees.put(successorId, --oldDegree);
        } else {
          if (entityWeightedDegrees.get(successorId) != null) {
            double oldWeightedDegree = entityWeightedDegrees.get(successorId);

            double newWeightedDegree = oldWeightedDegree - edgeWeight;
            entityWeightedDegrees.put(successorId, newWeightedDegree);
            entitySortedDegrees.remove(successorId + ":::" + oldWeightedDegree);
            entitySortedDegrees.add(successorId + ":::" + newWeightedDegree);
          } else if (notRemovableEntityWeightedDegrees.get(successorId) != null) {
            double oldWeightedDegree = notRemovableEntityWeightedDegrees.get(successorId);
            double newWeightedDegree = oldWeightedDegree - edgeWeight;
            notRemovableEntityWeightedDegrees.put(successorId, newWeightedDegree);
            notRemovableEntitySortedDegrees.remove(successorId + ":::" + oldWeightedDegree);
            notRemovableEntitySortedDegrees.add(successorId + ":::" + newWeightedDegree);
          }
        }

      } // end updating all the neighbor nodes

      double oldDegree = entityWeightedDegrees.get(en);
      entitySortedDegrees.remove(en + ":::" + oldDegree);
      entityWeightedDegrees.remove(en);
      graph.setRemoved(en);

      // removed++;
    } // end remove loop
    logger.debug("Iteration 1 Nodes removed: " + toRemove.size());

    GraphTracer.gTracer.addStat(graph.getName(), "Entities removed by distance constraint", Integer.toString(toRemove.size()));
  }

  protected double calcEntityDistance(double[] ds) {
    ArrayList<Double> finiteDistanceNodes = new ArrayList<Double>();
    double finiteDistance = 0.0;

    for (int w : mentionDegrees.keySet()) {
      if (ds[w] != Double.POSITIVE_INFINITY) {
        finiteDistanceNodes.add(ds[w]);
        finiteDistance += ds[w];
      }
    }

    double entityDistance = Double.NaN;

    if (finiteDistanceNodes.size() > 0) {
      entityDistance = finiteDistance / finiteDistanceNodes.size();
    }

    return entityDistance;
  }

  private double firstScanAndCalculateInitialObjective(Graph graph) throws IOException {
    double initialObjective = Double.POSITIVE_INFINITY;

    for (GraphNode node : graph.getNodes()) {
      if (node.getId() == null) {
        logger.error("ERROR: node has no id: " + node.getNodeData() + " " + node.getType());
      }
      int nodeId = node.getId();
      int degree = graph.getNodeOutdegree(nodeId);
      if (graph.isMentionNode(nodeId)) { // mention node
        mentionDegrees.put(nodeId, degree);
        bestMentionDegrees.put(nodeId, degree);
      } else { // entity node
        double weightedDegree = graph.getNodeWeightedDegrees(nodeId);
        boolean notRemovable = false;

        TIntDoubleHashMap successorsMap = node.getSuccessors();
        TIntDoubleIterator successorsIterator = successorsMap.iterator();
        for (int i = successorsMap.size(); i-- > 0; ) {
          successorsIterator.advance();

          int successorId = successorsIterator.key();

          if (graph.isMentionNode(successorId)) {
            // The current successor is a mention
            if (graph.getNodeOutdegree(successorId) == 1) notRemovable = true;
          }
        }

        if (notRemovable) {
          notRemovableEntityWeightedDegrees.put(nodeId, weightedDegree);
          notRemovableEntitySortedDegrees.add(nodeId + ":::" + weightedDegree);
        } else {
          entitySortedDegrees.add(nodeId + ":::" + weightedDegree);
          entityWeightedDegrees.put(nodeId, weightedDegree);
        }
        if (weightedDegree < initialObjective) {
          initialObjective = weightedDegree;
        }
      }
    }

    return initialObjective;

  }

  private void debugAndTraceInitialGraphProperties(Graph graph, int diameter, double threshold, double globalAverageWeight) {
    logger.debug("Using " + this.getClass() + " to solve");
    logger.debug("Diameter: " + diameter);
    logger.debug("Average Edge Weight: " + globalAverageWeight);
    logger.debug("Resulting threshold: " + threshold);
    logger.debug("Number of nodes: " + graph.getNodesCount());
    logger.debug("Number of edges: " + graph.getEdgesCount());

    GraphTracer.gTracer.addStat(graph.getName(), "Graph Algorithm", this.getClass().getCanonicalName());
    GraphTracer.gTracer.addStat(graph.getName(), "Diameter", Integer.toString(diameter));
    GraphTracer.gTracer.addStat(graph.getName(), "Avergage Edge Weight", Double.toString(globalAverageWeight));
    GraphTracer.gTracer.addStat(graph.getName(), "Distance Threshold", Double.toString(threshold));
    GraphTracer.gTracer.addStat(graph.getName(), "Number of Initial Nodes", Integer.toString(graph.getNodesCount()));
    GraphTracer.gTracer.addStat(graph.getName(), "Number of Initial Edges", Long.toString(graph.getEdgesCount()));
  }

  private void traceIntitialGraphStructure(Graph graph) {
    for (int menNodeId : mentionDegrees.keySet()) {
      GraphNode menNode = graph.getNode(menNodeId);
      Mention mention = (Mention) menNode.getNodeData();

      TIntDoubleHashMap successorsMap = menNode.getSuccessors();
      TIntDoubleIterator successorsIterator = successorsMap.iterator();
      for (int i = successorsMap.size(); i-- > 0; ) {
        successorsIterator.advance();

        int successorNodeId = successorsIterator.key();
        double sim = successorsIterator.value();

        double weight = 0.0;

        if (entityWeightedDegrees.containsKey(successorNodeId)) {
          weight = entityWeightedDegrees.get(successorNodeId);
        } else {
          weight = notRemovableEntityWeightedDegrees.get(successorNodeId);
        }

        GraphNode entityNode = graph.getNode(successorNodeId);
        int entity = (int) entityNode.getNodeData();

        GraphTracer.gTracer.addCandidateEntityToOriginalGraph(graph.getName(), mention.getIdentifiedRepresentation(), entity, weight, sim,
            getConnectedEntities(graph, successorNodeId));

      }
    }
  }

  private void debugAndTraceInitialDismabiguationProblemProperties(Graph graph) {
    logger.debug("Initial number of entities: " + entitySortedDegrees.size());
    logger.debug("Initial number of mentions: " + mentionDegrees.keySet().size());

    GraphTracer.gTracer.addStat(graph.getName(), "Number of Initial Mentions", Integer.toString(mentionDegrees.keySet().size()));
    GraphTracer.gTracer.addStat(graph.getName(), "Number of Initial Entities", Integer.toString(entitySortedDegrees.size()));

  }

  private void debugAndTraceFinalDismabiguationProblemProperties(Graph graph, int mentions, int entities) {

    logger.debug("Number of nodes in the final solution: ");
    logger.debug("Mentions " + mentions);
    logger.debug("Entities " + entities);

    GraphTracer.gTracer.addStat(graph.getName(), "Final Number of Mentions", Integer.toString(mentions));
    GraphTracer.gTracer.addStat(graph.getName(), "Final Number of Entities", Integer.toString(entities));
  }

  private void traceCleanedGraphStructure(Graph graph) {

    for (int menNodeId : mentionDegrees.keySet()) {

      GraphNode menNode = graph.getNode(menNodeId);
      Mention mention = (Mention) menNode.getNodeData();

      TIntDoubleHashMap successorsMap = menNode.getSuccessors();
      TIntDoubleIterator successorsIterator = successorsMap.iterator();
      for (int i = successorsMap.size(); i-- > 0; ) {
        successorsIterator.advance();

        int successorNodeId = successorsIterator.key();

        if (!graph.isRemoved(successorNodeId)) {
          double sim = 0;
          double weight = 0.0;
          if (entityWeightedDegrees.containsKey(successorNodeId)) {
            weight = entityWeightedDegrees.get(successorNodeId);
          } else {
            weight = notRemovableEntityWeightedDegrees.get(successorNodeId);
          }

          GraphNode entityNode = graph.getNode(successorNodeId);
          int entity = (int) entityNode.getNodeData();

          GraphTracer.gTracer.addCandidateEntityToCleanedGraph(graph.getName(), mention.getIdentifiedRepresentation(), entity, weight, sim);
        }
      }
    }

  }

  private void traceEntityRemovalStep(Graph graph, int removableMinimumNode, double removableMinimumWeightedDegree) {
    TIntLinkedList entityMentions = getEntityMentionsNodesIds(graph, removableMinimumNode);
    GraphNode node = graph.getNode(removableMinimumNode);
    int entity = (int) node.getNodeData();
    List<String> entityMentionsStringsIds = new LinkedList<String>();
    TIntIterator iterator = entityMentions.iterator();
    while (iterator.hasNext()) {
      int mentionNodeId = iterator.next();
      GraphNode mentionNode = graph.getNode(mentionNodeId);
      Mention mention = (Mention) mentionNode.getNodeData();
      String mentionIdString = mention.getIdentifiedRepresentation();
      entityMentionsStringsIds.add(mentionIdString);
    }
    GraphTracer.gTracer.addEntityRemovalStep(graph.getName(), entity, removableMinimumWeightedDegree, entityMentionsStringsIds);
  }

  private void traceFinalGraphStructure(Graph graph) {
    for (int menNodeId : mentionDegrees.keySet()) {

      GraphNode menNode = graph.getNode(menNodeId);
      Mention mention = (Mention) menNode.getNodeData();

      TIntDoubleHashMap successorsMap = menNode.getSuccessors();
      TIntDoubleIterator successorsIterator = successorsMap.iterator();
      for (int i = successorsMap.size(); i-- > 0; ) {
        successorsIterator.advance();

        int successorNodeId = successorsIterator.key();
        if (!bestRemoved[successorNodeId]) {

          double sim = 0;
          double weight = 0.0;

          if (bestWeightedDegrees.containsKey(successorNodeId)) {
            weight = bestWeightedDegrees.get(successorNodeId);
          } else if (notRemovableEntityWeightedDegrees.containsKey(successorNodeId)) {
            weight = notRemovableEntityWeightedDegrees.get(successorNodeId);
          } else {
            weight = GraphTracer.gTracer.getRemovedEntityDegree(graph.getName(), (int) graph.getNode(successorNodeId).getNodeData());
          }

          GraphNode entityNode = graph.getNode(successorNodeId);
          int entity = (int) entityNode.getNodeData();

          GraphTracer.gTracer.addCandidateEntityToFinalGraph(graph.getName(), mention.getIdentifiedRepresentation(), entity, weight, sim);
        }

      }
    }


    GraphTracer.gTracer.cleanRemovalSteps(graph.getName());
  }

  class Edge {

    int source;

    int target;

    public Edge(int x, int y) {
      source = Math.min(x, y);
      target = Math.max(x, y);
    }

    @Override public boolean equals(Object o) {
      if (o instanceof Edge) {
        Edge e = (Edge) o;
        return source == e.source && target == e.target;
      }
      return false;
    }

    @Override public int hashCode() {
      return source * target;
    }
  }
}
