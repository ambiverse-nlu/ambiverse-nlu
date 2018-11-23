package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.EntityMetaData;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.measures.MeasureTracer;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Collection;

public class NullEntityEntityTracing extends EntityEntityTracing {

  @Override public String generateOutput(TIntObjectHashMap<EntityMetaData> emd) {
    return "";
  }

  @Override public void addEntityEntityMeasureTracer(int e1, int e2, MeasureTracer mt) {
  }

  @Override public void setCorrectEntities(Collection<Integer> correctEntities) {
  }
}
