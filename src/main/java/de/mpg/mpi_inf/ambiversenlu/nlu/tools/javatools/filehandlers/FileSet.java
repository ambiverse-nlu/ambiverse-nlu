package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative.D;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
 It is licensed under the Creative Commons Attribution License
 (see http://creativecommons.org/licenses/by/3.0) by
 the YAGO-NAGA team (see http://mpii.de/yago-naga).





 The class represents a set of files as given by a wildcard string.
 It does not include folders and is not case-sensitive.<BR>
 Example:
 <PRE>
 for(File f : new FileSet("c:\\myfiles\\*.jAvA"))
 System.out.println(f);
 -->
 c:\myfiles\FileSet.java
 c:\myfiles\HTMLReader.java
 ...
 </PRE>
 */
public class FileSet extends ArrayList<File> {

  private static final long serialVersionUID = 1L;

  /** Constructs a file from a folder, subfolder names and a filename */
  public static File file(File f, String... s) {
    for (String n : s) {
      f = new File(f, n);
    }
    return (f);
  }

  /** Constructs a FileSet from a wildcard string (including path) */
  public FileSet(String folderPlusWildcard) {
    this(new File(folderPlusWildcard));
  }

  /** Constructs a FileSet from a wildcard string (including path) */
  public FileSet(File folderPlusWildcard) {
    this(folderPlusWildcard.getParentFile() == null ? new File(".") : folderPlusWildcard.getParentFile(), folderPlusWildcard.getName());
  }

  /** Constructs a FileSet from a wildcard string */
  public FileSet(File path, String fname) {
    String regex = "";
    for (int i = 0; i < fname.length(); i++) {
      switch (fname.charAt(i)) {
        case '.':
          regex += "\\.";
          break;
        case '?':
          regex += '.';
          break;
        case '*':
          regex += ".*";
          break;
        default:
          regex += "" + '[' + Character.toLowerCase(fname.charAt(i)) + Character.toUpperCase(fname.charAt(i)) + ']';
      }
    }
    final Pattern wildcard = Pattern.compile(regex);
    File[] files = path.listFiles(new FileFilter() {

      public boolean accept(File pathname) {
        return (wildcard.matcher(pathname.getName()).matches());
      }
    });
    if (files == null) throw new RuntimeException("Can't find files in " + path);
    // Stupid, but the internal array is inaccessible
    ensureCapacity(files.length);
    for (File f1 : files) add(f1);
  }

  /** Returns the extension of a filename */
  public static String extension(File f) {
    return (extension(f.getName()));
  }

  /** Returns the extension of a filename with dot*/
  public static String extension(String f) {
    int dot = f.lastIndexOf('.');
    if (dot == -1) return ("");
    return (f.substring(dot));
  }

  /** Exchanges the extension of a filename. This is different from newExtension(String) because the "last dot" may be in the folder name, if the file itself has no extension  */
  public static File newExtension(File f, String newex) {
    if (newex == null) newex = "";
    // Extension may be given with preceding dot or without
    if (newex.startsWith(".")) newex = newex.substring(1);
    int i = f.getName().lastIndexOf('.');
    // If the task is to delete the extension...
    if (newex.length() == 0) {
      if (i == -1) return (f);
      return (new File(f.getParentFile(), f.getName().substring(0, i)));
    }
    // Else add or replace the extension
    if (i == -1) return (new File(f.getParentFile(), f.getName() + '.' + newex));
    return (new File(f.getParentFile(), f.getName().substring(0, i) + '.' + newex));
  }

  /** Exchanges the extension of a filename */
  public static String newExtension(String f, String newex) {
    if (newex == null) newex = "";
    // Extension may be given with preceding dot or without
    if (newex.startsWith(".")) newex = newex.substring(1);
    int i = f.lastIndexOf('.');
    // If the task is to delete the extension...
    if (newex.length() == 0) {
      if (i == -1) return (f);
      return (f.substring(0, i));
    }
    // Else add or replace the extension
    if (i == -1) return (f + '.' + newex);
    return (f.substring(0, i) + '.' + newex);
  }

  /** Deletes the file extension */
  public static String noExtension(String f) {
    int i = f.lastIndexOf('.');
    if (i == -1) return (f);
    return (f.substring(0, i));
  }

  /** Deletes the file extension */
  public static File noExtension(File f) {
    return (new File(noExtension(f.getPath())));
  }

  /** Test routine */
  public static void main(String argv[]) {
    D.p("Enter a filename with wildcards and hit ENTER. Press CTRL+C to abort");
    while (true) {
      for (File f : new FileSet(D.r())) D.p(f);
    }
  }
}
