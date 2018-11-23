package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.FeatureExtractionFactory;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SCAS;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Model;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.storage.StorageLevel;
import org.apache.uima.cas.CASException;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.IOException;
import java.util.Arrays;

public class TrainingSparkRunner {
    private static Logger logger = LoggerFactory.getLogger(TrainingSparkRunner.class);

    /**
     * Train classification model for documents by doing cross validation and hyper parameter optimization at the same time.
     * The produced model contains the best model and statistics about the runs, which are later saved from the caller method.
     *
     * @param jsc
     * @param sqlContext
     * @param documents
     * @param trainingSettings
     * @return
     * @throws ResourceInitializationException
     * @throws IOException
     */
    public CrossValidatorModel crossValidate(JavaSparkContext jsc, SQLContext sqlContext, JavaRDD<SCAS> documents, TrainingSettings trainingSettings) throws ResourceInitializationException, IOException {

        FeatureExtractorSpark fesr = FeatureExtractionFactory.createFeatureExtractorSparkRunner(trainingSettings);

        //Extract features for each document as LabelPoints
        DataFrame trainData = fesr.extract(jsc, documents, sqlContext);

        //Save the data for future use, instead of recomputing it all the time
        trainData.persist(StorageLevel.MEMORY_AND_DISK_SER_2());

        //DataFrame trainData = sqlContext.createDataFrame(labeledPoints, LabeledPoint.class);

        //Wrap the classification model base on the training settings
        SparkClassificationModel model = new SparkClassificationModel(trainingSettings.getClassificationMethod());

        //Train the be best model using CrossValidator
        CrossValidatorModel cvModel = model.crossValidate(trainData, trainingSettings);

        return cvModel;
    }

    /**
     * Train a specific model only without doing any cross validation or hyper parameter optimization.
     * The chosen hyper parameters should be set in the trainingSettings object. Set the corresponding map of the hyper paramets,
     * not the single parameters.
     *
     * @param jsc
     * @param sqlContext
     * @param documents
     * @param trainingSettings
     * @return
     * @throws ResourceInitializationException
     * @throws IOException
     */
    public Model train(JavaSparkContext jsc, SQLContext sqlContext, JavaRDD<SCAS> documents, TrainingSettings trainingSettings) throws ResourceInitializationException, IOException {

        FeatureExtractorSpark fesr = FeatureExtractionFactory.createFeatureExtractorSparkRunner(trainingSettings);

        //Extract features for each document as LabelPoints
        DataFrame trainData = fesr.extract(jsc, documents, sqlContext);

        //Save the data for future use, instead of recomputing it all the time
        trainData.persist(StorageLevel.MEMORY_AND_DISK_SER_2());

        //DataFrame trainData = sqlContext.createDataFrame(labeledPoints, LabeledPoint.class);

        //Wrap the classification model base on the training settings
        SparkClassificationModel model = new SparkClassificationModel(trainingSettings.getClassificationMethod());

        Model resultModel = model.train(trainData, trainingSettings);

        return resultModel;
    }

