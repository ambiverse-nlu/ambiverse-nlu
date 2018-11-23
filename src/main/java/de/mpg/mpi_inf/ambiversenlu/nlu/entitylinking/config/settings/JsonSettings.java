package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings;

import java.io.Serializable;

public class JsonSettings implements Serializable {

  private static final long serialVersionUID = -4865501451793585305L;

  public enum JSONTYPE {
    DEFAULT, COMPACT, WEB, STICS, EXTENDED, ANNOTATED_TEXT
  }
}
