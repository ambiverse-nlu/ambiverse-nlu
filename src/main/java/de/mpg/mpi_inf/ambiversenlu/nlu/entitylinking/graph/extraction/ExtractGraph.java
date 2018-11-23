package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.extraction;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.Graph;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EnsembleEntityEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.ParallelEntityEntityRelatednessComputation;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExtractGraph {

  private static final Logger logger = LoggerFactory.getLogger(ExtractGraph.class);

  private Graph graph;

  /** The list of mentions that were found in the text */
  private Mentions mentions;

  private Entities uniqueEntities;

  /** Id for measuring the running time */
  private Integer timerId;

  /**
   * Keeps the precalculated similarities. Do not access directly, always use
   * the getEntityEntitySimilarity() method. Not all similarities are
   * calculated because of performance.
   */
  private Map<Entity, Map<Entity, Double>> entityEntitySimilarities = Collections.synchronizedMap(new HashMap<Entity, Map<Entity, Double>>());

  private EnsembleEntityEntitySimilarity entitySimilarity;

  private int nodesCount;

  public ExtractGraph(String graphName, Mentions m, Entities ue, EnsembleEntityEntitySimilarity eeSim, double alpha) {
    mentions = m;
    uniqueEntities = ue;
    entitySimilarity = eeSim;
    nodesCount = m.getMentions().size() + ue.size();
    graph = new Graph(graphName, nodesCount, alpha);

  }

  /*
   * This method contains the sequence of steps that we must follow to
   * generate the graph I guess we need a constructor and then all the
   * sequences of steps But perhaps we only want to execute some of these
   * steps.
   */
  public Graph generateGraph() throws Exception {
    logger.debug("Building the graph ...");
    timerId = RunningTimer.recordStartTime("ExtractGraph");
    calculateSimilarities();
    addGraphNodes();
    addGraphEdges();
    RunningTimer.recordEndTime("ExtractGraph", timerId);
    return graph;
  }

  private void addGraphNodes() {
    // add mention nodes to the graph
    Integer id = RunningTimer.recordStartTime("AddGraphNode");
    for (Map<Integer, Mention> innerMap : this.mentions.getMentions().values()) {
      for (Mention mention : innerMap.values()) {
        graph.addMentionNode(mention);
      }
    }
    for (Entity entity : uniqueEntities) {
      graph.addEntityNode(entity);
    }
    RunningTimer.recordEndTime("AddGraphNode", id);
  }

  private void addGraphEdges() {
    Integer id = RunningTimer.recordStartTime("AddGraphEdge");
    // add mention-entity edges
    for (Map<Integer, Mention> innerMap : this.mentions.getMentions().values()) {
      for (Mention mention : innerMap.values()) {
        Entities entities = mention.getCandidateEntities();
        for (Entity entity : entities) {
          double meSimilarity = entity.getMentionEntitySimilarity();
          graph.addEdge(mention, entity, meSimilarity);
        }
      }
    }
    // add entity-entity edges
    for (Entity e1 : uniqueEntities.getEntities()) {
      for (Entity e2 : uniqueEntities.getEntities()) {
        if (e1.compareTo(e2) < 1) continue;
        double eeSim = getEntityEntitySimilarity(e1, e2);
        if (eeSim > 0.0) {
          graph.addEdge(e1, e2, eeSim);
        }
      }
    }
    RunningTimer.recordEndTime("AddGraphEdge", id);
  }

  private void calculateSimilarities() throws Exception {
    logger.debug("Computing EE sims on '" + graph.getName() + "' for " + uniqueEntities.size() + " entities (" + EntityLinkingConfig
        .get(EntityLinkingConfig.GRAPH_ENTITIES_COMP_THREADS) + " threads)");
    long start = System.currentTimeMillis();
    Integer id = RunningTimer.recordStartTime("EESimCalc");

    ParallelEntityEntityRelatednessComputation peerc = new ParallelEntityEntityRelatednessComputation();
    entityEntitySimilarities = peerc.computeRelatedness(entitySimilarity, uniqueEntities, mentions);

    RunningTimer.recordEndTime("EESimCalc", id);
    long dur = System.currentTimeMillis() - start;
    logger.debug("Done calculating EE sims on '" + graph.getName() + "' (" + dur / 1000 + "s)");

    // rescale mention-entity similarities and entity-entity similarities
    // to be in [0,1]
    id = RunningTimer.recordStartTime("MentionRescale");
    rescaleMentionEdgeWeights(mentions);
    RunningTimer.recordEndTime("MentionRescale", id);

    id = RunningTimer.recordStartTime("EntityRescale");
    rescaleEntityEdgeWeights(entityEntitySimilarities);
    RunningTimer.recordEndTime("EntityRescale", id);

    // scale to make absolute values of mention-entity and entity-entity
    // edges comparable - only rescale if there are EE edges
    if (entityEntitySimilarities.size() > 0) {
      double averageMentionEdgeWeight = calculateAverageMentionEdgeWeight(mentions);
      double averageEntityEdgeWeight = calculateAverageEntityEdgeWeight(uniqueEntities.getEntities());

      // no need for rescaling if one side is only zeros      
      if (!Double.isNaN(averageEntityEdgeWeight) && !Double.isNaN(averageMentionEdgeWeight) && averageEntityEdgeWeight > 0.0
          && averageMentionEdgeWeight > 0.0) {
        double mentionEntityScaling = averageMentionEdgeWeight / averageEntityEdgeWeight;

        // always rescale the side with the larger weights to stay inside
        // [0,1]
        id = RunningTimer.recordStartTime("ScaleME");
        if (mentionEntityScaling > 1.0) {
          scaleMentionEntityEdges(mentions, 1 / mentionEntityScaling);
        } else {
          scaleEntityEntityEdges(uniqueEntities.getEntities(), mentionEntityScaling);
        }
        RunningTimer.recordEndTime("ScaleME", id);
      }
    }

    double averageMentionEdgeWeight = calculateAverageMentionEdgeWeight(mentions);
    double averageEntityEdgeWeight = 0.0;

    if (entityEntitySimilarities.size() > 0) {
      averageEntityEdgeWeight = calculateAverageEntityEdgeWeight(uniqueEntities.getEntities());
    }
    /*
     * FileWriter writer = new FileWriter(graphBasename + ".weights");
     * writer.write("average-mention-entity-edgeweight=" +
     * averageMentionEdgeWeight + "\n");
     * writer.write("average-entity-entity-edgeweight=" +
     * averageEntityEdgeWeight + "\n"); writer.flush(); writer.close();
     */
    graph.setAverageMEweight(averageMentionEdgeWeight);
    graph.setAverageEEweight(averageEntityEdgeWeight);
  }

  private void rescaleMentionEdgeWeights(Mentions ms) {
    // get max/min
    double maxWeight = 0.0;
    double minWeight = Double.MAX_VALUE;

    double totalWeight = 0.0;

    int edgeCount = 0;

    for (Map<Integer, Mention> innerMap : ms.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        for (Entity e : m.getCandidateEntities()) {
          edgeCount++;
          totalWeight += e.getMentionEntitySimilarity();
    
          if (e.getMentionEntitySimilarity() > maxWeight) {
            maxWeight = e.getMentionEntitySimilarity();
          }
    
          if (e.getMentionEntitySimilarity() < minWeight) {
            minWeight = e.getMentionEntitySimilarity();
          }
        }
      }
    }

    if (edgeCount <= 1 || maxWeight == minWeight || totalWeight == 0.0) {
      // don't rescale
      return;
    }

    // rescale
    for (Map<Integer, Mention> innerMap : ms.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        for (Entity e : m.getCandidateEntities()) {
          double scaledWeight = (e.getMentionEntitySimilarity() - minWeight) / (maxWeight - minWeight);
          e.setMentionEntitySimilarity(scaledWeight);
        }
      }
    }
  }

  private void rescaleEntityEdgeWeights(Map<Entity, Map<Entity, Double>> ees) {
    // get max/min
    double maxWeight = 0.0;
    double minWeight = Double.MAX_VALUE;

    int edgeCount = 0;

    for (Entity e1 : ees.keySet()) {
      for (Entity e2 : ees.get(e1).keySet()) {
        edgeCount++;
        double weight = getEntityEntitySimilarity(e1, e2);
        if (weight > maxWeight) {
          maxWeight = weight;
        }
        if (weight < minWeight) {
          minWeight = weight;
        }
      }
    }
    if (edgeCount <= 1 || maxWeight == minWeight) {
      // don't rescale
      return;
    }
    // rescale
    for (Entity e1 : ees.keySet()) {
      for (Entity e2 : ees.get(e1).keySet()) {
        double scaledWeight = (getEntityEntitySimilarity(e1, e2) - minWeight) / (maxWeight - minWeight);
        setEntityEntitySimilarity(e1, e2, scaledWeight);
      }
    }
  }

  private double calculateAverageMentionEdgeWeight(Mentions ms) {
    Integer id = RunningTimer.recordStartTime("calcAvgMEWeight");
    double totalEdgeWeight = 0.0;
    long totalEdgeCount = 0;
    for (Map<Integer, Mention> innerMap : ms.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        for (Entity e : m.getCandidateEntities()) {
          double sim = e.getMentionEntitySimilarity();
          if (sim > 0.0) {
            totalEdgeWeight += sim;
            totalEdgeCount++;
          }
        }
      }
    }

    if (totalEdgeCount == 0) {
      RunningTimer.recordEndTime("calcAvgMEWeight", id);
      return 0.0;
    }

    double averageEdgeWeight = totalEdgeWeight / totalEdgeCount;
    RunningTimer.recordEndTime("calcAvgMEWeight", id);
    return averageEdgeWeight;
  }

  private double calculateAverageEntityEdgeWeight(Set<Entity> ue) {
    Integer id = RunningTimer.recordStartTime("calcAvgEEWeight");
    double totalEdgeWeight = 0.0;
    long totalEdgeCount = 0;
    for (Entity e : ue) {
      if (entityEntitySimilarities.get(e) == null) {
        continue;
      }
      for (Entity adjancentE : entityEntitySimilarities.get(e).keySet()) {
        double eeWeight = getEntityEntitySimilarity(e, adjancentE);

        // edges with 0 weight do not exist
        if (eeWeight > 0.0) {
          totalEdgeCount++;
          totalEdgeWeight += eeWeight;
        }
      }
    }

    // each edge is counted twice (for each entity), but that doesn't affect
    // the average weight
    RunningTimer.recordEndTime("calcAvgEEWeight", id);
    if (totalEdgeCount > 0) {
      double averageEntityEdgeWeight = totalEdgeWeight / totalEdgeCount;
      return averageEntityEdgeWeight;
    } else {
      return 0.0;
    }
  }

  private void scaleMentionEntityEdges(Mentions ms, double scalingFactor) {
    for (Map<Integer, Mention> innerMap : ms.getMentions().values()) {
      for (Mention m : innerMap.values()) {
        for (Entity e : m.getCandidateEntities()) {
          double scaledWeight = e.getMentionEntitySimilarity() * scalingFactor;
          e.setMentionEntitySimilarity(scaledWeight);
        }
      }
    }
  }

  private void scaleEntityEntityEdges(Set<Entity> ue, double scalingFactor) {
    for (Entity e : ue) {
      if (entityEntitySimilarities.get(e) == null) {
        continue;
      }
      for (Entity adjacentE : entityEntitySimilarities.get(e).keySet()) {
        double scaledWeight = getEntityEntitySimilarity(e, adjacentE) * scalingFactor;
        entityEntitySimilarities.get(e).put(adjacentE, scaledWeight);
      }
    }
  }

  private void setEntityEntitySimilarity(Entity e1, Entity e2, double sim) {
    if (e1.compareTo(e2) < 0) {
      entityEntitySimilarities.get(e1).put(e2, sim);
    } else {
      entityEntitySimilarities.get(e2).put(e1, sim);
    }
  }

  private double getEntityEntitySimilarity(Entity e1, Entity e2) {
    if (e1.equals(e2)) {
      return 1.0; // entities are fully similar to themselves
    }

    Entity first = e1;
    Entity second = e2;

    if (e1.compareTo(e2) > 0) {
      second = e1;
      first = e2;
    }

    if (entityEntitySimilarities.get(first) == null || entityEntitySimilarities.get(first).get(second) == null) {
      return -1.0;
    } else {
      return entityEntitySimilarities.get(first).get(second);
    }
  }
}
