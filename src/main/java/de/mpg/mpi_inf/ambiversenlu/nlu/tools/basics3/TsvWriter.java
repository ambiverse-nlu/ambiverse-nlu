package de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers.FileSet;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * Class TsvWriter
 *
 * This code is part of the YAGO project at the Max Planck Institute for
 * Informatics and the Telecom ParisTech University. It is licensed under a
 * Creative Commons Attribution License by the YAGO team:
 * https://creativecommons.org/licenses/by/3.0/
 *
 * This class writes facts to TSV files
 *
 * @author Fabian M. Suchanek
 *
 */
public class TsvWriter extends FactWriter {

  protected Writer out;

  protected boolean writeDoubleValue = false;

  @Override public void close() throws IOException {
    out.close();
  }

  @Override public void write(Fact f) throws IOException {
    out.write(f.toTsvLine(writeDoubleValue));
  }

  public TsvWriter(File f) throws IOException {
    this(f, false);
  }

  public TsvWriter(File f, String header) throws IOException {
    this(f, false, header);
  }

  public TsvWriter(File f, boolean writeDoubleValue) throws IOException {
    this(f, writeDoubleValue, null);
  }

  public TsvWriter(File f, boolean writeDoubleValue, String header) throws IOException {
    super(f);
    this.writeDoubleValue = writeDoubleValue;
    out = FileUtils.getBufferedUTF8Writer(f);
    if (header != null) {
      header = header.replaceAll("\\s+", " ");
      Fact comment = new Fact(FactComponent.forYagoEntity("yagoTheme_" + FileSet.newExtension(f.getName(), null)), YAGO.hasGloss,
          FactComponent.forString(header));
      write(comment);
    }
  }

  /** Test*/
  public static void main(String[] args) throws Exception {
    try (FactWriter w = new TsvWriter(new File("c:/fabian/temp/t.tsv"), "Blah blah \n   \t blub \t blah")) {
      w.write(new Fact("<Elvis>", "rdf:type", "<livingPerson>"));
    }

  }
}
