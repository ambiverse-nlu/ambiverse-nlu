package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.entitysalience;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.extractor.FeatureExtractor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.extractor.NYTEntitySalienceFeatureExtractor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.featureset.FeatureSetFactory;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util.EntityInstance;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util.FeatureValueInstanceUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.SQLContext;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Entity Salience Analysis engine that loads a already trained data model from a file in libsvm format.
 * It makes prediction for a single document and writes the predictions back to jCas.
 * <p>
 * See example from here:
 * https://github.com/apache/spark/blob/v1.6.3/examples/src/main/java/org/apache/spark/examples/ml/JavaRandomForestClassifierExample.java
 */
public class EntitySalienceSpark extends JCasAnnotator_ImplBase {

    private Logger logger = LoggerFactory.getLogger(EntitySalienceSpark.class);

    protected JavaSparkContext jsc;
    protected SQLContext sqlContext;

    private PipelineModel trainingModel;

    public static final String PARAM_MODEL_PATH = "modelPath";
    @ConfigurationParameter(
            name = "modelPath",
            mandatory = true
    )
    private String modelPath;


    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        synchronized (EntitySalienceSpark.class) {
            SparkConf conf = new SparkConf()
                    .setAppName("EntitySalienceTagger")
                    .set("spark.driver.allowMultipleContexts","true")
                    .setMaster("local");
            jsc = new JavaSparkContext(conf);

            //Load the training model
            //trainingModel = PipelineModel.load(modelPath);
            trainingModel = (PipelineModel) jsc.objectFile(modelPath).first();
            jsc.close();
            jsc.stop();
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        long startTime = System.currentTimeMillis();

        FeatureExtractor fe = new NYTEntitySalienceFeatureExtractor();
        List<EntityInstance> entityInstances;
        try {
            entityInstances = fe.getEntityInstances(jCas, TrainingSettings.FeatureExtractor.ENTITY_SALIENCE);

            final int featureVectorSize = FeatureSetFactory.createFeatureSet(TrainingSettings.FeatureExtractor.ENTITY_SALIENCE).getFeatureVectorSize();

            //TODO: For each model create separate implementation.
            RandomForestClassificationModel rfm = (RandomForestClassificationModel)trainingModel.stages()[2];
            for(EntityInstance ei : entityInstances) {
                Vector vei = FeatureValueInstanceUtils.convertToSparkMLVector(ei, featureVectorSize);

                double label = rfm.predict(vei);
                Vector probabilities = rfm.predictProbability(vei);
                double salience = probabilities.toArray()[1];

                SalientEntity salientEntity = new SalientEntity(jCas, 0, 0);
                salientEntity.setLabel(label);
                salientEntity.setID(ei.getEntityId());
                salientEntity.setSalience(salience);
                salientEntity.addToIndexes();
            }
            long endTime = System.currentTimeMillis() - startTime;
            logger.debug("Annotating salient entities finished in {}ms.", endTime);


        } catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }


    @Override
    public void destroy() {
        synchronized (EntitySalienceSpark.class) {
            jsc.stop();
        }
    }

}
