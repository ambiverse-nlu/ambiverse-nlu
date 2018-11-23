package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.EntitySalienceFactory;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.extractor.FeatureExtractor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.extractor.NYTEntitySalienceFeatureExtractor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.featureset.FeatureSetFactory;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util.FeatureValueInstanceUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util.TrainingInstance;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SCAS;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SparkSerializableAnalysisEngine;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SparkUimaUtils;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.spark.Accumulator;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.VectorUDT;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntitySalienceAnnotatorAndFeatureExtractorSpark extends FeatureExtractorSpark  {

    public EntitySalienceAnnotatorAndFeatureExtractorSpark(TrainingSettings trainingSettings) {
        setTrainingSettings(trainingSettings);
    }

    /**
     * Annotated documents for entities and extract features from set of documents
     * @param jsc
     * @param documents
     * @return
     * @throws ResourceInitializationException
     */
    public JavaRDD<LabeledPoint> extract (JavaSparkContext jsc, JavaRDD<SCAS> documents) throws ResourceInitializationException {
        Accumulator<Integer> TOTAL_DOCS = jsc.accumulator(0, "TOTAL_DOCS");
        Accumulator<Integer> SALIENT_ENTITY_INSTANCES = jsc.accumulator(0, "SALIENT_ENTITY_INSTANCES");
        Accumulator<Integer> NON_SALIENT_ENTITY_INSTANCES = jsc.accumulator(0, "NON_SALIENT_ENTITY_INSTANCES");

        TrainingSettings trainingSettings = getTrainingSettings();

        final SparkSerializableAnalysisEngine ae = EntitySalienceFactory.createEntitySalienceEntityAnnotator(trainingSettings.getEntitySalienceEntityAnnotator());
        FeatureExtractor fe = new NYTEntitySalienceFeatureExtractor();
        final int featureVectorSize = FeatureSetFactory.createFeatureSet(trainingSettings.getFeatureExtractor()).getFeatureVectorSize();

        JavaRDD<TrainingInstance> trainingInstances =
                documents
                        .map(s -> {
                            TOTAL_DOCS.add(1);
                            Logger tmpLogger = LoggerFactory.getLogger(EntitySalienceFeatureExtractorSpark.class);
                            String docId = JCasUtil.selectSingle(s.getJCas(), DocumentMetaData.class).getDocumentId();
                            tmpLogger.info("Processing document {}.", docId);
                            //Before processing the document through the Disambiguation Pipeline, add the AIDA settings
                            // in each document.
                            SparkUimaUtils.addSettingsToJCas(s.getJCas(),
                                    trainingSettings.getDocumentCoherent(),
                                    trainingSettings.getDocumentConfidenceThreshold());
                            return ae.process(s);
                        })
                        .flatMap(s -> fe.getTrainingInstances(s.getJCas(),
                                trainingSettings.getFeatureExtractor(),
                                trainingSettings.getPositiveInstanceScalingFactor()));

        // Create a LabelPoint
        JavaRDD<LabeledPoint> labeledPoints = trainingInstances
                .map(ti -> {
                    if (ti.getLabel() == 1.0) {
                        SALIENT_ENTITY_INSTANCES.add(1);
                    } else {
                        NON_SALIENT_ENTITY_INSTANCES.add(1);
                    }
                    return FeatureValueInstanceUtils.convertToSparkMLLabeledPoint(ti, featureVectorSize);
                });

        return labeledPoints;
    }

    /**
     * Extract a DataFrame ready for training or testing.
     * @param jsc
     * @param documents
     * @param sqlContext
     * @return
     * @throws ResourceInitializationException
     */
    public DataFrame extract(JavaSparkContext jsc, JavaRDD<SCAS> documents, SQLContext sqlContext) throws ResourceInitializationException {
        Accumulator<Integer> TOTAL_DOCS = jsc.accumulator(0, "TOTAL_DOCS");
        Accumulator<Integer> SALIENT_ENTITY_INSTANCES = jsc.accumulator(0, "SALIENT_ENTITY_INSTANCES");
        Accumulator<Integer> NON_SALIENT_ENTITY_INSTANCES = jsc.accumulator(0, "NON_SALIENT_ENTITY_INSTANCES");

        TrainingSettings trainingSettings = getTrainingSettings();

        final SparkSerializableAnalysisEngine ae = EntitySalienceFactory.createEntitySalienceEntityAnnotator(trainingSettings.getEntitySalienceEntityAnnotator());
        FeatureExtractor fe = new NYTEntitySalienceFeatureExtractor();
        final int featureVectorSize = FeatureSetFactory.createFeatureSet(TrainingSettings.FeatureExtractor.ENTITY_SALIENCE).getFeatureVectorSize();

        JavaRDD<TrainingInstance> trainingInstances =
                documents
                        .map(s -> {
                            TOTAL_DOCS.add(1);
                            Logger tmpLogger = LoggerFactory.getLogger(EntitySalienceFeatureExtractorSpark.class);
                            String docId = JCasUtil.selectSingle(s.getJCas(), DocumentMetaData.class).getDocumentId();
                            tmpLogger.info("Processing document {}.", docId);
                            //Before processing the document through the Disambiguation Pipeline, add the AIDA settings
                            // in each document.
                            SparkUimaUtils.addSettingsToJCas(s.getJCas(),
                                    trainingSettings.getDocumentCoherent(),
                                    trainingSettings.getDocumentConfidenceThreshold());
                            return ae.process(s);
                        })
                        .flatMap(s -> fe.getTrainingInstances(s.getJCas(),
                                trainingSettings.getFeatureExtractor(),
                                trainingSettings.getPositiveInstanceScalingFactor()));

        StructType schema = new StructType(new StructField[]{
                new StructField("docId", DataTypes.StringType, false, Metadata.empty() ),
                new StructField("entity", DataTypes.StringType, false, Metadata.empty() ),
                new StructField("label", DataTypes.DoubleType, false, Metadata.empty() ),
                new StructField("features", new VectorUDT(), false, Metadata.empty())
        });

        JavaRDD<Row> withFeatures = trainingInstances.map(ti -> {
            if (ti.getLabel() == 1.0) {
                SALIENT_ENTITY_INSTANCES.add(1);
            } else {
                NON_SALIENT_ENTITY_INSTANCES.add(1);
            }
            Vector vei = FeatureValueInstanceUtils.convertToSparkMLVector(ti, featureVectorSize);
            return RowFactory.create(ti.getDocId(), ti.getEntityId(), ti.getLabel(), vei);
        });

        return sqlContext.createDataFrame(withFeatures, schema);
    }
}
