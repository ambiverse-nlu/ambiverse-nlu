package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.runners;

import com.beust.jcommander.Parameter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.ParameterizedExecutable;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.SparkClassificationModel;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.TrainingSparkRunner;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SCAS;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Model;
import org.apache.spark.ml.feature.VectorIndexer;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.mllib.linalg.SparseVector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow;
import org.apache.spark.sql.catalyst.util.GenericArrayData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class for Training an entity salience model.
 *
 *

 spark-submit \
 --class de.mpg.mpi_inf.ambiversenlu.nlu.entitysaliance.spark.runners.EntitySalienceTrainingSparkRunner \
 --master yarn-cluster \
 --conf "spark.executor.extraJavaOptions=-Daida.conf=aida_20170320_de_en_es_zh_v16_cass" \
 --executor-memory 12G \
 --driver-memory 8g \
 --driver-java-options "-Dspark.akka.timeout=60000 -Dspark.driver.maxResultSize=10G -Dspark.driver.memory=10G -Dspark.yarn.maxAppAttempts=1" \
 --num-executors 10 \
 --executor-cores 6 \
 --files src/main/resources/log4j.properties \
 target/entitylinking-1.0.0-SNAPSHOT-standalone.jar \
 -i hdfs:/user/d5aida/nyt_entity_salience/annotated-seqs/training/ \
 -o hdfs:/user/d5aida/nyt_entity_salience/result/models/ \
 -m RANDOM_FOREST \
 -f 3 \
 -d cassandra \
 -s 2


 */
public class EntitySalienceTrainingSparkRunner extends ParameterizedExecutable {

    private Logger logger = LoggerFactory.getLogger(EntitySalienceTrainingSparkRunner.class);

    @Parameter(names = { "--input", "-i"}, description = "Input directory.")
    private String input;

    @Parameter(names = { "--output", "-o"}, description = "Model Output.")
    private String output;

    @Parameter(names = { "--folds", "-f" }, description = "Number of folds to run.")
    private Integer folds;

    @Parameter(names = { "--method", "-m"}, description = "Classification Method.")
    private String method;

    @Parameter(names = { "--partitions", "-p"}, description = "Number of partitions for reading the sequence files.")
    private Integer partitions;

    @Parameter(names = { "--defaultConf", "-d"}, description = "Default configuration (cassandra or db).")
    private String defaultConf;

    @Parameter(names = { "--positive", "-s"}, description = "Positive instance scaling factor.")
    private Integer scalingFactor;

    public EntitySalienceTrainingSparkRunner(String[] args) throws Exception {
        super(args);
    }

    public static void main(String[] args) throws Throwable {
        try {
            ConfigUtils.setConfOverride("aida_20170320_de_en_es_zh_v16_db");
            new EntitySalienceTrainingSparkRunner(args).run();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION OCCURRED!!!!!! " +e.getMessage());
            e.getCause().printStackTrace();
            EntityLinkingManager.shutDown();
        }
    }

    @Override
    protected int run() throws Exception {

        SparkConf sparkConf = new SparkConf()
                .setAppName("EntitySalienceTrainingSparkRunner")
                .set("spark.hadoop.validateOutputSpecs", "false")
                .set("spark.yarn.executor.memoryOverhead", "3072")
                .set("spark.rdd.compress", "true")
                .set("spark.core.connection.ack.wait.timeout", "600")
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                //.set("spark.kryo.registrationRequired", "true")
                .registerKryoClasses(new Class[] {SCAS.class, LabeledPoint.class, SparseVector.class, int[].class, double[].class,
                        InternalRow[].class, GenericInternalRow.class, Object[].class, GenericArrayData.class,
                        VectorIndexer.class})
                ;//.setMaster("local[4]"); //Remove this if you run it on the server.

        TrainingSettings trainingSettings = new TrainingSettings();

        if(folds != null) {
            trainingSettings.setNumFolds(folds);
        }
        if(method != null) {
            trainingSettings.setClassificationMethod(TrainingSettings.ClassificationMethod.valueOf(method));
        }
        if(defaultConf != null) {
            trainingSettings.setAidaDefaultConf(defaultConf);
        }

        if(scalingFactor != null) {
            trainingSettings.setPositiveInstanceScalingFactor(scalingFactor);
        }

        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        int totalCores = Integer.parseInt(sc.getConf().get("spark.executor.instances"))
                * Integer.parseInt(sc.getConf().get("spark.executor.cores"));

//        int totalCores = 4;
////        trainingSettings.setFeatureExtractor(TrainingSettings.FeatureExtractor.ANNOTATE_AND_ENTITY_SALIENCE);
////        trainingSettings.setAidaDefaultConf("db");
//        //trainingSettings.setClassificationMethod(TrainingSettings.ClassificationMethod.LOG_REG);
//        trainingSettings.setPositiveInstanceScalingFactor(1);

        //Add the cache files to each node only if annotation is required.
        //The input documents could already be annotated, and in this case no caches are needed.
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


        FileSystem fs = FileSystem.get(new Configuration());

        int partitionNumber = 3 * totalCores;
        if(partitions != null) {
            partitionNumber = partitions;
        }

        //Read training documents serialized as SCAS
        JavaRDD<SCAS> documents = sc.sequenceFile(input, Text.class, SCAS.class, partitionNumber).values();

        //Instanciate a training spark runner
        TrainingSparkRunner trainingSparkRunner = new TrainingSparkRunner();

        //Train a model
        CrossValidatorModel model = trainingSparkRunner.crossValidate(sc, sqlContext, documents, trainingSettings);


        //Create the model path
        String modelPath = output+"/"+sc.getConf().getAppId()+"/model_"+trainingSettings.getClassificationMethod();

        //Delete the old model if there is one
        fs.delete(new Path(modelPath), true);

        //Save the new model model
        List<Model> models = new ArrayList<>();
        models.add(model.bestModel());
        sc.parallelize(models, 1).saveAsObjectFile(modelPath);

        //Save the model stats
        SparkClassificationModel.saveStats(model, trainingSettings, output+"/"+sc.getConf().getAppId()+"/");


        return 0;
    }

}
