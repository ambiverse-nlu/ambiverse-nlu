package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative.Announce;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.PeekIterator;

import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * This class is part of the Java Tools (see
 * http://mpii.de/yago-naga/javatools). It is licensed under the Creative
 * Commons Attribution License (see http://creativecommons.org/licenses/by/3.0)
 * by the YAGO-NAGA team (see http://mpii.de/yago-naga).
 *
 * The class provides an iterator over the lines in a file<BR>
 * Example:
 *
 * <PRE>
 * for (String s : new FileLines(&quot;c:\\autoexec.bat&quot;)) {
 * 	System.out.println(s);
 * }
 * </PRE>
 *
 * If desired, the iterator can make a nice progress bar by calling
 * Announce.progressStart/At/Done automatically in the right order. If there are
 * no more lines, the file is closed. If you do not use all lines of the
 * iterator, close the iterator manually.
 */
public class FileLines extends PeekIterator<String> implements Iterable<String>, Iterator<String>, Closeable {

  /** number of chars for announce (or -1) */
  protected long announceChars = -1;

  /** Containes the Reader */
  protected BufferedReader br;

  /** Constructs FileLines from a filename */
  public FileLines(String f) throws IOException {
    this(f, null);
  }

  /** Constructs FileLines from a file */
  public FileLines(File f) throws IOException {
    this(f, null);
  }

  /** Constructs FileLines from a filename, shows progress bar */
  public FileLines(String f, String announceMsg) throws IOException {
    this(new File(f), announceMsg);
  }

  /**
   * Constructs FileLines from a filename with a given encoding, shows
   * progress bar
   */
  public FileLines(String f, String encoding, String announceMsg) throws IOException {
    this(new File(f), encoding, announceMsg);
  }

  /**
   * Constructs FileLines from a file with an encoding, shows progress bar
   * (main constructor 1)
   */
  public FileLines(File f, String encoding, String announceMsg) throws IOException {
    if (announceMsg != null) {
      Announce.progressStart(announceMsg, f.length());
      announceChars = 0;
    }
    br = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));
  }

  /**
   * Constructs FileLines from a file, shows progress bar (main constructor 2)
   */
  public FileLines(File f, String announceMsg) throws IOException {
    if (announceMsg != null) {
      Announce.progressStart(announceMsg, f.length());
      announceChars = 0;
    }
    br = new BufferedReader(new FileReader(f));
  }

  /** Constructs FileLines from a Reader */
  public FileLines(Reader f) {
    this(new BufferedReader(f));
  }

  /** Constructs FileLines from a BufferedReader (main constructor 3) */
  public FileLines(BufferedReader r) {
    br = r;
  }

  /** For subclasses */
  protected FileLines() {

  }

  /** Unsupported, throws an UnsupportedOperationException */
  public void remove() throws UnsupportedOperationException {
    throw new UnsupportedOperationException("FileLines does not support \"remove\"");
  }

  /**
   * Returns next line. In case of an IOException, the exception is wrapped in
   * a RuntimeException
   */
  public String internalNext() {
    String next;
    try {
      next = br.readLine();
      if (announceChars != -1 && next != null) Announce.progressAt(announceChars += next.length());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return (next);
  }

  /** Returns a simple identifier */
  public String toString() {
    return ("FileLines of " + br);
  }

  /** Returns this */
  public Iterator<String> iterator() {
    return this;
  }

  /** Closes the reader */
  public void close() {
    try {
      br.close();
    } catch (IOException e) {
    }
    if (announceChars != -1) Announce.progressDone();
    announceChars = -1;
  }

  /** Closes the reader */
  public void finalize() {
    close();
  }

  /**
   * Reads until one of the strings is found, returns its index or -1.
   *
   * @throws IOException
   */
  public static int find(Reader in, String... findMe) throws IOException {
    int[] pos = new int[findMe.length];
    while (true) {
      int c = in.read();
      if (c == -1) return (-1);
      if (c == -2) continue; // Let's be compliant with HTMLReader: Skip tags
      for (int i = 0; i < findMe.length; i++) {
        if (c == findMe[i].charAt(pos[i])) pos[i]++;
        else pos[i] = 0;
        if (pos[i] == findMe[i].length()) return (i);
      }
    }
  }

  /**
   * Reads until one of the strings is found, returns its index or -1.
   *
   * @throws IOException
   */
  public static int find(Reader in, StringBuilder b, String... findMe) throws IOException {
    int[] pos = new int[findMe.length];
    while (true) {
      int c = in.read();
      b.append((char) c);
      if (c == -1) return (-1);
      if (c == -2) continue; // Let's be compliant with HTMLReader: Skip tags
      for (int i = 0; i < findMe.length; i++) {
        if (c == findMe[i].charAt(pos[i])) pos[i]++;
        else pos[i] = 0;
        if (pos[i] == findMe[i].length()) {
          b.setLength(b.length() - findMe[i].length());
          return (i);
        }
      }
    }
  }

  /**
   * Reads until one of the strings is found, returns its index or -1.
   *
   * @throws IOException
   */
  public static int findIgnoreCase(Reader in, String... findMe) throws IOException {
    int[] pos = new int[findMe.length];
    for (int i = 0; i < findMe.length; i++)
      findMe[i] = findMe[i].toUpperCase();
    while (true) {
      int c = in.read();
      if (c == -1) return (-1);
      if (c == -2) continue; // Let's be compliant with HTMLReader: Skip tags
      c = Character.toUpperCase(c);
      for (int i = 0; i < findMe.length; i++) {
        if (c == findMe[i].charAt(pos[i])) pos[i]++;
        else pos[i] = 0;
        if (pos[i] == findMe[i].length()) return (i);
      }
    }
  }

  /** Maximum chars to read by readTo (or -1) */
  public static long maxChars = -1;

  /**
   * Reads to a specific character, returns the text in between
   *
   * @throws IOException
   */
  public static CharSequence readTo(Reader in, char... limit) throws IOException {
    StringBuilder result = new StringBuilder();
    int c;
    outer:
    while ((c = in.read()) != -1) {
      for (int i = 0; i < limit.length; i++)
        if (c == limit[i]) break outer;
      result.append((char) c);
      if (maxChars != -1 && result.length() > maxChars) break;
    }
    return (result);
  }

  /**
   * Reads to a whitespace
   *
   * @throws IOException
   */
  public static CharSequence readToSpace(Reader in) throws IOException {
    StringBuilder result = new StringBuilder();
    int c;
    outer:
    while ((c = in.read()) != -1) {
      if (Character.isWhitespace(c)) break outer;
      result.append((char) c);
      if (maxChars != -1 && result.length() > maxChars) break;
    }
    return (result);
  }

  /**
   * Skips space
   *
   * @throws IOException
   */
  public static int firstCharAfterSpace(Reader in) throws IOException {
    int c;
    while ((c = in.read()) != -1 && Character.isWhitespace(c)) {
    }
    return (c);
  }

  /**
   * Reads to a specific character, returns the text in between
   *
   * @throws IOException
   */
  public static CharSequence readTo(Reader in, char limit) throws IOException {
    StringBuilder result = new StringBuilder();
    int c;
    outer:
    while ((c = in.read()) != -1) {
      if (c == limit) break outer;
      result.append((char) c);
      if (maxChars != -1 && result.length() > maxChars) break;
    }
    return (result);
  }

  /**
   * Reads to a specific String, returns the text up to there, including the
   * limit
   *
   * @throws IOException
   */
  public static CharSequence readTo(Reader in, String limit) throws IOException {
    StringBuilder result = new StringBuilder(2000);
    int c;
    int position = 0;
    while ((c = in.read()) != -1) {
      result.append((char) c);

      if (c == limit.charAt(position)) {
        if (++position == limit.length()) break;
      } else {
        position = 0;
      }
      if (maxChars != -1 && result.length() > maxChars) break;
    }
    return (result);
  }

  /** Reads to a specific string, returns text up to there, without boundary or returns NULL*/
  public static String readToBoundary(Reader in, String limit) throws IOException {
    StringBuilder result = new StringBuilder(2000);
    int c;
    int position = 0;
    while ((c = in.read()) != -1) {
      result.append((char) c);

      if (c == limit.charAt(position)) {
        if (++position == limit.length()) return (result.toString().substring(0, result.length() - limit.length()));
      } else {
        position = 0;
      }
      if (maxChars != -1 && result.length() > maxChars) return (null);
    }
    return (null);
  }

  /**
   * Scrolls to one of the characters
   *
   * @throws IOException
   */
  public static boolean scrollTo(Reader in, char... delimiters) throws IOException {
    int c;
    while ((c = in.read()) != -1) {
      for (int i = 0; i < delimiters.length; i++)
        if (c == delimiters[i]) return (true);
    }
    return (false);
  }

  /**
   * Reads to a specific character
   *
   * @throws IOException
   */
  public static boolean scrollTo(Reader in, char delimiter) throws IOException {
    int c;
    while ((c = in.read()) != -1) {
      if (c == delimiter) return (true);
    }
    return (false);
  }

  /**
   * Scrolls to a specific limit and beyond
   *
   * @throws IOException
   */
  public static boolean scrollTo(Reader in, String limit) throws IOException {
    int c;
    int position = 0;
    while ((c = in.read()) != -1) {
      if (c == limit.charAt(position)) {
        if (++position == limit.length()) return (true);
      } else {
        position = 0;
      }
    }
    return (false);
  }

  /**
   * Reads the string between the delimiters
   *
   * @throws IOException
   */
  public static String readBetween(Reader in, String start, String end) throws IOException {
    if (!scrollTo(in, start)) return (null);
    CharSequence r = readTo(in, end);
    if (r == null) return (null);
    String s = r.toString();
    if (!s.endsWith(end)) return (null);
    return (r.toString().substring(0, r.length() - end.length()));
  }

  /**
   * Reads the string between the delimiters
   *
   * @throws IOException
   */
  public static String readBetween(String in, String start, String end) {
    try {
      return (readBetween(new StringReader(in), start, end));
    } catch (Exception e) {
      return (null);
    }
  }

  /**
   * Reads to a specific String, returns the text up to there, including the
   * limit
   *
   * @throws IOException
   */
  public static CharSequence readTo(Reader in, String limit, List<Integer> results) throws IOException {
    StringBuilder result = new StringBuilder();
    int c;
    int position = 0;
    while ((c = in.read()) != -1) {
      result.append((char) c);
      results.add(c);
      if (c == limit.charAt(position)) {
        if (++position == limit.length()) break;
      } else {
        position = 0;
      }
      if (maxChars != -1 && result.length() > maxChars) break;
    }
    return (result);
  }

  /** returns number of lines in file */
  public static int numFileLines(File f) throws IOException {
    int counter = 0;
    for (@SuppressWarnings("unused") String l : new FileLines(f, "Counting lines in" + f))
      counter++;
    return (counter);
  }

  /** returns number of lines in file */
  public static int numAllFileLines(File f, String ext) throws IOException {
    if (f.isFile()) return (f.getName().endsWith(ext) ? numFileLines(f) : 0);
    int counter = 0;
    Announce.doing("Counting in", f);
    for (File f2 : f.listFiles()) {
      counter += numAllFileLines(f2, ext);
    }
    Announce.done();
    return (counter);
  }

}
