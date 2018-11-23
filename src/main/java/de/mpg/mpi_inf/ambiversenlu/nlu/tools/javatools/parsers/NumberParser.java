package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative.D;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.FinalMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is part of the Java Tools (see
 * http://mpii.de/yago-naga/javatools). It is licensed under the Creative
 * Commons Attribution License (see http://creativecommons.org/licenses/by/3.0)
 * by the YAGO-NAGA team (see http://mpii.de/yago-naga).
 *
 *
 *
 *
 *
 * The NumberParser normalizes number expressions in English natural language
 * text. It can work with expressions like
 *
 * <PRE>
 * 10 million meters
 *          7 inches
 *          2.3 sq ft
 *          12:30 pm
 *          100 km
 *          12 ml
 *          10 Mb
 * </PRE>
 *
 * Example:
 *
 * <PRE>
 * System.out.println(NumberParser.normalize("It was 1.2 inches long"));
 *          --> "It was 0.030479999999999997#meter long"
 *
 *          System.out.println(toLong("more than ten kB"));
 *          --> 10000.0
 * </PRE>
 */

public class NumberParser {

  /** Creates a normalized number from a number and a type */
  public static final String newNumber(String n, String type) {
    return (n + '#' + type);
  }

  /**
   * Creates a normalized number without a type.
   *
   * @see newNumber(String n, String type)
   */
  public static final String newNumber(String n) {
    return (n);
  }

  /**
   * Creates a normalized number from a double and a type.
   *
   * @see newNumber(String n, String type)
   */
  public static final String newNumber(double d, String type) {
    return (newNumber(d + "", type));
  }

  /** Maps decimal prefixes (like "giga") to their double */
  public static final Map<String, Double> prefixes = new FinalMap<String, Double>("tera", 1000000000000.0, "T", 1000000000000.0, "giga", 1000000000.0,
      "G", 1000000000.0, "M", 1000000.0, "mega", 1000000.0, "kilo", 1000.0, "k", 1000.0, "deci", 0.1, "", 1.0, "d", 0.1, "centi", 0.01, "c", 0.01,
      "milli", 0.001, "m", 0.001, "micro", 0.000001, "mu", 0.000001, "nano", 0.000000001, "n", 0.000000001);

  /** A dot or comma as a RegEx */
  private static final String DC = "[\\.,]";

  /** A dot as a RegEx */
  private static final String DOT = "\\.";

  /** A comma as a RegEx */
  // private static final String C=",";

  /** A digit as a capturing RegEx */
  private static final String DIG = "(\\d)";

  /** A blank as a RegEx */
  private static final String B = "[\\s_]*+";

  /** A word boundary as a capturing RegEx */
  private static final String WB = "\\b";

  /** A hyphen as a RegEx with blanks */
  // private static final String H=B+"(?:-+|to|until)"+B;

  /** ##th as a capturing RegEx with blank */
  private static final String NTH = "(\\d+)(?:th|rd|nd|st)" + WB;

  /** a/one/<> with a blank as a RegEx */
  private static final String A = "(?:a|one|A|One)" + B;

  /** Prefixes as a capturing RegEx */
  private static final String P = "(tera|T|giga|G|mega|M|kilo|k|deci|d|centi|c|milli|m|micro|mu|nano|n|)";

  /** A number as a capturing RegEx */
  public static final String FLOAT = "([\\-\\+]?\\d++(?:\\.[0-9]++)?(?:[Ee]\\-?[0-9]++)?)";

  /** An integer as a capturing regex */
  public static final String POSINT = "(\\+?[0-9]++)";

  /** An integer as a capturing regex */
  public static final String INT = "([\\-\\+]?[0-9]++)";

  /** A short integer as a capturing regex */
  private static final String SINT = "([0-9]{1,3})";

  /** A unit as a captuing regex */
  private static final String UNIT = "([/a-zA-Z\\%]++(?:\\^\\d)?)";

  /** The number pattern */
  public static final Pattern NUMBERPATTERN = Pattern.compile(newNumber(FLOAT + "(?:", UNIT + ")?"));

  /** Tells whether this string is a normalized number */
  public static boolean isFloat(String s) {
    return (s.matches(FLOAT));
  }

  /** Tells whether this string is a normalized integer number */
  public static boolean isInt(String s) {
    return (s.matches(INT));
  }

  /** Tells whether this string is a normalized non-negative integer number */
  public static boolean isNonNegativeInt(String s) {
    return (s.matches(POSINT));
  }

  /** Tells whether this string is a normalized number with unit */
  public static boolean isNumberAndUnit(String s) {
    return (NUMBERPATTERN.matcher(s).matches());
  }

  /** Just a pair of a Pattern and a replacement string */
  private static class FindReplace {

    public Pattern pattern;

    public String replacement;

    public String toString() {
      return (pattern + "   -->   " + replacement);
    }

    public FindReplace(String f, String r) {
      pattern = Pattern.compile(f);
      replacement = r;
    }

    /* applies the pattern-replacement 
     *  Note: If you fix something in this version, please try to apply the same fix at the position change tracking function below */
    public void apply(StringBuilder s, StringBuilder result) {
      result.setLength(0);
      Matcher m = pattern.matcher(s);
      if (!m.find()) return;
      int pos = 0;
      do {
        for (int i = pos; i < m.start(); i++)
          result.append(s.charAt(i));
        pos = m.end();
        for (int i = 0; i < replacement.length(); i++) {
          if (replacement.charAt(i) == '$') {
            String rep = m.group(replacement.charAt(i + 1) - '0');
            if (rep != null) result.append(rep);
            i++;
          } else {
            result.append(replacement.charAt(i));
          }
        }
      } while (m.find());
      for (int i = pos; i < s.length(); i++)
        result.append(s.charAt(i));
    }

    /* applies the pattern-replacement 
     *  Note: If you fix something in this version, please try to apply the same fix at the non-tracking function */
    public void apply(StringBuilder s, StringBuilder result, PositionTracker posTracker) {
      result.setLength(0);
      Matcher m = pattern.matcher(s);
      if (!m.find()) return;
      int pos = 0;
      int adddiff = 0;
      int difference = 0;
      do {
        for (int i = pos; i < m.start(); i++)
          result.append(s.charAt(i));
        pos = m.end();
        adddiff = 0;
        for (int i = 0; i < replacement.length(); i++) {
          if (replacement.charAt(i) == '$') {
            String rep = m.group(replacement.charAt(i + 1) - '0');
            adddiff -= 2;
            if (rep != null) {
              adddiff += rep.length();
              result.append(rep);
            }
            i++;
          } else {
            result.append(replacement.charAt(i));
          }
        }
        difference = replacement.length() + adddiff - (m.end() - m.start());
        posTracker.addPositionChange(m.end(), difference);
      } while (m.find());
      for (int i = pos; i < s.length(); i++)
        result.append(s.charAt(i));
      posTracker.closeRun();
    }
  }

  /** A FindReplace that can do additional computations */
  private static class FindCompute extends FindReplace {

    public double factor;

    public double summand;

    public FindCompute(String f, String unit, double fac, double sum) {
      super(FLOAT + B + P + f + WB, unit);
      factor = fac;
      summand = sum;
    }

    /**
     * This constructor enforces the unit to follow the number without space
     */
    public FindCompute(String f, String unit, double fac, double sum, boolean dummy) {
      super(FLOAT + P + f + WB, unit);
      factor = fac;
      summand = sum;
    }

    public FindCompute(String f, String unit) {
      this(f, unit, 1, 0);
    }

    /* applies the pattern-replacement 
     *  Note: If you fix something in this version, please try to apply the same fix at the position change tracking function below */
    public void apply(StringBuilder s, StringBuilder result) {
      result.setLength(0);
      Matcher m = pattern.matcher(s);
      if (!m.find()) return;
      int pow = (replacement != null && Character.isDigit(replacement.charAt(replacement.length() - 1))) ?
          replacement.charAt(replacement.length() - 1) - '0' :
          1;
      int pos = 0;
      do {
        for (int i = pos; i < m.start(); i++)
          result.append(s.charAt(i));
        pos = m.end();
        double val = Double.parseDouble(m.group(1));
        if (replacement == null) result.append((val + summand) * factor * prefixes.get(m.group(2)) + "").append(' ');
        else result.append(newNumber((val + summand) * factor * Math.pow(prefixes.get(m.group(2)), pow), replacement)).append(' ');
      } while (m.find());
      for (int i = pos; i < s.length(); i++)
        result.append(s.charAt(i));
    }

    /* applies the pattern-replacement 
     *  Note: If you fix something in this version, please try to apply the same fix at the non-tracking function */
    public void apply(StringBuilder s, StringBuilder result, PositionTracker posTracker) {
      Integer difference = 0;
      result.setLength(0);
      Matcher m = pattern.matcher(s);
      if (!m.find()) return;
      int pow = (replacement != null && Character.isDigit(replacement.charAt(replacement.length() - 1))) ?
          replacement.charAt(replacement.length() - 1) - '0' :
          1;
      int pos = 0;
      do {
        for (int i = pos; i < m.start(); i++)
          result.append(s.charAt(i));
        pos = m.end();
        double val = Double.parseDouble(m.group(1));
        if (replacement == null) {
          String rep = (val + summand) * factor * prefixes.get(m.group(2)) + " ";
          //result.append((val+summand)*factor*prefixes.get(m.group(2))+"").append(' ');
          result.append(rep);
          difference = rep.length() - (m.end() - m.start());
          posTracker.addPositionChange(m.end(), difference);
        } else {
          String rep = newNumber((val + summand) * factor * Math.pow(prefixes.get(m.group(2)), pow), replacement) + ' ';
          //result.append(newNumber((val+summand)*factor*Math.pow(prefixes.get(m.group(2)),pow),replacement)).append(' ');
          result.append(rep);
          difference = rep.length() - (m.end() - m.start());
          posTracker.addPositionChange(m.end(), difference);
        }
      } while (m.find());
      for (int i = pos; i < s.length(); i++)
        result.append(s.charAt(i));
      posTracker.closeRun();
    }

  }

  /** A FindReplace that can add an amount */
  private static class FindAdd extends FindCompute {

    public FindAdd(String f, String unit, double sum) {
      super(f, unit, 1, sum);
    }
  }

  /** A FindReplace that can multiply by an amount */
  private static class FindMultiply extends FindCompute {

    public FindMultiply(String f, String unit, double fac) {
      super(f, unit, fac, 0);
    }
  }

  /** Holds the number patterns */
  private static final FindReplace[] patterns = new FindReplace[] {
      // --------- separators ------------
      // Blank
      new FindReplace("\u00A0", " "),
      // c.##
      new FindReplace(WB + "ca?\\.? ?(\\d)", "about $1"),
      // #-#
      new FindReplace("([^-\\d])(\\d+)-(\\d+)([^-\\d])", "$1$2 - $3$4"),
      // 1 000
      new FindReplace("(\\d{1,3}) (\\d{3}) ?(\\d{3})? ?(\\d{3})?", "$1$2$3$4"),
      // .09
      new FindReplace(" " + DC + POSINT, " 0.$1"),
      // 1,000
      new FindReplace("(\\d+),(\\d{3}),?(\\d{3})?,?(\\d{3})?,?(\\d{3})?", "$1$2$3$4$5"),
      // 1,00 -> 1.00
      new FindReplace("(\\d),(\\d)", "$1.$2"),

      // --------- 2-12 ------------
      new FindReplace(WB + "first" + WB, newNumber("1", "th")),
      new FindReplace(WB + "two" + WB, "2"),
      new FindReplace(WB + "second" + WB, newNumber("2", "th")),
      new FindReplace(WB + "three" + WB, "3"),
      new FindReplace(WB + "third" + WB, newNumber("3", "th")),
      new FindReplace(WB + "four" + WB, "4"),
      new FindReplace(WB + "fourth" + WB, newNumber("4", "th")),
      new FindReplace(WB + "five" + WB, "5"),
      new FindReplace(WB + "fiveth" + WB, newNumber("5", "th")),
      new FindReplace(WB + "six" + WB, "6"),
      new FindReplace(WB + "sixth" + WB, newNumber("6", "th")),
      new FindReplace(WB + "seven" + WB, "7"),
      new FindReplace(WB + "seventh" + WB, newNumber("7", "th")),
      new FindReplace(WB + "eight" + WB, "8"),
      new FindReplace(WB + "eighth" + WB, "" + newNumber("8", "th")),
      new FindReplace(WB + "nine" + WB, "9"),
      new FindReplace(WB + "nineth" + WB, newNumber("9", "th")),
      new FindReplace(WB + "ten" + WB, "10"),
      new FindReplace(WB + "tenth" + WB, newNumber("10", "th")),
      new FindReplace(WB + "eleven" + WB, "11"),
      new FindReplace(WB + "eleventh" + WB, newNumber("11", "th")),
      new FindReplace(WB + "twelve" + WB, "12"),
      new FindReplace(WB + "twelveth" + WB, newNumber("12", "th")),

      // --------- billions and millions ------------
      // Currencies often have just a 'm' to indicate million
      new FindReplace("(?i:US\\$|USD|\\$|\\$US)" + B + FLOAT + B + "[Mm]" + WB, "$1 million dollar"),
      new FindReplace("(?i:euro|eur|euros|\u20AC)" + B + FLOAT + B + "[Mm]" + WB, "$1 million euro"),
      new FindReplace("(?i:US\\$|USD|\\$|\\$US)" + B + FLOAT + B + "[bB]" + WB, "$1 billion dollar"),
      new FindReplace("(?i:euro|eur|euros|\u20AC)" + B + FLOAT + B + "[bB]" + WB, "$1 billion euro"),
      // # trillion
      new FindReplace(SINT + DOT + DIG + B + "[Tt]rillion", "$1$200000000000"),
      new FindReplace(SINT + DOT + DIG + DIG + B + "[Tt]rillion", "$1$2$30000000000"),
      new FindReplace(SINT + DOT + DIG + DIG + DIG + B + "[Tt]rillion", "$1$2$3$4000000000"),
      new FindReplace(SINT + B + "[Tt]rillion", "$1000000000000"),
      // # billion
      new FindReplace(SINT + DOT + DIG + B + "[Bb]illion", "$1$200000000"),
      new FindReplace(SINT + DOT + DIG + DIG + B + "[Bb]illion", "$1$2$30000000"),
      new FindReplace(SINT + DOT + DIG + DIG + DIG + B + "[Bb]illion", "$1$2$3$4000000"),
      new FindReplace(SINT + B + "[Bb]illion", "$1000000000"),
      new FindReplace(SINT + DOT + DIG + B + "[Bb]n?", "$1$200000000"),
      new FindReplace(SINT + DOT + DIG + DIG + B + "[Bb]n?", "$1$2$30000000"),
      new FindReplace(SINT + DOT + DIG + DIG + DIG + B + "[Bb]n?", "$1$2$3$4000000"),
      new FindReplace(SINT + B + "[Bb]n", "$1000000000"),
      // # million
      new FindReplace(SINT + DOT + DIG + B + "[Mm]illion", "$1$200000"),
      new FindReplace(SINT + DOT + DIG + DIG + B + "[Mm]illion", "$1$2$30000"),
      new FindReplace(SINT + DOT + DIG + DIG + DIG + B + "[Mm]illion", "$1$2$3$4000"),
      new FindReplace(SINT + B + "[Mm]illion", "$1000000"),
      // # thousand
      new FindReplace(SINT + DOT + DIG + B + "[Tt]housand", "$1$200"),
      new FindReplace(SINT + B + "[Tt]housand", "$1000"),
      // # hundred
      new FindReplace(SINT + B + "[Hh]undred", "$100"),
      // # dozen
      new FindMultiply("[Dd]ozens?", null, 12),
      // billions
      // new FindReplace("billions"+OF,"3000000000"),
      // millions
      // new FindReplace("millions"+OF,"3000000"),
      // thousands
      // new FindReplace("thousands"+OF,newNumber("3000",CARDINAL)),
      // hundreds
      // new FindReplace("hundreds"+OF,newNumber("300",CARDINAL)),
      // dozens
      // new FindReplace("dozens"+OF,newNumber("40",CARDINAL)),
      // a billion
      new FindReplace(A + "[Bb]illion", "1000000000"),
      // a million
      new FindReplace(A + "[Mm]illion", "1000000"),
      // a thousand
      new FindReplace(A + "[Tt]housand", "1000"),
      // a hundred
      new FindReplace(A + "[Hh]undred", "100"),
      // a dozen
      new FindReplace(A + "[Dd]ozen", "12"),

      // --------- Ordinal numbers and super scripts -----------
      // 1st
      new FindReplace(NTH, newNumber("$1", "th")),
      // ^1
      new FindReplace("¹", "^1"),
      // ^2
      new FindReplace("²", "^2"),
      // ^3
      new FindReplace("³", "^3"),

      // --------- Location coordinates, times and inches ----------------
      new FindReplace("-?" + B + "([\\d\\.]+)" + B + "(°|degrees?)" + B + "([\\d\\.]+)?" + B + "('|minutes|min|mn|m)" + B + "([\\d\\.]+)?" + B
          + "(''|seconds|sec|s|\")" + B + "(N|E|W|S)", null) {

        /* applies the pattern-replacement 
         *  Note: If you fix something in this version, please try to apply the same fix at the position change tracking function below */
        public void apply(StringBuilder s, StringBuilder result) {
          result.setLength(0);
          Matcher m = pattern.matcher(s);
          if (!m.find()) return;
          int pos = 0;
          do {
            for (int i = pos; i < m.start(); i++)
              result.append(s.charAt(i));
            pos = m.end();
            try {
              double num = Double.parseDouble(m.group(1));
              if (m.group(3) != null) num += Double.parseDouble(m.group(3)) / 60.0;
              if (m.group(5) != null) num += Double.parseDouble(m.group(5)) / 60.0 / 60.0;
              char loc = m.group(7).charAt(0);
              if (loc == 'W') {
                num = -num;
              }
              if (loc == 'S') {
                num = -num;
              }
              result.append(newNumber(Double.toString(num), "degrees"));
            } catch (Exception e) {
              // Some number format exception
              result.append(m.group());
            }
          } while (m.find());
          for (int i = pos; i < s.length(); i++)
            result.append(s.charAt(i));
        }

        /* applies the pattern-replacement 
         *  Note: If you fix something in this version, please try to apply the same fix at the non-tracking function */
        public void apply(StringBuilder s, StringBuilder result, PositionTracker posTracker) {
          Integer difference = 0;
          result.setLength(0);
          Matcher m = pattern.matcher(s);
          if (!m.find()) return;
          int pos = 0;
          do {
            for (int i = pos; i < m.start(); i++)
              result.append(s.charAt(i));
            pos = m.end();
            try {
              double num = Double.parseDouble(m.group(1));
              if (m.group(3) != null) num += Double.parseDouble(m.group(3)) / 60.0;
              if (m.group(5) != null) num += Double.parseDouble(m.group(5)) / 60.0 / 60.0;
              char loc = m.group(7).charAt(0);
              if (loc == 'W') {
                num = -num;
              }
              if (loc == 'S') {
                num = -num;
              }
              String add = newNumber(Double.toString(num), "degrees");
              result.append(add);
              difference = add.length() - (m.end() - m.start());
              posTracker.addPositionChange(m.end(), difference);
            } catch (Exception e) {
              // Some number format exception
              result.append(m.group());
            }
          } while (m.find());
          for (int i = pos; i < s.length(); i++)
            result.append(s.charAt(i));
          posTracker.closeRun();
        }

      },
      new FindReplace(POSINT + ':' + POSINT + B + "pm" + WB, null) {

        /* applies the pattern-replacement 
        *  Note: If you fix something in this version, please try to apply the same fix at the non-tracking function */
        public void apply(StringBuilder s, StringBuilder result, PositionTracker posTracker) {
          result.setLength(0);
          Integer difference = 0;
          Matcher m = pattern.matcher(s);
          if (!m.find()) return;
          int pos = 0;
          do {
            for (int i = pos; i < m.start(); i++)
              result.append(s.charAt(i));
            pos = m.end();
            String rep = newNumber((Integer.parseInt(m.group(1)) + 12) + "." + m.group(2), "oc");
            //result.append(newNumber((Integer.parseInt(m.group(1))+12)+"."+m.group(2),"oc"));
            result.append(rep);
            difference = rep.length() - (m.end() - m.start());
            posTracker.addPositionChange(m.end(), difference);
          } while (m.find());
          for (int i = pos; i < s.length(); i++)
            result.append(s.charAt(i));
          posTracker.closeRun();
        }

        /* applies the pattern-replacement 
         *  Note: If you fix something in this version, please try to apply the same fix at the position change tracking function above */
        public void apply(StringBuilder s, StringBuilder result) {
          result.setLength(0);
          Matcher m = pattern.matcher(s);
          if (!m.find()) return;
          int pos = 0;
          do {
            for (int i = pos; i < m.start(); i++)
              result.append(s.charAt(i));
            pos = m.end();
            result.append(newNumber((Integer.parseInt(m.group(1)) + 12) + "." + m.group(2), "oc"));
          } while (m.find());
          for (int i = pos; i < s.length(); i++)
            result.append(s.charAt(i));
        }
      },
      new FindReplace("(\\d+):(\\d{2})(?::(\\d{2})(?:\\.(\\d+))?)?\\s*h", null) {

        /* applies the pattern-replacement 
        *  Note: If you fix something in this version, please try to apply the same fix at the non-tracking function */
        public void apply(StringBuilder s, StringBuilder result, PositionTracker posTracker) {
          result.setLength(0);
          Matcher m = pattern.matcher(s);
          if (!m.find()) return;
          int pos = 0;
          do {
            for (int i = pos; i < m.start(); i++)
              result.append(s.charAt(i));
            pos = m.end();
            double val = Double.parseDouble(m.group(1)) * 3600 + Double.parseDouble(m.group(2)) * 60;
            if (m.group(3) != null) val += Double.parseDouble(m.group(3));
            if (m.group(4) != null) val += Double.parseDouble("0." + m.group(4));
            String rep = newNumber(val, "s");
            result.append(rep).append(' ');
            Integer difference = rep.length() + 1 - (m.end() - m.start());
            posTracker.addPositionChange(m.end(), difference);

          } while (m.find());
          for (int i = pos; i < s.length(); i++)
            result.append(s.charAt(i));
          posTracker.closeRun();
        }

        /* applies the pattern-replacement 
         *  Note: If you fix something in this version, please try to apply the same fix at the position change tracking function above */
        public void apply(StringBuilder s, StringBuilder result) {
          result.setLength(0);
          Matcher m = pattern.matcher(s);
          if (!m.find()) return;
          int pos = 0;
          do {
            for (int i = pos; i < m.start(); i++)
              result.append(s.charAt(i));
            pos = m.end();
            double val = Double.parseDouble(m.group(1)) * 3600 + Double.parseDouble(m.group(2)) * 60;
            if (m.group(3) != null) val += Double.parseDouble(m.group(3));
            if (m.group(4) != null) val += Double.parseDouble("0." + m.group(4));
            result.append(newNumber(val, "s")).append(' ');
          } while (m.find());
          for (int i = pos; i < s.length(); i++)
            result.append(s.charAt(i));
        }
      },
      new FindReplace("(\\d++)\\s*+h(?:ours?)?\\W*+(\\d++)\\s*+min(?:utes)?", null) {

        /* applies the pattern-replacement 
        *  Note: If you fix something in this version, please try to apply the same fix at the non-tracking function */
        public void apply(StringBuilder s, StringBuilder result, PositionTracker posTracker) {
          result.setLength(0);
          Matcher m = pattern.matcher(s);
          if (!m.find()) return;
          int pos = 0;
          do {
            for (int i = pos; i < m.start(); i++)
              result.append(s.charAt(i));
            pos = m.end();
            double val = Double.parseDouble(m.group(1)) * 3600 + Double.parseDouble(m.group(2)) * 60;
            String rep = newNumber(val, "s");
            result.append(rep).append(' ');
            Integer difference = rep.length() + 1 - (m.end() - m.start());
            posTracker.addPositionChange(m.end(), difference);
          } while (m.find());
          for (int i = pos; i < s.length(); i++)
            result.append(s.charAt(i));
          posTracker.closeRun();
        }

        /* applies the pattern-replacement 
          *  Note: If you fix something in this version, 
          *  please try to apply the same fix at 
          *  the position change tracking function above */
        public void apply(StringBuilder s, StringBuilder result) {
          result.setLength(0);
          Matcher m = pattern.matcher(s);
          if (!m.find()) return;
          int pos = 0;
          do {
            for (int i = pos; i < m.start(); i++)
              result.append(s.charAt(i));
            pos = m.end();
            double val = Double.parseDouble(m.group(1)) * 3600 + Double.parseDouble(m.group(2)) * 60;
            result.append(newNumber(val, "s")).append(' ');
          } while (m.find());
          for (int i = pos; i < s.length(); i++)
            result.append(s.charAt(i));
        }
      },
      new FindReplace(POSINT + B + "(?:[Ss]t\\.?|[Ss]tones?)" + B + POSINT + B + "(?:[lL]b\\.?)" + WB, null) {

        /* applies the pattern-replacement 
        *  Note: If you fix something in this version, please try to apply the same fix at the non-tracking function */
        public void apply(StringBuilder s, StringBuilder result) {
          result.setLength(0);
          Matcher m = pattern.matcher(s);
          if (!m.find()) return;
          int pos = 0;
          do {
            for (int i = pos; i < m.start(); i++)
              result.append(s.charAt(i));
            pos = m.end();
            String rep = newNumber(Integer.parseInt(m.group(1)) * 6350.29 + Integer.parseInt(m.group(2)) * 453.592, "g");
            result.append(rep);
          } while (m.find());
          for (int i = pos; i < s.length(); i++)
            result.append(s.charAt(i));
        }
      },
      new FindReplace(POSINT + B + "(?:'|[Ff]t\\.?|[fF]eet)" + B + POSINT + B + "(?:\"|[iI]ns?\\.?|[iI]nch(?:es))" + WB, null) {

        /* applies the pattern-replacement 
        *  Note: If you fix something in this version, please try to apply the same fix at the non-tracking function */
        public void apply(StringBuilder s, StringBuilder result, PositionTracker posTracker) {
          result.setLength(0);
          Matcher m = pattern.matcher(s);
          if (!m.find()) return;
          int pos = 0;
          do {
            for (int i = pos; i < m.start(); i++)
              result.append(s.charAt(i));
            pos = m.end();
            String rep = newNumber(Integer.parseInt(m.group(1)) * 0.3048 + Integer.parseInt(m.group(2)) * 0.0254, "m");
            result.append(rep);
            Integer difference = rep.length() - (m.end() - m.start());
            posTracker.addPositionChange(m.end(), difference);

          } while (m.find());
          for (int i = pos; i < s.length(); i++)
            result.append(s.charAt(i));
        }

        /* applies the pattern-replacement 
         *  Note: If you fix something in this version, 
         *  please try to apply the same fix at 
         *  the position change tracking function above */
        public void apply(StringBuilder s, StringBuilder result) {
          result.setLength(0);
          Matcher m = pattern.matcher(s);
          if (!m.find()) return;
          int pos = 0;
          do {
            for (int i = pos; i < m.start(); i++)
              result.append(s.charAt(i));
            pos = m.end();
            result.append(newNumber(Integer.parseInt(m.group(1)) * 0.3048 + Integer.parseInt(m.group(2)) * 0.0254, "m"));
          } while (m.find());
          for (int i = pos; i < s.length(); i++)
            result.append(s.charAt(i));
        }
      },

      // The internal order of the patterns is essential!
      new FindCompute("(pounds|pound|lb|lbs)", "g", 453.59237, 0),
      // TODO: The UK currency is a problem

      // --------- SI-Units ---------------
      new FindReplace(FLOAT + B + "/km\\^2", newNumber("$1", "/km^2")),
      new FindReplace(POSINT + B + "o'?clock am" + WB, newNumber("$1", "oc")),
      new FindAdd("o'?clock pm", "oc", 12),
      new FindReplace(POSINT + B + "o'?clock", newNumber("$1", "oc")),
      new FindReplace(POSINT + ':' + POSINT + B + "am" + WB, newNumber("$1.$2", "oc")),
      new FindReplace(POSINT + B + "am" + WB, newNumber("$1", "oc")),
      new FindAdd("pm", "oc", 12),
      new FindReplace(SINT + "," + DIG + '%', newNumber("$1.$2", "%")),
      new FindReplace(FLOAT + B + '%', newNumber("$1", "%")),
      new FindReplace("(?i:US\\$|USD|\\$|\\$US|US\\$)" + B + FLOAT, newNumber("$1", "dollar")),
      new FindReplace("(?i:eur|euro|euros|\u20AC)" + B + FLOAT, newNumber("$1", "euro")),
      new FindCompute("((?i:dollars|dollar|\\$|US\\$|USD|\\$US))", "dollar"),
      new FindCompute("((?i:euro?s?))", "euro"),
      new FindCompute("(metres|meters|meter|metre|m)(\\^)?2", "m^2"),
      new FindCompute("(metres|meters|meter|metre|m)(\\^)?3", "m^3"),
      new FindCompute("(metres|meters|meter|metre|m)", "m"),
      new FindCompute("(square|sq)" + B + P + "(metres|meters|meter|metre|m)", "m^2"),
      new FindCompute("(cubic|cu)" + B + P + "(metres|meters|meter|metre|m)", "m^3"),
      new FindCompute("(g|grams|gram)", "g"),
      new FindCompute("(seconds|s)", "s"),
      new FindCompute("(amperes|ampere|A)", "a"),
      new FindCompute("(Kelvin|K)", "K"),
      new FindCompute("(Mol|mol)", "mol"),
      new FindCompute("(candela|cd)", "can"),
      new FindCompute("(radians|rad)", "rad"),
      new FindCompute("(hertz|Hz)", "hz"),
      new FindCompute("(newton|N)", "N"),
      new FindCompute("(joule|J)", "J"),
      new FindCompute("(watt|W)", "W"),
      new FindCompute("(pascal|Pa|pa)", "pa"),
      new FindCompute("(lumen|lm)", "lm"),
      new FindCompute("(lux|lx)", "lx"),
      new FindCompute("(coulomb|C)", "C"),
      new FindCompute("(volt|V)", "V"),
      new FindCompute("(ohm|O)", "ohm"),
      new FindCompute("(farad|F)", "F"),
      new FindCompute("(weber|Wb)", "Wb"),
      new FindCompute("(tesla|T)", "T"),
      new FindCompute("(henry|H)", "H"),
      new FindCompute("(siemens|S)", "S"),
      new FindCompute("(becquerel|Bq)", "Bq"),
      new FindCompute("(gray|Gy)", "Gy"),
      new FindCompute("(sievert|Sv)", "Sv"),
      new FindCompute("(katal|kat)", "kat"),
      new FindCompute("(bytes|b|Bytes|B)", "byte"),

      // --------- Quasi-SI-Units ---------------
      new FindCompute("(degrees|degree)" + B + "(Celsius|C)", "kelvin", 1, +273.15),
      new FindCompute("([Mm]inutes|[Mm]inute|[Mm]in|[Mm]ins)", "s", 60, 0),
      new FindCompute("(hours|hour|h)", "s", 3600, 0),
      new FindCompute("(days|day)", "s", 86400, 0),
      new FindCompute("(litres|liters|litre|liter|l|L)", "m^3", 0.001, 0),
      new FindCompute("(metric )?(tonnes|tons|tonne|ton|t)", "g", 1000000, 0),

      // --------- Non-SI-Units ---------------
      new FindCompute("nautical (miles|mile)", "m", 1852, 0),
      new FindCompute("(knots|knot)", "m/h", 1852, 0),
      new FindCompute("(hectares|hectare|ha)", "m^2", 10000, 0),
      new FindCompute("bar", "pa", 100000, 0),
      new FindCompute("(inches|inch|in)\\^2", "m^2", 0.00064516, 0),
      new FindCompute("(foot|ft|feet)\\^2", "m^2", 0.0009290304, 0),
      new FindCompute("(miles|mile|mi)\\^2", "m^2", 2589988.110336, 0),
      new FindCompute("(inches|inch|in)\\^3", "m^3", 0.000016387064, 0),
      new FindCompute("(feet|foot|ft)\\^3", "m^3", 0.028317, 0),
      new FindCompute("(yards|yard|yd)\\^3", "m^3", 0.007646, 0),
      new FindCompute("(inches|inch)", "m", 0.0254, 0),
      new FindCompute("in", "m", 0.0254, 0, true),
      new FindCompute("(foot|feet|ft)", "m", 0.3048, 0),
      new FindCompute("(yards|yard|yd)", "m", 0.9144, 0),
      new FindCompute("(miles|mile|mi)", "m", 1609.344, 0),
      new FindCompute("(square|sq)" + B + "(inches|inch|in)", "m^2", 0.00064516, 0),
      new FindCompute("(square|sq)" + B + "(foot|ft|feet)", "m^2", 0.09290304, 0),
      new FindCompute("(acres|acre)", "m^2", 4046.8564224, 0),
      new FindCompute("(square|sq)" + B + "(miles|mile|mi)", "m^2", 2589988.11, 0),
      new FindCompute("(cubic|cu)" + B + "(inches|inch|in)", "m^3", 0.000016387064, 0),
      new FindCompute("(cubic|cu)" + B + "(feet|foot|ft)", "m^3", 0.0283168466, 0),
      new FindCompute("(cubic|cu)" + B + "(yards|yard|yd)", "m^3", 0.764554858, 0),
      new FindCompute("acre-foot", "m^2", 12334.818, 0),
      new FindCompute("(gallon|gallons|gal)", "m^2", 0.0037854118, 0),
      new FindCompute("(ounces|ounce|oz)", "g", 28.3495231, 0),
      new FindCompute("(pounds|pound|lb|lbs)", "g", 453.59237, 0),
      new FindCompute("(stones|stone)", "g", 6350.29318, 0),
      new FindCompute("(degrees? Fahrenheit|degrees? F|Fahrenheit)", "K", 0.55555555555, +459.67),
      new FindCompute("(degrees|degree)", "rad", 0.0174532925, 0)

      // ---------- Hyphens ---------------
  /*
   * newFindReplace(FLOAT+H+FLOAT+B+UNIT+"&%([^&]*)&",newNumber("$1","$3")+
   * "&%$4& to "+newNumber("$2","$3")+"&%$4&"), new
   * FindReplace(FLOAT+H+FLOAT+B
   * +UNIT+"&\\*([^&]*)&",newNumber("$1","$3")+"&*$4& to "
   * +newNumber("$2","$3")+"&*$4&"), new
   * FindReplace(FLOAT+H+FLOAT,newNumber("$1")+" to "+newNumber("$2"))
   */

      // --------- Speed ----------
  /*
   * new
   * FindReplace(MNUMBER+" &%([^&]*)&"+B+"(per|/)"+B+"(hour|h)"+FB,newNumber
   * ("$1","ms")+"&*3600&&%$2&$5"), new
   * FindReplace(MNUMBER+" &\\*([^&]*)&"+B+"(per|/)"
   * +B+"(hour|h)"+FB,newNumber("$1","ms")+"&*3600&&*$2&$5"), new
   * FindReplace(MNUMBER
   * +B+"(per|/)"+B+"(hour|h)"+FB,newNumber("$1","ms")+"$4"), new
   * FindReplace(MNUMBER
   * +B+"(per|/)"+B+"(second|s)"+FB,newNumber("$1","ms")+"$4"),
   */

  };

  /** Normalizes all numbers in a String 
   *  Note: If you fix something in this version,
   *  please try to apply the same fix at
   *  the position change tracking function below */
  public static String normalize(CharSequence s) {
    StringBuilder in = new StringBuilder((int) (s.length() * 1.1)).append(s);
    StringBuilder result = new StringBuilder((int) (s.length() * 1.1));
    for (FindReplace fr : patterns) {
      fr.apply(in, result);
      if (result.length() != 0) {
        // D.p(fr.pattern,"--->    ",result); // For debugging
        StringBuilder temp = in;
        in = result;
        result = temp;
      }
    }
    result = null;
    return (in.toString());
  }

  /** Normalizes all numbers in a String and updates a position mapping with the introduced pos changes 
   *  Note: If you fix something in this version,
   *  please try to apply the same fix at
   *  the non-tracking function above */
  public static String normalize(CharSequence s, PositionTracker posTracker) {

    StringBuilder in = new StringBuilder((int) (s.length() * 1.1)).append(s);
    StringBuilder result = new StringBuilder((int) (s.length() * 1.1));
    for (FindReplace fr : patterns) {
      fr.apply(in, result, posTracker);
      if (result.length() != 0) {
        StringBuilder temp = in;
        in = result;
        result = temp;
      }
    }
    result = null;
    return (in.toString());
  }

  /**
   * Extracts the pure number from a String containing a normalized number,
   * else null
   */
  public static String getNumber(CharSequence d) {
    if (d == null) return (null);
    Matcher m = NUMBERPATTERN.matcher(d);
    if (m.find()) return (m.group(1));
    return (null);
  }

  /**
   * Extracts the number and its unit from a String containing a normalized
   * number, else null, returns start and end position in pos[0] and pos[1]
   * resp.
   */
  public static String[] getNumberAndUnit(CharSequence d, int[] pos) {
    Matcher m = NUMBERPATTERN.matcher(d);
    if (!m.find()) return (null);
    pos[0] = m.start();
    pos[1] = m.end();
    return (new String[] { m.group(1), m.group(2) });
  }

  /** Extracts the numbers and units from a normalized String */
  public static List<String> getNumbers(CharSequence d) {
    List<String> result = new ArrayList<String>(3);
    Matcher m = NUMBERPATTERN.matcher(d);
    while (m.find()) result.add(m.group());
    return (result);
  }

  /**
   * Converts a String that contains a (non-normalized) number to a double or
   * null
   */
  public static Double getDouble(CharSequence d) {
    String number = getNumber(normalize(d));
    if (number == null) return (null);
    return (new Double(number));
  }

  /**
   * Converts a String that contains a (non-normalized) number to a long or
   * null
   */
  public static Long getLong(CharSequence d) {
    String number = getNumber(normalize(d));
    if (number == null) return (null);
    return (new Double(number).longValue());
  }

  /**
   * Converts a String that contains a (non-normalized) number to a int or
   * null
   */
  public static Integer getInt(CharSequence d) {
    String number = getNumber(normalize(d));
    if (number == null) return (null);
    return (new Double(number).intValue());
  }

  /** Calls Integer.parseInt, returns an Integer or NULL*/
  public static Integer parseInt(String d) {
    try {
      return (Integer.parseInt(d));
    } catch (Exception e) {
      return (null);
    }
  }

  /** Calls Double.parseDouble, returns an Integer or NULL*/
  public static Double parseDouble(String d) {
    try {
      return (Double.parseDouble(d));
    } catch (Exception e) {
      return (null);
    }
  }

  /** TRUE if the numbers differ in the unit and or by more than 10% */
  public static boolean different(String n1, String n2) {
    String[] num1 = getNumberAndUnit(n1, new int[2]);
    String[] num2 = getNumberAndUnit(n2, new int[2]);
    if (!D.equal(num1[1], num2[1])) return (true);
    double val1 = Double.parseDouble(num1[0]);
    double val2 = Double.parseDouble(num2[0]);
    return (Math.abs(val1 - val2) > Math.abs(val1) / 10);
  }

  /** Test method */
  public static void main(String[] argv) throws Exception {
    System.out.println("Enter a string that contains a number and hit ENTER. Press CTRL+C to abort");
    while (true) {
      String in = D.r();
      System.out.println(normalize(in));
      System.out.println(getDouble(in));
    }
  }

}
