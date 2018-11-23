package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.datapreparation;

import com.beust.jcommander.Parameter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.ParameterizedExecutable;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SCAS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.CoreNlpUtils;
import edu.stanford.nlp.ling.CoreLabel;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.feature.VectorIndexer;
import org.apache.spark.mllib.linalg.SparseVector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow;
import org.apache.spark.sql.catalyst.util.GenericArrayData;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

/**
 * Main class for Training an entity salience model.
 *
 *

 spark-submit \
 --class de.mpg.mpi_inf.ambiversenlu.nlu.entitysaliance.spark.datapreparation.Data2CoNLL \
 --master yarn-cluster \
 --executor-memory 12G \
 --driver-memory 8g \
 --driver-java-options "-Dspark.akka.timeout=60000 -Dspark.driver.maxResultSize=10G -Dspark.driver.memory=10G -Dspark.yarn.maxAppAttempts=1" \
 --num-executors 10 \
 --executor-cores 6 \
 --files src/main/resources/log4j.properties \
 target/entitylinking-1.0.0-SNAPSHOT-standalone.jar \
 -i hdfs:/user/d5aida/nyt_entity_salience/newannotated-seqs/2007 \
 -o hdfs:/user/d5aida/nyt_entity_salience/coNLLFormat/simp/2007

 */
public class Data2CoNLL extends ParameterizedExecutable implements Serializable {

  @Parameter(names = { "--input", "-i"}, description = "Input directory.")
  private String input;

  @Parameter(names = { "--output", "-o"}, description = "Output.")
  private String output;

  @Parameter(names = { "--partitions", "-p"}, description = "Number of partitions for reading the sequence files.")
  private Integer partitions;


  public Data2CoNLL(String[] args) throws Exception {
    super(args);
  }

  public static void main(String[] args) throws Throwable {
    try {
      ConfigUtils.setConfOverride("aida_20170320_de_en_es_zh_v16_db");
      new Data2CoNLL(args).run();
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
        .setAppName("Data2CoNLL")
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


    JavaSparkContext sc = new JavaSparkContext(sparkConf);
    int totalCores = Integer.parseInt(sc.getConf().get("spark.executor.instances"))
        * Integer.parseInt(sc.getConf().get("spark.executor.cores"));

    FileSystem fs = FileSystem.get(new Configuration());

    int partitionNumber = 3 * totalCores;
    if(partitions != null) {
      partitionNumber = partitions;
    }

    //Read training documents serialized as SCAS
    JavaRDD<SCAS> documents = sc.sequenceFile(input, Text.class, SCAS.class, partitionNumber).values();

    JavaRDD<String> docStrings = documents.map( s -> {
      JCas jCas = s.getJCas();
      NYTArticleMetaData metadata = JCasUtil.selectSingle(jCas, NYTArticleMetaData.class);

      StringJoiner docBuilder = new StringJoiner("\n");

      docBuilder.add("-DOCSTART- (" +  metadata.getGuid() + ")");
      docBuilder.add("");

      Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
      for(Sentence sentence: sentences) {
        List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
        for(Token token: tokens) {
          CoreLabel taggedWord = CoreNlpUtils.tokenToWord(token);
          StringJoiner lineBuilder = new StringJoiner("\t");
          lineBuilder.add(taggedWord.word().toLowerCase());
          docBuilder.add(lineBuilder.toString());
        }
        docBuilder.add("");
      }
      return docBuilder.toString();
    });

    docStrings.saveAsTextFile(output);
    sc.stop();
    return 0;
  }

}

