package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative.D;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Triple;

import java.util.*;
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
 * The DateParser normalizes date expressions in english natural language text
 * to ISO-dates <BR>
 * <CENTER><i>year-month-day</i></CENTER><BR>
 * where <i>year</i> is either positive negative. The DateParser understands
 * expressions like "4th century BC" or "3rd of November 2004". Dates may be
 * underspecified: The character '#' stands for "at least one digit".<BR>
 * Example:
 *
 * <PRE>
 * DateParser.normalize("It was November 23rd to 24th 1998.")
 *          --> "It was 1998-11-23 to 1998-11-24."
 *          DateParser.getDate("It was 1998-11-23 to 1998-11-24.")
 *          -->  1998, 11, 23
 *          NumberFormatter.ISOtime(DateParser.getCalendar("November 24th 1998"))
 *          --> 1998-12-24 T 00:00:00.00
 * </PRE>
 *
 * TODO 1st century transformed to 0##-##-##. Should be 00##-##-##. Just padding
 * doesn't work because millenium would then be padded too much (need to distinguish
 * between centuries and millenias).
 */

public class DateParser {

  /** Creates a date-string of the form "year-month-day" */
  public static final String newDate(String y, String m, String d) {
    return (y + "-" + m + "-" + d);
  }

  /** Creates a date-string from a day, month and year as ints */
  public static final String newDate(int y, int m, int d) {
    return (newDate("" + y, m < 10 ? "0" + m : "" + m, d < 10 ? "0" + d : "" + d));
  }

  /** Creates a date-string of the form "year-month-day" */
  public static final String newSubDate(String y, String m) {
    return (y + "-" + m);
  }

  /** A Date as a capturing RegEx */
  //	public static final String DATE = newDate("(-?[0-9#]++)", "([0-9#]{2})",
  //			"([0-9#]{2})");
  //	public static final Pattern DATEPATTERN = Pattern.compile(DATE); // No
  // trailing
  // boundary
  // because
  // of
  // '#'

  // Required to deal with negative dates
  private static final String WBNEG = "[(?:(?<=[A-Za-z0-9_-])(?![A-Za-z0-9_-])|(?<![A-Za-z0-9_-])(?=[A-Za-z0-9_-]))&&[^|\\(\\)]]";

  public static final String DATE = newDate("(-?[0-9#X]++)", "([0-9#X]{1,2})", "([0-9#X]{1,2})");

  public static final Pattern DATEPATTERN = Pattern.compile(WBNEG + DATE); // No trailing boundary because of '#'

  public static final Pattern JUSTDATEPATTERN = Pattern.compile(DATE); // No trailing boundary because of '#'

  public static final String SDATE = newSubDate("(-?[0-9#X]++)", "([0-9#X]{1,2})");

  public static final Pattern SDATEPATTERN = Pattern.compile(WBNEG + SDATE); // No trailing boundary because of '#'

  /** A year as a pattern */
  public static final Pattern SIMPLEYEARPATTERN = Pattern.compile("\\b(\\d{3,4})\\b");

  public static final Pattern YEARPATTERN = Pattern.compile("\\d{4}");

  /** Just a pair of a Pattern and a replacement string */
  private static class FindReplace {

    public Pattern pattern;

    public String replacement;

    public static int counter = 0;

    public int id = counter++;

    public FindReplace(String f, String r) {
      pattern = Pattern.compile(f);
      replacement = r;
    }

    @Override public String toString() {
      return id + ": " + pattern + "     -->     " + replacement;
    }
  }

  /** Contains the month short names */
  //private static final String[] MONTHS = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

  private static final String[] MONTHS_EN = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

