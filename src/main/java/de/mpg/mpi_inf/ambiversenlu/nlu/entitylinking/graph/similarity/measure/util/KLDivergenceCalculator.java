package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.util;

public class KLDivergenceCalculator {

  private double klDivergence;

  private boolean normalized;

  public KLDivergenceCalculator(boolean normalized) {
    klDivergence = 0d;
    this.normalized = normalized;
  }

  public double getKLDivergence() {
    return klDivergence;
  }

  /**
   * Adds a new summand to the KLDivergence equation.
   *
   * @param documentTermCount The number occurrences of the term in the document.
   * @param documentTermSum The number of occurrences of all terms in the document summed up.
   * @param entityTermCount The number of relations between the term and the entity.
   * @param entityTermSum The number of all relations from the entity to terms.
   * @param globalTermCount The number of entities the term is related to.
   * @param globalEntityCount The number of entities.
   * @param smoothingParameter A smoothing parameter.
   * @return The calculated summand.
   */
  public double addSummand(int documentTermCount, int documentTermSum, int entityTermCount, int entityTermSum, int globalTermCount,
      int globalEntityCount, double smoothingParameter) {
    double termDocumentProb = calcTermDocumentProb(documentTermCount, documentTermSum);
    if (termDocumentProb == 0) return 0;
    double termEntityProb = calcTermEntityProb(entityTermCount, entityTermSum, globalTermCount, globalEntityCount, smoothingParameter);
    double klDivergenceAddition = calcKLDivergenceAddition(termDocumentProb, termEntityProb);
    klDivergence += klDivergenceAddition;
    return klDivergenceAddition;
  }

  private double calcKLDivergenceAddition(double termDocumentProb, double termEntityProb) {
    return (termDocumentProb * Math.log(termDocumentProb / termEntityProb));
  }

  private double calcTermEntityProb(int entityTermCount, int entityTermSum, int globalTermCount, int globalEntityCount, double smoothingParameter) {
    if (normalized)
      return calcNormalizedSmoothedTermEntityProb(entityTermCount, entityTermSum, globalTermCount, globalEntityCount, smoothingParameter);
    else return calcUnnormalizedSmoothedTermEntityProb(entityTermCount, globalTermCount, globalEntityCount, smoothingParameter);
  }

  private double calcNormalizedSmoothedTermEntityProb(int entityTermCount, int entityTermSum, int globalTermCount, int globalEntityCount,
      double smoothingParameter) {
    return (entityTermCount + smoothingParameter * ((double) globalTermCount / globalEntityCount)) / (entityTermSum + smoothingParameter);
  }

  private double calcUnnormalizedSmoothedTermEntityProb(int entityTermCount, int globalTermCount, int globalEntityCount, double smoothingParameter) {
    return (entityTermCount + smoothingParameter * ((double) globalTermCount / globalEntityCount)) / smoothingParameter;
  }

  private double calcTermDocumentProb(int documentTermCount, int documentTermSum) {
    return (double) documentTermCount / documentTermSum;
  }
}
