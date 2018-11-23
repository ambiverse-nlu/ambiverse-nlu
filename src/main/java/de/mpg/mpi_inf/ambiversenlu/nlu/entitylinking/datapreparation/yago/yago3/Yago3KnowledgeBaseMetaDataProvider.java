package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.knowledgebasemetadata.KnowledgeBaseMetaDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the YAGO3 <_yagoMetadata> relation.
 */
public class Yago3KnowledgeBaseMetaDataProvider extends KnowledgeBaseMetaDataProvider {

  private YAGO3Reader reader_;

  public Yago3KnowledgeBaseMetaDataProvider(YAGO3Reader yago3Reader) {
    reader_ = yago3Reader;
  }

  @Override public Map<String, String> getMetaData() throws IOException {
    List<Fact> metadata = null;
    try {
      metadata = reader_.getFacts("<_yagoMetadata>");
    } catch (IOException e) {
      throw new IOException(e);
    }

    Map<String, String> metadataPairs = new HashMap<>();
    for (Fact f : metadata) {
      metadataPairs.put(f.getSubject(), f.getObject());
    }

    return metadataPairs;
  }
}
