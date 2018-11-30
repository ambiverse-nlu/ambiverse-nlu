package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization.function.AIDAFiniteDifference;
import edu.jhu.hlt.optimize.AdaDelta;
import edu.jhu.hlt.optimize.AdaDelta.AdaDeltaPrm;
import edu.jhu.hlt.optimize.Optimizer;
import edu.jhu.hlt.optimize.SGD;
import edu.jhu.hlt.optimize.SGD.SGDPrm;
import edu.jhu.hlt.optimize.function.Bounds;
import edu.jhu.hlt.optimize.function.DifferentiableBatchFunction;
import edu.jhu.prim.vector.IntDoubleDenseVector;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Not efficient enough, for now the regular ParameterOptimizer is the better bet.
 *
 */
public class SGDParameterOptimizer {

  private static final Logger logger_ = Logger.getLogger(SGDParameterOptimizer.class);

  public static void main(String[] args) throws IOException, EntityLinkingDataAccessException {
    if (args.length != 2) {
      System.out.println("Usage:\n\tParamaterOptimizer " +
              "<conf-file> <output-log>");
      System.exit(1);
    }

    SGDParameterOptimizer adt = new SGDParameterOptimizer();
    adt.testAIDAFDTest(args);
  }

  public void testAIDAFDTest(String[] args) throws IOException, EntityLinkingDataAccessException {
    AIDAOptimizable opt = new AIDAOptimizable(new File(args[0]));
    int paraSize = opt.getParameters().size();
    double[] initialValues = new double[paraSize];
    double[] lower = new double[paraSize];
    double[] upper = new double[paraSize];
    double[] stepSize = new double[paraSize];
    int index = 0;

    for (Parameter p : opt.getParameters()) {
      if (!(p instanceof DoubleParam)) {
        logger_.error("SGD Optimizer cannot deal with IntegerParams at the moment.");
      }
      DoubleParam dp = (DoubleParam) p;
      initialValues[index] = dp.getValue();

      lower[index] = dp.getMin();
      upper[index] = dp.getMax();
      stepSize[index] = dp.getStepSize();
      index++;
    }

    Optimizer<DifferentiableBatchFunction> optimizer = getOptimizer();
    Bounds b = new Bounds(lower, upper);
    DifferentiableBatchFunction aida = new AIDAFiniteDifference(initialValues.length, stepSize, b, opt, new File(args[1]));
    optimizer.maximize(aida, new IntDoubleDenseVector(initialValues));
    double[] max = initialValues;

    // This is a brutal hack - just make sure that the constraints are not violated AFTER the actual optimization.
    for (int i = 0; i < max.length; i++) {
      if (max[i] > upper[i]) {
        AIDAFiniteDifference.logStep(
            "WARNING: '" + opt.getParameters().get(i).getName() + "' violated a boundary. Setting from " + max[i] + " to " + upper[i]);
        max[i] = upper[i];
      } else if (max[i] < lower[i]) {
        AIDAFiniteDifference.logStep(
            "WARNING: '" + opt.getParameters().get(i).getName() + "' violated a boundary. Setting from " + max[i] + " to " + lower[i]);
        max[i] = lower[i];
      }
    }

    String paraName = "[";
    for (int i = 0; i < max.length; i++) {
      paraName += max[i] + ", ";
    }
    paraName += "]";

    AIDAFiniteDifference.logStep("Done.");
    AIDAFiniteDifference.logStep("Best Parameter settings: " + paraName);
    AIDAFiniteDifference.logStep("Best value: " + aida.getValue(new IntDoubleDenseVector(max)));
  }

  private Optimizer<DifferentiableBatchFunction> getOptimizer() {
    AdaDeltaPrm sched = new AdaDeltaPrm();
//    sched.decayRate = 0.95;
//    sched.constantAddend = Math.pow(Math.E, -6);

    SGDPrm prm = new SGDPrm();
    prm.sched = new AdaDelta(sched);
    prm.numPasses = 10;
    prm.batchSize = 50;
    prm.autoSelectLr = false;
    return new SGD(prm);
  }
}
