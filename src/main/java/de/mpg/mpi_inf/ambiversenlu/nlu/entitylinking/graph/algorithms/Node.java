package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms;

import java.util.Comparator;

/**
 * Utility class to be used in the implemenation of the shortest-path
 * algorithms. We store a node together with its distance, and then we develop a
 * comparator that sorts nodes according to their distances
 */
public class Node {

  private int key;

  private double distance;

  public Node(int k, double d) {

    key = k;
    distance = d;

  }

  public int getKey() {

    return key;
  }

  public double getDistance() {

    return distance;
  }

  public void setDistance(double d) {

    distance = d;

  }
}

class NodeComparator implements Comparator<Node> {

  public int compare(Node first, Node second) {

    // I want to use the opposite order, so that I can build a max priority
    // queue using the default
    // implementation of a min priority queue
    Double firstDistance = first.getDistance();
    Double secondDistance = second.getDistance();
    return firstDistance.compareTo(secondDistance);

  }

  public boolean equals(Node first, Node second) {

    // I just want only one node with a given key in the priority queue
    if (first.getKey() == second.getKey()) return true;
    else return false;
  }

}