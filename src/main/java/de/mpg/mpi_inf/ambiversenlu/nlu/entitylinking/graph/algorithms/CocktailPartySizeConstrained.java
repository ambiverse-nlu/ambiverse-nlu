package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.Graph;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.GraphNode;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.PreparedInputChunk;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;


public class CocktailPartySizeConstrained extends CocktailParty {

  private int initialGraphSize;

  public CocktailPartySizeConstrained(PreparedInputChunk input, DisambiguationSettings settings, Tracer tracer) throws Exception {
    super(input, settings, tracer);
    this.initialGraphSize = settings.getGraphSettings().getEntitiesPerMentionConstraint();
  }
  
  @Override protected int getDiameter() throws IOException {
    return 1;
  }
  
  protected void removeInitialEntitiesByDistance(Graph graph) {
    ArrayList<Integer> toRemove = new ArrayList<Integer>();

    int nodesCount = graph.getNodesCount();

    double[][] allDistances = new double[nodesCount][nodesCount];

    fillDistances(graph, allDistances);

    Map<Integer, Double> entityDistances = new HashMap<Integer, Double>();

    for (int q : entityWeightedDegrees.keySet()) {
      if (graph.isRemoved(q)) continue;

      double entityDistance = calcEntityDistance(allDistances[q]);
      entityDistances.put(q, entityDistance);
    }

    List<Entry<Integer, Double>> entries = new ArrayList<Entry<Integer, Double>>(entityDistances.entrySet());

    Collections.sort(entries, new Comparator<Entry<Integer, Double>>() {

      @Override public int compare(Entry<Integer, Double> e0, Entry<Integer, Double> e1) {
        return Double.compare(e0.getValue(), e1.getValue());
      }
    });

    Map<Integer, Double> sortedEntityDistances = new LinkedHashMap<Integer, Double>();
    for (Entry<Integer, Double> entry : entries) {
      sortedEntityDistances.put(entry.getKey(), entry.getValue());
    }

    HashMap<Integer, Integer> checkMentionDegree = new HashMap<Integer, Integer>();
    HashMap<Integer, Double> mentionMaxWeightedDegree = new HashMap<Integer, Double>();
    HashMap<Integer, Integer> mentionMaxEntity = new HashMap<Integer, Integer>();

    int numberToKeep = (int) Math.ceil(mentionDegrees.size() * initialGraphSize);

    int i = 0;
    for (int entityNodeId : sortedEntityDistances.keySet()) {
      i++;

      if (i > numberToKeep) {
        toRemove.add(entityNodeId);
        GraphNode entityNode = graph.getNode(entityNodeId);
        TIntDoubleHashMap successorsMap = entityNode.getSuccessors();
        TIntDoubleIterator successorsIterator = successorsMap.iterator();
        for (int s = successorsMap.size(); s-- > 0; ) {
          successorsIterator.advance();

          int succId = successorsIterator.key();

          if (!graph.isEntityNode(succId)) {
            if (checkMentionDegree.get(succId) == null) checkMentionDegree.put(succId, 1);
            else checkMentionDegree.put(succId, 1 + checkMentionDegree.get(succId));
            double weightedDegree = entityWeightedDegrees.get(entityNodeId);
            if (mentionMaxWeightedDegree.get(succId) == null) {
              mentionMaxWeightedDegree.put(succId, weightedDegree);
              mentionMaxEntity.put(succId, entityNodeId);
            } else {
              if (weightedDegree > mentionMaxWeightedDegree.get(succId)) {
                mentionMaxWeightedDegree.put(succId, weightedDegree);
                mentionMaxEntity.put(succId, entityNodeId);
              }
            }
          } // end mention neighbor
        }// end scanning neighbors of the entity selected
        // for
        // removal.

      }
    }

    removeAndUpdateEntities(graph, toRemove, checkMentionDegree, mentionMaxEntity, mentionMaxWeightedDegree);
  }

  private void fillDistances(Graph graph, double[][] allDistances) {
    for (int m : mentionDegrees.keySet()) {
      double[] shortest = shortestPath.run(m, graph);
      for (int e : entityWeightedDegrees.keySet()) {
        allDistances[e][m] = shortest[e];
      }
    } // end distance loop
  }

  protected double calcEntityDistance(double[] ds) {
    ArrayList<Double> finiteDistanceNodes = new ArrayList<Double>();
    double finiteDistance = 0.0;

    for (int w : mentionDegrees.keySet()) {
      if (ds[w] != Double.POSITIVE_INFINITY) {
        finiteDistanceNodes.add(ds[w]);
        finiteDistance += Math.pow(ds[w], 2);
      }
    }

    return finiteDistance;
  }
}
