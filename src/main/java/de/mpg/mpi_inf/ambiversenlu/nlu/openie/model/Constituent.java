package de.mpg.mpi_inf.ambiversenlu.nlu.openie.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;

public class Constituent {

  String text;
  String uri;
  int begin;
  int end;
  Tokens tokens;
  double confidence;
  String normalizedForm;


  public String getNormalizedForm() {
    return normalizedForm;
  }

  public void setNormalizedForm(String normalizedForm) {
    this.normalizedForm = normalizedForm;
  }


  public boolean isExplicit() {
    return explicit;
  }

  public void setExplicit(boolean explicit) {
    this.explicit = explicit;
  }

  boolean explicit;

  public String getAnnotator() {
    return annotator;
  }

  public void setAnnotator(String annotator) {
    this.annotator = annotator;
  }

  public String annotator;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public int getBegin() {
    return begin;
  }

  public void setBegin(int begin) {
    this.begin = begin;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public Tokens getTokens() {
    return tokens;
  }

  public void setTokens(Tokens tokens) {
    this.tokens = tokens;
  }

  public double getConfidence() {
    return confidence;
  }

  public void setConfidence(double confidence) {
    this.confidence = confidence;
  }

  public static<K extends Constituent> K getConstituentFromUimaConstituent(de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent constituent, Class<K> clazz) {
    Constituent result = getCnonstituentInstance(clazz);
    if(constituent.getUri() != null) {
      result.setUri(constituent.getUri());
    }
    if(constituent.getText() != null) {
      result.setText(constituent.getText());
    }
    if(constituent.getExplicit()) {
      result.setBegin(constituent.getBegin());
      result.setEnd(constituent.getEnd());
    }
    result.setExplicit(constituent.getExplicit());
    if(constituent.getAnnotator() != null) {
      result.setAnnotator(constituent.getAnnotator());
    }
    if(constituent.getNormalizedForm() != null) {
      result.setNormalizedForm(constituent.getNormalizedForm());
    }

    return clazz.cast(result);
  }

  private static <K extends Constituent> Constituent getCnonstituentInstance(Class<K> clazz) {
    if(clazz.equals(Subject.class)) {
      return new Subject();
    } else if(clazz.equals(Object.class)) {
      return new Object();
    } else if(clazz.equals(Relation.class)) {
      return new Relation();
    } else {
      throw new IllegalArgumentException("A constituent should be a subject, an object or a relation");
    }

  }
}
