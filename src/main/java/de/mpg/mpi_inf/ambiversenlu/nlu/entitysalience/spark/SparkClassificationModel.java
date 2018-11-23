package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.featureset.FeatureSet;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.featureset.FeatureSetFactory;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.settings.TrainingSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.eval.MulticlassClassificationEvaluatorByClass;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.ml.*;
import org.apache.spark.ml.classification.*;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.evaluation.Evaluator;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.*;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.sql.DataFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.when;

/**
 * Common access for classification models in a Spark environment.
 *
 *
 */
public class SparkClassificationModel {

    private static Logger logger = LoggerFactory.getLogger(SparkClassificationModel.class);

    private TrainingSettings.ClassificationMethod classificationMethod;
    private Pipeline pipeline;
    private Evaluator evaluator;

    private CrossValidatorModel bestModel;

    private ParamMap[] paramGrid;

    public SparkClassificationModel(TrainingSettings.ClassificationMethod classificationMethod) {
        this.classificationMethod = classificationMethod;
    }

    /**
     * Create a chain of Transformers and Estimators to specify an ML workflow.
     * Depending on the classification method used, a different pipeline is created.
     *
     * @param trainData
     *
     */
    private void createPipeline(DataFrame trainData, TrainingSettings trainingSettings) {

        ParamGridBuilder paramGridBuilder;

        FeatureSet fs = FeatureSetFactory.createFeatureSet(trainingSettings.getFeatureExtractor());
        int maxCategoryCount = trainingSettings.getMaxCategories();

        // Index labels, adding metadata to the label column.
        // Fit on whole dataset to include all labels in index.
        StringIndexerModel labelIndexer = new StringIndexer()
                .setInputCol("label")
                .setOutputCol("indexedLabel")
                .fit(trainData);

        // Automatically identify categorical features, and index them.
        // Set maxCategories so features with > maxCategoryCount distinct values are treated as continuous.
        VectorIndexerModel featureIndexer = new VectorIndexer()
                .setInputCol("features")
                .setOutputCol("indexedFeatures")
                .setMaxCategories(maxCategoryCount)
                .fit(trainData);

        // Convert indexed labels back to original labels.
        IndexToString labelConverter = new IndexToString()
                .setInputCol("prediction")
                .setOutputCol("predictedLabel")
                .setLabels(labelIndexer.labels());

        switch (classificationMethod) {
            case RANDOM_FOREST:

                logger.info("Creating Random Forest Classification Estimator.");

                // Train a RandomForest model.
                RandomForestClassifier rf = new RandomForestClassifier()
                        .setLabelCol("indexedLabel")
                        .setFeaturesCol("indexedFeatures");


                //Set settings defined in the TrainingSettings
                if (trainingSettings.getFeatureSubsetStrategy() != null) {
                    rf.setFeatureSubsetStrategy(trainingSettings.getFeatureSubsetStrategy());
                }
                if (trainingSettings.getMaxBins() != null) {
                    rf.setMaxBins(trainingSettings.getMaxBins());
                }
                if (trainingSettings.getSeed() != null) {
                    rf.setSeed(trainingSettings.getSeed());
                }
                if (trainingSettings.getImpurity() != null) {
                    rf.setImpurity(trainingSettings.getImpurity());
                }


                //Crate an ML pipeline
                pipeline = new Pipeline()
                        .setStages(new PipelineStage[]{labelIndexer, featureIndexer, rf, labelConverter});

                //Set the parameters for cross validation
                paramGridBuilder = new ParamGridBuilder();
                if(trainingSettings.getNumTreesX() != null) {
                    paramGridBuilder.addGrid(rf.numTrees(), trainingSettings.getNumTreesX());
                }

                if(trainingSettings.getMaxDepthX() != null) {
                    paramGridBuilder.addGrid(rf.maxDepth(), trainingSettings.getMaxDepthX());
                }

                paramGrid = paramGridBuilder.build();
                logger.debug("Parameter Grid for Cross Validation: "+ Arrays.toString(paramGrid));

                evaluator = new MulticlassClassificationEvaluatorByClass()
                        .setLabelCol("indexedLabel")
                        .setPredictionCol("prediction")
                        .setMetricName(trainingSettings.getMetricName());


//                evaluator = new BinaryClassificationEvaluator()
//                        .setLabelCol("label")
//                        .setRawPredictionCol("rawPrediction")
//                        .setMetricName(trainingSettings.getMetricName());

                break;
            case GBT:

                // Train a GBT model.
                GBTClassifier gbt = new GBTClassifier()
                        .setLabelCol("label")
                        .setFeaturesCol("indexedFeatures");

                if(trainingSettings.getMaxBins() != null) {
                    gbt.setMaxBins(trainingSettings.getMaxBins());
                }
                if(trainingSettings.getMaxIterations() != null) {
                    gbt.setMaxIter(trainingSettings.getMaxIterations());
                }
                if(trainingSettings.getMaxDepth() != null) {
                    gbt.setMaxDepth(trainingSettings.getMaxDepth());
                }


                pipeline = new Pipeline()
                        .setStages(new PipelineStage[]{labelIndexer, featureIndexer, gbt, labelConverter});


                paramGridBuilder = new ParamGridBuilder();
                if(trainingSettings.getMaxDepthX() != null) {
                    paramGridBuilder.addGrid(gbt.maxDepth(), trainingSettings.getMaxDepthX());
                }

                if(trainingSettings.getMaxIterationsX() != null) {
                    paramGridBuilder.addGrid(gbt.maxIter(), trainingSettings.getMaxIterationsX());
                }

                paramGrid = paramGridBuilder.build();

                logger.debug("Parameter Grid for Cross Validation: "+ Arrays.toString(paramGrid));
                evaluator = new MulticlassClassificationEvaluator()
                        .setLabelCol("label")
                        .setPredictionCol("prediction")
                        .setMetricName(trainingSettings.getMetricName());

                break;

            case LOG_REG:

                LogisticRegression lr = new LogisticRegression()
                        .setWeightCol("classWeightCol")
                        .setLabelCol("label")
                        .setFeaturesCol("features");


                if(trainingSettings.getMaxIterations() != null) {
                    lr.setMaxIter(trainingSettings.getMaxIterations());
                }

                if(trainingSettings.getLrElasticNetParam() != null) {
                    lr.setElasticNetParam(trainingSettings.getLrElasticNetParam());
                }

                if(trainingSettings.getLrRegParam() != null) {
                    lr.setRegParam(trainingSettings.getLrRegParam());
                }

                pipeline = new Pipeline()
                        .setStages(new PipelineStage[]{lr});

                paramGridBuilder = new ParamGridBuilder();

                if(trainingSettings.getMaxIterationsX() != null) {
                    paramGridBuilder.addGrid(lr.maxIter(), trainingSettings.getMaxIterationsX());
                }
                if(trainingSettings.getLrElasticNetParamCV() != null) {
                    paramGridBuilder.addGrid(lr.elasticNetParam(), trainingSettings.getLrElasticNetParamCV());
                }
                if(trainingSettings.getLrRegParamCV() != null) {
                    paramGridBuilder.addGrid(lr.regParam(), trainingSettings.getLrRegParamCV());
                }

                paramGrid = paramGridBuilder.build();
                logger.debug("Parameter Grid for Cross Validation: "+ Arrays.toString(paramGrid));

                //evaluator = new BinaryClassificationEvaluator().setMetricName(trainingSettings.getMetricName());
                evaluator = new MulticlassClassificationEvaluatorByClass()
                        .setLabelCol("label")
                        .setPredictionCol("prediction")
                        .setMetricName(trainingSettings.getMetricName());
                break;

            default:
                throw new IllegalStateException("No classificationMethod: '" +classificationMethod + "'.");
        }
    }

