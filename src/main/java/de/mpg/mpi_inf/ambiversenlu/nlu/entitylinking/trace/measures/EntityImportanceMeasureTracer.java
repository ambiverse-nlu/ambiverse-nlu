package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures;

import java.text.DecimalFormat;

public class EntityImportanceMeasureTracer extends MeasureTracer {

  public EntityImportanceMeasureTracer(String name, double weight) {
    super(name, weight);
  }

  @Override public String getOutput() {
    DecimalFormat formatter = new DecimalFormat("#0.000");
    return "<strong style='color: #0000FF;'>score = " + formatter.format(score) + "</strong>";
  }

}
