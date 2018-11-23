package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import java.util.ArrayList;
import java.util.List;

public class Util {

  public static List<Integer> asIntegerList(int[] items) {
    List<Integer> itemsList = new ArrayList<Integer>(items.length);

    for (int i = 0; i < items.length; i++) {
      itemsList.add(i, items[i]);
    }
    return itemsList;
  }
}
