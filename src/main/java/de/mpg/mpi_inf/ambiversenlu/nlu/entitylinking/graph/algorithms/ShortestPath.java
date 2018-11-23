package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.Graph;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.GraphNode;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.PriorityQueue;

/** Solves SSSP problem for an arc-labelled graph with non-negative edge weights */

public class ShortestPath {

  private static final Logger logger = LoggerFactory.getLogger(ShortestPath.class);

  // Use a method that takes the vector of flags telling which nodes have been
  // removed - THIS IS USED BY FCPSC
  public double[] run(int mention, Graph graph) {

    Integer id = RunningTimer.recordStartTime("ShortestPathRun");
    int nodesCount = graph.getNodesCount();
    double[] distances = new double[nodesCount];

    Arrays.fill(distances, Double.POSITIVE_INFINITY);

    int[] previous = new int[nodesCount];
    Arrays.fill(previous, -1);

    PriorityQueue<Node> unvisited = new PriorityQueue<Node>(nodesCount, new NodeComparator());

    mloop:
    for (int n = 0; n < nodesCount; n++) {
      Node current = null;
      if (graph.isRemoved(n)) continue mloop;
      if (n == mention) {
        current = new Node(n, 0.0);
        unvisited.add(current);
      } else {
        current = new Node(n, Double.POSITIVE_INFINITY);
        unvisited.add(current);
      }
    }

    // mainloop
    while (!unvisited.isEmpty()) {

      Node current = unvisited.poll();

      if (current.getDistance() == Double.POSITIVE_INFINITY) break; // all the remaining nodes are unreachable from the
      // source node
      distances[current.getKey()] = current.getDistance();

      GraphNode currentGraphNode = graph.getNode(current.getKey());

      TIntDoubleHashMap successorsMap = currentGraphNode.getSuccessors();
      TIntDoubleIterator successorsIterator = successorsMap.iterator();
      for (int i = successorsMap.size(); i-- > 0; ) {
        successorsIterator.advance();

        int neighborId = successorsIterator.key();
        if (graph.isRemoved(neighborId)) continue;

        double weight = successorsIterator.value();

        // All the similarity measures are in 0,1
        double distance = 1.0 - weight;

        if (distance < 0.0 || distance > 1.0) {
          logger.error("VIOLATION");
          throw new IllegalArgumentException("Distance '" + distance + "' not in [0,1], this is not valid, EXITING!");
        }

        if (distances[neighborId] > (distances[current.getKey()] + distance)) {
          distances[neighborId] = distances[current.getKey()] + distance;

          Node nnode = new Node(neighborId, distances[neighborId]);
          unvisited.remove(nnode);
          unvisited.add(nnode);
          previous[neighborId] = current.getKey();
        }

      } // end scanning all the neighbors of the current node

    } // end main loop

    // Return the distances
    RunningTimer.recordEndTime("ShortestPathRun", id);
    return distances;
  }

  public double[] closeness(int mention, Graph graph, boolean[] isRemoved) {

    double[] distances = new double[graph.getNodesCount()];
    double[] closeValues = new double[graph.getNodesCount()];

    Arrays.fill(distances, Double.POSITIVE_INFINITY);
    Arrays.fill(closeValues, Double.NEGATIVE_INFINITY);

    int[] previous = new int[graph.getNodesCount()];
    Arrays.fill(previous, -1);

    PriorityQueue<Node> unvisited = new PriorityQueue<Node>(graph.getNodesCount(), new NodeComparator());

    mloop:
    for (int n = 0; n < graph.getNodesCount(); n++) {

      Node current = null;

      if (isRemoved[n]) continue mloop;

      if (n == mention) {

        current = new Node(n, 0.0);
        closeValues[n] = 0.0;
        unvisited.add(current);
      } else {

        current = new Node(n, Double.POSITIVE_INFINITY);
        unvisited.add(current);
      }
    }

    // mainloop
    while (!unvisited.isEmpty()) {

      Node current = unvisited.poll();
      if (current.getDistance() == Double.POSITIVE_INFINITY) break; // all the remaining nodes are unreachable from the
      // source node

      distances[current.getKey()] = current.getDistance();

      GraphNode currentGraphNode = graph.getNode(current.getKey());

      TIntDoubleHashMap successorsMap = currentGraphNode.getSuccessors();
      TIntDoubleIterator successorsIterator = successorsMap.iterator();
      for (int i = successorsMap.size(); i-- > 0; ) {
        successorsIterator.advance();

        int neighborId = successorsIterator.key();
        if (isRemoved[neighborId]) continue;

        double weight = successorsIterator.value();

        // All the similarity measures are in 0,1
        double distance = 1.0 - weight;

        if (distances[neighborId] > (distances[current.getKey()] + distance)) {
          distances[neighborId] = distances[current.getKey()] + distance;
          closeValues[neighborId] = closeValues[current.getKey()] + weight;
          Node nnode = new Node(neighborId, distances[neighborId]);
          unvisited.remove(nnode);
          unvisited.add(nnode);
          previous[neighborId] = current.getKey();
        }

      }

    } // end main loop

    return closeValues;
  }

} // end class

