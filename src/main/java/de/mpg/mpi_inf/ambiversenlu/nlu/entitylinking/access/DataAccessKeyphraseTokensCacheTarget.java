package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DataAccessKeyphraseTokensCacheTarget extends DataAccessCacheTarget {

  public static final String ID = "KEYPHRASE_TOKENS";

  private TIntObjectHashMap<int[]> data_;

  @Override public String getId() {
    return ID;
  }

  @Override protected File getCacheFile() {
    return new File("aida-keyphrase_tokens.cache");
  }

  @Override protected void loadFromDb() throws EntityLinkingDataAccessException {
    data_ = DataAccess.getAllKeyphraseTokens();
  }

  protected void loadFromDisk() throws IOException {
    File cacheFile = getCacheFile();
    DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(cacheFile))));
    int keyphraseCount = in.readInt();
    data_ = new TIntObjectHashMap<>((int) (keyphraseCount / Constants.DEFAULT_LOAD_FACTOR));
    for (int i = 0; i < keyphraseCount; ++i) {
      int wordId = in.readInt();
      int arrLen = in.readInt();
      int[] arrTokens = new int[arrLen];
      for (int j = 0; j < arrLen; j++) {
        arrTokens[j] = in.readInt();
      }
      data_.put(wordId, arrTokens);
    }
    in.close();
  }

  protected void cacheToDisk() throws IOException {
    File cacheFile = getCacheFile();
    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(cacheFile))));
    out.writeInt(data_.size());
    for (TIntObjectIterator<int[]> itr = data_.iterator(); itr.hasNext(); ) {
      itr.advance();
      out.writeInt(itr.key());
      out.writeInt(itr.value().length);
      for (int value : itr.value()) {
        out.writeInt(value);
      }
    }
    out.flush();
    out.close();
  }

  public int[] getData(int wordId) {
    return data_.get(wordId);
  }

  public TIntObjectHashMap<int[]> getAllData() {
    return data_;
  }
}
