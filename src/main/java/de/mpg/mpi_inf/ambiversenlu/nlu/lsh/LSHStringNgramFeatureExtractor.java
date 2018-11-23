package de.mpg.mpi_inf.ambiversenlu.nlu.lsh;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates features by extracting ngrams from the given strings, then hashes them to get an int representation.
 */
public class LSHStringNgramFeatureExtractor extends LSHFeatureExtractor<String> {

  private int ngramLength_;

  /**
   * Uses ngrams of length 3.
   */
  public LSHStringNgramFeatureExtractor() {
    this(3);
  }

  /**
   * Creates ngrams of the specified length.
   *
   * @param ngramLength Length of the ngram to extract.
   */
  public LSHStringNgramFeatureExtractor(int ngramLength) {
    ngramLength_ = ngramLength;
  }

  @Override public int[] convert(Collection<String> features) {
    List<Integer> featureIds = features.stream().flatMap(f -> StringUtils.getNgrams(f, ngramLength_).stream()).map(String::hashCode)
        .collect(Collectors.toList());
    int[] fIds = new int[featureIds.size()];
    int i = 0;
    for (Integer f : featureIds) {
      fIds[i++] = f;
    }
    return fIds;
  }
}
