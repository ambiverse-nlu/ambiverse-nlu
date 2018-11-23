package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

/** A constituent of a clause. */
public abstract class Constituent {


  // -- types -----------------------------------------------------------------------------------

  /** Constituent types */
  public enum Type {
    SUBJECT, VERB, DOBJ, IOBJ, COMPLEMENT, CCOMP, XCOMP, ACOMP, ADVERBIAL, UNKOWN
  }

  ;

  /** Constituent flags */
  public enum Flag {
    REQUIRED, OPTIONAL, IGNORE
  }

  ;

  // -- member variables ------------------------------------------------------------------------

  /** Type of this constituent */
  protected Type type;

  // -- construction ----------------------------------------------------------------------------

  /** Constructs a constituent of the specified type. */
  protected Constituent(Type type) {
    this.type = type;
  }

  /** Constructs a constituent of unknown type. */
  protected Constituent() {
    this.type = Type.UNKOWN;
  }

  // -- getters/setters -------------------------------------------------------------------------

  /** Returns the type of this constituent. */
  public Type getType() {
    return type;
  }

  // -- utility methods -------------------------------------------------------------------------

  /** Returns a textual representation of the root word of this constituent. */
  public abstract String rootString();

  public abstract Constituent clone();
}