    private Evaluator getEvaluator(TrainingSettings trainingSettings, Predictor predictor) {

        Evaluator evaluator = null;
        if(predictor instanceof RandomForestClassifier
               || predictor instanceof GBTClassifier
               || predictor instanceof DecisionTreeClassifier) {

            evaluator = new MulticlassClassificationEvaluator()
                    .setLabelCol("indexedLabel")
                    .setPredictionCol("prediction")
                    .setMetricName(trainingSettings.getMetricName());
        }

        if(predictor instanceof LogisticRegression) {
            evaluator = new BinaryClassificationEvaluator().setMetricName(trainingSettings.getMetricName());
        }

        return evaluator;
    }

    /**
     * Get the parameter grid for CrossValidation
     * @param trainingSettings
     * @param predictor
     * @return
     */
    private ParamMap[] getParameters(TrainingSettings trainingSettings, Predictor predictor) {
        ParamGridBuilder paramGridBuilder;

        ParamMap[] paramGrid = null;

        if(predictor instanceof RandomForestClassifier) {

            logger.info("Creating ParamGrid for Random Forest Classifier.");
            RandomForestClassifier rf = (RandomForestClassifier)predictor;
            //Set the parameters for cross validation
            paramGridBuilder = new ParamGridBuilder();
            if(trainingSettings.getNumTreesX() != null) {
                paramGridBuilder.addGrid(rf.numTrees(), trainingSettings.getNumTreesX());
            }

            if(trainingSettings.getMaxDepthX() != null) {
                paramGridBuilder.addGrid(rf.maxDepth(), trainingSettings.getMaxDepthX());
            }

            paramGrid = paramGridBuilder.build();

            logger.debug("Parameter Grid for Cross Validation: "+ Arrays.toString(paramGrid));
        }

        if(predictor instanceof  GBTClassifier) {

            logger.info("Creating ParamGrid for GBT Classifier.");
            GBTClassifier gbt = (GBTClassifier)predictor;
            paramGridBuilder = new ParamGridBuilder();
            if(trainingSettings.getMaxDepthX() != null) {
                paramGridBuilder.addGrid(gbt.maxDepth(), trainingSettings.getMaxDepthX());
            }

            if(trainingSettings.getMaxIterationsX() != null) {
                paramGridBuilder.addGrid(gbt.maxIter(), trainingSettings.getMaxIterationsX());
            }

            paramGrid = paramGridBuilder.build();

            logger.debug("Parameter Grid for Cross Validation: "+ Arrays.toString(paramGrid));
        }

        if(predictor instanceof LogisticRegression) {

            logger.info("Creating ParamGrid for LogisticRegression.");
            LogisticRegression lr = (LogisticRegression)predictor;
            paramGridBuilder = new ParamGridBuilder();

            if(trainingSettings.getMaxIterationsX() != null) {
                paramGridBuilder.addGrid(lr.maxIter(), trainingSettings.getMaxIterationsX());
            }
            if(trainingSettings.getLrElasticNetParamCV() != null) {
                paramGridBuilder.addGrid(lr.elasticNetParam(), trainingSettings.getLrElasticNetParamCV());
            }
            if(trainingSettings.getLrRegParamCV() != null) {
                paramGridBuilder.addGrid(lr.regParam(), trainingSettings.getLrRegParamCV());
            }

            paramGrid = paramGridBuilder.build();

            logger.debug("Parameter Grid for Cross Validation: "+ Arrays.toString(paramGrid));
        }

        return paramGrid;
    }

