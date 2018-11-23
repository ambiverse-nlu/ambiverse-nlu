package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.util;


public class VectorHelpers {

  public static double calcDotProduct(double[] v1, double[] v2) {
    double result = 0;
    if (v1.length != v2.length) {
      System.err.println("Size of the word vectors have to be the same.");
      return result;
    }
    for (int i = 0; i<v1.length; i++) {
      result += (v1[i]*v2[i]);
    }
    return result;
  }
  
}
