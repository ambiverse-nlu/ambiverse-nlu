package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.Char;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

public class Token implements Serializable {

  private static final long serialVersionUID = 585919047497016142L;

  private int standfordTokenId = -1;

  private String original = null;

  private String originalEnd = "";

  private int beginIndex = -1;

  private int endIndex = -1;

  private int length = -1;

  private int sentence = -1;

  private int paragraph = -1;

  private String ptag = null;

  private String ne = null;

  private String lemma = null;

  private int pageNumber = -1;

  private HashMap<String, Pair<String, Integer>> mapDataIdToEntity = null;

  public Token(int standfordTokenId, String original, int beginIndex, int endIndex, int paragraph) {
    this.standfordTokenId = standfordTokenId;
    this.original = original;
    this.beginIndex = beginIndex;
    this.endIndex = endIndex;
    this.paragraph = paragraph;
    this.length = this.endIndex - this.beginIndex;
  }

  public Token(int standfordTokenId, String original, String originalEnd, int beginIndex, int endIndex, int sentence, int paragraph, String pos,
      String ne) {
    this.standfordTokenId = standfordTokenId;
    this.original = original;
    this.originalEnd = originalEnd;
    this.beginIndex = beginIndex;
    this.endIndex = endIndex;
    this.sentence = sentence;
    this.paragraph = paragraph;
    this.length = this.endIndex - this.beginIndex;
    //this.tag = tag;
    this.ptag = pos;
    this.ne = ne;
  }

  public int getId() {
    return getStandfordId();
  }

  public int getStandfordId() {
    return standfordTokenId;
  }

  public String getOriginal() {
    return original;
  }

  public void setOriginal(String original) {
    this.original = original;
  }

  public String getOriginalEnd() {
    return originalEnd;
  }

  public void setOriginalEnd(String value) {
    this.originalEnd = value;
  }

  public int getBeginIndex() {
    return beginIndex;
  }

  public int getEndIndex() {
    return endIndex;
  }

  public void setSentence(int value) {
    this.sentence = value;
  }

  public int getSentence() {
    return sentence;
  }

  public int getParagraph() {
    return paragraph;
  }

  public void setParagraph(int value) {
    this.paragraph = value;
  }

  public void setNE(String ne) {
    this.ne = ne;
  }

  public String getNE() {
    return ne;
  }

  public void setPOS(String pos) {
    this.ptag = pos;
  }

  public String getPOS() {
    return ptag;
  }

  public void setLemma(String lemma) {
    this.lemma = lemma;
  }

  public String getLemma() {
    return lemma;
  }

  public String toString() {
    return standfordTokenId + " :: " + getOriginal() + " :: " + ne + " / " + ptag + " / " + lemma + " (" + beginIndex + "," + endIndex + ";" + length
        + ") " + " {" + sentence + "/" + paragraph + "}";
  }

  public String toHtmlString() {
    return "<th><a name = token" + standfordTokenId + ">" + standfordTokenId + "</a></th><th>" + "<b><a href=\"#text" + standfordTokenId
        + "\" STYLE=\"color:#000000;text-decoration:none\">" + Char.toHTML(getOriginal()) + "</a></b></th><th>" + ne + " / " + ptag + "</th><th>"
        + beginIndex + " / " + endIndex + " / " + length + "</th><th>" + sentence + " / " + paragraph + "" + "</th>";
  }

  public void addFinalEntity(String datasetId, String entity, int togo) {
    if (mapDataIdToEntity == null) {
      mapDataIdToEntity = new HashMap<String, Pair<String, Integer>>();
    }
    Pair<String, Integer> entry = new Pair<String, Integer>(entity, togo);
    mapDataIdToEntity.put(datasetId, entry);
  }

  public Pair<String, Integer> getFinalEntity(String id) {
    return mapDataIdToEntity.get(id);
  }

  public Iterator<String> getDataSetIds() {
    return mapDataIdToEntity.keySet().iterator();
  }

  public boolean containsData() {
    return mapDataIdToEntity != null;
  }

  public boolean containsDataForId(String id) {
    if (mapDataIdToEntity == null) {
      return false;
    }
    return mapDataIdToEntity.containsKey(id);
  }

  public int getMaxTogo() {
    if (mapDataIdToEntity == null) {
      return -1;
    }
    int max = 0;
    Iterator<String> iter = mapDataIdToEntity.keySet().iterator();
    while (iter.hasNext()) {
      String key = iter.next();
      if (mapDataIdToEntity.get(key).second > max) {
        max = mapDataIdToEntity.get(key).second;
      }
    }
    return max;
  }

  public boolean allEntityEq(String name) {
    if (mapDataIdToEntity == null) {
      return true;
    }
    boolean eq = true;
    Iterator<String> iter = mapDataIdToEntity.keySet().iterator();
    while (eq && iter.hasNext()) {
      String key = iter.next();
      if (!name.equals(mapDataIdToEntity.get(key).first)) {
        eq = false;
      }
    }
    return eq;
  }

  public String getEntity(String Id) {
    if (mapDataIdToEntity.containsKey(Id)) {
      return mapDataIdToEntity.get(Id).first;
    }
    return null;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pNumber) {
    pageNumber = pNumber;
  }
}