    /**
     *
     * @param trainData
     * @param trainingSettings
     * @return
     */
    public CrossValidatorModel crossValidate(DataFrame trainData, TrainingSettings trainingSettings) {

        //First create the pipeline and the ParamGrid
        createPipeline(trainData, trainingSettings);

        // We now treat the Pipeline as an Estimator, wrapping it in a CrossValidator instance.
        // This will allow us to jointly choose parameters for all Pipeline stages.
        // A CrossValidator requires an Estimator, a set of Estimator ParamMaps, and an Evaluator.
        CrossValidator cv = new CrossValidator()
                .setEstimator(pipeline)
                .setEvaluator(evaluator)
                .setEstimatorParamMaps(paramGrid)
                .setNumFolds(trainingSettings.getNumFolds());

        if(classificationMethod.equals(TrainingSettings.ClassificationMethod.LOG_REG)) {
            long numPositive = trainData.filter(col("label").equalTo("1.0")).count();
            long datasetSize = trainData.count();
            double balancingRatio = (double)(datasetSize - numPositive) / datasetSize;

            trainData = trainData
                    .withColumn("classWeightCol",
                            when(col("label").equalTo("1.0"), 1* balancingRatio)
                                    .otherwise((1 * (1.0 - balancingRatio))));
        }
        // Run cross-validation, and choose the best set of parameters.
        bestModel = cv.fit(trainData);
        System.out.println("IS LARGER BETTER ?"+bestModel.getEvaluator().isLargerBetter());
        return bestModel;
    }

