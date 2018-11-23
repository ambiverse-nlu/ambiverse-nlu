package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util;

import org.apache.spark.ml.param.Param;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a transformer which build a transformation
 * starting from an input column and generating the results of
 * transformation into an output column. The output schema
 * will be the same as the input schema with the addition at the
 * end of fields of a given output column.
 * column
 *
 * @author Tiziano Fagni (tiziano.fagni@isti.cnr.it)
 */
public abstract class UnaryTransformer extends JavaTransformer {

    private final Param<String> inputColParam;
    private final Param<String> outputColParam;

    public UnaryTransformer() {
        inputColParam = new Param<String>(this, "inputCol", "Input column name");
        outputColParam = new Param<String>(this, "outputCol", "Output column name");
        setDefault(this.inputColParam, "inputCol");
        setDefault(this.outputColParam, "outputCol");
    }

    // ------ Generated param getter to ensure that Scala params() function works well! --------
    public Param<String> getInputColParam() {
        return inputColParam;
    }

    // ------ Generated param getter to ensure that Scala params() function works well! --------
    public Param<String> getOutputColParam() {
        return outputColParam;
    }

    /**
     * Get the input column name.
     *
     * @return The input column name.
     */
    public String getInputCol() {
        return getOrDefault(inputColParam);
    }


    /**
     * Set the input column name.
     *
     * @param inputCol The input column name.
     * @return
     */
    public UnaryTransformer setInputCol(String inputCol) {
        set(this.inputColParam, inputCol);
        return this;
    }

    /**
     * Get the output column name.
     *
     * @return The output column name.
     */
    public String getOutputCol() {
        return getOrDefault(outputColParam);
    }


    /**
     * Set the output column name.
     *
     * @param outputCol The output column name.
     * @return
     */
    public UnaryTransformer setOutputCol(String outputCol) {
        set(this.outputColParam, outputCol);
        return this;
    }



    @Override
    public StructType transformSchema(StructType structType) {
        String inputCol = getInputCol();
        String outputCol = getOutputCol();
        DataType inputType = structType.apply(inputCol).dataType();
        this.validateInputType(inputType);
        List<String> names = Arrays.asList(structType.fieldNames());
        Cond.require(!names.contains(outputCol), "The output column " + outputCol + " already exists in this schema!");
        List<StructField> fields = new ArrayList<>();
        for (int i = 0; i < structType.fields().length; i++) {
            fields.add(structType.fields()[i]);
        }
        DataType dt = getOutputDataType();
        fields.add(DataTypes.createStructField(outputCol, dt, isOutputDataTypeNullable()));
        return DataTypes.createStructType(fields);
    }


    /**
     * Return the output data type to be included in the resulting schema.
     *
     * @return The output data type to be included in the resulting schema.
     */
    protected abstract DataType getOutputDataType();

    /**
     * Indicate if the output column can contains 'null' values.
     *
     * @return True if the output column can contains 'null' values, false otherwise.
     */
    protected abstract boolean isOutputDataTypeNullable();

    /**
     * Validate the provided input type. Raise an exception if this transformer is unable to
     * process this kind of data.
     *
     * @param inputType The input type to validate.
     * @throws Exception Raised if the input type is not valid.
     */
    protected abstract void validateInputType(DataType inputType);

}