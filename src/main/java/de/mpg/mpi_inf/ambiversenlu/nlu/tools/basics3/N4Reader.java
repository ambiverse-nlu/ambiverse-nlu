package de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative.Announce;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers.FileLines;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.Char17;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * N4Reader
 *
 * This code is part of the YAGO project at the Max Planck Institute for
 * Informatics and the Telecom ParisTech University. It is licensed under a
 * Creative Commons Attribution License by the YAGO team:
 * https://creativecommons.org/licenses/by/3.0/
 *
 * This class provides a reader for facts from an N4 document. This follows the
 * Turtle Specification
 * http://www.w3.org/TeamSubmission/turtle/#sec-grammar-grammar It (1)
 * understands a first (optional) component in the preceding comment (2) and it
 * does not support all Turtle features
 *
 * Passes all tests from
 * http://www.w3.org/TeamSubmission/turtle/#sec-conformance except 23
 * http://www.w3.org/2000/10/rdf-tests/rdfcore/ntriples/test.nt
 *
 * @author Fabian M. Suchanek
 *
 */
public class N4Reader implements Iterator<Fact>, Closeable {

  /** Reads the file */
  protected Reader reader;

  /** Maps custom prefixes */
  protected Map<String, String> prefixes = new TreeMap<String, String>();

  /** Custom base */
  protected String base = null;

  /** Creates a N4 reader */
  public N4Reader(Reader r) throws IOException {
    reader = r;
    next();
  }

  /** Creates a N4 reader */
  public N4Reader(File f) throws IOException {
    this(FileUtils.getBufferedUTF8Reader(f));
  }

  /** Counter for blank nodes */
  protected int blankCounter = 0;

  /** Value for "Ignore this character, read new one */
  public static final int READNEW = -2;

  /** Current character */
  protected int c = READNEW;

  /** returns the next item */
  protected String nextItem() throws IOException {
    if (c == READNEW) c = FileLines.firstCharAfterSpace(reader);
    switch (c) {
      case '@':
        c = READNEW;
        return ('@' + FileLines.readToSpace(reader).toString());
      case '#':
        c = reader.read();
        // Special YAGO fact identifier
        if (c == '@') {
          c = READNEW;
          return ('&' + nextItem());
        } else {
          // Normal comment
          c = READNEW;
          FileLines.scrollTo(reader, '\n', '\r');
          return (nextItem());
        }
      case -1:
        return ("EOF");
      case '<':
        c = READNEW;
        String uri = FileLines.readTo(reader, '>').toString();
        if (base != null && !uri.startsWith("http://")) uri = base + uri;
        return (FactComponent.forUri(uri));
      case '"':
        String language = null;
        String datatype = null;
        String string = FileLines.readTo(reader, '"').toString();
        while (string.endsWith("\\") && !string.endsWith("\\\\")) string += '"' + FileLines.readTo(reader, '"').toString();
        c = reader.read();
        switch (c) {
          case '@':
            language = "";
            while (Character.isLetter(c = reader.read()) || c == '-') language += (char) c;
            c = READNEW;
            break;
          case '^':
            reader.read();
            c = READNEW;
            datatype = nextItem();
            break;
          case '"':
            string = FileLines.readTo(reader, "\"\"\"").toString();
            if (string.length() > 2) string = string.substring(0, string.length() - 3);
            c = READNEW;
            break;
        }
        if (Character.isWhitespace(c)) c = READNEW;
        if (language == null) return (FactComponent.forStringWithDatatype(Char17.decodeBackslash(string), datatype));
        else return (FactComponent.forStringWithLanguage(Char17.decodeBackslash(string), language));
      case '[':
        String blank = FileLines.readTo(reader, ']').toString().trim();
        if (blank.length() != 0) {
          Announce.warning("Properties of blank node ignored", blank);
        }
        c = READNEW;
        return (FactComponent.forYagoEntity("blank" + (blankCounter++)));
      case '(':
        c = READNEW;
        String list = FileLines.readTo(reader, ')').toString().trim();
        Announce.warning("Cannot handle list", list);
        return (FactComponent.forQname("rdf:", "nil"));
      case '.':
        c = READNEW;
        return (".");
      case ',':
        c = READNEW;
        Announce.warning("Commas are not supported");
        FileLines.scrollTo(reader, '.');
        return (".");
      case ';':
        c = READNEW;
        // Announce.warning("Semicolons are not supported");
        // FileLines.scrollTo(reader, '.');
        return (";");
      case '+':
      case '-':
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        String number = ((char) c) + FileLines.readToSpace(reader).toString();
        c = READNEW;
        return (FactComponent.forNumber(number));
      default:
        String name = ((char) c) + FileLines.readToSpace(reader).toString();

        // Save some stuff that follows...
        if (".,<".indexOf(Char17.last(name)) != -1 || name.endsWith(";")) {
          c = Char17.last(name);
          name = Char17.cutLast(name);
        } else {
          c = READNEW;
        }
        // Predefined Turtle entities
        if (name.equals("a")) return (FactComponent.forQname("rdf:", "type"));
        if (name.equals("true")) return (FactComponent.forStringWithDatatype("true", FactComponent.forQname("xsd:", "boolean")));
        if (name.equals("false")) return (FactComponent.forStringWithDatatype("false", FactComponent.forQname("xsd:", "boolean")));
        // Prefixes
        int colon = name.indexOf(':');
        if (colon == -1) {
          Announce.warning("Invalid entity", Char17.encodeHex(name, Char17.alphaNumericAndSpace));
          FileLines.scrollTo(reader, '.');
          c = READNEW;
          return (".");
        }
        String prefix = name.substring(0, colon + 1);
        name = name.substring(colon + 1);
        if (prefixes.containsKey(prefix)) {
          return (FactComponent.forUri(prefixes.get(prefix) + name));
        }
        // Other
        return (FactComponent.forQname(prefix, name));
    }
  }

