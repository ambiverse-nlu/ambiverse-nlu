package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.extractor;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.feature.Feature;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.featureset.FeatureSet;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.featureset.FeatureSetFactory;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util.EntityInstance;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util.TrainingInstance;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Extracts features in the form of a list of key:value from a given jCas.
 */
public class NYTEntitySalienceFeatureExtractor extends FeatureExtractor_ImplBase {
  private static final long serialVersionUID = 1613675066393471441L;

  @Override
  public List<TrainingInstance> getTrainingInstances(JCas jCas, TrainingSettings.FeatureExtractor featureExtractor, int positiveInstanceScalingFactor) throws Exception {
    List<TrainingInstance> trainingInstances = new ArrayList<>();

    Collection<SalientEntity> salientEntities = JCasUtil.select(jCas, SalientEntity.class);
    Map<String, SalientEntity> salientEntityMap = new HashMap<>();

    //The salient entities at this point don't have IDs. ITs better if we find the ids from the Aida Entities
    for(SalientEntity salientEntity : salientEntities) {
        salientEntityMap.put(salientEntity.getID(), salientEntity);
    }

    Logger logger = LoggerFactory.getLogger(NYTEntitySalienceFeatureExtractor.class);
    String docId = JCasUtil.selectSingle(jCas, DocumentMetaData.class).getDocumentId();
    logger.info("[{}] Document entities: {}.", docId,  salientEntityMap.size());

    List<EntityInstance> entityInstances = getEntityInstances(jCas, featureExtractor);

    // Extract features for entities.
    for (EntityInstance ei : entityInstances) {
      String entityId = ei.getEntityId();
      if(salientEntityMap.containsKey(entityId)) {
        Double label = salientEntityMap.get(entityId).getLabel();

        // Generate the training instance with boolean label.
        TrainingInstance ti = new TrainingInstance(label, ei.getFeatureValues(), entityId, docId);
        logger.debug("[{}] for entity {} ti: {}.", docId, entityId, ti);
        trainingInstances.add(ti);

        // Scale positive examples if necessary.
        int addCount = (label == 1.0) ? positiveInstanceScalingFactor : 1;
        for (int i = 1; i < addCount; ++i) {
          trainingInstances.add(ti);
        }
      }
    }

    return trainingInstances;
  }

  @Override
  public List<EntityInstance> getEntityInstances(JCas jCas, TrainingSettings.FeatureExtractor featureExtractor) throws Exception {
    Collection<AidaEntity> aidaEntities = JCasUtil.select(jCas, AidaEntity.class);
    ListMultimap<String, AidaEntity> entitiesMentions = ArrayListMultimap.create();

    // Group by actual entity (uima.Entity is a mention).
    for (AidaEntity aidaEntity : aidaEntities) {
      entitiesMentions.put(aidaEntity.getID(), aidaEntity);
    }

    Logger logger = LoggerFactory.getLogger(NYTEntitySalienceFeatureExtractor.class);
    String docId = JCasUtil.selectSingle(jCas, DocumentMetaData.class).getDocumentId();
    logger.debug("[" + docId + "] AIDA entities: " + entitiesMentions.keySet());

    List<EntityInstance> entityInstances = new ArrayList<>(entitiesMentions.size());

    // Extract features for entities.
    for (Map.Entry<String, Collection<AidaEntity>> entry : entitiesMentions.asMap().entrySet()) {
      String entityId = entry.getKey();
      Collection<AidaEntity> entityMentions = entry.getValue();

      // Generate feature 8.
      Map<Integer, Double> entityFeatureValues = getEntityFeatureValues(jCas, entityMentions, featureExtractor);
      EntityInstance ei = new EntityInstance(entityId, entityFeatureValues);
      entityInstances.add(ei);
    }

    return entityInstances;
  }

  /**
   * Main method to extract entity-based feature-value pairs.
   *
   * @param jCas
   * @param entityMentions
   * @return
   */
  public  Map<Integer, Double> getEntityFeatureValues(
      JCas jCas, Collection<AidaEntity> entityMentions, TrainingSettings.FeatureExtractor featureExtractor) throws Exception {

    FeatureSet fs = FeatureSetFactory.createFeatureSet(featureExtractor, entityMentions);

    Map<Integer, Double> features = new HashMap<>();
    for (Feature f : fs.features()) {
      features.putAll(f.extract(jCas));
    }

    return features;
  }
}
