package de.mpg.mpi_inf.ambiversenlu.nlu.ner;

import java.util.Comparator;

public class Name {

  private String name;

  private int start;

  private int length;

  private double score;

  private String nerAnnotatorId;

  public Name(String name) {
    super();
    this.name = name;

  }

  public Name(String name, int start) {
    super();
    this.name = name;
    this.start = start;
    this.length = name.length();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    this.length = name.length();
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  /**
   * returns true if the mention overlaps with text[start:end]
   */
  public boolean overlaps(int start, int end) {
    if (start > getEnd()) return false;
    if (end < getStart()) return false;
    return true;

  }

  public String getNerAnnotatorId() {
    return nerAnnotatorId;
  }

  public void setNerAnnotatorId(String nerAnnotatorId) {
    this.nerAnnotatorId = nerAnnotatorId;
  }

  public int getEnd() {
    return start + length - 1;
  }

  public int getLength() {
    return length;
  }

  @Override public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + length;
    result = prime * result + start;
    return result;
  }

  @Override public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Name other = (Name) obj;
    if (length != other.length) return false;
    if (start != other.start) return false;
    return true;
  }

  @Override public String toString() {
    return "Name [name=" + name + ", start=" + start + ", length=" + length + ", score=" + score + ", nerAnnotatorId=" + nerAnnotatorId + "]";
  }

  public static class SortByStartPosition implements Comparator<Name> {

    @Override public int compare(Name o1, Name o2) {
      return o1.getStart() - o2.getStart();
    }

  }

}
