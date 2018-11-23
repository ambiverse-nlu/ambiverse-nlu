package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of different ways to compute keyphrase weights based on the
 * associativity to the respective entity.
 *
 *
 */
public class WeightComputation {

  private static final Logger logger = LoggerFactory.getLogger(WeightComputation.class);

  public enum MI_TYPE {
    MUTUAL_INFORMATION, NORMALIZED_MUTUAL_INFORMATION, NORMALIZED_POINTWISE_MUTUAL_INFORMATION
  }

  /**
   * Convenience method to call the appropriate MI-score computation according
   * to the passed type.
   *
   * @param a Occurrence count of event a (e.g. the entity doc freq).
   * @param b Occurrence count of event b (e.g. the keyphrase doc freq).
   * @param ab  Joint occurrence count of both events.
   * @param total The total size of the event space.
   * @param type  MI score type to compute
   * @return MI score between two events.
   */
  public static double computeMI(int a, int b, int ab, int total, MI_TYPE type) {
    double mi = -1337.1337;
    // Default, -1337.1337 to be identifiable, should never be returned
    switch (type) {
      case MUTUAL_INFORMATION:
        mi = computeMI(a, b, ab, total, false);
      case NORMALIZED_MUTUAL_INFORMATION:
        mi = computeMI(a, b, ab, total, true);
      case NORMALIZED_POINTWISE_MUTUAL_INFORMATION:
        mi = computeNPMI(a, b, ab, total);
      default:
        break;
    }
    return mi;
  }

  /**
   * Computes the normalized pointwise mutual information (normalized by
   * -ln(p(x,y)) ). 
   *
   * @param a Occurrence count of event a (e.g. the entity doc freq).
   * @param b Occurrence count of event b (e.g. the keyphrase doc freq).
   * @param ab  Joint occurrence count of both events.
   * @param total The total size of the event space.
   * @return Normalized Pointwise Mutual Information between a and b.
   */
  public static double computeNPMI(int a, int b, int ab, int total) {
    assert a <= total : "a: " + a + " was bigger than total: " + total;
    assert b <= total : "b: " + b + " was bigger than total: " + total;
    assert ab <= total;
    assert ab <= a : "ab: " + ab + " was bigger than a: " + a;
    assert ab <= b : "ab: " + ab + " was bigger than b: " + b;
    assert a + b - ab <= total;

    if (ab == 0) {
      return -1;  // No correlation is defined as -1.  
    }

    double aOcc = (double) a;
    double bOcc = (double) b;
    double abOcc = (double) ab;
    double totalOcc = (double) total;

    double jointProb = abOcc / totalOcc;

    if (jointProb >= 1.0) {
      // If they always occur together, npmi is 1.
      return 1.0;
    }

    double aProb = aOcc / totalOcc;
    double bProb = bOcc / totalOcc;

    double npmi = (Math.log(jointProb / (aProb * bProb))) / -Math.log(jointProb);

    assert npmi >= -1.001 : "npmi was < -1" + npmi;
    assert npmi <= 1.001 : "npmi was > 1: " + npmi;

    return npmi;
  }

  /**
   * Computes the mutual information. Set normalize to use normalization.
   *
   * @param a Occurrence count of event a (e.g. the entity doc freq).
   * @param b Occurrence count of event b (e.g. the keyphrase doc freq).
   * @param ab  Joint occurrence count of both events.
   * @param total The total size of the event space.
   * @param normalize Set to true to use normalization according to 
   *                  Press et al.: Numerical Recipies in C. 
   *                  1998 Cambridge University Press
   * @return Mutual Information between a and b.
   */
  public static double computeMI(int a, int b, int ab, int total, boolean normalize) {
    assert a < total;
    assert b < total;
    assert ab < total;
    assert ab <= a : "ab (" + ab + ") has to be <= a (" + a + ")";
    ;
    assert ab <= b : "ab (" + ab + ") has to be <= b (" + b + ")";
    assert a + b - ab <= total;

    double aOcc = (double) a;
    double bOcc = (double) b;
    double abOcc = (double) ab;
    double totalOcc = (double) total;

    // -- denominators for all cases --

    // probability that it is in the doc set
    double p_d = aOcc / totalOcc;
    if (p_d == 0.0) {
      p_d = 1.0 / totalOcc;
    }

    // probability that it is not in the doc set
    double p_Nd = (totalOcc - aOcc) / totalOcc;
    if (p_Nd == 0.0) {
      p_Nd = 1.0 / totalOcc;
    }

    // probability that it is in the keyword set
    double p_k = bOcc / totalOcc;
    if (p_k == 0.0) {
      p_k = 1.0 / totalOcc;
    }

    // probability that it is not in the keyword set
    double p_Nk = (totalOcc - bOcc) / totalOcc;
    if (p_Nk == 0.0) {
      p_Nk = 1.0 / totalOcc;
    }

    // -- calc all 4 cases --

    // both true:
    double p_d_k = getPdk(totalOcc, abOcc);
    double score = p_d_k * log2(p_d_k / (p_d * p_k));

    // !d && k
    double p_Nd_k = getPNdk(totalOcc, bOcc, abOcc);
    score += (p_Nd_k * log2(p_Nd_k / (p_Nd * p_k)));

    // d && !k
    double p_d_Nk = getPdNk(totalOcc, aOcc, abOcc);
    score += (p_d_Nk * log2(p_d_Nk / (p_d * p_Nk)));

    // both false
    double p_Nd_Nk = getPNdNk(totalOcc, aOcc, bOcc, abOcc);
    score += (p_Nd_Nk * log2(p_Nd_Nk / (p_Nd * p_Nk)));

    // -- do normalization
    if (normalize) {
      // According to Press et al.: Numerical Recipies in C. 
      // 1998 Cambridge University Press
      // Weighted average of the two uncertainty coefficients.
      double d_ent = -((p_d * log2(p_d)) + (p_Nd * log2(p_Nd)));
      double k_ent = -((p_k * log2(p_k)) + (p_Nk * log2(p_Nk)));

      score = (2 * score) / (d_ent + k_ent);
    }

    if (Double.isNaN(score)) {
      logger.warn("MI NaN");
    }

    return score;
  }

  private static double getPdk(double pc, double ic) {
    double numerator = ic;

    if (numerator == 0.0) {
      numerator = 1.0; // otherwise undefined
    }

    double prob = numerator / pc;
    return prob;
  }

  private static double getPNdk(double pc, double koc, double ic) {
    double numerator = (koc - ic);

    if (numerator <= 0.0) { // TODO: this skips keywords where the frequencies are borked
      numerator = 1.0; // otherwise undefined
    }

    double prob = numerator / pc;
    return prob;
  }

  private static double getPdNk(double pc, double doc, double ic) {
    double numerator = (doc - ic);

    if (numerator == 0.0) {
      numerator = 1.0; // otherwise undefined
    }

    double prob = numerator / pc;
    return prob;
  }

  private static double getPNdNk(double pc, double doc, double koc, double ic) {
    double numerator = (pc - (doc + koc - ic));

    if (numerator == 0.0) {
      numerator = 1.0; // otherwise undefined
    }

    double prob = numerator / pc;
    return prob;
  }

  public static double log2(double x) {
    return Math.log(x) / Math.log(2);
  }
}