    /**
     * Evaluate a single trained model with test data.
     *
     * @param jsc
     * @param sqlContext
     * @param testDocuments
     * @param model
     * @param trainingSettings
     * @return
     * @throws ResourceInitializationException
     */
    public void evaluate(JavaSparkContext jsc, SQLContext sqlContext, JavaPairRDD<Text, SCAS> testDocuments, Model model,
                         TrainingSettings trainingSettings, String output) throws ResourceInitializationException, IOException, CASException {
        FeatureExtractorSpark fesr = FeatureExtractionFactory.createFeatureExtractorSparkRunner(trainingSettings);

        FileSystem fs = FileSystem.get(new Configuration());

        Path debugPath = new Path(output+"debug_"+trainingSettings.getClassificationMethod()+".txt");


        FSDataOutputStream fsdos = fs.create(debugPath);

        //Extract features for each document as LabelPoints
        DataFrame testingData = fesr.extract(jsc, testDocuments.values(), sqlContext);


        //Save the data for future use, instead of recomputing it all the time
        testingData.persist(StorageLevel.MEMORY_AND_DISK_SER());

        DataFrame predictions = model.transform(testingData);

        boolean hasProbability = Arrays.asList(predictions.columns()).contains("probability");
        String currDocId = "";
        for(Row r: predictions.collect()) {
            String docId = r.getAs("docId");
            String entityId = r.getAs("entityId");
            Double label = r.getAs("label");
            Double salience = r.getAs("prediction");
            if(hasProbability) {
                Vector probabilities = r.getAs("probability");
                salience = probabilities.toArray()[1];
            }
            if(!currDocId.equals(docId)) {
                currDocId = docId;
                IOUtils.write("\n\n"+docId, fsdos);
                IOUtils.write("\n========================================", fsdos);
            }
            logger.debug("docId="+docId+" entityId="+entityId+" label="+label+" prediction="+r.getAs("prediction")+" salience="+salience);
            IOUtils.write("\nentityId="+entityId+"\tlabel="+label+"\tprediction="+r.getAs("prediction")+"\tsalience="+salience, fsdos);

        }

        fsdos.flush();
        IOUtils.closeQuietly(fsdos);


        binaryEvaluation(predictions, output, trainingSettings);
        multiClassEvaluation(predictions, output, trainingSettings);


//        // Select (prediction, true label) and compute test error
//        MulticlassClassificationEvaluator f1Evaluator = new MulticlassClassificationEvaluator()
//                .setLabelCol("indexedLabel")
//                .setPredictionCol("prediction")
//                .setMetricName("f1");
//
//        MulticlassClassificationEvaluator pEvaluator = new MulticlassClassificationEvaluator()
//                .setLabelCol("indexedLabel")
//                .setPredictionCol("prediction")
//                .setMetricName("precision");
//
//        MulticlassClassificationEvaluator rEvaluator = new MulticlassClassificationEvaluator()
//                .setLabelCol("indexedLabel")
//                .setPredictionCol("prediction")
//                .setMetricName("recall");
//
//        MulticlassClassificationEvaluator wpEvaluator = new MulticlassClassificationEvaluator()
//                .setLabelCol("indexedLabel")
//                .setPredictionCol("prediction")
//                .setMetricName("weightedPrecision");
//
//        MulticlassClassificationEvaluator wrEvaluator = new MulticlassClassificationEvaluator()
//                .setLabelCol("indexedLabel")
//                .setPredictionCol("prediction")
//                .setMetricName("weightedRecall");
//
//        double f1 = f1Evaluator.evaluate(predictions);
//        double precision = pEvaluator.evaluate(predictions);
//        double recall = rEvaluator.evaluate(predictions);
//        double wPrecision = wpEvaluator.evaluate(predictions);
//        double wRecall = wrEvaluator.evaluate(predictions);
//
//
//        logger.info("f1={}.",f1);
//        logger.info("precision={}.",+precision);
//        logger.info("recall={}.",recall);
//        logger.info("weightedPrecision={}.",wPrecision);
//        logger.info("weightedRecall={}.",wRecall);
//
//        IOUtils.write("\nf1="+f1, fsdos);
//        IOUtils.write("\nprecision="+precision, fsdos);
//        IOUtils.write("\nrecall="+recall, fsdos);
//        IOUtils.write("\nweightedPrecision="+wPrecision, fsdos);
//        IOUtils.write("\nweightedRecall="+wRecall, fsdos);


    }

