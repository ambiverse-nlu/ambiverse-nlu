package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.knowledgebasemetadata;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Dummy provider, giving empty metadata.
 */
public class DummyKnowledgeBaseMetaDataProvider extends KnowledgeBaseMetaDataProvider {

  @Override public Map<String, String> getMetaData() throws IOException {
    return new HashMap<>();
  }
}