  private static final String[] MONTHS_DE = new String[] { "Jan", "Feb", "M\u00E4r", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez" };

  private static final String[] MONTHS_ES = new String[] { "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic" };

  private static final String[] MONTHS_FR = new String[] {
      "janv",
      "f\u00E9vr",
      "mars",
      "avr",
      "mai",
      "juin",
      "juil",
      "ao\u00FBt",
      "sept",
      "oct",
      "nov",
      "d\u00E9c" };

  private static final String[] MONTHS_IT = new String[] { "gen", "feb", "mar", "apr", "mag", "giu", "lug", "ago", "sep", "ott", "nov", "dic" };

  /** A blank as a RegEx */
  private static final String B = "[\\W_&&[^-]]*+";

  /** A forced blank as a RegEx */
  private static final String FB = "[\\W_&&[^-(]]++";

  /** A word boundary as a RegEx */
  private static final String WB = "\\b";

  /** A hyphen as a RegEx with blanks */
  private static final String H = B + "(?:-+|to|until)" + B;

  /** BC as a RegEx with blank */
  private static final String BC = B + "(?:BC|B\\.C\\.|BCE|AC|A\\.C\\.)";

  /** AD as a RegEx with blank */
  private static final String AD = "AD|A\\.D\\." + B;

  /** CE as a RegEx with blank */
  private static final String CE = B + "CE|C\\.E\\.";

  /** ##th as a capturing RegEx with blank */
  private static final String NTH = "(\\d{1,2})(?:[a-z]{2}|\\#th)" + B;

  /** ##[th] as a capturing RegEx */
  private static final String N = "(\\d{1,2})[a-z]{0,2}";

  /** #### as a capturing RegEx */
  private static final String Y4 = "(\\d{4})";

  /** ###...# as a capturing RegEx */
  private static final String Y = "(-?\\d{1,10})";

  /** MONTH## as a capturing RegEx */
  private static final String M = "MONTH(\\d\\d)";

  /** A "the" as a RegEx with blank */
  private static final String THE = "(?:the)?" + B;

  /** century as a RegEx */
  private static final String CENTURY = "[cC]entur(?:y|(?:ies))";

  /** millenium as a RegEx */
  private static final String MILENNIUM = "[mM]ill?enn?ium";

  /** Holds the date patterns */
  // The internal order of the patterns is essential!
  // We always process periods of time first in order to capture the part of
  // it
  // that lacks explicit date identification
  // Furthermore, we first process "BC" because we might loose it else
  private static final FindReplace[] patterns = new FindReplace[] {

      // --------- Process ISO8601 ------------------
      new FindReplace(Y + " ?[-\\|] ?(\\d{1,2}) ?[-\\|] ?(\\d{1,2})", newDate("$1", "$2", "$3")),

      // --------- Process ISO8601 include Month------------------
      new FindReplace(Y + " ?[-\\|] ?" + M + " ?[-\\|] ?(\\d{1,2})", newDate("$1", "$2", "$3")),

      // --------- Process BC, CE and AD ------------
      // 2267 - 2213 BC
      new FindReplace(Y + H + Y + BC, newDate("-$1", "##", "##") + " to " + newDate("-$2", "##", "##")),
      // 2267 - 2213 CE
      new FindReplace(Y + H + Y + CE, newDate("$1", "##", "##") + " to " + newDate("$2", "##", "##")),
      // 1000 BC - AD 120
      new FindReplace(Y + BC + H + AD + Y, newDate("-$1", "##", "##") + " to " + newDate("$2", "##", "##")),
      // 1000 BC - 120 CE
      new FindReplace(Y + BC + H + Y + CE, newDate("-$1", "##", "##") + " to " + newDate("$2", "##", "##")),
      // AD 46 - 120
      new FindReplace(AD + Y + H + Y, newDate("$1", "##", "##") + " to " + newDate("$2", "##", "##")),
      // 1000 BC
      new FindReplace(Y + BC, newDate("-$1", "##", "##")),
      // 1000 CE
      new FindReplace(Y + CE, newDate("$1", "##", "##")),
      // AD 1000
      new FindReplace(AD + Y, newDate("$1", "##", "##")),

      // --------- Process complete dates ----------
      // 23rd - 24th of November 1998
      new FindReplace(WB + N + H + N + B + "(?:of)?" + B + M + B + Y, newDate("$4", "$3", "$1") + " to " + newDate("$4", "$3", "$2")),
      // November 23rd - 24th 1998
      new FindReplace(M + B + N + H + N + FB + Y, newDate("$4", "$1", "$2") + " to " + newDate("$4", "$1", "$3")),
      // November 23rd - March 24th 1998
      new FindReplace(M + B + N + H + M + B + N + FB + Y, newDate("$5", "$1", "$2") + " to " + newDate("$5", "$3", "$4")),
      // November 23[rd] 1998
      new FindReplace(M + B + N + FB + Y, newDate("$3", "$1", "$2")),
      // November 23, 1998
      new FindReplace(M + B + N + B + "," + B + Y, newDate("$3", "$1", "$2")),
      // 23[rd] November 1998
      new FindReplace(N + B + M + B + Y, newDate("$3", "$2", "$1")),
      // the 23[rd] of November 1998
      new FindReplace("the " + N + B + "of" + B + M + B + Y, newDate("$3", "$2", "$1")),
      // 23[rd] of November 1998
      new FindReplace(N + B + "of" + B + M + B + Y, newDate("$3", "$2", "$1")),
      // 1.11.1998
      new FindReplace(N + "\\." + N + "\\." + Y, newDate("$3", "$2", "$1")),
      // 12-Sep-1970
      new FindReplace(N + H + M + H + Y, newDate("$3", "$2", "$1")),

      // --------- Process dates with months ----------
      // June - April 1980
      new FindReplace(M + H + M + B + Y4, newDate("$3", "$1", "##") + " to " + newDate("$3", "$2", "##")),
      // April 1980
      new FindReplace(M + B + Y4, newDate("$2", "$1", "##")),
      // April of 1980
      new FindReplace(M + B + "of" + B + Y4, newDate("$2", "$1", "##")),

      // --------- Process days of the year ----------
      // June 14th - July 17th
      new FindReplace(M + B + N + H + M + B + N, newDate("##", "$1", "$2") + " to " + newDate("##", "$3", "$4")),
      // June 14th - 17th
      new FindReplace(M + B + N + H + N, newDate("##", "$1", "$2") + " to " + newDate("##", "$1", "$3")),
      // June 14th
      new FindReplace(M + B + N, newDate("##", "$1", "$2")),

      // ----------- Process centuries (add subtraction marker)
      // -----------
      // ##th - ##th millennium BC
      new FindReplace(THE + NTH + H + NTH + B + MILENNIUM + BC, ' ' + newDate("-&DEC$1###", "##", "##") + " to " + newDate("-&DEC$2###", "##", "##")),
      // ##th - ##th century BC
      new FindReplace(THE + NTH + H + NTH + B + CENTURY + BC, ' ' + newDate("-&DEC$1##", "##", "##") + " to " + newDate("-&DEC$2##", "##", "##")),
      // ##th - ##th century
      new FindReplace(THE + NTH + H + NTH + B + CENTURY, ' ' + newDate("&DEC$1##", "##", "##") + " to " + newDate("&DEC$2##", "##", "##")),
      // ##th millennium BC
      new FindReplace(THE + NTH + B + MILENNIUM + BC, ' ' + newDate("-&DEC$1###", "##", "##")),
      // ##th century BC
      new FindReplace(THE + NTH + B + CENTURY + BC, ' ' + newDate("-&DEC$1##", "##", "##")),
      // ##th millennium
      new FindReplace(THE + NTH + B + MILENNIUM, ' ' + newDate("&DEC$1###", "##", "##")),
      // ##th century
      new FindReplace(THE + NTH + B + CENTURY, ' ' + newDate("&DEC$1##", "##", "##")),

      // ------------ Process special constructions -----------
      // 1850s
      new FindReplace("(\\d{3})0'?s", newDate("$1#", "##", "##")),

      //    ---------------in the summer of 2009 -------------------
      new FindReplace("summer of " + Y, ' ' + newDate("$1", "68", "##")),
      //    ---------------in the winter of 2009 -------------------
      new FindReplace("winter of " + Y, ' ' + newDate("$1", "22", "##")),
      //    ---------------in the spring of 2009 -------------------
      new FindReplace("spring of " + Y, ' ' + newDate("$1", "35", "##")),
      //    ---------------in the autum of 2009 -------------------
      new FindReplace("autumn of " + Y, ' ' + newDate("$1", "91", "##")),

      // ------------ Make Months and days 2-digit -----------
      new FindReplace(newDate("([0-9#]++)", "([0-9])", "([0-9\\#]++)"), newDate("$1", "0$2", "$3")),
      new FindReplace(newDate("([0-9#]++)", "([0-9#]{2})", "([0-9])" + WB), newDate("$1", "$2", "0$3")) };

  /** Holds the pattern seeking for month names */
  //private static final Pattern monthPattern = Pattern.compile(WB + "(Jan|January|Feb|February|Febr|Mar|March|Apr|April|May|Jun|June|Jul|July|Aug|August|Sep|September|Sept" + "|Oct|October|Nov|November|Dec|December)" + WB);

  private static final Pattern monthPatternEn = Pattern.compile(
      WB + "(Jan|January|Feb|February|Febr|Mar|March|Apr|April|May|Jun|June|Jul|July|Aug|August|Sep|September|Sept"
          + "|Oct|October|Nov|November|Dec|December)" + WB);

  private static final Pattern monthPatternDe = Pattern.compile(
      WB + "(Jan|Januar|Feb|Februar|Febr|M\u00E4r|M\u00E4rz|Apr|April|Mai|Jun|Juni|Jul|Juli|Aug|August|Sep|September|Sept"
          + "|Okt|Oktober|Nov|November|Dez|Dezember)" + WB);

  private static final Pattern monthPatternEs = Pattern.compile(
      WB + "(ene|enero|feb|febrero|febr|mar|marzo|abr|abril|may|mayo|jun|junio|julio|ago|agosto|sep|septiembre"
          + "|oct|octubre|nov|noviembre|dic|diciembre)" + WB);

  private static final Pattern monthPatternFr = Pattern.compile(
      WB + "(janv|janvier|f\u00E9vr|f\u00E9vrier|mars|avr|avril|mai|juin|juil|juillet|ao\u00FBt|sept|septembre"
          + "|oct|octobre|nov|novembre|d\u00E9c|d\u00E9cembre)" + WB);

  private static final Pattern monthPatternIt = Pattern.compile(
      WB + "(gen|gennaio|feb|febbraio|febr|mar|marzo|apr|aprile|mag|maggio|giu|giugno|lug|luglio|ago|agosto|set|settembre"
          + "|ott|ottobre|nov|novembre|dic|dicembre)" + WB);

  /**
   * Holds the pattern that determines whether a string contains a year
   * expression
   */
  private static final Pattern yearPattern = Pattern.compile("\\d{2,4}|\\d{4}s|B\\.?C\\.?|A\\.?C\\.?|A\\.?D\\.?|" + CENTURY + "|\\d{2,4}s");

  /** Normalizes all dates in a String 
   * DEPRECATED - can go away when multilingual version accepted */
  /*
   * public static String normalize(CharSequence s) {

  	// If it does not contain years or months, return it unchanged
  	if (!monthPattern.matcher(s).find() && !yearPattern.matcher(s).find())
  		return (s.toString());

  	StringBuffer in = new StringBuffer((int) (s.length() * 1.1));

  	// Replace all the months
  	Matcher m = monthPattern.matcher(s);
  	while (m.find()) {
  		int monthNum = D.indexOf(m.group().substring(0, 3),
  				(Object[]) MONTHS) + 1;
  		m
  				.appendReplacement(in, "MONTH" + (monthNum / 10)
  						+ (monthNum % 10));
  	}
  	m.appendTail(in);
  	StringBuffer out = new StringBuffer((int) (in.length() * 1.1));

  	// Apply the patterns
  	for (FindReplace p : patterns) {
  		m = p.pattern.matcher(in);
  		if (m.find()) {
  			out.setLength(0);
  			do {
  				m.appendReplacement(out, p.replacement);
  			} while (m.find());
  			m.appendTail(out);
  			StringBuffer temp = out;
  			out = in;
  			in = temp;
  		}
  	}

  	m = Pattern.compile("&DEC(\\d++)").matcher(in);
  	if (m.find()) {
  		out.setLength(0);
  		do {
  			m.appendReplacement(out, ""
  					+ (Integer.parseInt(m.group(1)) - 1));
  		} while (m.find());
  		m.appendTail(out);
  		in = null;
  		return (out.toString());
  	}
  	out = null;
  	return (in.toString());
  }
  */

  /** Normalizes all dates in a String */
  public static String normalize(CharSequence s) {
    return normalize(s, Language.ENGLISH);
  }

  /** Normalizes all dates in a String 
   * Note: If you bugfix something in this version, 
   * please check for applying the same fix at the position change tracking function below */
  public static String normalize(CharSequence s, Language language) {

    Pattern monthPattern;
    String[] months = null;
    if (language.equals(Language.ENGLISH)) {
      months = MONTHS_EN;
      monthPattern = monthPatternEn;
    } else if (language.equals(Language.GERMAN)) {
      months = MONTHS_DE;
      monthPattern = monthPatternDe;
    } else if (language.equals(Language.SPANISH)) {
      months = MONTHS_ES;
      monthPattern = monthPatternEs;
    } else if (language.equals(Language.FRENCH)) {
      months = MONTHS_FR;
      monthPattern = monthPatternFr;
    } else if (language.equals(Language.ITALIAN)) {
      months = MONTHS_IT;
      monthPattern = monthPatternIt;
    } else {
      throw new IllegalArgumentException("Unsupported language: " + language);
    }

    // If it does not contain years or months, return it unchanged
    if (!monthPattern.matcher(s).find() && !yearPattern.matcher(s).find()) return (s.toString());

    StringBuffer in = new StringBuffer((int) (s.length() * 1.1));

    // Replace all the months
    Matcher m = monthPattern.matcher(s);
    while (m.find()) {
      int monthNum = D.indexOf(m.group().substring(0, 3), (Object[]) months);
      if (monthNum == -1) monthNum = D.indexOf(m.group().substring(0, 4), (Object[]) months);
      monthNum++; // convert from 0-based to 1-based
      m.appendReplacement(in, "MONTH" + (monthNum / 10) + (monthNum % 10));
    }
    m.appendTail(in);
    StringBuffer out = new StringBuffer((int) (in.length() * 1.1));

    // Apply the patterns
    for (FindReplace p : patterns) {
      m = p.pattern.matcher(in);
      if (m.find()) {
        out.setLength(0);
        do {
          m.appendReplacement(out, p.replacement);
        } while (m.find());
        m.appendTail(out);
        StringBuffer temp = out;
        out = in;
        in = temp;
      }

    }

    m = Pattern.compile("&DEC(\\d++)").matcher(in);
    if (m.find()) {
      out.setLength(0);
      do {
        m.appendReplacement(out, "" + (Integer.parseInt(m.group(1)) - 1));
      } while (m.find());
      m.appendTail(out);
      in = null;
      return (out.toString());
    }
    out = null;
    return (in.toString());
  }

  public static String normalize(CharSequence s, PositionTracker posTracker) {
    return normalize(s, Language.ENGLISH, posTracker);
  }

  /** Normalizes all dates in a String keeping track of the position changes with respect to the newly created string 
   * Note: If you bugfix something in this version, please try and check for applying the same fix at the non-tracking function */
  public static String normalize(CharSequence s, Language language, PositionTracker posTracker) {

    Pattern monthPattern;
    String[] months = null;
    if (language.equals(Language.ENGLISH)) {
      months = MONTHS_EN;
      monthPattern = monthPatternEn;
    } else if (language.equals(Language.GERMAN)) {
      months = MONTHS_DE;
      monthPattern = monthPatternDe;
    } else if (language.equals(Language.SPANISH)) {
      months = MONTHS_ES;
      monthPattern = monthPatternEs;
    } else if (language.equals(Language.FRENCH)) {
      months = MONTHS_FR;
      monthPattern = monthPatternFr;
    } else if (language.equals(Language.ITALIAN)) {
      months = MONTHS_IT;
      monthPattern = monthPatternIt;
    } else {
      throw new IllegalArgumentException("Unsupported language: " + language);
    }

    Integer difference = 0;

    // If it does not contain years or months, return it unchanged
    if (!monthPattern.matcher(s).find() && !yearPattern.matcher(s).find()) return (s.toString());

    StringBuffer in = new StringBuffer((int) (s.length() * 1.1));

    // Replace all the months
    Matcher m = monthPattern.matcher(s);
    while (m.find()) {
      m.end();
      int monthNum = D.indexOf(m.group().substring(0, 3), (Object[]) months);
      if (monthNum == -1) monthNum = D.indexOf(m.group().substring(0, 4), (Object[]) months);
      monthNum++; // convert from 0-based to 1-based
      String replacement = "MONTH" + (monthNum / 10) + (monthNum % 10);
      m.appendReplacement(in, replacement);
      difference = replacement.length() - (m.end() - m.start());
      posTracker.addPositionChange(m.end(), difference);
    }
    m.appendTail(in);
    StringBuffer out = new StringBuffer((int) (in.length() * 1.1));

    //merge position changes into the position mapping
    posTracker.closeRun();

    // Apply the patterns
    for (FindReplace p : patterns) {
      m = p.pattern.matcher(in);
      if (m.find()) {
        out.setLength(0);
        do {
          m.appendReplacement(out, p.replacement); //TODO: solve this more efficiently?
          String test = in.substring(m.start(), m.end());
          test = p.pattern.matcher(test).replaceFirst(p.replacement);
          difference = test.length() - (m.end() - m.start());
          posTracker.addPositionChange(m.end(), difference);
        } while (m.find());
        m.appendTail(out);
        StringBuffer temp = out;
        out = in;
        in = temp;
        //merge position changes into a single mapping
        posTracker.closeRun();
      }
    }

    m = Pattern.compile("&DEC(\\d++)").matcher(in);
    if (m.find()) {
      out.setLength(0);
      do {
        m.appendReplacement(out, "" + (Integer.parseInt(m.group(1)) - 1));
        String replacement = "" + (Integer.parseInt(m.group(1)) - 1);
        difference = replacement.length() - (m.end() - m.start());
        posTracker.addPositionChange(m.end(), difference);
      } while (m.find());
      m.appendTail(out);

      //merge position changes into a single mapping
      posTracker.closeRun();

      in = null;
      return (out.toString());
    }
    out = null;
    return (in.toString());
  }

  /** Normalizes all dates in a String */
  public static Collection<String> getAllDates(CharSequence s) {
    return getAllDates(s, Language.ENGLISH);
  }

  /** Normalizes all dates in a String */
  public static Collection<String> getAllDates(CharSequence s, Language language) {

    Pattern monthPattern;
    String[] months = null;
    if (language.equals(Language.ENGLISH)) {
      months = MONTHS_EN;
      monthPattern = monthPatternEn;
    } else if (language.equals(Language.GERMAN)) {
      months = MONTHS_DE;
      monthPattern = monthPatternDe;
    } else if (language.equals(Language.SPANISH)) {
      months = MONTHS_ES;
      monthPattern = monthPatternEs;
    } else if (language.equals(Language.FRENCH)) {
      months = MONTHS_FR;
      monthPattern = monthPatternFr;
    } else if (language.equals(Language.ITALIAN)) {
      months = MONTHS_IT;
      monthPattern = monthPatternIt;
    } else {
      throw new IllegalArgumentException("Unsupported language: " + language);
    }

    // If it does not contain years or months, return it unchanged
    if (!monthPattern.matcher(s).find() && !yearPattern.matcher(s).find()) return (null);

    ArrayList<String> dates = new ArrayList<String>();
    StringBuffer in = new StringBuffer((int) (s.length() * 1.1));

    // Replace all the months
    Matcher m = monthPattern.matcher(s);
    while (m.find()) {
      int monthNum = D.indexOf(m.group().substring(0, 3), (Object[]) months);
      if (monthNum == -1) monthNum = D.indexOf(m.group().substring(0, 4), (Object[]) months);
      monthNum++; // convert from 0-based to 1-based
      m.appendReplacement(in, "MONTH" + (monthNum / 10) + (monthNum % 10));
    }
    m.appendTail(in);
    StringBuffer out = new StringBuffer((int) (in.length() * 1.1));

    // Apply the patterns
    for (FindReplace p : patterns) {
      m = p.pattern.matcher(in);
      if (m.find()) {
        out.setLength(0);
        do {
          String retrivel = m.group();
          StringBuffer tem = new StringBuffer(retrivel);
          m.appendReplacement(out, p.replacement);
          Matcher tt = p.pattern.matcher(tem);
          StringBuffer o1 = new StringBuffer();
          if (tt.find()) {
            tt.appendReplacement(o1, p.replacement);
            dates.add(o1.toString());
          }

        } while (m.find());
        m.appendTail(out);
        StringBuffer temp = out;
        out = in;
        in = temp;
      }
    }

    m = Pattern.compile("&DEC(\\d++)").matcher(in);
    if (m.find()) {
      out.setLength(0);
      do {
        String retrivel = m.group();
        StringBuffer tem = new StringBuffer(retrivel);
        m.appendReplacement(out, "" + (Integer.parseInt(m.group(1)) - 1));
        Matcher tt = Pattern.compile("&DEC(\\d++)").matcher(tem);
        StringBuffer o1 = new StringBuffer();
        if (tt.find()) {
          tt.appendReplacement(o1, "" + (Integer.parseInt(m.group(1)) - 1));
          dates.add(o1.toString());
        }

      } while (m.find());
      m.appendTail(out);
      in = null;
      //      return(out.toString());
    }
    out = null;
    return dates;
  }

  /** Returns all the dates values and their position in the text */
  public static Collection<Triple<String, Integer, Integer>> getAllDatePositions(CharSequence s) {
    return getAllDatePositions(s, Language.ENGLISH);
  }

  /** Returns all the dates values and their position in the text */
  public static Collection<Triple<String, Integer, Integer>> getAllDatePositions(CharSequence s, Language language) {
    Pattern monthPattern;
    String[] months = null;
    if (language.equals(Language.ENGLISH)) {
      months = MONTHS_EN;
      monthPattern = monthPatternEn;
    } else if (language.equals(Language.GERMAN)) {
      months = MONTHS_DE;
      monthPattern = monthPatternDe;
    } else if (language.equals(Language.SPANISH)) {
      months = MONTHS_ES;
      monthPattern = monthPatternEs;
    } else if (language.equals(Language.FRENCH)) {
      months = MONTHS_FR;
      monthPattern = monthPatternFr;
    } else if (language.equals(Language.ITALIAN)) {
      months = MONTHS_IT;
      monthPattern = monthPatternIt;
    } else {
      throw new IllegalArgumentException("Unsupported language: " + language);
    }
    ArrayList<Triple<String, Integer, Integer>> dates = new ArrayList<Triple<String, Integer, Integer>>();
    // If it does not contain years or months, return an empty list
    if (!monthPattern.matcher(s).find() && !yearPattern.matcher(s).find()) return (dates);
    StringBuffer in = new StringBuffer((int) (s.length() * 1.1));

    // Replace all the months and remeber where are where with difference,
    List<Triple<Integer, Integer, Integer>> offSetList = new LinkedList<Triple<Integer, Integer, Integer>>();
    Matcher m = monthPattern.matcher(s);
    while (m.find()) {
      int monthNum = D.indexOf(m.group().substring(0, 3), (Object[]) months);
      if (monthNum == -1) monthNum = D.indexOf(m.group().substring(0, 4), (Object[]) months);
      monthNum++; // convert from 0-based to 1-based
      String newMonth = "MONTH" + (monthNum / 10) + (monthNum % 10);
      int diff = (m.end() - m.start()) - newMonth.length();
      m.appendReplacement(in, newMonth);
      int position = in.length() - newMonth.length();
      Triple<Integer, Integer, Integer> offsetPair = new Triple<Integer, Integer, Integer>(position, m.end() - m.start(), diff);
      offSetList.add(offsetPair);
    }
    m.appendTail(in);
    StringBuffer out = new StringBuffer((int) (in.length() * 1.1));
    // Apply the patterns
    int previousOffSet = 0;
    HashSet<Integer> offsetUsed = new HashSet<Integer>();
    for (FindReplace p : patterns) {
      m = p.pattern.matcher(in);
      List<Triple<Integer, Integer, Integer>> toRemove = new LinkedList<Triple<Integer, Integer, Integer>>();
      List<Triple<Integer, Integer, Integer>> toAdd = new LinkedList<Triple<Integer, Integer, Integer>>();
      if (m.find()) {
        out.setLength(0);
        previousOffSet = 0;
        do {
          String retrivel = m.group();
          StringBuffer tem = new StringBuffer(retrivel);
          m.appendReplacement(out, p.replacement);
          Matcher tt = p.pattern.matcher(tem);
          StringBuffer o1 = new StringBuffer();
          if (tt.find()) {
            tt.appendReplacement(o1, p.replacement);
            int start = m.start();
            int end = m.end();
            List<Triple<Integer, Integer, Integer>> offSetPairs = new LinkedList<Triple<Integer, Integer, Integer>>();
            Iterator<Triple<Integer, Integer, Integer>> iter = offSetList.iterator();
            previousOffSet = 0;
            boolean ignore = false;
            while (iter.hasNext()) {
              Triple<Integer, Integer, Integer> nextOffSetPair = iter.next();
              if (start <= nextOffSetPair.first && end >= nextOffSetPair.first) {
                offSetPairs.add(nextOffSetPair);
              } else if (nextOffSetPair.first < start) {
                previousOffSet = previousOffSet + nextOffSetPair.third;
              }
              if (nextOffSetPair.first < start && nextOffSetPair.second + nextOffSetPair.first >= end) {
                ignore = true;
              }
            }
            if (ignore) {
              continue;
            }
            if (offSetPairs.size() > 0) {
              int diff = 0;
              iter = offSetPairs.iterator();
              while (iter.hasNext()) {
                Triple<Integer, Integer, Integer> offSetPair = iter.next();
                toRemove.add(offSetPair);
                diff = diff + offSetPair.third;
              }
              end = end - start + diff;
              start = start + previousOffSet;
              previousOffSet = previousOffSet + diff;
              int position = out.length() - o1.length();
              int length = end - o1.length();
              Triple<Integer, Integer, Integer> newOffSetPair = new Triple<Integer, Integer, Integer>(position, o1.length(), length);
              toAdd.add(newOffSetPair);
            } else {
              end = end - start;
              start = start + previousOffSet;
              int position = out.length() - o1.length();
              int length = end - o1.length();
              Triple<Integer, Integer, Integer> newOffSetPair = new Triple<Integer, Integer, Integer>(position, o1.length(), length);
              toAdd.add(newOffSetPair);
            }
            if (!offsetUsed.contains(start)) {
              dates.add(new Triple<String, Integer, Integer>(o1.toString(), start, end));
              offsetUsed.add(start);
            }
          }
        } while (m.find());
        m.appendTail(out);
        StringBuffer temp = out;
        out = in;
        in = temp;
        Iterator<Triple<Integer, Integer, Integer>> iter = toRemove.iterator();
        while (iter.hasNext()) {
          offSetList.remove(iter.next());
        }
        iter = toAdd.iterator();
        while (iter.hasNext()) {
          offSetList.add(iter.next());
        }
      }
    }

    m = Pattern.compile("&DEC(\\d++)").matcher(in);
    if (m.find()) {
      List<Triple<Integer, Integer, Integer>> toRemove = new LinkedList<Triple<Integer, Integer, Integer>>();
      List<Triple<Integer, Integer, Integer>> toAdd = new LinkedList<Triple<Integer, Integer, Integer>>();
      out.setLength(0);
      previousOffSet = 0;
      do {
        String retrivel = m.group();
        StringBuffer tem = new StringBuffer(retrivel);
        m.appendReplacement(out, "" + (Integer.parseInt(m.group(1)) - 1));
        Matcher tt = Pattern.compile("&DEC(\\d++)").matcher(tem);
        StringBuffer o1 = new StringBuffer();
        if (tt.find()) {
          tt.appendReplacement(o1, "" + (Integer.parseInt(m.group(1)) - 1));
          int start = m.start();
          int end = m.end();
          List<Triple<Integer, Integer, Integer>> offSetPairs = new LinkedList<Triple<Integer, Integer, Integer>>();
          Iterator<Triple<Integer, Integer, Integer>> iter = offSetList.iterator();
          previousOffSet = 0;
          boolean ignore = false;
          while (iter.hasNext()) {
            Triple<Integer, Integer, Integer> nextOffSetPair = iter.next();
            if (start <= nextOffSetPair.first && end >= nextOffSetPair.first) {
              offSetPairs.add(nextOffSetPair);
            } else if (nextOffSetPair.first < start) {
              previousOffSet = previousOffSet + nextOffSetPair.third;
            }
            if (nextOffSetPair.first < start && nextOffSetPair.second + nextOffSetPair.first >= end) {
              ignore = true;
            }
          }
          if (ignore) {
            continue;
          }
          if (offSetPairs.size() > 0) {
            int diff = 0;
            iter = offSetPairs.iterator();
            while (iter.hasNext()) {
              Triple<Integer, Integer, Integer> offSetPair = iter.next();
              toRemove.add(offSetPair);
              diff = diff + offSetPair.third;
            }
            end = end - start + diff;
            start = start + previousOffSet;
            previousOffSet = previousOffSet + diff;
            int position = out.length() - o1.length();
            int length = end - o1.length();
            Triple<Integer, Integer, Integer> newOffSetPair = new Triple<Integer, Integer, Integer>(position, o1.length(), length);
            toAdd.add(newOffSetPair);
          } else {
            end = end - start;
            start = start + previousOffSet;
            int position = out.length() - o1.length();
            int length = end - o1.length();
            Triple<Integer, Integer, Integer> newOffSetPair = new Triple<Integer, Integer, Integer>(position, o1.length(), length);
            toAdd.add(newOffSetPair);
          }
          if (!offsetUsed.contains(start)) {
            dates.add(new Triple<String, Integer, Integer>(o1.toString(), start, end));
            offsetUsed.add(start);
          }
        }
      } while (m.find());
      m.appendTail(out);
      in = null;
      //      return(out.toString());
    }
    out = null;

    return dates;
  }

  /** Normalizes all dates in a String 
   *  DEPRECATED - can go away when multilingual version accepted*/
  /*
   * public static Collection<String> getAllDates(CharSequence s) {
   

  	ArrayList<String> dates = new ArrayList<String>();
  	// If it does not contain years or months, return it unchanged
  	if (!monthPattern.matcher(s).find() && !yearPattern.matcher(s).find())
  		return (null);

  	StringBuffer in = new StringBuffer((int) (s.length() * 1.1));

  	// Replace all the months
  	Matcher m = monthPattern.matcher(s);
  	while (m.find()) {
  		int monthNum = D.indexOf(m.group().substring(0, 3),
  				(Object[]) MONTHS) + 1;
  		m
  				.appendReplacement(in, "MONTH" + (monthNum / 10)
  						+ (monthNum % 10));
  	}
  	m.appendTail(in);
  	StringBuffer out = new StringBuffer((int) (in.length() * 1.1));

  	// Apply the patterns
  	for (FindReplace p : patterns) {
  		m = p.pattern.matcher(in);
  		if (m.find()) {
  			out.setLength(0);
  			do {
  				String retrivel = m.group();
  				StringBuffer tem = new StringBuffer(retrivel);
  				m.appendReplacement(out, p.replacement);
  				Matcher tt = p.pattern.matcher(tem);
  				StringBuffer o1 = new StringBuffer();
  				if (tt.find()) {
  					tt.appendReplacement(o1, p.replacement);
  					dates.add(o1.toString());
  				}

  			} while (m.find());
  			m.appendTail(out);
  			StringBuffer temp = out;
  			out = in;
  			in = temp;
  		}
  	}

  	m = Pattern.compile("&DEC(\\d++)").matcher(in);
  	if (m.find()) {
  		out.setLength(0);
  		do {
  			String retrivel = m.group();
  			StringBuffer tem = new StringBuffer(retrivel);
  			m.appendReplacement(out, ""
  					+ (Integer.parseInt(m.group(1)) - 1));
  			Matcher tt = Pattern.compile("&DEC(\\d++)").matcher(tem);
  			StringBuffer o1 = new StringBuffer();
  			if (tt.find()) {
  				tt.appendReplacement(o1, ""
  						+ (Integer.parseInt(m.group(1)) - 1));
  				dates.add(o1.toString());
  			}

  		} while (m.find());
  		m.appendTail(out);
  		in = null;
  		// return(out.toString());
  	}
  	out = null;
  	return dates;
  }  */

  /**
   * Returns the components of the date (year, month, day) in a normalized
   * date string (or null) and writes the start and end position in pos[0] and
   * pos[1]
   */
  public static String[] getDate(CharSequence d, int[] pos) {
    if (d == null) return (null);
    Matcher m = JUSTDATEPATTERN.matcher(d);
    if (!m.find()) {
      m = SIMPLEYEARPATTERN.matcher(d.toString());
      if (!m.find()) return (null);
      pos[0] = m.start();
      pos[1] = m.end();
      return (new String[] { m.group(1), "##", "##" });
    }
    pos[0] = m.start();
    pos[1] = m.end();
    String[] result = new String[] { m.group(1), m.group(2), m.group(3) };
    return (result);
  }

  /**
   * Returns the dates in a normalized date string
   */
  /**
   * Returns the dates in a normalized date string
   */
  public static List<String> getDates(CharSequence d) {
    List<String> preresult = new ArrayList<String>(3);
    Matcher m = DATEPATTERN.matcher(d);
    while (m.find()) {
      preresult.add(m.group());
    }
    List<String> result = new ArrayList<String>(3);
    for (String date : preresult) {
      String[] split = getDate(date);
      if (split.length != 3) {
        split = new String[3];
        for (int i = 0; i < split.length; i++) {
          split[i] = "##";
        }
      }
      // check year does not start with a symbol (due to regular expression)
      if (!split[0].contains("#")) {
        try {
          Integer.parseInt(split[0]);
        } catch (NumberFormatException e) {
          split[0] = "##";
        }
      }
      // A year cannot be more than 4 digits
      if (split[0].length() > 4) return result;

      // checks the month value
      if (!split[1].contains("#")) {
        Integer value = NumberParser.parseInt(split[1]);
        if (value == null || value > 12 || value < 1) {
          split[1] = "##";
        }
      }
      // could check with date
      if (!split[2].contains("#")) {
        Integer value = NumberParser.parseInt(split[2]);
        if (value == null || value > 31 || value < 1) {
          split[2] = "##";
        }
      }

      result.add(newDate(split[0], split[1], split[2]));
    }
    if (result.size() == 0) {
      m = SIMPLEYEARPATTERN.matcher(d);
      while (m.find()) {
        result.add(newDate(m.group(), "##", "##"));
      }
    }
    return (result);
  }

  /**
   * Returns the components of the date (year, month, day) in a normalized
   * date string (or null)
   */
  public static String[] getDate(CharSequence d) {
    return (getDate(d, new int[2]));
  }

  /** Tells whether this string is a normlized date (and nothing else) */
  public static boolean isDate(CharSequence s) {
    return (DATEPATTERN.matcher(s).matches());
  }

  /** Converts a normalized Date to a Calendar */
  public static Calendar asCalendar(int[] date) {
    Calendar c = Calendar.getInstance();
    c.clear();
    int year = date[0];
    if (year < 0) {
      year = -year;
      c.set(Calendar.ERA, GregorianCalendar.BC);
    }
    c.set(Calendar.YEAR, year);
    if (date[1] != Integer.MAX_VALUE) c.set(Calendar.MONTH, date[1]);
    if (date[2] != Integer.MAX_VALUE) c.set(Calendar.DAY_OF_MONTH, date[2]);
    return (c);
  }

  public static Calendar asCalendar(String[] date) {
    return (asCalendar(asInts(date)));
  }

  public static Calendar asCalendar(String date) {
    return (asCalendar(asInts(getDate(date))));
  }

  /** TRUE if the first String matches the second */
  protected static boolean matches(String a, String b) {
    if (a.length() != b.length()) return (false);
    for (int i = 0; i < a.length(); i++) {
      if (a.charAt(i) == '#' || a.charAt(i) == b.charAt(i)) continue;
      return (false);
    }
    return (true);
  }

  /**
   * Parses the normalized date into ints, putting Integer.MAX_VALUE for '#'.
   * This looses partial information in the year!! (e.g. 18## -> ####)
   */
  public static int[] asInts(String[] yearMonthDay) {
    int[] result = new int[3];

    for (int i = 0; i < 3; i++) {
      if (yearMonthDay == null)// changed because yearMonthDay is
      // ####-##-##
      {
        result[i] = Integer.MAX_VALUE;
      } else if (yearMonthDay[i].contains("#")) result[i] = Integer.MAX_VALUE;
      else result[i] = Integer.parseInt(yearMonthDay[i]);
    }
    return (result);
  }

  /**
   * TRUE if the first date is earlier than the second. This does not define a
   * total order on dates, as,e.g., 1800-##-## is neither earlier nor later
   * than 1800-05-##
   */
  public static boolean isEarlier(int[] date1, int[] date2) {
    for (int i = 0; i < 3; i++) {
      if (date1[i] == Integer.MAX_VALUE || date2[i] == Integer.MAX_VALUE) return (false);
      if (date1[i] > date2[i]) return (false);
      if (date1[i] < date2[i]) return (true);
    }
    return (false);
  }

  /**
   * TRUE if the first date is earlier than the second. This does not define a
   * total order on dates, as,e.g., 1800-##-## is neither earlier nor later
   * than 1800-05-##
   */
  public static boolean isEarlier(String[] date1, String[] date2) {
    return (isEarlier(asInts(date1), asInts(date2))); // TODO does not work for wildcards in years
  }

  public static boolean isEarlier(String date1, String date2) {
    return (isEarlier(getDate(date1), getDate(date2)));
  }

  /**
   * TRUE if the first date includes the second, e.g., 1800-##-## includes
   * 1800-05-##
   */
  public static boolean includes(int[] date1, int[] date2) {
    for (int i = 0; i < 3; i++) {
      if (date1[i] == Integer.MAX_VALUE) return (true);
      if (date1[i] != date2[i]) return (false);
    }
    return (true);
  }

  public static boolean includes(String[] date1, String[] date2) {
    for (int i = 0; i < 3; i++) {
      if (!matches(date1[i], date2[i])) return (false);
    }
    return (true);
  }

  public static boolean includes(String date1, String date2) {
    return (includes(getDate(date1), getDate(date2)));
  }

  /** TRUE if the dates are exactly equal, including '#' */
  public static boolean equal(int[] date1, int[] date2) {
    for (int i = 0; i < 3; i++) {
      if (date1[i] != date2[i]) return (false);
    }
    return (true);
  }

  public static boolean equal(String[] date1, String[] date2) {
    if (date1 == null || date2 == null) // changed because of ####-##-##
      return false;

    for (int i = 0; i < 3; i++) {
      if (!date1[i].equals(date2[i])) return (false);
    }
    return (true);
  }

  public static boolean equal(String date1, String date2) {
    return (equal(getDate(date1), getDate(date2)));
  }

  /** TRUE if the dates are disjoint, i.e. none includes the other */
  public static boolean disjoint(int[] date1, int[] date2) {
    return (!includes(date1, date2) && !includes(date2, date1));
  }

  public static boolean disjoint(String[] date1, String[] date2) {
    return (!includes(date1, date2) && !includes(date2, date1));
  }

  public static boolean disjoint(String date1, String date2) {
    return (!includes(date1, date2) && !includes(date2, date1));
  }

  /** Test routine */
  public static void main(String[] argv) throws Exception {
    System.out.println(NumberFormatter.ISOtime(DateParser.asCalendar(DateParser.normalize("November 24th 1998"))));
    System.out.println("Enter a string containing a date expression and hit ENTER. Press CTRL+C to abort");
    while (true) {
      String in = D.r();
      System.out.println(normalize(in));
      System.out.println(getDates(normalize(in)));
      //      System.out.println(NumberFormatter.ISOtime(asCalendar(normalize(in))));
    }
  }

}