    public Model train(DataFrame trainData, TrainingSettings trainingSettings) {
        //First create the pipeline and the ParamGrid
        createPipeline(trainData, trainingSettings);

        return pipeline.fit(trainData);
    }

    public static void saveStats(CrossValidatorModel model, TrainingSettings trainingSettings, String output) throws IOException {
        double[] avgMetrics = model.avgMetrics();
        double bestMetric = 0;
        int bestIndex=0;

        for(int i=0; i<avgMetrics.length; i++) {
            if(avgMetrics[i] > bestMetric) {
                bestMetric = avgMetrics[i];
                bestIndex = i;
            }
        }


        FileSystem fs = FileSystem.get(new Configuration());
        Path statsPath = new Path(output+"stats_"+trainingSettings.getClassificationMethod()+".txt");
        fs.delete(statsPath, true);

        FSDataOutputStream fsdos = fs.create(statsPath);

        String avgLine="Average cross-validation metrics: "+ Arrays.toString(model.avgMetrics());
        String bestMetricLine="\nBest cross-validation metric ["+trainingSettings.getMetricName()+"]: "+bestMetric;
        String bestSetParamLine= "\nBest set of parameters: "+model.getEstimatorParamMaps()[bestIndex];

        logger.info(avgLine);
        logger.info(bestMetricLine);
        logger.info(bestSetParamLine);


        IOUtils.write(avgLine, fsdos);
        IOUtils.write(bestMetricLine, fsdos);
        IOUtils.write(bestSetParamLine, fsdos);

        PipelineModel pipelineModel = (PipelineModel) model.bestModel();
        for(Transformer t : pipelineModel.stages()) {
            if(t instanceof ClassificationModel) {
                IOUtils.write("\n"+((Model) t).parent().extractParamMap().toString(), fsdos);
                logger.info(((Model) t).parent().extractParamMap().toString());
            }
        }

        fsdos.flush();
        IOUtils.closeQuietly(fsdos);

        debugOutputModel(model,trainingSettings, output);
    }

    public static void debugOutputModel(CrossValidatorModel model, TrainingSettings trainingSettings, String output) throws IOException {
        FileSystem fs = FileSystem.get(new Configuration());
        Path statsPath = new Path(output+"debug_"+trainingSettings.getClassificationMethod()+".txt");
        fs.delete(statsPath, true);

        FSDataOutputStream fsdos = fs.create(statsPath);
        PipelineModel pipelineModel = (PipelineModel) model.bestModel();
        switch (trainingSettings.getClassificationMethod()) {
            case RANDOM_FOREST:
                for(int i=0; i< pipelineModel.stages().length; i++) {
                    if (pipelineModel.stages()[i] instanceof RandomForestClassificationModel) {
                        RandomForestClassificationModel rfModel = (RandomForestClassificationModel) (pipelineModel.stages()[i]);
                        IOUtils.write(rfModel.toDebugString(), fsdos);
                        logger.info(rfModel.toDebugString());
                    }
                }
                break;
            case LOG_REG:
                for(int i=0; i< pipelineModel.stages().length; i++) {
                    if (pipelineModel.stages()[i] instanceof LogisticRegressionModel) {
                        LogisticRegressionModel lgModel = (LogisticRegressionModel) (pipelineModel.stages()[i]);
                        IOUtils.write(lgModel.toString(), fsdos);
                        logger.info(lgModel.toString());
                    }
                }
                break;
        }
        fsdos.flush();
        IOUtils.closeQuietly(fsdos);
    }


    public TrainingSettings.ClassificationMethod getClassificationMethod() {
        return classificationMethod;
    }

    public void setClassificationMethod(TrainingSettings.ClassificationMethod classificationMethod) {
        this.classificationMethod = classificationMethod;
    }
}
