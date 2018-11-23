package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

/** A textual expression of a constituent. The constituent is represented as a plain string
 *  without identifying its internal structure
 *
 * @date $ $
 * @version $ $ */
public class TextConstituent extends Constituent {

  String text;

  /** Constructs a constituent with a specified textual representation and type. */
  public TextConstituent(String text, Type type) {
    super(type);
    this.text = text;
  }

  /** Returns a textual representation of the constituent. */
  public String text() {
    return text;
  }

  /** Returns a textual representation of the constituent. */
  public String rootString() {
    return text;
  }

  @Override public Constituent clone() {
    return new TextConstituent(this.text, this.type);
  }

}
