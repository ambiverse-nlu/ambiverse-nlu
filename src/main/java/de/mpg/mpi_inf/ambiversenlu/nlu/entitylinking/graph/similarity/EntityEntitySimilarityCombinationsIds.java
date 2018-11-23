package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity;

import java.util.ArrayList;
import java.util.List;

public enum EntityEntitySimilarityCombinationsIds {

  MilneWitten,
  InlinkOverlap;
  
  public List<String[]> getConfig() {
    List<String[]> result = new ArrayList<>();
    if (this == MilneWitten) {
      result.add(new String[]{"MilneWittenEntityEntitySimilarity", "1.0"});
    }
    else if (this == InlinkOverlap) {
      result.add(new String[]{"InlinkOverlapEntityEntitySimilarity", "1.0"});
    }
    return result;
  }
}
