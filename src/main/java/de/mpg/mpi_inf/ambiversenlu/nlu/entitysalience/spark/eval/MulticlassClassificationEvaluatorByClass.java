package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.spark.eval;

import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.util.SchemaUtils;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class MulticlassClassificationEvaluatorByClass extends MulticlassClassificationEvaluator {

    //By default optimize by the positive class
    private double evaluationClass = 1.0;


    public double getEvaluationClass() {
        return evaluationClass;
    }

    public void setEvaluationClass(double evaluationClass) {
        this.evaluationClass = evaluationClass;
    }

    @Override
    public double evaluate(DataFrame dataset) {
        StructType schema = dataset.schema();

        SchemaUtils.checkColumnType(schema, this.getPredictionCol(), DataTypes.DoubleType, "");
        SchemaUtils.checkColumnType(schema, this.getLabelCol(), DataTypes.DoubleType, "");

        MulticlassMetrics metrics = new MulticlassMetrics(dataset
                .select(this.getPredictionCol(), this.getLabelCol()));


        int labelColumn = 0;
        for(int i=0; i < metrics.labels().length; i++) {
            if(metrics.labels()[i] == evaluationClass) {
                labelColumn = i;
            }
        }

        double metric=0d;
        switch(getMetricName()) {
            case "f1":
                metric = metrics.fMeasure(metrics.labels()[labelColumn]);
                break;
            case "precision":
                metric = metrics.precision(metrics.labels()[labelColumn]);
                break;
            case "recall":
                metric = metrics.recall(metrics.labels()[labelColumn]);
                break;
            case "weightedPrecision":
                metric = metrics.weightedPrecision();
                break;
            case "weightedRecall":
                metric = metrics.weightedRecall();
                break;


        }

        return metric;
    }
}
