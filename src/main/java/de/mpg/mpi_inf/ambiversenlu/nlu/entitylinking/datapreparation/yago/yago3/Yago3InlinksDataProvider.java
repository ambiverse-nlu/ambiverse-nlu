package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.inlinks.InlinksEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.YagoIdsMapper;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3RelationNames;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 * This providers iterates over all inlinks in YAGO
 *
 */
public class Yago3InlinksDataProvider extends InlinksEntriesDataProvider {

  private boolean mapYagoIdsToOtherKBIds = false;

  private YagoIdsMapper yagoIdsMapper;

  private String knowledgebaseName = Yago3PrepConf.YAGO3;

  private YAGO3Reader yago3Reader;

  public Yago3InlinksDataProvider(YAGO3Reader yago3Reader) {
    this.yago3Reader = yago3Reader;
  }

  public Yago3InlinksDataProvider(YAGO3Reader yago3Reader, YagoIdsMapper yagoIdsMapper, String knowledgebaseName) {
    this(yago3Reader);
    this.yagoIdsMapper = yagoIdsMapper;
    this.knowledgebaseName = knowledgebaseName;
    mapYagoIdsToOtherKBIds = true;
  }

  @Override public Iterator<Entry<String, String>> iterator() {
    try {
      return run().iterator();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private List<Entry<String, String>> run() throws IOException {
    List<Entry<String, String>> inlinksList = new LinkedList<Entry<String, String>>();

    List<Fact> inlinksSet = yago3Reader.getFacts(YAGO3RelationNames.hasInternalWikipediaLinkTo);

    for (Fact entry : inlinksSet) {
      String inlinkName = entry.getSubject();
      if (mapYagoIdsToOtherKBIds) {
        inlinkName = yagoIdsMapper.mapFromYagoId(inlinkName);
        if (inlinkName == null) continue;
      }

      String entityName = entry.getObject();
      if (mapYagoIdsToOtherKBIds) {
        entityName = yagoIdsMapper.mapFromYagoId(entityName);
        if (entityName == null) continue;
      }

      inlinksList.add(new AbstractMap.SimpleEntry<String, String>(inlinkName, entityName));
    }

    return inlinksList;

  }

  @Override public String getKnowledgebaseName() {
    return knowledgebaseName;
  }

}
