package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature;

/**
 * Categorical feature.
 */
public abstract class CategoricalFeature extends Feature {

  public abstract int getId();

  @Override public int getMaxFeatureId() {
    return getRange().getId() + getRange().getCount();
  }

  protected abstract Features.CategoricalFeatureRange getRange();

  /**
   * @return Number of categories in this feature.
   */
  public int getCategoryCount() {
    return getRange().getCount();
  }

  /**
   * @param category
   * @return Numerical value associated with category.
   */
  public abstract double getValue(String category);
}
