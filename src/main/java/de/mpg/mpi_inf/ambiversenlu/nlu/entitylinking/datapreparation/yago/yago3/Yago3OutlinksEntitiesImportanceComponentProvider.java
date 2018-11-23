package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entityimportancecomponent.EntityImportanceComponentEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3RelationNames;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Yago3OutlinksEntitiesImportanceComponentProvider extends EntityImportanceComponentEntriesDataProvider {

  private static final Logger logger = LoggerFactory.getLogger(Yago3OutlinksEntitiesImportanceComponentProvider.class);

  private static String DB_NAME = "yago_per_entity_outlinks_counts";

  private YAGO3Reader yago3Reader;

  public Yago3OutlinksEntitiesImportanceComponentProvider(YAGO3Reader yago3Reader) {
    this.yago3Reader = yago3Reader;
  }

  @Override public Iterator<Entry<String, Integer>> iterator() {
    Map<String, Set<String>> entitiesInlinks = new HashMap<>();

    List<Fact> hasInternalLinkSet = null;
    try {
      hasInternalLinkSet = yago3Reader.getFacts(YAGO3RelationNames.hasInternalWikipediaLinkTo);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    for (Fact entry : hasInternalLinkSet) {
      String entityName = entry.getSubject();
      String outlinkName = entry.getObject();
      Set<String> inlinks = entitiesInlinks.get(entityName);
      if (inlinks == null) {
        inlinks = new HashSet<>();
        entitiesInlinks.put(entityName, inlinks);
      }
      inlinks.add(outlinkName);
    }

    logger.info("Counting the outlinks  per entity ...");
    Map<String, Integer> inlinksCounts = new HashMap<String, Integer>(entitiesInlinks.size());
    for (Entry<String, Set<String>> entry : entitiesInlinks.entrySet()) {
      inlinksCounts.put(entry.getKey(), entry.getValue().size());
    }

    return inlinksCounts.entrySet().iterator();
  }

  @Override public String getDBTableNameToStoreIn() {
    return DB_NAME;
  }

  public static String getDBName() {
    return DB_NAME;
  }

  @Override public String getKnowledgebaseName() {
    return Yago3PrepConf.YAGO3;
  }

}
