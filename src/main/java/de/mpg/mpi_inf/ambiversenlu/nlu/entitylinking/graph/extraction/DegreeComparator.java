package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.extraction;

import java.util.Comparator;

public class DegreeComparator implements Comparator<String> {

  @Override public int compare(String arg0, String arg1) {
    // I want to use the opposite order, so that I can build a max priority queue using the default
    // implementation of a min priority queue
    String first = arg0;
    String second = arg1;
    Double firstDegree = Double.parseDouble(first.split(":::")[1]);
    Double secondDegree = Double.parseDouble(second.split(":::")[1]);
    return firstDegree.compareTo(secondDegree);
  }
}
