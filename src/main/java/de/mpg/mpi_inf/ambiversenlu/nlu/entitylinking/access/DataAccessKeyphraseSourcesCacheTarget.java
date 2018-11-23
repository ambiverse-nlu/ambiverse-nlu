package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.File;
import java.io.IOException;

public class DataAccessKeyphraseSourcesCacheTarget extends DataAccessCacheTarget {

  public static final String ID = "KEYPHRASE_SOURCES";

  private TObjectIntHashMap<String> data_;

  @Override public String getId() {
    return ID;
  }

  @Override protected File getCacheFile() {
    return new File("aida-keyphrase_sources.cache");
  }

  @Override protected void loadFromDb() throws EntityLinkingDataAccessException {
    data_ = DataAccess.getAllKeyphraseSources();
  }

  public int getData(String source) {
    return data_.get(source);
  }

  public TObjectIntHashMap<String> getAllData() {
    return data_;
  }

  @Override protected void loadFromDisk() throws IOException {
    // Auto-generated method stub

  }

  @Override protected void cacheToDisk() throws IOException {
    // Auto-generated method stub

  }
}
