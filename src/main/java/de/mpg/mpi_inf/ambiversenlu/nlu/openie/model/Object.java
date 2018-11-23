package de.mpg.mpi_inf.ambiversenlu.nlu.openie.model;

public class Object extends Constituent {

  public static Object getObjectFromUimaRelation(de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ObjectF object) {
    return (Object) getConstituentFromUimaConstituent(object, Object.class);
  }
}
