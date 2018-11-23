package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import java.io.File;

public class DataAccessWordExpansionCacheTarget extends DataAccessIntIntCacheTarget {

  public static final String ID = "WORD_EXPANSION";

  @Override public String getId() {
    return ID;
  }

  @Override protected File getCacheFile() {
    return new File("aida-word_expansions.cache");
  }

  @Override protected void loadFromDb() throws EntityLinkingDataAccessException {
    data_ = DataAccess.getAllWordExpansions();
  }
}
