package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeOutput;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Document {

  private String name;
  private String path;

  private Map<Configuration, AnalyzeOutput> confOutput = new LinkedHashMap<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Map<Configuration, AnalyzeOutput> getConfOutput() {
    return confOutput;
  }

  public void setConfOutput(Map<Configuration, AnalyzeOutput> confOutput) {
    this.confOutput = confOutput;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Document doc = (Document) o;
    return Objects.equals(this.name, doc.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override public String toString() {
    return "Document{" + "name='" + name + '\'' + ", path='" + path + '\'' + ", confOutput=" + confOutput + '}';
  }
}
