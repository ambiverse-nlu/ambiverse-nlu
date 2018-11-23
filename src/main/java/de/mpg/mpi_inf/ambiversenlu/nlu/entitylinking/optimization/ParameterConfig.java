package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ParameterConfig {
    
  private Map<String, Parameter> params_ = new HashMap<String, Parameter>();

  public ParameterConfig(Collection<Parameter> parameters) {
    for (Parameter p : parameters) {
      params_.put(p.getName(), Parameter.copyParam(p));
    }
  }

  public ParameterConfig(ParameterConfig config) {
    this(config.getParameters());
  }

  public Collection<Parameter> getParameters() {
    return params_.values();
  }

  public Parameter getParameter(String paramName) {
    return params_.get(paramName);
  }
  
  public void setParameter(Parameter p) {
    params_.put(p.getName(), p);
  }
  
  public String toString() {
    return StringUtils.join(params_.values(), ", ");
  }

  @Override
  public int hashCode() {
    int hash = 0;
    if (params_.size() > 0) {
      hash = 31;
      for (Parameter p : params_.values()) {
        hash *= p.hashCode();
      }
    }
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ParameterConfig other = (ParameterConfig) obj;
    if (params_ == null) {
      if (other.params_ != null) return false;
    } else if (!params_.equals(other.params_)) {
      return false;
    }
    return true;
  }
}
