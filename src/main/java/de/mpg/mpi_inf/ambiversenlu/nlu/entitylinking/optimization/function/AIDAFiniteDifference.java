package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization.function;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization.AIDAOptimizable;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization.DoubleParam;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization.Parameter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization.ParameterConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;
import edu.jhu.hlt.optimize.function.Bounds;
import edu.jhu.hlt.optimize.function.DifferentiableBatchFunction;
import edu.jhu.hlt.optimize.function.ValueGradient;
import edu.jhu.prim.vector.IntDoubleDenseVector;
import edu.jhu.prim.vector.IntDoubleVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AIDAFiniteDifference implements DifferentiableBatchFunction {

  private static Logger logger_ = LoggerFactory.getLogger(AIDAFiniteDifference.class);

  private int dim;
  private double[] stepSize;
  private Bounds b;
  private AIDAOptimizable opt;
  int index = 0;
  private static BufferedWriter writer_;

  public AIDAFiniteDifference(int dim, double[] stepSize, Bounds b, AIDAOptimizable opt, File outputLog) throws FileNotFoundException {
    this.dim = dim;
    this.stepSize = stepSize;
    this.b = b;
    this.opt = opt;
    writer_ = FileUtils.getBufferedUTF8Writer(outputLog);
  }

  @Override
  public double getValue(IntDoubleVector point, int[] batch) {
    AIDAFiniteDifference.logStep("Getting Value for: " + point + " as batch: " + Arrays.toString(batch));
    double result = 0;
    List<Parameter> newParameters = new ArrayList<>();
    String[] names = new String[dim];
    try {
      for (int i = 0; i < dim; i++) {
        Parameter p = opt.getParameters().get(i);
        DoubleParam dp = (DoubleParam) p;
        names[i] = dp.getName();
        int numSteps = dp.getNumSteps();

        // This is a hack - make sure that the constraints are not violated during the actual optimization.
        double value = point.get(i);
        double upper = b.getUpper(i);
        double lower = b.getLower(i);
        if (value > upper) {
          logStep("WARNING: '" + opt.getParameters().get(i).getName() + "' violated a boundary. Setting from " + value + " to " + upper);
          value = upper;
        } else if (value < lower) {
          logStep("WARNING: '" + opt.getParameters().get(i).getName() + "' violated a boundary. Setting from " + value + " to " + lower);
          value = lower;
        }
        DoubleParam d =
                new DoubleParam(names[i], value, b.getLower(i), b.getUpper(i), stepSize[i], numSteps);
        newParameters.add(d);
      }

      ParameterConfig currentConfig =
              new ParameterConfig(newParameters);

      result = opt.run(currentConfig);
      AIDAFiniteDifference.logStep("Result: " + result);

    } catch (ExecutionException e) {
      logger_.error("Exception: " + e.getMessage());
      e.printStackTrace();
    }
    System.out.println(index + "----" + point + "-----" + result);

    try {
      Thread.sleep(50);
    } catch (InterruptedException e1) {
      Thread.currentThread().interrupt();
    }
    return result;
  }

  @Override public int getNumExamples() {
    return opt.getCollectionSize();
  }

  @Override public double getValue(IntDoubleVector point) {
    AIDAFiniteDifference.logStep("Getting Value for: " + point);
    return getValue(point, opt.getFullBatch());
  }

  @Override public IntDoubleVector getGradient(IntDoubleVector point) {
    AIDAFiniteDifference.logStep("Getting Gradient for: " + point);
    return getGradient(point, opt.getFullBatch());
  }

  @Override public ValueGradient getValueGradient(IntDoubleVector point) {
    AIDAFiniteDifference.logStep("Getting ValueGradient for: " + point);
    return getValueGradient(point, opt.getFullBatch());
  }

  @Override
  public int getNumDimensions() {
    return dim;
  }

  @Override
  public IntDoubleVector getGradient(IntDoubleVector point, int[] batch) {
    AIDAFiniteDifference.logStep("Getting Gradient for: " + point + " as batch: " + Arrays.toString(batch));
    IntDoubleVector x = new IntDoubleDenseVector(computeFiniteDifferenceGradient(point.toNativeArray(), stepSize, batch));
    return x;
  }

  @Override
  public ValueGradient getValueGradient(IntDoubleVector point, int[] batch) {
    AIDAFiniteDifference.logStep("Getting ValueGradient for: " + point + " as batch: " + Arrays.toString(batch));
    return new ValueGradient(getValue(point, batch), getGradient(point, batch));
  }

  // TODO this uses a fixed step-size - this should be reduced every round!
  private double[] computeFiniteDifferenceGradient(double[] point, double[] step, int[] batch) {
    double[] fd = new double[point.length];

    for (int i = 0; i < point.length; i++) {
      double[] upper = new double[point.length];
      System.arraycopy(point, 0, upper, 0, point.length);
      upper[i] += step[i];
      double[] lower = new double[point.length];
      System.arraycopy(point, 0, lower, 0, point.length);
      lower[i] -= step[i];
      fd[i] = (evaluate(upper, batch) - evaluate(lower, batch)) / 2.0 / step[i];
    }

    return fd;
  }

  private double evaluate(double[] x, int[] batch) {
    IntDoubleVector ps = new IntDoubleDenseVector(x);
    return getValue(ps, batch);
  }

  //writing logs
  public BufferedWriter getWriter() {
    return writer_;
  }

  public static void logStep(String logMessage) {
    try {
      logger_.info("LOG: " + logMessage);
      writer_.write("[" + new Date() + "] " + logMessage);
      writer_.newLine();
      writer_.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
