package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Use this to optimize multiple parameter values with different
 * ranges. This is extremely crude and simple, just one step above
 * changing the parameters by hand.
 * 
 * It will change one parameter at a time, finding a local maximum, iterating
 * over all parameters. After a fixed number of rounds, it stops.
 * 
 */
public class ParameterOptimizer {
  
  public static Random random = new Random(1337);
  
  public static ParameterOptimizer po_;
  
  private BufferedWriter writer_;
    
  private Optimizable optimizable_;
   
  private ParameterConfig bestConfiguration_;
  
  private double bestValue_;
  
  private int maxRounds_ = 4;
    
  public static void main(String[] args) throws IOException, ExecutionException, EntityLinkingDataAccessException {
    if (args.length != 2) {
      System.out.println("Usage:\n\tParamaterOptimizer " +
          "<conf-file> <output-log>");
      System.exit(1);
    }
    AIDAOptimizable opt = new AIDAOptimizable(new File(args[0]));
    po_ = new ParameterOptimizer(opt, new File(args[1]));
    po_.run();
  }
  
  public ParameterOptimizer(Optimizable optimizable, File outputLog) throws FileNotFoundException {
    optimizable_ = optimizable;
    File logDir = outputLog.getParentFile(); 
    if (!logDir.exists()) {
      logDir.mkdirs();
    }
    writer_ = FileUtils.getBufferedUTF8Writer(outputLog);
  }
  
  public void run() throws IOException, ExecutionException {  
    Map<ParameterConfig, Double> allResults = 
        new HashMap<ParameterConfig, Double>();
    
    ParameterConfig currentConfig = 
        new ParameterConfig(optimizable_.getParameters());
        
    ParameterOptimizer.logStep("Starting optimization at " + new Date());
    ParameterOptimizer.logStep("Optimizing: " + optimizable_.getParameters());
    
    for (int i = 0; i < maxRounds_; ++i) {
      int round = i + 1;
      ParameterOptimizer.logStep("Starting round " + round + "/" + maxRounds_);
      
      for (Parameter p : optimizable_.getParameters()) {
        Pair<ParameterConfig, Double> result =
            findMaximum(p, currentConfig, optimizable_, i, allResults);
        ParameterOptimizer.logStep("Round " + round + " tested param " + p.getName() + ". Current best config: " + result.first);
        ParameterOptimizer.logStep("Round " + round + " tested param " + p.getName() + ". Current max: " + result.second);
        if (result.second > bestValue_) {
          ParameterOptimizer.logStep(
              "Update best configuration from " + bestConfiguration_ + " to " 
          + result.first + ". Value from " + bestValue_ + " to " + result.second);
          bestValue_ = result.second;
          bestConfiguration_ = new ParameterConfig(result.first);
        }
        currentConfig = new ParameterConfig(bestConfiguration_);
      }
    }
    
    ParameterOptimizer.logStep("Done.");
    ParameterOptimizer.logStep("Best Parameter settings: " + bestConfiguration_);
    ParameterOptimizer.logStep("Best value: " + bestValue_);
  }
  
  public static synchronized void logStep(String logMessage) throws IOException {
    po_.getWriter().write("[" + new Date() + "] " + logMessage);
    po_.getWriter().newLine();
    po_.getWriter().flush();
  }

  private Pair<ParameterConfig, Double>
    findMaximum(final Parameter p, final ParameterConfig config,
        Optimizable opt, int round, Map<ParameterConfig, Double> allResults) throws IOException, ExecutionException {
    double currentBest = bestValue_;
    ParameterConfig bestConfig = new ParameterConfig(config);
    ParameterConfig currentConfig = new ParameterConfig(config);
    
    for (int i = 0; i < p.getNumSteps(); ++i) {
      // Get actual p from currentConfig.
      Parameter pFromConfig = currentConfig.getParameter(p.getName());
      pFromConfig.update(round, i);
      currentConfig.setParameter(pFromConfig);
            
      int roundNum = round + 1;
      double result = -1.0;
      if (allResults.containsKey(currentConfig)) {
        ParameterOptimizer.logStep("Round " + roundNum + ", step " + i + 
            ", Taking cached result for " + currentConfig + ": " + allResults.get(currentConfig));
        result = allResults.get(currentConfig);
      } else {
        result = opt.run(currentConfig);
        allResults.put(new ParameterConfig(currentConfig), result);
        ParameterOptimizer.logStep("Round " + roundNum + ", step " + i + ": " + result + " with " + pFromConfig);
      }
      if (result > currentBest) {
        currentBest = result;
        ParameterOptimizer.logStep("Step updating best config from " + bestConfig + 
            " to " + currentConfig + ": " + currentBest);
        bestConfig = new ParameterConfig(currentConfig);
      }
    }
    return new Pair<ParameterConfig, Double>(bestConfig, currentBest);
  }
  
  public BufferedWriter getWriter() {
    return writer_;
  }
}
