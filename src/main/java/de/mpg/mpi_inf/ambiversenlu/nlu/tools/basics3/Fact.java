package de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative.D;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.DateParser;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.NumberParser;

/**
 * Class Fact
 *
 * This code is part of the YAGO project at the Max Planck Institute for
 * Informatics and the Telecom ParisTech University. It is licensed under a
 * Creative Commons Attribution License by the YAGO team:
 * https://creativecommons.org/licenses/by/3.0/
 *
 * @author Fabian M. Suchanek
 *
 *         This class represents a fact for YAGO. Convention: all fact
 *         components must be the output of a method of the class FactComponent
 */
public class Fact {

  /** ID (or NULL) */
  private String id;

  /** Argument 1 */
  protected final String subject;

  /** Relation */
  protected final String relation;

  /** Argument 2 */
  protected final String object;

  /** Hash code */
  protected int hashCode;

  /**
   * All fact components must be the output of a method of the class
   * FactComponent!
   */
  public Fact(String id, String arg1, String relation, String object) {
    this.id = id;
    this.subject = arg1;
    this.relation = relation;
    this.object = object;
    this.hashCode = arg1.hashCode() ^ relation.hashCode() ^ object.hashCode();
  }

  /**
   *
   */
  public Fact(String id, String subject, String relation, String object, boolean doHashCode) {
    this.id = id;
    this.subject = subject;
    this.relation = relation;
    this.object = object;
    if (doHashCode) {
      this.hashCode = subject.hashCode() ^ relation.hashCode() ^ object.hashCode();
    }
  }

  /**
   * All fact components must be the output of a method of the class
   * FactComponent!
   */
  public Fact(String arg1, String relation, String arg2withDataType) {
    this(null, arg1, relation, arg2withDataType);
  }

  /** Creates a copy of the fact */
  public Fact(Fact copy) {
    this.subject = copy.subject;
    this.object = copy.object;
    this.relation = copy.relation;
    this.id = copy.getId();
    this.hashCode = copy.hashCode;
  }

  @Override public int hashCode() {
    if (hashCode != 0) {
      return hashCode;
    } else {
      return subject.hashCode() ^ relation.hashCode() ^ object.hashCode();
    }
  }

  @Override public boolean equals(Object obj) {
    if (!(obj instanceof Fact)) return (false);
    Fact f = (Fact) obj;
    return (D.equal(id, f.id) && subject.equals(f.subject) && relation.equals(f.relation) && object.equals(f.object));
  }

  /** Returns object as a Java string */
  public String getObjectAsJavaString() {
    return (FactComponent.asJavaString(object));
  }

  /** Gets subject */
  public String getSubject() {
    return (subject);
  }

  /** Gets object with data type */
  public String getObject() {
    return (object);
  }

  /** returns the relation */
  public String getRelation() {
    return (relation);
  }

  @Override public String toString() {
    return (getId() == null ? "" : getId() + " ") + subject + " " + relation + " " + object;
  }

  /** returns the id */
  public String getId() {
    return id;
  }

  /** returns a TSV line */
  public String toTsvLine() {
    return toTsvLine(false);
  }

  /** returns a TSV line */
  public String toTsvLine(boolean withValue) {
    if (withValue && FactComponent.isLiteral(object)) {
      String val = getValue();
      if (val == null) val = "";
      return ((id == null ? "" : id) + "\t" + getArg(1) + "\t" + getRelation() + "\t" + getArg(2) + "\t" + val + "\n");
    } else {
      return ((id == null ? "" : id) + "\t" + getArg(1) + "\t" + getRelation() + "\t" + getArg(2) + (withValue ? "\t\n" : "\n"));
    }
  }

  public String getValue() {
    String val = null;
    if (FactComponent.isLiteral(object)) {
      String datatype = getDataType();
      if (datatype != null && datatype.equals("xsd:date")) {
        String[] split = DateParser.getDate(object);
        if (split != null && split.length == 3) {
          for (int i = 0; i < 3; i++) {
            split[i] = split[i].replace('#', '0');
            while (split[i].length() < 2) split[i] = "0" + split[i];
          }
          val = split[0] + "." + split[1] + split[2];
        }
      } else if (datatype != null) {
        val = NumberParser.getNumber(FactComponent.stripQuotes(object));
      }
    }
    return val;
  }

  /** returns the datatype of the second argument */
  public String getDataType() {
    return (FactComponent.getDatatype(object));
  }

  /** Gets argument 1 or 2 */
  public String getArg(int a) {
    return (a == 1 ? getSubject() : getObject());
  }
}