  /** caches the next fact, initially a dummy fact; null for EOF */
  protected Fact nextFact = new Fact("Elvis", "rdf:type", "theBest");

  @Override public Fact next() {
    Fact toReturn = nextFact;
    if (toReturn != null) {
      try {
        nextFact = internalNext();
      } catch (Exception e) {
        e.printStackTrace();
        nextFact = null;
      }
      if (nextFact == null) close();
    }

    return (toReturn);
  }

  @Override public boolean hasNext() {
    return nextFact != null;
  }

  /** Holds the previous subject (for ;-lists) */
  protected String prevSubj;

  /** returns the next fact */
  protected Fact internalNext() throws Exception {
    while (true) {
      String item = nextItem();
      if (item.equals("EOF")) return (null);
      // Prefix
      if (item.equalsIgnoreCase("@PREFIX")) {
        String prefix = FileLines.readTo(reader, ':').toString().trim() + ':';
        FileLines.scrollTo(reader, '<');
        String dest = FileLines.readTo(reader, '>').toString().trim();
        if (base != null && !dest.startsWith("http://")) dest = base + dest;
        FileLines.scrollTo(reader, '.');
        if (FactComponent.standardPrefixes.containsKey(prefix)) {
          if (dest.equals(FactComponent.standardPrefixes.get(prefix))) continue;
          else Announce.warning("Redefining standard prefix", prefix, "from", FactComponent.standardPrefixes.get(prefix), "to", dest);
        }
        prefixes.put(prefix, dest);
        continue;
      }

      // Base
      if (item.equalsIgnoreCase("@BASE")) {
        FileLines.scrollTo(reader, '<');
        String uri = FileLines.readTo(reader, '>').toString().trim();
        if (uri.startsWith("http://")) base = uri;
        else base = base + uri;
        FileLines.scrollTo(reader, '.');
        continue;
      }

      // Unknown
      if (item.startsWith("@")) {
        Announce.warning("Unknown directive:", item);
        FileLines.scrollTo(reader, '.');
        continue;
      }

      // Fact identifier
      String factId = null;
      // Subject Verb Object dot
      String subject = null;
      String predicate = null;
      String object = null;
      String dot = null;
      Fact f = null;

      // subject
      if (item.startsWith("&")) {
        factId = item.substring(1);
        subject = nextItem();
      } else {
        subject = item;
      }

      if (subject.equals(".")) {
        // Announce.warning("Dot on empty line");
        continue;
      }
      // predicate
      predicate = nextItem();
      if (predicate.equals(".")) {
        Announce.warning("Only one item on line", subject);
        continue;
      }

      if (predicate != null && predicate.endsWith(";")) {
        f = new Fact(factId, prevSubj, subject, predicate.replaceAll(";", ""));
        dot = ";";
      } else {
        object = nextItem();
        if (object.equals(";") || object.equals(".")) {
          f = new Fact(factId, prevSubj, subject, predicate);
          dot = object;

        } else if (object != null && !(object.startsWith("\"") && object.endsWith("\"")) && object.contains(";")) {
          f = new Fact(factId, subject, predicate, object.replaceAll(";", ""));
          dot = ";";
          prevSubj = subject;
        } else {
          dot = nextItem();
          f = new Fact(factId, subject, predicate, object);
          prevSubj = subject;

        }
      }
      if (!(dot.equals(".") || dot.equals(";"))) {
        // Line too long
        Announce.warning("More than three items on line", factId, subject, predicate, object, dot);
        FileLines.scrollTo(reader, '.');
        continue;
      }
      return (f);
    }
  }

  @Override public void close() {
    try {
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override public void remove() {
    throw new UnsupportedOperationException("remove() on N4Reader");
  }
}
