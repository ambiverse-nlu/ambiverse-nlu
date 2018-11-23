package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.textmanipulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Whitespace {

  private static Pattern whiteSpacePattern = Pattern.compile("^[\\p{Zl}\\p{Zs}\\p{Zp}\\n]+$");

  public static boolean isWhiteSpace(String text) {
    return whiteSpacePattern.matcher(text).find();
  }

  public static void main(String[] args) throws FileNotFoundException {
    String text = " This is a test ";
    System.out.println(isWhiteSpace(text));
    Scanner in = new Scanner(new File("/Users/corrogg/Downloads/keyphrases.txt"));
    String line = null;
    while (in.hasNextLine()) {
      line = in.nextLine();
    }
    in.close();
    System.out.println(isWhiteSpace(line + line));
    System.out.println(isWhiteSpace(line + "pppppp"));
  }

}
