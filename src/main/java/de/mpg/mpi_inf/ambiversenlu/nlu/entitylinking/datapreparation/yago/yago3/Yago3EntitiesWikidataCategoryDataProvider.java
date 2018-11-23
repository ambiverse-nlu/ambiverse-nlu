package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiestypes.EntitiesTypesEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3RelationNames;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;


public class Yago3EntitiesWikidataCategoryDataProvider extends EntitiesTypesEntriesDataProvider {
  private YAGO3Reader yago3Reader;
  
  public Yago3EntitiesWikidataCategoryDataProvider(YAGO3Reader yago3Reader) {
    this.yago3Reader = yago3Reader;
  }
  
  private Multimap<String, String> run() throws IOException {
    List<Fact> typesRelationSet = yago3Reader.getFacts(YAGO3RelationNames.hasWikipediaCategory);
    Multimap<String, String> entityTypeMap = ArrayListMultimap.create();
    
    for(Fact entry: typesRelationSet) {
      entityTypeMap.put(entry.getSubject(), entry.getObject());
    }
    
    return entityTypeMap;
  }  

  @Override
  public Iterator<Entry<String, String>> iterator() {
    try {
      return run().entries().iterator();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String getKnowledgebaseName() {
    return Yago3PrepConf.YAGO3;
  }

}
