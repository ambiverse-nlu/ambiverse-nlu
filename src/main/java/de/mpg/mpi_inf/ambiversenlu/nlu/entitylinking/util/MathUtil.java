package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class MathUtil {

  private static final Logger logger = LoggerFactory.getLogger(MathUtil.class);

  public static double rescale(double value, double min, double max) {
    if (min == max) {
      // No score or only one, return max.
      return 1.0;
    }

    if (value < min) {
      logger.debug("Wrong normalization, " + value + " not in [" + min + "," + max + "], " + "renormalizing to 0.0.");
      return 0.0;
    } else if (value > max) {
      logger.debug("Wrong normalization, " + value + " not in [" + min + "," + max + "], " + "renormalizing to 1.0.");
      return 1.0;
    }
    return (value - min) / (max - min);
  }

  public static double rescale(int value, int min, int max) {
    double dValue = value;
    double dMin = min;
    double dMax = max;
    return rescale(dValue, dMin, dMax);
  }

  public static double F1(double precision, double recall) {
    return (precision == 0 && recall == 0) ? 0 : (2 * precision * recall) / (1.0 * (precision + recall));
  }

  public static double logDamping(double value2damp, double dampingFactor) {
    if (dampingFactor <= 0d) throw new IllegalArgumentException("Damping factor must be greater than 0: " + dampingFactor);
    if (value2damp < 0d) throw new IllegalArgumentException("Value to damp must be greater or equal than 0: " + value2damp);
    return Math.log(value2damp * dampingFactor + 1) / Math.log(dampingFactor + 1);
  }

  public static <T> double computeJaccardSimilarity(Set<T> a, Set<T> b) {
    int aSize = a.size();
    Set<T> aCopy = new HashSet<>(a);
    aCopy.retainAll(b);
    int intersection = aCopy.size();
    Set<T> bCopy = new HashSet<>(b);
    bCopy.removeAll(a);
    int union = aSize + bCopy.size();
    return (double) intersection / (double) union;
  }

  public static double tfidf(int termFrequency, long documentCount, long collectionSize) {
    double idf = Math.log(new Long(collectionSize).doubleValue() / (1 + new Long(documentCount).doubleValue()));

    return termFrequency * idf;
  }
}
