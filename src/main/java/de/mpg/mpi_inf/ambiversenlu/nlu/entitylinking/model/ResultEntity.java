package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import org.apache.uima.jcas.JCas;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.apache.uima.fit.util.JCasUtil.select;

/**
 * Entity the was assigned to a ResultMention. 
 *
 */
public class ResultEntity implements Comparable<ResultEntity>, Serializable {

  private static final long serialVersionUID = -7062155406718136994L;

  private KBIdentifiedEntity kbEntity;

  /** Score assigned to the entity */
  private double score;

  private double salience;

  public ResultEntity() {
    // bean.
  }

  public ResultEntity(String identifier, String knowledgebase, double score) {
    super();
    this.kbEntity = KBIdentifiedEntity.getKBIdentifiedEntity(identifier, knowledgebase);
    this.score = score;
  }

  public ResultEntity(Entity entity, double score) {
    super();
    if (entity instanceof NullEntity || entity.isOOKBentity()) {
      this.kbEntity = KBIdentifiedEntity.getKBIdentifiedEntity(Entity.OOKBE, "AIDA");
      this.score = 0.0;
    } else {
      this.kbEntity = KBIdentifiedEntity.getKBIdentifiedEntity(entity.getIdentifierInKb(), entity.getKnowledgebase());
      this.score = score;
    }
  }

  public static ResultEntity getNoMatchingEntity() {
    return new ResultEntity(Entity.OOKBE, "AIDA", 0.0);
  }

  public static List<ResultEntity> getResultEntityAsList(ResultEntity re) {
    List<ResultEntity> res = new ArrayList<ResultEntity>(1);
    res.add(re);
    return res;
  }

  public String getKgId() {
    return kbEntity.getDictionaryKey();
  }

  public void setKgId(String kgId) {
    kbEntity = new KBIdentifiedEntity(kgId);
  }

  /**
   * @return original knowledgebase identifier of the entity
   */
  @XmlTransient public String getEntity() {
    return kbEntity.getIdentifier();
  }

  @XmlTransient public String getKnowledgebase() {
    return kbEntity.getKnowledgebase();
  }

  @XmlTransient public KBIdentifiedEntity getKbEntity() {
    return kbEntity;
  }

  @XmlTransient public void setKbEntity(KBIdentifiedEntity kbEntity) {
    this.kbEntity = kbEntity;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public double getSalience() {
    return salience;
  }

  public void setSalience(double salience) {
    this.salience = salience;
  }

  @XmlTransient public boolean isNoMatchingEntity() {
    return kbEntity.getIdentifier().equals(Entity.OOKBE);
  }

  @Override public int compareTo(ResultEntity re) {
    // natural ordering for ResultEntities is descending
    return new Double(new Double(re.getScore())).compareTo(score);
  }

  public String toString() {
    NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
    df.setMaximumFractionDigits(5);
    return kbEntity + " (" + df.format(score) + ")";
  }

  @Override public int hashCode() {
    return kbEntity.hashCode();
  }

  @Override public boolean equals(Object obj) {
    if (obj instanceof ResultEntity) {
      return kbEntity.equals(((ResultEntity) obj).kbEntity);
    } else {
      return false;
    }
  }

  public static List<ResultEntity> getAidaResultEntitiesFromJCas(JCas jCas) {
    List<ResultEntity> result = new ArrayList<>();
    for (AidaEntity ne : select(jCas, AidaEntity.class)) {
      ResultEntity re = new ResultEntity();
      re.setScore(ne.getScore());
      re.setKbEntity(new KBIdentifiedEntity(ne.getID()));
      result.add(re);
    }
    return result;
  }
}
