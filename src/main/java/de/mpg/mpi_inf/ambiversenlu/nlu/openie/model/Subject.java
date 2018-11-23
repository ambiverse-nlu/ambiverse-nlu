package de.mpg.mpi_inf.ambiversenlu.nlu.openie.model;

public class Subject extends Constituent {

  public static Subject getSubjectFromUimaSubject(de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Subject subject) {
    return (Subject) getConstituentFromUimaConstituent(subject, Subject.class);
  }

}
