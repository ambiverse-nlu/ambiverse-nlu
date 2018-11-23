package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.Char17;

import java.io.*;

/**
 This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
 It is licensed under the Creative Commons Attribution License
 (see http://creativecommons.org/licenses/by/3.0) by
 the YAGO-NAGA team (see http://mpii.de/yago-naga).





 This allows to write characters as UTF8 to a file<BR>
 Example:
 <PRE>
 Writer w=new UTF8Writer("c:\\blah.blb");
 w.write(Char.decodePercentage("Hall&ouml;chen"));
 w.close();
 </PRE>
 */
public class UTF8Writer extends Writer {

  /** The real writer */
  protected OutputStream out;

  /** Writes to a file*/
  public UTF8Writer(File f, boolean append) throws IOException {
    this(new FileOutputStream(f, append));
  }

  /** Writes to a file*/
  public UTF8Writer(File f) throws IOException {
    this(f, false);
  }

  /** Writes nowhere*/
  public UTF8Writer() {
  }

  /** Writes to a file*/
  public UTF8Writer(String f) throws IOException {
    this(new File(f));
  }

  /** Writes to a writer*/
  public UTF8Writer(OutputStream f) {
    out = f;
  }

  @Override public void close() throws IOException {
    synchronized (lock) {
      if (out != null) out.close();
    }
  }

  @Override public void flush() throws IOException {
    synchronized (lock) {
      if (out != null) out.flush();
    }
  }

  @Override public void write(char[] cbuf, int off, int len) throws IOException {
    synchronized (lock) {
      for (int i = off; i < off + len; i++)
        write(cbuf[i]);
    }
  }

  @Override public void write(int c) throws IOException {

    synchronized (lock) {
      if (out == null) return;
      String s = Char17.encodeUTF8((char) c);
      for (int i = 0; i < s.length(); i++)
        out.write((byte) s.charAt(i));
    }
  }

  /** Writes a line*/
  public void writeln(String s) throws IOException {
    write(s);
    write('\n');
  }

  /** Writes a string*/
  public void write(String s) throws IOException {
    for (int i = 0; i < s.length(); i++)
      write(s.charAt(i));
  }

}
