package de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative.D;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers.FileSet;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;

import java.io.File;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/**
 * Class FactSource
 *
 * This code is part of the YAGO project at the Max Planck Institute for
 * Informatics and the Telecom ParisTech University. It is licensed under a
 * Creative Commons Attribution License by the YAGO team:
 * https://creativecommons.org/licenses/by/3.0/
 *
 * This class represents a source of facts (given by an URL, or a file) in any
 * format (N4, TSV, ...) that can be read multiple times.
 *
 * @author Steffen Metzger
 *
 */
public abstract class FactSource implements Iterable<Fact> {

  /** Name of this source */
  protected String name;

  /** returns a fact source from a file. assumes ttl by default. */
  public static FactSource from(File f) {
    if (!f.getName().contains(".")) f = FileSet.newExtension(f, ".ttl");
    return (new FileFactSource(f));
  }

  /**
   * returns a fact source from a file or URL
   *
   * @throws MalformedURLException
   */
  public static FactSource from(String f) throws MalformedURLException {
    if (f.startsWith("http:")) return (new UrlFactSource(new URL(f)));
    return (new FileFactSource(new File(f)));
  }

  /** returns a fact source from an url */
  public static FactSource from(URL f) {
    return (new UrlFactSource(f));
  }

  /** returns a fact reader depending on the extension */
  protected static Iterator<Fact> factReader(Reader reader, String fileExtension) throws Exception {
    switch (fileExtension) {
      case ".ttl":
        return (new N4Reader(reader));
      case ".tsv":
        return (new TsvReader(reader));
      default:
        throw new RuntimeException("Unknown file format " + fileExtension);
    }
  }

  public String name() {
    return name;
  }

  /** Fact source from file */
  public static class FileFactSource extends FactSource {

    protected File file;

    @Override public Iterator<Fact> iterator() {
      try {
        return factReader(FileUtils.getBufferedUTF8Reader(file), FileSet.extension(file));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public FileFactSource(File file) {
      super();
      this.file = file;
      this.name = file == null ? "FactSource" : file.toString();
    }

    @Override public String toString() {
      return file.toString();
    }

  }

  /** Fact source from url */
  protected static class UrlFactSource extends FactSource {

    URL file;

    @Override public Iterator<Fact> iterator() {
      try {
        return factReader(FileUtils.getBufferedUTF8Reader(file.openStream()), FileSet.extension(file.toString()));
      } catch (Exception e) {
        e.printStackTrace();
        return (null);
      }
    }

    public UrlFactSource(URL file) {
      super();
      this.file = file;
      this.name = file.toString();
    }

    @Override public String toString() {
      return file.toString();
    }

  }

  public static void main(String[] args) throws Exception {
    for (Fact f : FactSource.from("c:/fabian/data/yago2s/yagoMetaFacts.tsv")) {
      D.p(f);
    }
  }

}
