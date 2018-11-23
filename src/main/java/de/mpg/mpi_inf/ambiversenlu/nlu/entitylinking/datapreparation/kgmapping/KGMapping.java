package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.kgmapping;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema.GenericReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema.Relations;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.AIDASchemaPreparationConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class KGMapping {

  private static Map<String, String> mapping = new HashMap<>();

  public void addKGMapping(GenericReader reader) throws IOException {
    List<Fact> kgmappings = reader.getFacts(Relations.SAME_AS.getRelation());
    for(Fact fact: kgmappings) {
      String kb = fact.getObject().substring(0, fact.getObject().indexOf(":"));
      if(kb.equals(AIDASchemaPreparationConfig.get("target.db"))) {
        mapping.put(fact.getObject(), fact.getSubject());
      }
    }
  }

  public static String getMapping(String kbname, String id) {
    String result = mapping.get(kbname+":"+id);
    return result;
  }







}
