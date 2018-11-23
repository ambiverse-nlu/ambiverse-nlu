package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace;

import java.util.List;
import java.util.Map;

public class NullGraphTracer extends GraphTracer {

  @Override public void addCandidateEntityToOriginalGraph(String docId, String mention, int candidateEntity, double entityWeightedDegree,
      double MESimilairty, Map<Integer, Double> connectedEntities) {
  }

  @Override public void addCandidateEntityToCleanedGraph(String docId, String mention, int candidateEntity, double entityWeightedDegree,
      double MESimilairty) {
  }

  @Override public void addCandidateEntityToFinalGraph(String docId, String mention, int candidateEntity, double entityWeightedDegree,
      double MESimilairty) {
  }

  @Override public void addEntityRemovalStep(String docId, int entity, double entityWeightedDegree, List<String> connectedMentions) {
  }

  @Override public void writeOutput(String outputPath) {
  }

  @Override public void addStat(String docId, String description, String value) {
  }
}
