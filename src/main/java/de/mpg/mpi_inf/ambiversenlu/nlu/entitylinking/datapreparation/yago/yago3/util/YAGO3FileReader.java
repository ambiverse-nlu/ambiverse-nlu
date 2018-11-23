package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.FactSource.FileFactSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YAGO3FileReader implements YAGO3Reader {

  private Logger logger = LoggerFactory.getLogger(YAGO3FileReader.class);

  private String yago3FileLocation;

  private Map<String, List<Fact>> relations2SubjectObjectPairs;

  private boolean loaded = false;

  public YAGO3FileReader(String yago3FileLocation) {
    this.yago3FileLocation = yago3FileLocation;
    relations2SubjectObjectPairs = new HashMap<>();
  }

  private void load() {
    FileFactSource fileFactSource = new FileFactSource(new File(yago3FileLocation));

    int count = 0;
    for (Fact fact : fileFactSource) {
      //Remove facts on which we cannot build the index
      if (fact.getObject().length() > 1000) {
        continue;
      }
      String relation = fact.getRelation();
      List<Fact> list = relations2SubjectObjectPairs.get(relation);
      if (list == null) {
        list = new ArrayList<>();
        relations2SubjectObjectPairs.put(relation, list);
      }
      list.add(fact);
      if (++count % 10000000 == 0) {
        logger.info("Finished reading " + (count / 1000000) + " million facts!");
      }
    }
  }

  public synchronized List<Fact> getFacts(String relation) {
    if (!loaded) {
      load();
      loaded = true;
    }
    return relations2SubjectObjectPairs.get(relation);
  }

  public static void main(String[] args) throws IOException {
    YAGO3Reader yagoReader = new YAGO3FileReader("/Users/mamir/tmp/aida_yago3_test/aidaFacts.tsv");
    List<Fact> m = yagoReader.getFacts(YAGO3RelationNames.hasAnchorText);
    for (Fact a : m) {
      //System.out.println(FactComponent.unYagoEntity(a.getSubject()));
      System.out.println(a);
    }
  }
}
