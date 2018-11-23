package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization;

import java.io.IOException;

public class DoubleParam extends Parameter {
 
  private double value_;
  
  private double stepSize_;
  
  private double min_;
  
  private double max_;
    
  private double initialStepValue_;
  
  public DoubleParam(Parameter p) {
    DoubleParam dp = (DoubleParam) p;
    name_ = dp.getName();
    value_ = dp.getValue();
    min_ = dp.getMin();
    max_ = dp.getMax();
    stepSize_ = dp.getStepSize();
    numSteps_ = dp.getNumSteps();
    initialStepValue_ = dp.getInitialStepValue();
  }

  public DoubleParam(String name, double initialValue, double min, 
      double max, double stepSize, int numSteps) {
    name_ = name;
    value_ = initialValue;
    min_ = min;
    max_ = max;
    stepSize_ = stepSize;
    numSteps_ = numSteps;
  }

  @Override
  public void update(int round, int currentStep) {
    double step = stepSize_;
    if (round > 0) {
      // Every round, half the step size.
      step = stepSize_ / (round * 2);
    }

    // Sweep around the initial value for each step.
    if (currentStep == 0) {
      initialStepValue_ = value_;
    }
    int offset = (getNumSteps() / 2) - currentStep;
    value_ = (offset * step) + initialStepValue_;         
    
    // Make sure value stays in bounds.
    if (value_ < min_) {
      try {
        ParameterOptimizer.logStep("WARN: " + this + ": value " + value_ + " < min: " + min_);
      } catch (IOException e) {
        e.printStackTrace();
      }
      value_ = min_;
    }
    if (value_ > max_) {
      try {
        ParameterOptimizer.logStep("WARN: " + this + ": value " + value_ + " > max: " + min_);
      } catch (IOException e) {
        e.printStackTrace();
      }
      value_ = max_;
    }
  }
  
  public double getValue() {
    return value_;
  }

  
  public double getStepSize() {
    return stepSize_;
  }
  
  public double getMin() {
    return min_;
  }

  
  public double getMax() {
    return max_;
  }
  
  public double getInitialStepValue() {
    return initialStepValue_;
  }

  @Override
  public String getValueRepresentation() {
    return String.valueOf(value_);
  }
}
