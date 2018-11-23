package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.typetaxonomy.TypeTaxonomyEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3RelationNames;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This providers iterates over the subclassOf hierarch in YAGO
 * IMPORTANT: You have to iterate over all records till the end, otherwise, there will be dangling DB connections!
 *
 */
public class Yago3TypeTaxonomyDataProvider extends TypeTaxonomyEntriesDataProvider {

  private YAGO3Reader yago3Reader;

  public Yago3TypeTaxonomyDataProvider(YAGO3Reader yago3Reader) {
    this.yago3Reader = yago3Reader;
  }

  public Map<String, String> run() throws IOException {
    Map<String, String> taxonomyMap = new HashMap<>();

    List<Fact> subClassTripleSet = yago3Reader.getFacts(YAGO3RelationNames.subclassOf);

    for (Fact entry : subClassTripleSet) {
      String child = entry.getSubject();
      String parent = entry.getObject();
      taxonomyMap.put(child, parent);
    }

    return taxonomyMap;
  }

  @Override public Iterator<Entry<String, String>> iterator() {
    try {
      return run().entrySet().iterator();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override public String getKnowledgebaseName() {
    return Yago3PrepConf.YAGO3;
  }

}
