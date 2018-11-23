package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class DataAccessIntIntCacheTarget extends DataAccessCacheTarget {

  private Logger logger_ = LoggerFactory.getLogger(DataAccessIntIntCacheTarget.class);

  protected int[] data_;

  public abstract String getId();

  protected abstract File getCacheFile();

  public int getData(int id) {
    assert id >= 0 : "id must not be negative.";
    if (id >= data_.length) {
      throw new IllegalArgumentException("id out of range.");
    }
    return data_[id];
  }

  public void createAndLoadCache(boolean needsCacheCreation) throws FileNotFoundException, IOException, EntityLinkingDataAccessException {
    boolean requireReadFromDB = false;
    File cacheFile = getCacheFile();
    if (cacheFile.exists()) {
      if (!needsCacheCreation) {
        logger_.debug("Loading " + getId() + " from cache.");
        loadFromDisk();
      } else {
        cacheFile.delete();
        requireReadFromDB = true;
      }
    } else {
      logger_.debug(getId() + " cache file doesn't exist.");
      requireReadFromDB = true;
    }

    if (requireReadFromDB) {
      logger_.debug("Loading " + getId() + " from DB.");
      loadFromDb();
      logger_.debug("Caching " + getId() + " to disk.");
      cacheToDisk();
    }
  }

  protected abstract void loadFromDb() throws EntityLinkingDataAccessException;

  protected void loadFromDisk() throws IOException {
    File cacheFile = getCacheFile();
    DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(cacheFile))));
    data_ = new int[in.readInt()];
    for (int i = 0; i < data_.length; ++i) {
      data_[i] = in.readInt();
    }
    in.close();
  }

  protected void cacheToDisk() throws IOException {
    File cacheFile = getCacheFile();
    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(cacheFile))));
    out.writeInt(data_.length);
    for (int exp : data_) {
      out.writeInt(exp);
    }
    out.flush();
    out.close();
  }
}
