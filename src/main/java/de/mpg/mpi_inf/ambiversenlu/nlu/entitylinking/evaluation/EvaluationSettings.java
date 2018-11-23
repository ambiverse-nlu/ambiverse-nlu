package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;

import java.io.IOException;
import java.util.Properties;

public class EvaluationSettings {

  private boolean ignoreGroundTruthOOKBE;

  private boolean evaluateRecognition;

  private boolean trace;

  private PipelineType pipelineType;

  private boolean evaluateConcepts;

  private boolean evaluateNE;
  
  private String extraFlag;
  
  private boolean trainingSimilarityWeights = false;
  
  private boolean addMentionToBoth = false;
  
  public static EvaluationSettings readFromFile(String path) throws IOException {
    EvaluationSettings es = new EvaluationSettings();
    Properties prop = ClassPathUtils.getPropertiesFromClasspath(path);
    for (String key : prop.stringPropertyNames()) {
      switch (key) {
        case "ignoreGroundTruthOOKBE":
          es.setIgnoreGroundTruthOOKBE(Boolean.parseBoolean(prop.getProperty(key)));
          break;
        case "evaluateRecognition": 
          es.setEvaluateRecognition(Boolean.parseBoolean(prop.getProperty(key))); 
          break;
        case "trace":
          es.setTrace(Boolean.parseBoolean(prop.getProperty(key)));
          break;
        case "pipelineType": 
          es.setPipelineType(PipelineType.valueOf(prop.getProperty(key))); 
          break;
        case "evaluateConcepts": 
          es.setEvaluateConcepts(Boolean.parseBoolean(prop.getProperty(key))); 
          break;
        case "evaluateNE": 
          es.setEvaluateNE(Boolean.parseBoolean(prop.getProperty(key))); 
          break;
        case "extraFlag":
          es.setExtraFlag(prop.getProperty(key)); 
          break;
        case "addMentionToBoth": 
          es.setAddMentionToBoth(Boolean.parseBoolean(prop.getProperty(key))); 
          break;
        case "trainingSimilarityWeights":
          es.setTrainingSimilarityWeights(Boolean.parseBoolean(prop.getProperty(key)));
          break;
        default:
          throw new IllegalArgumentException("Unknown property " + prop.getProperty(key));
      }
    }
    return es;
  }
  
  public boolean isIgnoreGroundTruthOOKBE() {
    return ignoreGroundTruthOOKBE;
  }

  public void setIgnoreGroundTruthOOKBE(boolean ignoreGroundTruthOOKBE) {
    this.ignoreGroundTruthOOKBE = ignoreGroundTruthOOKBE;
  }

  public void setTrace(boolean trace) {
    this.trace = trace;
  }

  public boolean isTrace() {
    return trace;
  }
    
  public PipelineType getPipelineType() {
    return pipelineType;
  }
  
  public void setPipelineType(PipelineType pipelineType) {
    this.pipelineType = pipelineType;
  }

  public boolean isEvaluateConcepts() {
    return evaluateConcepts;
  }

  public void setEvaluateConcepts(boolean evaluateConcepts) {
    this.evaluateConcepts = evaluateConcepts;
  }

  public boolean isEvaluateNE() {
    return evaluateNE;
  }

  public void setEvaluateNE(boolean evaluateNE) {
    this.evaluateNE = evaluateNE;
  }

  public boolean isEvaluateRecognition() {
    return evaluateRecognition;
  }

  public void setEvaluateRecognition(boolean evaluateRecognition) {
    this.evaluateRecognition = evaluateRecognition;
  }
  
  public String getExtraFlag() {
    return extraFlag;
  }

  public void setExtraFlag(String extraFlag) {
    this.extraFlag = extraFlag;
  }

  public boolean getAddMentionToBoth() {
    return addMentionToBoth;
  }

  public void setAddMentionToBoth(boolean addMentionToBoth) {
    this.addMentionToBoth = addMentionToBoth;
  }

  public String getSettingInString() {
    return getPipelineType().toString() + "_" + isEvaluateNE() + "_" + isEvaluateConcepts() + "_" + isEvaluateRecognition() + "_" + isIgnoreGroundTruthOOKBE()
    + "_" + getAddMentionToBoth() + "_" + getExtraFlag();
  }

  public boolean isTrainingSimilarityWeights() {
    return trainingSimilarityWeights;
  }

  public void setTrainingSimilarityWeights(boolean trainingSimilarityWeights) {
    this.trainingSimilarityWeights = trainingSimilarityWeights;
  }
}