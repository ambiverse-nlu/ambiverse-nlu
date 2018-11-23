package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DataAccessEntitiesCacheTarget extends DataAccessCacheTarget {
  
  public static final String ID = "ENTITIES";
  
  private TIntObjectHashMap<EntityType> data_;

  @Override
  public String getId() {
    return ID;
  }

  @Override
  protected File getCacheFile() {
    return new File("aida-entity_classes.cache");
  }

  @Override
  protected void loadFromDb() throws EntityLinkingDataAccessException {
    data_ = DataAccess.getAllEntityClasses();
  }

  @Override
  protected void loadFromDisk() throws IOException {
    File cacheFile = getCacheFile();
    DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(cacheFile))));
    int size = in.readInt();
    data_ = new TIntObjectHashMap<>((int) (size / Constants.DEFAULT_LOAD_FACTOR));
    for (int i = 0; i < size; ++i) {
      int entityId = in.readInt();
      int entityClass = in.readInt();
      data_.put(entityId, EntityType.getNameforDBId(entityClass));
    }
    in.close();
  }

  @Override
  protected void cacheToDisk() throws IOException {
    File cacheFile = getCacheFile();
    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(cacheFile))));
    out.writeInt(data_.size());
    for (TIntObjectIterator<EntityType> itr = data_.iterator(); itr.hasNext(); ) {
      itr.advance();
      out.writeInt(itr.key());
      out.writeInt(itr.value().getDBId());
    }
    out.flush();
    out.close();
  }
  
  public TIntObjectHashMap<EntityType> getAllData() {
    return data_;
  }
}