    private void binaryEvaluation(DataFrame predictions, String output, TrainingSettings trainingSettings) throws IOException {

        FileSystem fs = FileSystem.get(new Configuration());
        Path evalPath = new Path(output+"binary_evaluation_"+trainingSettings.getClassificationMethod()+".txt");
        fs.delete(evalPath, true);
        FSDataOutputStream fsdos = fs.create(evalPath);

        BinaryClassificationMetrics metrics = new BinaryClassificationMetrics(predictions
                .select("rawPrediction", "label")
                .javaRDD()
                .map((Row row) -> {
                    Vector vector = row.getAs("rawPrediction");
                    Double label = row.getAs("label");
                    return new Tuple2<Object, Object>(vector.apply(1), label);
                }).rdd());


        // Precision by threshold
        JavaRDD<Tuple2<Object, Object>> precision = metrics.precisionByThreshold().toJavaRDD();
        IOUtils.write("\nPrecision by threshold: " + precision.collect(), fsdos);

        // Recall by threshold
        JavaRDD<Tuple2<Object, Object>> recall = metrics.recallByThreshold().toJavaRDD();
        IOUtils.write("\nRecall by threshold: " + recall.collect(), fsdos);

        // F Score by threshold
        JavaRDD<Tuple2<Object, Object>> f1Score = metrics.fMeasureByThreshold().toJavaRDD();
        IOUtils.write("\nF1 Score by threshold: " + f1Score.collect(), fsdos);

        JavaRDD<Tuple2<Object, Object>> f2Score = metrics.fMeasureByThreshold(2.0).toJavaRDD();
        IOUtils.write("\nF2 Score by threshold: " + f2Score.collect(), fsdos);

        // Precision-recall curve
        JavaRDD<Tuple2<Object, Object>> prc = metrics.pr().toJavaRDD();
        IOUtils.write("\nPrecision-recall curve: " + prc.collect(), fsdos);

        // Thresholds
        JavaRDD<Double> thresholds = precision.map(t -> new Double(t._1().toString()));

        // ROC Curve
        JavaRDD<Tuple2<Object, Object>> roc = metrics.roc().toJavaRDD();
        IOUtils.write("\nROC curve: " + roc.collect(), fsdos);

        // AUPRC
        IOUtils.write("\nArea under precision-recall curve = " + metrics.areaUnderPR(), fsdos);

        // AUROC
        IOUtils.write("\nArea under ROC = " + metrics.areaUnderROC(), fsdos);

        fsdos.flush();
        IOUtils.closeQuietly(fsdos);
    }

    private void multiClassEvaluation(DataFrame predictions, String output, TrainingSettings trainingSettings) throws IOException {
        FileSystem fs = FileSystem.get(new Configuration());
        Path evalPath = new Path(output+"multiclass_evaluation_"+trainingSettings.getClassificationMethod()+".txt");
        fs.delete(evalPath, true);
        FSDataOutputStream fsdos = fs.create(evalPath);

        MulticlassMetrics metrics = new MulticlassMetrics(predictions
                .select("prediction", "label"));

        // Confusion matrix
        Matrix confusion = metrics.confusionMatrix();
        IOUtils.write("\nConfusion matrix: \n" + confusion, fsdos);

        // Overall statistics
        IOUtils.write("\nPrecision = " + metrics.precision(), fsdos);
        IOUtils.write("\nRecall = " + metrics.recall(), fsdos);
        IOUtils.write("\nF1 Score = " + metrics.fMeasure(), fsdos);
        IOUtils.write("\n\n", fsdos);
        // Stats by labels
        for (int i = 0; i < metrics.labels().length; i++) {


            IOUtils.write(String.format("Class %f precision = %f\n", metrics.labels()[i],metrics.precision(metrics.labels()[i])), fsdos);
            IOUtils.write(String.format("Class %f recall = %f\n", metrics.labels()[i], metrics.recall(metrics.labels()[i])), fsdos);
            IOUtils.write(String.format("Class %f F1 score = %f\n", metrics.labels()[i], metrics.fMeasure(metrics.labels()[i])), fsdos);

            System.out.format("Class %f precision = %f\n", metrics.labels()[i],metrics.precision(metrics.labels()[i]));
            System.out.format("Class %f recall = %f\n", metrics.labels()[i], metrics.recall(metrics.labels()[i]));
            System.out.format("Class %f F1 score = %f\n", metrics.labels()[i], metrics.fMeasure(metrics.labels()[i]));
        }

        //Weighted stats
        IOUtils.write("\nWeighted precision = "+metrics.weightedPrecision(), fsdos);
        IOUtils.write("\nWeighted recall = "+metrics.weightedRecall(), fsdos);
        IOUtils.write("\nWeighted F1 score ="+metrics.weightedFMeasure(), fsdos);
        IOUtils.write("\nWeighted false positive rate = " +metrics.weightedFalsePositiveRate(), fsdos);

        fsdos.flush();
        IOUtils.closeQuietly(fsdos);

    }

}