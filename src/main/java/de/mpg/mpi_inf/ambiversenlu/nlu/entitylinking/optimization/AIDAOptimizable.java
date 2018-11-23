package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation.Utils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.run.UimaCommandLineDisambiguator;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers.FileLines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * Optimizable that initializes itself from a config file in the following 
 * format:
 * first line: 
 *  Base-parameters that will not change throughout the optimization.
 *  This is usually the AIDA setup for the collection reader + some additions.
 * following lines:
 *  Parameters that change. Starting with either "Integer" or "Double",
 *  followed by (space-separated) 
 *  name initialValue min max stepSize numSteps
 * 
 * Lines starting with # will be ignored.
 * 
 */
public class AIDAOptimizable extends Optimizable {

  private Logger logger_ = LoggerFactory.getLogger(AIDAOptimizable.class);
  
  private List<String> baseParams_;
  
  private List<Parameter> params_ = new ArrayList<Parameter>();

  private int[] fromTo_;
    
  private Pattern p = Pattern.compile("Mention-Average Precision = (.*)");
  
  public AIDAOptimizable(File confFile) throws IOException, EntityLinkingDataAccessException {
    EntityLinkingManager.init();
    parseConfigFile(confFile);
  }
   
  private void parseConfigFile(File file) throws IOException {
    // Parse conf file.
    boolean optiParams = false;
    for (String line : new FileLines(file)) {
      if (line.startsWith("#")) {
        continue;
      }
      if (!optiParams) {
        baseParams_ = Arrays.asList(line.split(" "));
        optiParams = true;
      } else {
        String[] data = line.split(" ");
        if (data[0].equals("Double")) {
          if (data.length != 7) {
            logger_.error(
                "Invalid parameter config for Double. Expecting: "
                + "Double name initialValue min max stepSize numSteps");
          }          
          String name = data[1];
          Double initialValue = Double.parseDouble(data[2]);
          Double min = Double.parseDouble(data[3]);
          Double max = Double.parseDouble(data[4]);
          Double stepSize = Double.parseDouble(data[5]);
          Integer numSteps = Integer.parseInt(data[6]);
          DoubleParam d = 
              new DoubleParam(name, initialValue, min, max, stepSize, numSteps);
          params_.add(d);
        } else if (data[0].equals("Integer")) {
          if (data.length != 7) {
            logger_.error(
                "Invalid parameter config for Double. Expecting: "
                + "Integer name initialValue min max stepSize numSteps");
          }          
          String name = data[1];
          Integer initialValue = Integer.parseInt(data[2]);
          Integer min = Integer.parseInt(data[3]);
          Integer max = Integer.parseInt(data[4]);
          Integer stepSize = Integer.parseInt(data[5]);
          Integer numSteps = Integer.parseInt(data[6]);
          IntegerParam i = 
              new IntegerParam(name, initialValue, min, max, stepSize, numSteps);
          params_.add(i);
        }
//        } else if (data[0].equals("COLLECTION")) {
//          fromTo_ = new int[2];
//          fromTo_[0] = Integer.parseInt(data[1]);
//          fromTo_[1] = Integer.parseInt(data[2]);
//        } 
          else {
          logger_.error("Invalid parameter '" + data[0] + "'");
        }
      }
    }
    Integer startD = baseParams_.indexOf("-b");
    Integer endD = baseParams_.indexOf("-f");
    if (startD != null && endD != null) {
      fromTo_ = new int[2];
      fromTo_[0] =  Integer.parseInt(baseParams_.get(startD+1));
      fromTo_[1] =  Integer.parseInt(baseParams_.get(endD+1));
    }
    
    logger_.info("Parsed config file.");
    logger_.info("Base params: " + baseParams_);
    logger_.info("Collection range: " + fromTo_[0] + "-" + fromTo_[1]);
    logger_.info("Optimization params: " + params_);
  }

  @Override
  public List<Parameter> getParameters() {
    return params_;
  }

  @Override
  public int getCollectionSize() {
    return fromTo_[1] - fromTo_[0];
  }

  @Override
  public int[] getFullBatch() {
    int[] batch = new int[fromTo_[1] - fromTo_[0] + 1];
    for (int i = 0; i < batch.length; ++i) {
      batch[i] = i;
    }
    return batch;
  }

  @Override
  public double run(ParameterConfig currentConfig) throws ExecutionException {
    double result = 0;
    try {
      String[] args = createArgsForParams(currentConfig.getParameters());
      new UimaCommandLineDisambiguator().run(args);
      Map<String, Double> results = Utils.readResults(currentConfig.getParameter("d").getValueRepresentation());
      result = results.get("F1");
    } catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    }
    return result;
  }
  
  private String[] createArgsForParams(Collection<Parameter> params) {
    List<String> argList = new ArrayList<String>(baseParams_);
    for (Parameter p : params) {
      argList.add(p.getName());
      argList.add(p.getValueRepresentation());
    }
    return argList.toArray(new String[argList.size()]);
  }
}
