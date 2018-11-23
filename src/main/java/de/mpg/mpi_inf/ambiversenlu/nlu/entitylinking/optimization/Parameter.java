package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Parameter {

  private static Logger sLogger_ = LoggerFactory.getLogger(Parameter.class);
  
  protected String name_;
  
  protected int numSteps_;
    
  public static Parameter copyParam(Parameter p) {
    if (p instanceof DoubleParam) {
      return new DoubleParam(p);
    } else if (p instanceof IntegerParam) {
      return new IntegerParam(p);
    } else {
      sLogger_.error("No parameter of this kind defined.");
      return null;
    }
  }

  public String getName() {
    return name_;
  }
  
  public abstract String getValueRepresentation();
  
  public int getNumSteps() {
    return numSteps_;
  }

  public abstract void update(int round, int currentStep);
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getName()).append(": ").append(getValueRepresentation());
    return sb.toString();    
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name_ == null) ? 0 : name_.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Parameter other = (Parameter) obj;
    if (name_ == null) {
      if (other.name_ != null) return false;
    } else if (!name_.equals(other.name_) || 
        !getValueRepresentation().equals(other.getValueRepresentation())) {
      return false;
    }
    return true;
  }
}
