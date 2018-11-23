package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

public class Wrapper {

  private int paragraph = 0;

  private int sentence = -1;

  private int np = 0;

  private int treeDepth = 0;

  private int standfordId = -1;

  public Wrapper() {
  }

  public int paragraph() {
    return paragraph;
  }

  public void setParagraph(int value) {
    paragraph = value;
  }

  public int sentence() {
    return sentence;
  }

  public void sentence(int value) {
    sentence = value;
  }

  public int np() {
    return np;
  }

  public void np(int value) {
    np = value;
  }

  public int treeDepth() {
    return treeDepth;
  }

  public void setTreeDepth(int value) {
    treeDepth = value;
  }

  public int getStandfordId() {
    return standfordId;
  }

  public void setStandfordId(int value) {
    this.standfordId = value;
  }

}