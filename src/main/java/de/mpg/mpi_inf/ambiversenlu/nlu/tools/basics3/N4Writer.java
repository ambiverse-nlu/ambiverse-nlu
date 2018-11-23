package de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers.FileSet;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Class N4Writer
 *
 * This code is part of the YAGO project at the Max Planck Institute for
 * Informatics and the Telecom ParisTech University. It is licensed under a
 * Creative Commons Attribution License by the YAGO team:
 * https://creativecommons.org/licenses/by/3.0/
 *
 * This class writes YAGO facts into a Turtle-compatible output format
 *
 * Output validates with http://www.rdfabout.com/demo/validator
 *
 * @author Fabian Suchanek
 *
 */
public class N4Writer extends FactWriter {

  /** Writes the file */
  protected Writer writer;

  /** Starts a writer to this file */
  public N4Writer(File f, String header, String base, Map<String, String> prefixes) throws Exception {
    super(f);
    writer = FileUtils.getBufferedUTF8Writer(f);
    for (String line : header.split("\n")) {
      writer.write("# " + line + "\n");
    }
    writer.write("\n@base <" + base + "> .\n");
    for (String prefix : FactComponent.standardPrefixes.keySet()) {
      writer.write("@prefix " + prefix + " <" + FactComponent.standardPrefixes.get(prefix) + "> .\n");
    }
    if (prefixes != null) for (Map.Entry<String, String> prefix : prefixes.entrySet()) {
      writer.write("@prefix " + prefix.getKey() + " <" + FactComponent.standardPrefixes.get(prefix.getValue()) + "> .\n");
    }
    writer.write("\n");
    if (header != null) {
      header = header.replaceAll("\\s+", " ");
      Fact comment = new Fact(FactComponent.forYagoEntity("yagoTheme_" + FileSet.newExtension(f.getName(), null)), YAGO.hasGloss,
          FactComponent.forString(header));
      write(comment);
    }
  }

  /** Starts a writer to this file */
  public N4Writer(File f, String header) throws Exception {
    this(f, header, "http://yago-knowledge.org/resource/", new HashMap<String, String>());
  }

  /** Writes a fact */
  @Override public synchronized void write(Fact f) throws IOException {
    if (f.getId() != null) writer.write("#@ " + f.getId() + "\n");
    writer.write(f.subject);
    writer.write("\t");
    writer.write(f.relation);
    writer.write("\t");
    writer.write(f.object);
    writer.write(" .\n");
  }

  @Override public void close() throws IOException {
    writer.close();
  }

  /** Test*/
  public static void main(String[] args) throws Exception {
    try (FactWriter w = new N4Writer(new File("c:/fabian/temp/t.ttl"), "Blah blah \n   \t blub \t blah")) {
      w.write(new Fact("<Elvis>", "rdf:type", "<livingPerson>"));
    }

  }

}
