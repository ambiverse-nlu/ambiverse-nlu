package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor;

public class UnprocessableDocumentException extends Exception {

  private static final long serialVersionUID = -2345734089885266148L;

  public UnprocessableDocumentException(Throwable cause) {
    super(cause);
  }

  public UnprocessableDocumentException(String message) {
    super(message);
  }

}
