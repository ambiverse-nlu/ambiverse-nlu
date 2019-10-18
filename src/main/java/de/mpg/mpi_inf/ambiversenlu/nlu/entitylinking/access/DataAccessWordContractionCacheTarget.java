package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import java.io.File;

public class DataAccessWordContractionCacheTarget extends DataAccessIntIntCacheTarget {

  public static final String ID = "WORD_CONTRACTION";

  public DataAccessWordContractionCacheTarget(String path) {
    super(path);
  }

  @Override public String getId() {
    return ID;
  }

  @Override protected File getCacheFile() {
    return new File(path + "/" + "aida-word_contractions.cache");
  }

  @Override protected void loadFromDb() throws EntityLinkingDataAccessException {
    data_ = DataAccess.getAllWordContractions();
  }
}
