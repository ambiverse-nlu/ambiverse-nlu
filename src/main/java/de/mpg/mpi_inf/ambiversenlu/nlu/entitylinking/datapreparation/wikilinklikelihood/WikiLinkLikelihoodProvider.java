package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.wikilinklikelihood;

import java.util.Map.Entry;

/**

 *
 */
public abstract class WikiLinkLikelihoodProvider implements Iterable<Entry<String, Double>> {

  public abstract String getKnowledgebaseName();
}
