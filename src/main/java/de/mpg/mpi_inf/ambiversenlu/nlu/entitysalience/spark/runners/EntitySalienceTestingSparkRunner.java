package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.runners;

import com.beust.jcommander.Parameter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.ParameterizedExecutable;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.TrainingSparkRunner;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SCAS;
import org.apache.hadoop.io.Text;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.feature.VectorIndexer;
import org.apache.spark.mllib.linalg.SparseVector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow;
import org.apache.spark.sql.catalyst.util.GenericArrayData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 *
 spark-submit \
 --class de.mpg.mpi_inf.ambiversenlu.nlu.entitysaliance.spark.runners.EntitySalienceTestingSparkRunner \
 --master yarn-cluster \
 --conf "spark.executor.extraJavaOptions=-Daida.conf=aida_20170320_de_en_es_zh_v16_cass" \
 --executor-memory 12G \
 --driver-memory 10g \
 --driver-java-options "-Dspark.akka.timeout=60000 -Dspark.driver.maxResultSize=10G -Dspark.driver.memory=10G -Dspark.yarn.maxAppAttempts=1" \
 --num-executors 40 \
 --executor-cores 4 \
 --files src/main/resources/log4j.properties \
 target/entitylinking-1.0.0-SNAPSHOT-standalone.jar \
 -i hdfs:/user/d5aida/nyt_entity_salience/annotated-seqs/2007/ \
 -m hdfs:/user/d5aida/nyt_entity_salience/result/models/application_1511363909014_0551/model_RANDOM_FOREST/part-00000 \
 -o hdfs:/user/d5aida/nyt_entity_salience/result/eval/
 */

public class EntitySalienceTestingSparkRunner extends ParameterizedExecutable {

    private Logger logger = LoggerFactory.getLogger(EntitySalienceTrainingSparkRunner.class);

    @Parameter(names = { "--input", "-i"}, description = "Input directory.")
    private String input;

    @Parameter(names = { "--model", "-m"}, description = "Model path.")
    private String model;

    @Parameter(names = { "--output", "-o"}, description = "Output path.")
    private String output;

    @Parameter(names = { "--defaultConf", "-d"}, description = "Default configuration (cassandra or db).")
    private String defaultConf;

    public EntitySalienceTestingSparkRunner(String[] args) throws Exception {
        super(args);
    }

    public static void main(String[] args) throws Throwable {
        try {
            ConfigUtils.setConfOverride("integration_test");
            new EntitySalienceTestingSparkRunner(args).run();
        } catch (Exception e) {
            e.printStackTrace();
            EntityLinkingManager.shutDown();
        }
    }

    @Override
    protected int run() throws Exception {

        SparkConf sparkConf = new SparkConf()
                .setAppName("EntitySalienceTrainingSparkRunner")
                .set("spark.hadoop.validateOutputSpecs", "false")
                //.set("spark.yarn.executor.memoryOverhead", "4096")
                .set("spark.rdd.compress", "true")
                .set("spark.core.connection.ack.wait.timeout", "600")
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                //.set("spark.kryo.registrationRequired", "true")
                .registerKryoClasses(new Class[] {SCAS.class, LabeledPoint.class, SparseVector.class, int[].class, double[].class,
                        InternalRow[].class, GenericInternalRow.class, Object[].class, GenericArrayData.class,
                        VectorIndexer.class})
                ;//setMaster("local"); //Remove this if you run it on the server.

        TrainingSettings trainingSettings = new TrainingSettings();

        if(defaultConf != null) {
            trainingSettings.setAidaDefaultConf(defaultConf);
        }


        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        int totalCores = Integer.parseInt(sc.getConf().get("spark.executor.instances"))
                * Integer.parseInt(sc.getConf().get("spark.executor.cores"));

//        int totalCores = 2;

        //trainingSettings.setClassificationMethod(TrainingSettings.ClassificationMethod.LOG_REG);

        trainingSettings.setPositiveInstanceScalingFactor(1);
        if(trainingSettings.getFeatureExtractor().equals(TrainingSettings.FeatureExtractor.ANNOTATE_AND_ENTITY_SALIENCE)) {
            sc.addFile(trainingSettings.getBigramCountCache());
            sc.addFile(trainingSettings.getKeywordCountCache());
            sc.addFile(trainingSettings.getWordContractionsCache());
            sc.addFile(trainingSettings.getWordExpansionsCache());
            if (trainingSettings.getAidaDefaultConf().equals("db")) {
                sc.addFile(trainingSettings.getDatabaseAida());
            } else {
                sc.addFile(trainingSettings.getCassandraConfig());
            }
        }

        SQLContext sqlContext = new SQLContext(sc);


        int partitionNumber = 3 * totalCores;
        //Read training documents serialized as SCAS
        JavaPairRDD<Text, SCAS> documents = sc.sequenceFile(input, Text.class, SCAS.class, partitionNumber);

        //Instanciate a training spark runner
        TrainingSparkRunner trainingSparkRunner = new TrainingSparkRunner();


        PipelineModel trainingModel = (PipelineModel) sc.objectFile(model).first();

        //Evaluate the model and write down the evaluation metrics.
        trainingSparkRunner.evaluate(sc, sqlContext, documents, trainingModel, trainingSettings, output+"/"+sc.getConf().getAppId()+"/");

        return 0;
    }
}
