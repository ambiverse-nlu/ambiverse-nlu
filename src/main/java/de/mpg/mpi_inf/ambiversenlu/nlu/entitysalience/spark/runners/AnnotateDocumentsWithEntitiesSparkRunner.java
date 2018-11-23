package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.runners;

import com.beust.jcommander.Parameter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.EntitySalienceFactory;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.ParameterizedExecutable;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.EntitySalienceFeatureExtractorSpark;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SCAS;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SparkSerializableAnalysisEngine;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SparkUimaUtils;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.IOException;

/**
 * Spark Runner class that annotates the documents with entities.

 spark-submit \
 --class de.mpg.mpi_inf.ambiversenlu.nlu.entitysaliance.spark.runners.AnnotateDocumentsWithEntitiesSparkRunner \
 --master yarn-cluster \
 --conf "spark.executor.extraJavaOptions=-Daida.conf=aida_20170320_de_en_es_zh_v16_cass" \
 --executor-memory 18G \
 --driver-memory 16g \
 --driver-java-options "-Dspark.akka.timeout=60000 -Dspark.driver.maxResultSize=16G -Dspark.driver.memory=16G -Dspark.yarn.maxAppAttempts=1" \
 --num-executors 10 \
 --executor-cores 2 \
 --files src/main/resources/log4j.properties \
 target/entitylinking-1.0.0-SNAPSHOT-standalone.jar \
 -i hdfs:/user/d5aida/nyt_entity_salience/newseqs/newseqs/2007.seq \
 -o hdfs:/user/d5aida/nyt_entity_salience/newannotated-seqs/2007 \
 -d cassandra

 */

public class AnnotateDocumentsWithEntitiesSparkRunner extends ParameterizedExecutable {

    private Logger logger = LoggerFactory.getLogger(AnnotateDocumentsWithEntitiesSparkRunner.class);

    @Parameter(names = { "--input", "-i"}, description = "Input directory.")
    private String input;

    @Parameter(names = { "--output", "-o"}, description = "Model Output.")
    private String output;

    @Parameter(names = { "--defaultConf", "-d"}, description = "Default configuration (cassandra or db).")
    private String defaultConf;

    public AnnotateDocumentsWithEntitiesSparkRunner(String[] args) throws Exception {
        super(args);
    }

    public static void main(String[] args) {
        try {
            ConfigUtils.setConfOverride("aida_20170320_de_en_es_zh_v16_cass");
            new AnnotateDocumentsWithEntitiesSparkRunner(args).run();
        } catch (Exception e) {
            e.printStackTrace();
            //e.getCause().printStackTrace();
            try {
                EntityLinkingManager.shutDown();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }

    @Override
    protected int run()  {

        System.out.print("START");


        SparkConf sparkConf = new SparkConf()
                .setAppName("AnnotateDocumentsWithEntitiesSparkRunner")
                .set("spark.hadoop.validateOutputSpecs", "false")
                .set("spark.yarn.executor.memoryOverhead", "3000")
                //.set("spark.rdd.compress", "true")
                //.set("spark.core.connection.ack.wait.timeout", "600")
                ;//.setMaster("local"); //Remove this if you run it on the server.

        TrainingSettings trainingSettings = new TrainingSettings();

        if(defaultConf != null) {
            trainingSettings.setAidaDefaultConf(defaultConf);
        }

        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        int totalCores = Integer.parseInt(sc.getConf().get("spark.executor.instances"))
                * Integer.parseInt(sc.getConf().get("spark.executor.cores"));

        sc.addFile(trainingSettings.getBigramCountCache());
        sc.addFile(trainingSettings.getKeywordCountCache());
        sc.addFile(trainingSettings.getWordContractionsCache());
        sc.addFile(trainingSettings.getWordExpansionsCache());
        if(trainingSettings.getAidaDefaultConf().equals("db")) {
            sc.addFile(trainingSettings.getDatabaseAida());
        }else {
            sc.addFile(trainingSettings.getCassandraConfig());
        }

        int partitionNumber = 3 * totalCores;


        System.out.print("READING DOCS");

        //Read training documents serialized as SCAS
        JavaRDD<SCAS> documents = sc.sequenceFile(input, Text.class, SCAS.class, partitionNumber).values();

        System.out.print("FINISH");




        Accumulator<Integer> TOTAL_DOCS = sc.accumulator(0, "TOTAL_DOCS");
        final SparkSerializableAnalysisEngine ae;
        try {
            ae = EntitySalienceFactory.createEntitySalienceEntityAnnotator(trainingSettings.getEntitySalienceEntityAnnotator());
        } catch (ResourceInitializationException e) {
            throw new RuntimeException(e);
        }
        JavaRDD<SCAS> annotatedDocuments = documents
                .map(s -> {
                    TOTAL_DOCS.add(1);
                    Logger tmpLogger = LoggerFactory.getLogger(EntitySalienceFeatureExtractorSpark.class);
                    String docId = JCasUtil.selectSingle(s.getJCas(), DocumentMetaData.class).getDocumentId();
                    tmpLogger.info("Processing document {}.", docId);

                    //Before processing the document through the Disambiguation Pipeline, add the AIDA settings
                    // in each document.
                    SparkUimaUtils.addSettingsToJCas(s.getJCas(), trainingSettings.getDocumentCoherent(), trainingSettings.getDocumentConfidenceThreshold());
                    return ae.process(s);
                });


        //Delete old files
        FileSystem fs = null;
        try {
            fs = FileSystem.get(new Configuration());
            fs.delete(new Path(output), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Save the annotated documents back to sequence files in an output directory
        JavaPairRDD<String, SCAS> seqRDD = annotatedDocuments.mapToPair(s-> {
            String docId = JCasUtil.selectSingle(s.getJCas(), DocumentMetaData.class).getDocumentId();
            return new Tuple2<>(docId, s.copy());
        });

        seqRDD.mapToPair(t -> new Tuple2<>(new Text(t._1), t._2))
                .saveAsHadoopFile(output, Text.class, SCAS.class, SequenceFileOutputFormat.class);

        return 0;
    }
}
