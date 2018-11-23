package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature;

/**
 * Feature with a static id.
 */
public abstract class StaticFeature extends Feature {

  protected abstract Features.StaticFeatureRange getRange();

  @Override public int getMaxFeatureId() {
    return getId();
  }

  public int getId() {
    return getRange().getId();
  }
}
