package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.wikilinklikelihood.WikiLinkLikelihoodProvider;
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
public class Yago3WikiLinkProbabilitiesProvider extends WikiLinkLikelihoodProvider {

  private String knowledgebaseName = Yago3PrepConf.YAGO3;

  private YAGO3Reader yago3Reader;

  public Yago3WikiLinkProbabilitiesProvider(YAGO3Reader yago3Reader) {
    this.yago3Reader = yago3Reader;
  }

  @Override public Iterator<Entry<String, Double>> iterator() {
    try {
      return run().iterator();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private List<Entry<String, Double>> run() throws IOException {
    List<Entry<String, Double>> entries = new LinkedList<>();

    List<Fact> linkLikelihoods = yago3Reader.getFacts(YAGO3RelationNames.hasWikiLinkLikelihood);

    for (Fact entry : linkLikelihoods) {
      String entityName = entry.getSubject();
      Double likelihood = Double.parseDouble(entry.getObjectAsJavaString());

      entries.add(new AbstractMap.SimpleEntry<String, Double>(entityName, likelihood));
    }

    return entries;

  }

  @Override public String getKnowledgebaseName() {
    return knowledgebaseName;
  }

}
