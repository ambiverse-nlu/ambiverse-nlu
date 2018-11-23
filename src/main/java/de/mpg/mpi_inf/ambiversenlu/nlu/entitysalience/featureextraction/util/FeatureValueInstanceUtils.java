package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utils for converting FeatureValueInstances
 */
public class FeatureValueInstanceUtils {

  public static Vector convertToSparkMLVector(FeatureValueInstance fvi, int vectorSize) {
    Map<Integer, Double> featureValues = fvi.getFeatureValues();
    List<Tuple2<Integer, Double>> sortedFeatureValues =
        featureValues.entrySet().stream()
            .sorted((o1, o2) -> Integer.compare(o1.getKey(), o2.getKey()))
            .map(o -> new Tuple2<>(o.getKey(), o.getValue()))
            .collect(Collectors.toList());

    int[] features = new int[sortedFeatureValues.size()];
    double[] values = new double[sortedFeatureValues.size()];

    int i = 0;
    for (Tuple2<Integer, Double> fv : sortedFeatureValues) {
      features[i] = fv._1();
      values[i] = fv._2();
      ++i;
    }

    Vector v = Vectors.sparse(vectorSize, features, values);
    return v;
  }

  public static LabeledPoint convertToSparkMLLabeledPoint(TrainingInstance ti, int vectorSize) {
    Vector v = convertToSparkMLVector(ti, vectorSize);
    LabeledPoint lp = new LabeledPoint(ti.getLabel(), v);
    return lp;
  }
}
