package de.mpg.mpi_inf.ambiversenlu.nlu.openie.model;

public class Relation extends Constituent {

  public static Relation getRelationFromUimaRelation(de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Relation relation) {
    return (Relation) getConstituentFromUimaConstituent(relation, Relation.class);
  }
}
