package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative.Announce;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.Char17;

import java.io.*;
import java.net.URL;

/**
 This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
 It is licensed under the Creative Commons Attribution License 
 (see http://creativecommons.org/licenses/by/3.0) by 
 the YAGO-NAGA team (see http://mpii.de/yago-naga).





 This class can read characters from a file that is UTF8 encoded.<BR>
 Example:
 <PRE>
 Reader f=new UTF8Reader(new File("blah.blb"));
 int c;
 while((c=f.read())!=-1) System.out.print(Char.normalize(c));
 f.close();
 </PRE>
 */
public class UTF8Reader extends Reader {

  /** Holds the input Stream */
  protected InputStream in;

  /** number of chars for announce */
  protected long numBytesRead = 0;

  /** tells whether we want a progress bar*/
  protected boolean progressBar = false;

  /** Constructs a UTF8Reader from a Reader */
  public UTF8Reader(InputStream s) {
    in = s;
  }

  /** Constructs a UTF8Reader for an URL
   * @throws IOException */
  public UTF8Reader(URL url) throws IOException {
    this(url.openStream());
  }

  /** Constructs a UTF8Reader from a File */
  public UTF8Reader(File f) throws FileNotFoundException {
    this(new FileInputStream(f));
  }

  /** Constructs a UTF8Reader from a File, makes a nice progress bar */
  public UTF8Reader(File f, String message) throws FileNotFoundException {
    this(new FileInputStream(f));
    progressBar = true;
    Announce.progressStart(message, f.length());
  }

  /** Constructs a UTF8Reader from a File */
  public UTF8Reader(String f) throws FileNotFoundException {
    this(new File(f));
  }

  /** Constructs a UTF8Reader from a File, makes a nice progress bar */
  public UTF8Reader(String f, String message) throws FileNotFoundException {
    this(new File(f), message);
  }

  @Override public void close() throws IOException {
    if (in == null) return;
    in.close();
    in = null;
    if (progressBar) Announce.progressDone();
    progressBar = false;
  }

  @Override public int read(char[] cbuf, int off, int len) throws IOException {
    if (in == null) return (-1);
    int c;
    int numRead = 0;
    while (numRead < len) {
      c = read();
      if (c == -1) {
        close();
        if (numRead > 0) return (numRead);
        else return (-1);
      }
      cbuf[off++] = (char) c;
      numRead++;
    }
    return numRead;
  }

  @Override public int read() throws IOException {
    if (in == null) return (-1);
    int c = in.read();
    if (c == -1) {
      close();
      return (-1);
    }
    int len = Char17.Utf8Length((char) c);
    numBytesRead += len;
    if (progressBar) Announce.progressAt(numBytesRead);
    if (len == 1) return (c);
    String seq = "" + c;
    while (--len > 0) seq += (char) in.read();
    return (Char17.decodeUtf8(seq).charAt(0));
  }

  /** Returns the number of bytes read from the underlying stream*/
  public long numBytesRead() {
    return (numBytesRead);
  }

  /** Reads a line */
  public String readLine() throws IOException {
    if (in == null) return (null);
    StringBuilder result = new StringBuilder();
    loop:
    while (true) {
      int c = read();
      switch (c) {
        case -1:
          close();
          break loop;
        case 13:
          break;
        case 0x85:
        case 0x0C:
        case 0x2028:
        case 0x2029:
        case 10:
          break loop;
        default:
          result.append((char) c);
      }
    }
    return (result.toString());
  }

  /** Test method
   * @throws IOException   */
  public static void main(String[] args) throws IOException {
    Reader f = new UTF8Reader(new File("blah.blb"));
    int c;
    while ((c = f.read()) != -1) System.out.print(Char17.normalize(c));
    f.close();
  }

}
