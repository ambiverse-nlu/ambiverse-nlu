package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;

import java.io.IOException;
import java.util.List;

public interface YAGO3Reader {

  public List<Fact> getFacts(String relation) throws IOException;
}
