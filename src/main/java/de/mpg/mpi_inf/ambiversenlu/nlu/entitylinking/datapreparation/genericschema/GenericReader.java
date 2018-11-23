package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public interface GenericReader extends YAGO3Reader {

  public List<Fact> getFacts(String relation) throws IOException;

  public Iterator<Fact> iterator(String relation) throws IOException;

}
