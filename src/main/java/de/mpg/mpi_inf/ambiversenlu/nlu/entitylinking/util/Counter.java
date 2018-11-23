package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import java.util.HashMap;
import java.util.Map;

public class Counter {

  private static Map<String, Integer> counts = new HashMap<>();

  public static void incrementCount(String counterName) {
    synchronized (counts) {
      Integer count = counts.get(counterName);
      if (count == null) {
        count = 0;
      }
      ++count;
      counts.put(counterName, count);
    }
  }

  public static void incrementCountByValue(String counterName, int value) {
    synchronized (counts) {
      Integer count = counts.get(counterName);
      if (count == null) {
        count = 0;
      }
      count += value;
      counts.put(counterName, count);
    }
  }

  public static String getOverview() {
    StringBuilder sb = new StringBuilder();
    sb.append("COUNTER_NAME\tCOUNTER_VALUE\n");
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      sb.append(entry.getKey()).append("\t").append(entry.getValue()).append("\n");
    }
    return sb.toString();
  }
}
