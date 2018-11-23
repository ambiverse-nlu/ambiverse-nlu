package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Abstract class containing feature-value pairs.
 */
public class FeatureValueInstance implements Serializable {
  private Map<Integer, Double> featureValues;

  public FeatureValueInstance(Map<Integer, Double> featureValues) {
    this.featureValues = featureValues;
  }

  public Map<Integer, Double> getFeatureValues() {
    return featureValues;
  }

  public String toLibSVMString() {
    List<String> pairs = new ArrayList<>(getFeatureValues().size());
    getFeatureValues().entrySet().stream()
        // Features need to be sorted by the id.
        .sorted((o1, o2) -> Integer.compare(o1.getKey(), o2.getKey()))
        .forEach(e -> pairs.add(e.getKey() + ":" + e.getValue()));
    return StringUtils.join(pairs, " ");
  }
}
