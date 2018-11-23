package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.knowledgebasemetadata;

import java.io.IOException;
import java.util.Map;

/**
 * Provides Metadata about the KB that should be part of the final AIDA repository.
 */
public abstract class KnowledgeBaseMetaDataProvider {

  /**
   * Implement this method to provide MetaData in key-value format.
   * AIDA will keep this in the 'meta' table with a 'KB:' prefix to the key.
   * @return Metadata as key-value.
   */
  public abstract Map<String, String> getMetaData() throws IOException;
}
