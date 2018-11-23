package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class DataAccessUnitCountCacheTarget extends DataAccessIntIntCacheTarget {

  private Logger logger_ = LoggerFactory.getLogger(DataAccessUnitCountCacheTarget.class);

  public final String ID;

  private UnitType unitType;

  public DataAccessUnitCountCacheTarget(UnitType unitType) {
    this.unitType = unitType;
    ID = unitType.getUnitName().toUpperCase() + "_COUNT";
  }

  @Override public String getId() {
    return ID;
  }

  @Override protected File getCacheFile() {
    return new File("aida-" + unitType.getUnitName() + "_count.cache");
  }

  @Override protected void loadFromDb() throws EntityLinkingDataAccessException {
    data_ = DataAccess.getAllUnitDocumentFrequencies(unitType);
    if (data_ == null) logger_.info("Could not load " + getId() + " from DB.");
  }

  @Override protected void cacheToDisk() throws IOException {
    if (data_ != null) super.cacheToDisk();
    else logger_.info("Caching failed because nothing loaded.");
  }
}
