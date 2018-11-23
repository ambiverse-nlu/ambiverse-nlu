package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers;

public class LanguageNotSupportedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public LanguageNotSupportedException() {
	super();
  }

  public LanguageNotSupportedException(String message) {
	super(message);
  }

  public LanguageNotSupportedException(String message, Throwable cause) {
	super(message, cause);
  }
}
