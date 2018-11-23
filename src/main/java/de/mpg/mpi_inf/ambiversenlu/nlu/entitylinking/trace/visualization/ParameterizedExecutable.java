package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Base class that initializes Parameters with JCommander. Useful
 * for classes with a main method. The main method only needs
 * to create a new instance of the class, passing all args, and implement
 * the run() method.
 *
 * run() has to be called once the object is instantiated.
 */
public abstract class ParameterizedExecutable {

  @Parameter(names = "--help", help = true)
  private boolean help;

  public ParameterizedExecutable(String[] args) throws Exception {
    parseArgs(args);
  }

  /** Checks if usage should be displayed, if not calls run. */
  private void parseArgs(String[] args) throws Exception {
    JCommander jc = new JCommander(this);
    jc.parse(args);
    if (help) {
      jc.usage();
    }
  }

  protected abstract void run() throws Exception;
}
