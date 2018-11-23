package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerParam extends Parameter {
  private Logger logger_ = LoggerFactory.getLogger(IntegerParam.class);
  
  private int value_;
  
  private int stepSize_;
  
  private int min_;
  
  private int max_;
    
  private int initialStepValue_;
  
  public IntegerParam(Parameter p) {
    IntegerParam dp = (IntegerParam) p;
    name_ = dp.getName();
    value_ = dp.getValue();
    min_ = dp.getMin();
    max_ = dp.getMax();
    stepSize_ = dp.getStepSize();
    numSteps_ = dp.getNumSteps();
    initialStepValue_ = dp.getInitialStepValue();
  }

  public IntegerParam(String name, int initialValue, int min, 
      int max, int stepSize, int numSteps) {
    name_ = name;
    value_ = initialValue;
    min_ = min;
    max_ = max;
    stepSize_ = stepSize;
    numSteps_ = numSteps;
  }

  @Override
  public void update(int round, int currentStep) {
    int step = stepSize_;
    if (round > 0) {
      // Every round, half the step size.
      step = stepSize_ / (round * 2);
      if (step == 0) {
        step = 1;
      }
    }

    // Sweep around the initial value for each step.
    if (currentStep == 0) {
      initialStepValue_ = value_;
    }
    int offset = (getNumSteps() / 2) - currentStep;
    value_ = (offset * step) + initialStepValue_;         
    
    // Make sure value stays in bounds.
    if (value_ < min_) {
      value_ = min_;
      logger_.warn(this + ": value " + value_ + " < min: " + min_);
    }
    if (value_ > max_) {
      value_ = max_;
      logger_.warn(this + ": value " + value_ + " > max: " + min_);
    }
  }
  
  public int getValue() {
    return value_;
  }

  
  public int getStepSize() {
    return stepSize_;
  }
  
  public int getMin() {
    return min_;
  }

  
  public int getMax() {
    return max_;
  }
  
  public int getInitialStepValue() {
    return initialStepValue_;
  }

  @Override
  public String getValueRepresentation() {
    return String.valueOf(value_);
  }
}
