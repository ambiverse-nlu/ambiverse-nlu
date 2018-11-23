package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class DataAccessCacheTarget {

  private Logger logger_ = LoggerFactory.getLogger(DataAccessCacheTarget.class);

  public abstract String getId();

  protected abstract File getCacheFile();

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
      logger_.info(getId() + " cache file doesn't exist.");
      requireReadFromDB = true;
    }

    if (requireReadFromDB) {
      logger_.info("Loading " + getId() + " from DB.");
      loadFromDb();
      logger_.info("Caching " + getId() + " to disk.");
      cacheToDisk();
    }
  }

  protected abstract void loadFromDb() throws EntityLinkingDataAccessException;

  protected abstract void loadFromDisk() throws IOException;

  protected abstract void cacheToDisk() throws IOException;
}
