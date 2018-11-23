package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Extracts feature if entity in heading.
 */
public class EntityHeadingFeature extends EntityBasedFeature {

  public EntityHeadingFeature(Collection<AidaEntity> entityMentions) {
    super(entityMentions);
  }

  @Override protected Features.StaticFeatureRange getRange() {
    return Features.StaticFeatureRange.IN_HEADING;
  }

  @Override public Map<Integer, Double> extract(JCas jCas) {
    Map<Integer, Double> features = new HashMap<>();

    // Heading ends after two sentences.
    Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
    int headingEnd = 0;
    if (sentences.size() > 2) {
      Iterator<Sentence> itr = sentences.iterator();
      itr.next();
      headingEnd = itr.next().getEnd();
    }
    boolean inHeading = getFirstOffset() < headingEnd;

    features.put(getId(), inHeading ? 1.0 : 0.0);
    return features;
  }
}
