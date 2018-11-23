package de.mpg.mpi_inf.ambiversenlu.nlu.openie.model;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

public class OpenFact {

  public OpenFact(Subject subject, Relation relation, Object object) {
    this.subject = subject;
    this.object = object;
    this.relation = relation;
  }

  Relation relation;
  Object object;
  String annotator;
  double confidence;
  String uri;
  Subject subject;
  int begin;
  int end;

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

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  String text;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public Subject getSubject() {
    return subject;
  }

  public void setSubject(Subject subject) {
    this.subject = subject;
  }

  public Relation getRelation() {
    return relation;
  }

  public void setRelation(Relation relation) {
    this.relation = relation;
  }

  public Object getObject() {
    return object;
  }

  public void setObject(Object object) {
    this.object = object;
  }

  public String getAnnotator() {
    return annotator;
  }

  public void setAnnotator(String annotator) {
    this.annotator = annotator;
  }

  public double getConfidence() {
    return confidence;
  }

  public void setConfidence(double confidence) {
    this.confidence = confidence;
  }

  public static List<OpenFact> getOpenFactsFromJCas(JCas jCas) {
    List<OpenFact> ofs = new ArrayList<>();
    for(de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact of: JCasUtil.select(jCas, de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact.class)) {
      Subject subject = Subject.getSubjectFromUimaSubject(of.getSubject());
      Relation relation = Relation.getRelationFromUimaRelation(of.getRelation());
      Object object = Object.getObjectFromUimaRelation(of.getObject());
      OpenFact open_fact = new OpenFact(subject, relation, object);
      if(of.getUri() != null) {
        open_fact.setUri(of.getUri());
      }
      if(of.getAnnotator() != null) {
        open_fact.setAnnotator(of.getAnnotator());
      }
      if(of.getText() != null) {
        open_fact.setText(of.getText());
     }
     open_fact.setBegin(of.getBegin());
      open_fact.setEnd(of.getEnd());
      open_fact.setConfidence(of.getConfidence());
     ofs.add(open_fact);
    }
    return ofs;
  }
}
