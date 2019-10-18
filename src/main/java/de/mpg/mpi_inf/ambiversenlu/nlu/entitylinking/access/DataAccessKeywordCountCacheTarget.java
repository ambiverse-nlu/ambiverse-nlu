package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import java.io.File;

public class DataAccessKeywordCountCacheTarget extends DataAccessIntIntCacheTarget {

  public static final String ID = "KEYWORD_COUNT";

  public DataAccessKeywordCountCacheTarget(String path) {
    super(path);
  }

  @Override public String getId() {
    return ID;
  }

  @Override protected File getCacheFile() {
    return new File(path + "/" + "aida-keyword_count.cache");
  }

  @Override protected void loadFromDb() throws EntityLinkingDataAccessException {
    data_ = DataAccess.getAllKeywordDocumentFrequencies();
  }
}
