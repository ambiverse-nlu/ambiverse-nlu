package de.mpg.mpi_inf.ambiversenlu.nlu.lsh;

import java.util.Collection;

/**
 * Abstract class - converts features into int[].
 */
public abstract class LSHFeatureExtractor<F> {

  public abstract int[] convert(Collection<F> features);
}
