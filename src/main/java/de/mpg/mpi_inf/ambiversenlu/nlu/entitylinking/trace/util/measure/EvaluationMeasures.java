package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.util.measure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationMeasures {

  public static Map<Integer, Double> convertToAverageRanks(List<List<Integer>> list) {
    Map<Integer, Double> rankedList = new HashMap<Integer, Double>();

    int i = 0;
    for (List<Integer> entityPartition : list) {
      double avgRank = 0.0;

      for (@SuppressWarnings("unused") Integer entity : entityPartition) {
        i++;
        avgRank += i;
      }

      avgRank /= (double) entityPartition.size();

      for (Integer entity : entityPartition) {
        rankedList.put(entity, avgRank);
      }
    }

    return rankedList;
  }
}
