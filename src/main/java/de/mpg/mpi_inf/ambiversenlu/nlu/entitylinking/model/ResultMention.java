package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation.Utils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Mention detected in the input text. It is identified uniquely
 * by the combination of the three members docId+mention+characterOffset.
 *
 *
 */
public class ResultMention implements Comparable<ResultMention>, Serializable {

  private static final Logger logger = LoggerFactory.getLogger(ResultMention.class);

  private static final long serialVersionUID = -6791087404868641006L;

  private String mention;

  private int characterOffset;

  private int characterLength;

  private List<ResultEntity> resultEntities;

  public ResultMention() {
    // dummy.
  }

  public ResultMention(String mention, int characterOffset, int characterLength) {
    super();
    this.mention = mention;
    this.characterOffset = characterOffset;
    this.characterLength = characterLength;
  }

  @XmlElement(name = "text") public String getMention() {
    return mention;
  }

  @XmlElement(name = "text") public void setMention(String mention) {
    this.mention = mention;
  }

  @XmlElement(name = "charOffset") public int getCharacterOffset() {
    return characterOffset;
  }

  @XmlElement(name = "charOffset") public void setCharacterOffset(int characterOffset) {
    this.characterOffset = characterOffset;
  }

  @XmlElement(name = "charLength") public int getCharacterLength() {
    return characterLength;
  }

  @XmlElement(name = "charLength") public void setCharacterLength(int characterLength) {
    this.characterLength = characterLength;
  }

  @XmlTransient public List<ResultEntity> getResultEntities() {
    return resultEntities;
  }

  public void setResultEntities(List<ResultEntity> resultEntities) {
    this.resultEntities = resultEntities;
    Collections.sort(resultEntities);
  }

  /**
   * Returns the best matching entity or null if there is none.
   *
   * @return Best matching entity or null if there is none.
   */
  @XmlElement(name = "entity") public ResultEntity getBestEntity() {
    if (resultEntities == null || resultEntities.size() == 0) {
      return null;
    } else {
      return resultEntities.get(0);
    }
  }

  @XmlElement(name = "entity") public void setBestEntity(ResultEntity bestEntity) {
    resultEntities = new ArrayList<>(1);
    resultEntities.add(bestEntity);
  }

  @Override public boolean equals(Object o) {
    if (o instanceof ResultMention) {
      ResultMention rm = (ResultMention) o;
      return (mention.equals(rm.getMention()) && characterOffset == rm.getCharacterOffset());
    } else {
      return false;
    }
  }

  @Override public int hashCode() {
    int hash = mention.hashCode() + characterOffset;
    return hash;
  }

  @Override public int compareTo(ResultMention rm) {
    return new Integer(characterOffset).compareTo(new Integer(rm.getCharacterOffset()));
  }

  public String toString() {
    return mention + " (" + characterOffset + "/" + characterLength + "): " + resultEntities;
  }

  public static List<ResultMention> getResultMentionsFromJCas(JCas jCas) {
    List<ResultMention> result = new ArrayList<>();

    Map<String, SalientEntity> salientEntities = Utils.getSalientEntitiesFromJCas(jCas);

    for (AidaEntity ne : JCasUtil.select(jCas, AidaEntity.class)) {
      ResultEntity re = new ResultEntity();
      re.setScore(ne.getScore());
      re.setKbEntity(new KBIdentifiedEntity(ne.getID()));
      if(salientEntities.containsKey(ne.getID())) {
        re.setSalience(salientEntities.get(ne.getID()).getSalience());
      }
      ResultMention rm = new ResultMention();
      rm.setBestEntity(re);
      rm.setCharacterLength(ne.getBegin() - ne.getEnd());
      rm.setCharacterOffset(ne.getBegin());
      rm.setMention(ne.getCoveredText());
      result.add(rm);
    }
    return result;
  }
  
  public static List<ResultMention> getResultAidaMentionsFromJCas(JCas jCas) {
    List<ResultMention> result = new ArrayList<>();
    for(AidaEntity ne: JCasUtil.select(jCas, AidaEntity.class)) {
      ResultEntity re = new ResultEntity();
      re.setScore(ne.getScore());
      re.setKbEntity(new KBIdentifiedEntity(ne.getID()));
      ResultMention rm = new ResultMention();
      rm.setBestEntity(re);
      rm.setCharacterLength(ne.getEnd() - ne.getBegin());
      rm.setCharacterOffset(ne.getBegin());
      rm.setMention(ne.getCoveredText());
      result.add(rm);
    }
    return result;
  }
  
  public static List<ResultMention> getResultConceptMentionsFromJCas(JCas jCas) {
    List<ResultMention> result = new ArrayList<>();
    for(ConceptEntity ne: JCasUtil.select(jCas, ConceptEntity.class)) {
      ResultEntity re = new ResultEntity();
      re.setScore(ne.getScore());
      re.setKbEntity(new KBIdentifiedEntity(ne.getID()));
      ResultMention rm = new ResultMention();
      rm.setBestEntity(re);
      rm.setCharacterLength(ne.getEnd() - ne.getBegin());
      rm.setCharacterOffset(ne.getBegin());
      rm.setMention(ne.getCoveredText());
      result.add(rm);
    }
    return result;
  }
}
