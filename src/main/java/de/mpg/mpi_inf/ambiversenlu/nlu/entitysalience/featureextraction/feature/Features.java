package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature;

/**
 * Enumeration to map (static, known) features to unique ids.
 */

public abstract class Features {
  public enum StaticFeatureRange {
    COUNT(0),
    FIRST_OCCURRENCE_ABSOLUTE(1),
    FIRST_OCCURRENCE_RELATIVE(2),
    IN_HEADING(3),
    IN_FIRST_SENTENCE(4),
    CONFIDENCE(5),
    TEMPORAL_IMPORTANCE(6),
    
    ROCCHIO_ENTITIES(50),
    ROCCHIO_NOUNLEMMA(51),
    
    PRIOR_GLOBAL(52),
    PRIOR_QUELLE(53),
    PRIOR_QUELLE_RUBRIK(54);

    private final int id;

    StaticFeatureRange(int id) {
      this.id = id;
    }

    public int getId() {
      return id;
    }
  }



  public enum CategoricalFeatureRange {
    ENTITY_TYPES(13, 7),
    QUELLE(14, 1000);

    private final int id;
    private final int count;

    /**
     * Defines a categorical feature by hashing the actual
     * values to ranges in [id, id + count)
     *
     * @param id Feature id.
     * @param count Number of classes.
     */
    CategoricalFeatureRange(int id, int count) {
      this.id = id;
      this.count = count;
    }

    public int getId() {
      return id;
    }

    public int getCount() {
      return count;
    }
  }

  public enum HashedFeatureRange {

    TYPES(1_000, 30_000),
    RUBRIK(30_000, 60_000),
    NOUN_LEMMA(60_000, 160_000),
    AUTHOR(160_000, 210_000),
    SIGNATURE_RANKING(210_000, 265_000),

    // Below is not used for now.
    WORDS(265_000, 365_000),
    ENTITIES(360_000, 560_000),
    KEYWORDS(22_000, 50_000);

    private final int begin;
    private final int end;

    HashedFeatureRange(int begin, int end) {
      this.begin = begin;
      this.end = end;
    }

    public int getBegin() {
      return begin;
    }

    public int getEnd() {
      return end;
    }
  }

  public static int getId(StaticFeature feature) {
    return feature.getId();
  }

  public static int getId(CategoricalFeature feature) {
    return feature.getId();
  }
}
