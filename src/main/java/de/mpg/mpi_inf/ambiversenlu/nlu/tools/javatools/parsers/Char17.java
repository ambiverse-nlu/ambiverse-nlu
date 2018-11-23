package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.IntHashMap;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.IntKeyMap;

import java.util.Arrays;

/**
 This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
 It is licensed under the Creative Commons Attribution License
 (see http://creativecommons.org/licenses/by/3.0) by
 the YAGO-NAGA team (see http://mpii.de/yago-naga).
 <p>
 This class is the improved version of the original Char class,
 which takes into account that Java 1.7 performs a copy for substring()
 <p>
 This class provides static methods to <I>decode, encode</I> and <I>normalize</I> Strings.<BR>
 <B>Decoding</B> converts the following codes to Java 16-bit characters (<TT>char</TT>):
 <UL>
 <LI>all HTML ampersand codes (like &amp;nbsp;) as specified by the W3C
 <LI>all backslash codes (like \ b) as specified by the Java language specification
 <LI>all percentage codes (like %2C) as used in URLs and E-Mails
 <LI>all UTF-8 codes (like ī) as specified in Wikipedia
 </UL>
 <P>
 <B>Encoding</B> is the inverse operation. It takes a Java 16-bit character (<TT>char</TT>) and
 outputs its encoding in HTML, as a backslash code, as a percentage code or in UTF8.
 <P>
 <B>Normalization</B> converts the following Unicode characters (Java 16-bit <TT>char</TT>s)
 to ASCII-characters in the range 0x20-0x7F:
 <UL>
 <LI>all ASCII control characters (0x00-0x1F)
 <LI>all Latin-1 characters (0x80-0xFF) to the closest transliteration
 <LI>all Latin Extended-A characters (0x100-0x17F) to the closest transliteration
 <LI>all Greek characters (0x374-0x3D6) to the closest transliteration as specified in Wikipedia
 <LI>all General-Punctuation characters (0x2016-0x2055) to the closest ASCII punctuation
 <LI>most mathematical symbols (in the range of 0x2000) to the common program code identifier or text
 <LI>all ligatures (0xFB00-0xFB06, the nasty things you get when you copy/paste from PDFs) to
 the separate characters
 </UL>
 */
public class Char17 {

  /** String returned by the default implementation of defaultNormalizer, "[?]"*/
  public static String UNKNOWN = "[?]";

  /** Maps a special character to a HTML ampersand sequence */
  public static IntKeyMap<String> charToAmpersand = new IntKeyMap<String>('&', "&amp;", '\'', "&apos;", '<', "&lt;", '>', "&gt;", '"', "&quot;");

  /** Maps a special character to a backslash sequence */
  public static IntKeyMap<String> charToBackslash = new IntKeyMap<String>('\\', "\\\\", '\n', "\\n");

  /** Maps HTML ampersand sequences to strings */
  public static IntHashMap<String> ampersandMap = new IntHashMap<String>(Arrays
      .asList((Object) "nbsp", (char) 160, "iexcl", (char) 161, "cent", (char) 162, "pound", (char) 163, "curren", (char) 164, "yen", (char) 165,
          "brvbar", (char) 166, "sect", (char) 167, "uml", (char) 168, "copy", (char) 169, "ordf", (char) 170, "laquo", (char) 171, "not", (char) 172,
          "shy", (char) 173, "reg", (char) 174, "macr", (char) 175, "deg", (char) 176, "plusmn", (char) 177, "sup2", (char) 178, "sup3", (char) 179,
          "acute", (char) 180, "micro", (char) 181, "para", (char) 182, "middot", (char) 183, "cedil", (char) 184, "sup1", (char) 185, "ordm",
          (char) 186, "raquo", (char) 187, "frac14", (char) 188, "frac12", (char) 189, "frac34", (char) 190, "iquest", (char) 191, "Agrave",
          (char) 192, "Aacute", (char) 193, "Acirc", (char) 194, "Atilde", (char) 195, "Auml", (char) 196, "Aring", (char) 197, "AElig", (char) 198,
          "Ccedil", (char) 199, "Egrave", (char) 200, "Eacute", (char) 201, "Ecirc", (char) 202, "Euml", (char) 203, "Igrave", (char) 204, "Iacute",
          (char) 205, "Icirc", (char) 206, "Iuml", (char) 207, "ETH", (char) 208, "Ntilde", (char) 209, "Ograve", (char) 210, "Oacute", (char) 211,
          "Ocirc", (char) 212, "Otilde", (char) 213, "Ouml", (char) 214, "times", (char) 215, "Oslash", (char) 216, "Ugrave", (char) 217, "Uacute",
          (char) 218, "Ucirc", (char) 219, "Uuml", (char) 220, "Yacute", (char) 221, "THORN", (char) 222, "szlig", (char) 223, "agrave", (char) 224,
          "aacute", (char) 225, "acirc", (char) 226, "atilde", (char) 227, "auml", (char) 228, "aring", (char) 229, "aelig", (char) 230, "ccedil",
          (char) 231, "egrave", (char) 232, "eacute", (char) 233, "ecirc", (char) 234, "euml", (char) 235, "igrave", (char) 236, "iacute", (char) 237,
          "icirc", (char) 238, "iuml", (char) 239, "eth", (char) 240, "ntilde", (char) 241, "ograve", (char) 242, "oacute", (char) 243, "ocirc",
          (char) 244, "otilde", (char) 245, "ouml", (char) 246, "divide", (char) 247, "oslash", (char) 248, "ugrave", (char) 249, "uacute",
          (char) 250, "ucirc", (char) 251, "uuml", (char) 252, "yacute", (char) 253, "thorn", (char) 254, "yuml", (char) 255, "fnof", (char) 402,
          "Alpha", (char) 913, "Beta", (char) 914, "Gamma", (char) 915, "Delta", (char) 916, "Epsilon", (char) 917, "Zeta", (char) 918, "Eta",
          (char) 919, "Theta", (char) 920, "Iota", (char) 921, "Kappa", (char) 922, "Lambda", (char) 923, "Mu", (char) 924, "Nu", (char) 925, "Xi",
          (char) 926, "Omicron", (char) 927, "Pi", (char) 928, "Rho", (char) 929, "Sigma", (char) 931, "Tau", (char) 932, "Upsilon", (char) 933,
          "Phi", (char) 934, "Chi", (char) 935, "Psi", (char) 936, "Omega", (char) 937, "alpha", (char) 945, "beta", (char) 946, "gamma", (char) 947,
          "delta", (char) 948, "epsilon", (char) 949, "zeta", (char) 950, "eta", (char) 951, "theta", (char) 952, "iota", (char) 953, "kappa",
          (char) 954, "lambda", (char) 955, "mu", (char) 956, "nu", (char) 957, "xi", (char) 958, "omicron", (char) 959, "pi", (char) 960, "rho",
          (char) 961, "sigmaf", (char) 962, "sigma", (char) 963, "tau", (char) 964, "upsilon", (char) 965, "phi", (char) 966, "chi", (char) 967,
          "psi", (char) 968, "omega", (char) 969, "thetasym", (char) 977, "upsih", (char) 978, "piv", (char) 982, "bull", (char) 8226, "hellip",
          (char) 8230, "prime", (char) 8242, "Prime", (char) 8243, "oline", (char) 8254, "frasl", (char) 8260, "weierp", (char) 8472, "image",
          (char) 8465, "real", (char) 8476, "trade", (char) 8482, "alefsym", (char) 8501, "larr", (char) 8592, "uarr", (char) 8593, "rarr",
          (char) 8594, "darr", (char) 8595, "harr", (char) 8596, "crarr", (char) 8629, "lArr", (char) 8656, "uArr", (char) 8657, "rArr", (char) 8658,
          "dArr", (char) 8659, "hArr", (char) 8660, "forall", (char) 8704, "part", (char) 8706, "exist", (char) 8707, "empty", (char) 8709, "nabla",
          (char) 8711, "isin", (char) 8712, "notin", (char) 8713, "ni", (char) 8715, "prod", (char) 8719, "sum", (char) 8721, "minus", (char) 8722,
          "lowast", (char) 8727, "radic", (char) 8730, "prop", (char) 8733, "infin", (char) 8734, "ang", (char) 8736, "and", (char) 8743, "or",
          (char) 8744, "cap", (char) 8745, "cup", (char) 8746, "int", (char) 8747, "there4", (char) 8756, "sim", (char) 8764, "cong", (char) 8773,
          "asymp", (char) 8776, "ne", (char) 8800, "equiv", (char) 8801, "le", (char) 8804, "ge", (char) 8805, "sub", (char) 8834, "sup", (char) 8835,
          "nsub", (char) 8836, "sube", (char) 8838, "supe", (char) 8839, "oplus", (char) 8853, "otimes", (char) 8855, "perp", (char) 8869, "sdot",
          (char) 8901, "lceil", (char) 8968, "rceil", (char) 8969, "lfloor", (char) 8970, "rfloor", (char) 8971, "lang", (char) 9001, "rang",
          (char) 9002, "loz", (char) 9674, "spades", (char) 9824, "clubs", (char) 9827, "hearts", (char) 9829, "diams", (char) 9830, "quot",
          (char) 34, "amp", (char) 38, "lt", (char) 60, "gt", (char) 62, "OElig", (char) 338, "oelig", (char) 339, "Scaron", (char) 352, "scaron",
          (char) 353, "Yuml", (char) 376, "circ", (char) 710, "tilde", (char) 732, "ensp", (char) 8194, "emsp", (char) 8195, "thinsp", (char) 8201,
          "zwnj", (char) 8204, "zwj", (char) 8205, "lrm", (char) 8206, "rlm", (char) 8207, "ndash", (char) 8211, // 0x2013
          "mdash", (char) 8212, "lsquo", (char) 8216, "rsquo", (char) 8217, "sbquo", (char) 8218, "ldquo", (char) 8220, "rdquo", (char) 8221, "bdquo",
          (char) 8222, "dagger", (char) 8224, "Dagger", (char) 8225, "permil", (char) 8240, "lsaquo", (char) 8249, "rsaquo", (char) 8250, "euro",
          (char) 8364, "apos", '\''));

  /** Maps characters to normalizations */
  public static IntKeyMap<String> normalizeMap = new IntKeyMap<String>(
      // ASCII
      (char) 7, "BEEP", (char) 9, " ", (char) 10, "\n",

      // Latin-1
      (char) 160, " ", (char) 161, "!", (char) 162, "cent", (char) 163, "pound", (char) 164, "currency", (char) 165, "yen", (char) 166, "|",
      (char) 167, "/", (char) 169, "(c)", (char) 170, "^a", (char) 171, "\"", (char) 172, "~", (char) 173, "", (char) 174, "(R)", (char) 176,
      "degree", (char) 177, "+/-", (char) 178, "^2", (char) 179, "^3", (char) 180, "'", (char) 181, "mu", (char) 182, "P", (char) 183, ".",
      (char) 184, ",", (char) 185, "^1", (char) 186, "^o", (char) 187, "\"", (char) 188, "1/4", (char) 189, "1/2", (char) 190, "3/4", (char) 191, "?",
      (char) 0xC4, "Ae", (char) 0xD6, "Oe", (char) 0xDC, "Ue", (char) 0xDF, "ss", (char) 0xC6, "Ae", (char) 0xC7, "C", (char) 0xD0, "D", (char) 0xD1,
      "N", (char) 0xD7, "x", (char) 0xDD, "Y", (char) 0xDE, "b", (char) 0xF7, "/", (char) 0xFF, "y",

      // Latin Extended-A
      (char) 0x132, "IJ", (char) 0x134, "J", (char) 0x170, "Ue", (char) 0x174, "W", (char) 0x17F, "f",

      // Greek
      (char) 0x374, "'", (char) 0x375, ",", (char) 0x37A, ",", (char) 0x37E, ";", (char) 0x384, "'", (char) 0x385, "'", (char) 0x386, "A",
      (char) 0x387, ".", (char) 0x388, "E", (char) 0x380, "I", (char) 0x38C, "O", (char) 0x38E, "Y", (char) 0x38F, "O", (char) 0x390, "i", (char) 215,
      "*", (char) 913, "A", (char) 914, "B", (char) 915, "G", (char) 916, "D", (char) 917, "E", (char) 918, "Z", (char) 919, "E", (char) 920, "Th",
      (char) 921, "I", (char) 922, "K", (char) 923, "L", (char) 924, "M", (char) 925, "N", (char) 926, "X", (char) 927, "O", (char) 928, "P",
      (char) 929, "R", (char) 931, "S", (char) 932, "T", (char) 933, "Y", (char) 934, "Ph", (char) 935, "Ch", (char) 936, "Ps", (char) 937, "O",
      (char) 977, "th", (char) 978, "y", (char) 982, "pi",

      // General Punctuation
      (char) 0x2013, "-", (char) 0x2016, "||", (char) 0x2017, "_", (char) 0x2020, "+", (char) 0x2021, "++", (char) 0x2022, "*", (char) 0x2023, "*",
      (char) 0x2024, ".", (char) 0x2025, "..", (char) 0x2026, "...", (char) 0x2027, ".", (char) 0x2028, "\n", (char) 0x2030, "/1000", (char) 0x2031,
      "/10000", (char) 0x2032, "'", (char) 0x2033, "''", (char) 0x2034, "'''", (char) 0x2035, "'", (char) 0x2036, "''", (char) 0x2037, "'''",
      (char) 0x2038, "^", (char) 0x2039, "\"", (char) 0x203A, "\"", (char) 0x203B, "*", (char) 0x203C, "!!", (char) 0x203D, "?!", (char) 0x2041, ",",
      (char) 0x2042, "***", (char) 0x2043, "-", (char) 0x2044, "/", (char) 0x2045, "[", (char) 0x2046, "]", (char) 0x2047, "??", (char) 0x2048, "?!",
      (char) 0x2049, "!?", (char) 0x204A, "-", (char) 0x204B, "P", (char) 0x204C, "<", (char) 0x204D, ">", (char) 0x204F, ";", (char) 0x2050, "-",
      (char) 0x2051, "**", (char) 0x2052, "./.", (char) 0x2053, "~", (char) 0x2054, "_", (char) 0x2055, "_",

      // Mathematical symbols
      (char) 8465, "I", (char) 8476, "R", (char) 8482, "(TM)", (char) 8501, "a", (char) 8592, "<-", (char) 8593, "^", (char) 8594, "->", (char) 8595,
      "v", (char) 8596, "<->", (char) 8629, "<-'", (char) 8656, "<=", (char) 8657, "^", (char) 8658, "=>", (char) 8659, "v", (char) 8660, "<=>",
      (char) 8704, "FOR ALL", (char) 8706, "d", (char) 8707, "EXIST", (char) 8709, "{}", (char) 8712, "IN", (char) 8713, "NOT IN", (char) 8715,
      "CONTAINS", (char) 8719, "PRODUCT", (char) 8721, "SUM", (char) 8722, "-", (char) 8727, "*", (char) 8730, "SQRT", (char) 8733, "~", (char) 8734,
      "INF", (char) 8736, "angle", (char) 8743, "&", (char) 8744, "|", (char) 8745, "INTERSECTION", (char) 8746, "UNION", (char) 8747, "INTEGRAL",
      (char) 8756, "=>", (char) 8764, "~", (char) 8773, "~=", (char) 8776, "~=", (char) 8800, "!=", (char) 8801, "==", (char) 8804, "=<", (char) 8805,
      ">=", (char) 8834, "SUBSET OF", (char) 8835, "SUPERSET OF", (char) 8836, "NOT SUBSET OF", (char) 8838, "SUBSET OR EQUAL", (char) 8839,
      "SUPERSET OR EQUAL", (char) 8853, "(+)", (char) 8855, "(*)", (char) 8869, "_|_", (char) 8901, "*", (char) 8364, "EUR",

      // Ligatures
      (char) 0xFB00, "ff", (char) 0xFB01, "fi", (char) 0xFB02, "fl", (char) 0xFB03, "ffi", (char) 0xFB04, "ffl", (char) 0xFB05, "ft", (char) 0xFB06,
      "st");

  /** Normalizes a character to a String of characters in the range 0x20-0x7F.
   *  Returns a String, because some characters are
   * normalized to multiple characters (e.g. umlauts) and
   * some characters are normalized to zero characters (e.g. special Unicode space chars).
   * Returns null for the EndOfFile character -1 */
  public static String normalize(int c) {
    // EOF
    if (c == -1) return (null);

    // ASCII chars
    if (c >= ' ' && c <= 128) return ("" + (char) c);

    // Upper case
    boolean u = Character.isUpperCase(c);
    char cu = (char) Character.toUpperCase(c);

    // Check map
    if (normalizeMap.get(cu) != null) return (u ? normalizeMap.get(cu) : normalizeMap.get(cu).toLowerCase());

    // ASCII
    if (c < ' ') return ("");

    // Latin-1
    if (cu >= 0xC0 && cu <= 0xC5) return (u ? "A" : "a");
    if (cu >= 0xC8 && cu <= 0xCB) return (u ? "E" : "e");
    if (cu >= 0xCC && cu <= 0xCF) return (u ? "I" : "i");
    if (cu >= 0xD2 && cu <= 0xD8) return (u ? "O" : "o");
    if (cu >= 0x80 && cu <= 0xA0) return (" ");

    // Latin Extended-A
    if (cu >= 0x100 && cu <= 0x105) return (u ? "A" : "a");
    if (cu >= 0x106 && cu <= 0x10D) return (u ? "C" : "c");
    if (cu >= 0x10E && cu <= 0x111) return (u ? "D" : "d");
    if (cu >= 0x112 && cu <= 0x11B) return (u ? "E" : "e");
    if (cu >= 0x11C && cu <= 0x123) return (u ? "G" : "g");
    if (cu >= 0x124 && cu <= 0x127) return (u ? "H" : "h");
    if (cu >= 0x128 && cu <= 0x131) return (u ? "I" : "i");
    if (cu >= 0x136 && cu <= 0x138) return (u ? "K" : "k");
    if (cu >= 0x139 && cu <= 0x142) return (u ? "L" : "l");
    if (cu >= 0x143 && cu <= 0x14B) return (u ? "N" : "n");
    if (cu >= 0x14C && cu <= 0x14F) return (u ? "O" : "o");
    if (cu >= 0x150 && cu <= 0x153) return (u ? "Oe" : "oe");
    if (cu >= 0x156 && cu <= 0x159) return (u ? "R" : "r");
    if (cu >= 0x15A && cu <= 0x161) return (u ? "S" : "s");
    if (cu >= 0x161 && cu <= 0x167) return (u ? "T" : "t");
    if (cu >= 0x176 && cu <= 0x178) return (u ? "Y" : "y");
    if (cu >= 0x179 && cu <= 0x17E) return (u ? "Z" : "z");

    // General Punctuation
    if (cu >= 0x2000 && cu <= 0x200A) return (" ");
    if (cu >= 0x200B && cu <= 0x200F) return ("");
    if (cu >= 0x2010 && cu <= 0x2015) return ("--");
    if (cu >= 0x2018 && cu <= 0x201B) return ("'");
    if (cu >= 0x201C && cu <= 0x201F) return ("\"");
    if (cu >= 0x2029 && cu <= 0x202F) return (" ");
    if (cu >= 0x203E && cu <= 0x2040) return ("-");
    if (cu >= 0x2056 && cu <= 0x205E) return (".");

    return (UNKNOWN);
  }

  /** Decodes percentage characters of the form "%xx" in a string. Also does UTF8 decoding.*/
  public static String decodePercentage(String string) {
    int pos = string.indexOf('%');
    if (pos == -1) return (string);
    StringBuilder result = new StringBuilder(string.length());
    for (int i = 0; i < string.length(); i++) {
      if (string.charAt(i) != '%' || i > string.length() - 3) {
        result.append(string.charAt(i));
        continue;
      }
      try {
        result.append((char) Integer.parseInt(string.substring(i + 1, i + 3), 16));
        i += 2;
      } catch (Exception e) {
        // Illegal percentage code, just keep it literally
        result.append('%');
      }
    }
    return (decodeUtf8(result.toString()));
  }

  /** Decodes an HTML ampersand code such as "& amp", returns -1 in case of failure*/
  public static int decodeAmpersandChar(String b) {
    // Get just the code portion
    if (b.startsWith("&")) b = b.substring(1);
    if (b.endsWith(";")) b = cutLast(b);
    // Hexadecimal characters
    if (b.startsWith("#x")) {
      try {
        return (((char) Integer.parseInt(b.substring(2), 16)));
      } catch (Exception e) {
        // Invalid char
        return (-1);
      }
    }
    // Decimal characters
    if (b.startsWith("#")) {
      try {
        return (((char) Integer.parseInt(b.substring(1))));
      } catch (Exception e) {
        // Invalid char
        return (-1);
      }
    }
    // Others
    if (ampersandMap.get(b) != -1) {
      return (ampersandMap.get(b));
    } else if (ampersandMap.get(b.toLowerCase()) != -1) {
      return (ampersandMap.get(b.toLowerCase()));
    }
    // Invalid char
    return (-1);
  }

  /** Eats an HTML ampersand code from a List of Characters*/
  public static String decodeAmpersand(String string) {
    int pos = string.indexOf('&');
    if (pos == -1) return (string);
    StringBuilder result = new StringBuilder(string.length());
    for (int i = 0; i < string.length(); i++) {
      if (string.charAt(i) != '&' || i > string.length() - 2) {
        result.append(string.charAt(i));
        continue;
      }
      // Seek to ';'
      // We also accept spaces and the end of the String as a delimiter
      int end = i;
      while (end < string.length() && !Character.isSpaceChar(string.charAt(end)) && string.charAt(end) != ';') end++;
      String b = string.substring(i + 1, end);
      if (end < string.length() && string.charAt(end) == ';') end++;
      int c = decodeAmpersandChar(b);
      if (c == -1) {
        result.append('&');
      } else {
        result.append((char) c);
        i = end - 1;
      }
    }
    return (result.toString());
  }

  /** Tells from the first UTF-8 code character how long the code is.
   * Returns -1 if the character is not an UTF-8 code start.
   * Returns 1 if the character is ASCII<128*/
  public static int Utf8Length(char c) {
    // 0xxx xxxx
    if ((c & 0x80) == 0x00) return (1);
    // 110x xxxx
    if ((c & 0xE0) == 0xC0) return (2);
    // 1110 xxxx
    if ((c & 0xF0) == 0xE0) return (3);
    // 1111 0xxx
    if ((c & 0xF8) == 0xF0) return (4);
    return (-1);
  }

  /** Decodes UTF8 characters in a string. There is also a built-in way in Java that converts
   * UTF8 to characters and back, but it does not work with all characters. */
  public static String decodeUtf8(String string) {
    if (string.isEmpty()) return (string);
    StringBuilder result = new StringBuilder(string.length());
    for (int i = 0; i < string.length(); i++) {
      int len = Utf8Length(string.charAt(i));
      if (string.length() - i >= len) switch (len) {
        case 2:
          if ((string.charAt(i + 1) & 0xC0) != 0x80) break;
          result.append((char) (((string.charAt(i) & 0x1F) << 6) + (string.charAt(i + 1) & 0x3F)));
          i++;
          continue;
        case 3:
          if ((string.charAt(i + 1) & 0xC0) != 0x80 || (string.charAt(i + 2) & 0xC0) != 0x80) break;
          result.append((char) (((string.charAt(i + 0) & 0x0F) << 12) + ((string.charAt(i + 1) & 0x3F) << 6) + ((string.charAt(i + 2) & 0x3F))));
          i += 2;
          continue;
        case 4:
          if ((string.charAt(i + 1) & 0xC0) != 0x80 || (string.charAt(i + 2) & 0xC0) != 0x80 || (string.charAt(i + 3) & 0xC0) != 0x80) break;
          result.append(
              (char) (((string.charAt(i + 0) & 0x07) << 18) + ((string.charAt(i + 1) & 0x3F) << 12) + ((string.charAt(i + 2) & 0x3F) << 6) + ((
                  string.charAt(i + 3) & 0x3F))));
          i += 3;
          continue;
      }
      result.append(string.charAt(i));
    }
    return (result.toString());
  }

  /** Used for encoding selected characters*/
  public static interface Legal {

    public boolean isLegal(char c);
  }

  /** Encodes with backslash all illegal characters*/
  public static String encodeBackslash(CharSequence s, Legal legal) {
    StringBuilder b = new StringBuilder((int) (s.length() * 1.5));
    for (int i = 0; i < s.length(); i++) {
      if (legal.isLegal(s.charAt(i)) && s.charAt(i) != '\\') {
        b.append(s.charAt(i));
      } else {
        if (charToBackslash.containsKey(s.charAt(i))) {
          b.append(charToBackslash.get(s.charAt(i)));
          continue;
        }
        b.append("\\u");
        String hex = Integer.toHexString(s.charAt(i));
        for (int j = 0; j < 4 - hex.length(); j++)
          b.append('0');
        b.append(hex);
      }
    }
    return (b.toString());
  }

  /** Decodes a backslash sequence*/
  public static String decodeBackslash(String string) {
    int pos = string.indexOf('\\');
    if (pos == -1) return (string);
    StringBuilder result = new StringBuilder(string.length());
    for (int i = 0; i < string.length(); i++) {
      if (string.charAt(i) != '\\' || i + 1 >= string.length()) {
        result.append(string.charAt(i));
        continue;
      }
      char nextChar = string.charAt(i + 1);
      switch (nextChar) {
        case 'u': // Unicodes BS u XXXX
          try {
            result.append((char) Integer.parseInt(string.substring(i + 2, i + 6), 16));
            i += 5;
          } catch (Exception e) {
            // Invalid char
            result.append('\\');
          }
          continue;
        case 'b':
          result.append((char) 8);
          i++;
          continue;
        case 't':
          result.append((char) 9);
          i++;
          continue;
        case 'n':
          result.append((char) 10);
          i++;
          continue;
        case 'f':
          result.append((char) 12);
          i++;
          continue;
        case 'r':
          result.append((char) 13);
          i++;
          continue;
        case '\\':
          result.append('\\');
          i++;
          continue;
        case '"':
          result.append('"');
          i++;
          continue;
        case '\'':
          result.append('\'');
          i++;
          continue;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7': // Octal codes
          int end = i + 1;
          while (end < string.length() && string.charAt(end) >= '0' && string.charAt(end) <= '8') end++;
          try {
            result.append((char) Integer.parseInt(string.substring(i + 1, end), 8));
            i = end - 1;
          } catch (Exception e) {
            // Wrong code
            result.append('\\');
          }
          continue;
        default:
          result.append('\\');
          continue;
      }
    }
    return (result.toString());
  }

  /** Replaces all codes in a String by the 16 bit Unicode characters */
  public static String decode(String string) {
    return (decodeAmpersand(decodeUtf8(decodeBackslash(decodePercentage(string)))));
  }

  /** Encodes a character to UTF8*/
  public static String encodeUTF8(char c) {
    if (c <= 0x7F) return ("" + (char) c);
    else if (c <= 0x7FF) return ("" + (char) (0xC0 + ((c >> 6) & 0x1F)) + (char) (0x80 + (c & 0x3F)));
    else return ("" + (char) (0xE0 + ((c >> 12) & 0x0F)) + (char) (0x80 + ((c >> 6) & 0x3F)) + (char) (0x80 + (c & 0x3F)));
  }

  /** Encodes a string to UTF8*/
  public static String encodeUTF8(String string) {
    StringBuilder result = new StringBuilder(string.length() * 12 / 10);
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      result.append(encodeUTF8(c));
    }
    return (result.toString());
  }

  /** Encodes non-legal characters to Ampersand codes*/
  public static String encodeAmpersand(String string, Legal legal) {
    StringBuilder result = new StringBuilder(string.length() * 12 / 10);
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if (legal.isLegal(c) && c != '&') {
        result.append(c);
        continue;
      }
      String s = charToAmpersand.get(c);
      if (s != null) {
        result.append(s);
        continue;
      }
      result.append("&#" + ((int) c) + ";");
    }
    return (result.toString());
  }

  /** Encodes a string into Percentage codes. Also does UTF8 encoding.*/
  public static String encodePercentage(String string, Legal legal) {
    string = encodeUTF8(string);
    StringBuilder result = new StringBuilder(string.length() * 12 / 10);
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if (legal.isLegal(c) && c != '%') {
        result.append(c);
        continue;
      }
      if (c < 16) result.append("%0" + Integer.toHexString(c).toUpperCase());
      else result.append("%" + Integer.toHexString(c).toUpperCase());
    }
    return (result.toString());
  }

  /**
   * Encodes a String with reserved XML characters into a valid xml string for attributes.
   * @param str
   * @return
   */
  public static String encodeXmlAttribute(String str) {
    if (str == null) return null;
    int len = str.length();
    if (len == 0) return str;
    StringBuffer encoded = new StringBuffer();
    for (int i = 0; i < len; i++) {
      char c = str.charAt(i);
      if (c == '<') encoded.append("&lt;");
      else if (c == '\"') encoded.append("&quot;");
      else if (c == '>') encoded.append("&gt;");
      else if (c == '\'') encoded.append("&apos;");
      else if (c == '&') encoded.append("&amp;");
      else encoded.append(c);
    }
    return encoded.toString();
  }

  /** Returns an HTML-String of the String */
  public static String toHTML(String s) {
    return (Char17.encodeAmpersand(s, alphaNumericAndSpace).replace("&#10;", "<BR>"));
  }

  /** Replaces illegal characters in the string by hex codes (cannot be undone)*/
  public static String encodeHex(String s, Legal legal) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (legal.isLegal(c)) result.append(c);
      else result.append(Integer.toHexString(s.charAt(i)).toUpperCase());
    }
    return (result.toString());
  }

  /** Tells whether a char is in a range*/
  public static boolean in(char c, char a, char b) {
    return (c >= a && c <= b);
  }

  /** Tells whether a char is in a string*/
  public static boolean in(char c, String s) {
    return (s.indexOf(c) != -1);
  }

  /** Tells whether a char is alphanumeric in the sense of URIs*/
  public static boolean isAlphanumeric(char c) {
    return (in(c, 'a', 'z') || in(c, 'A', 'Z') || in(c, '0', '9'));
  }

  /** Tells whether a char is reserved in the sense of URIs*/
  public static boolean isReserved(char c) {
    return (isSubDelim(c) || isGenDelim(c));
  }

  /** Tells whether a char is unreserved in the sense of URIs (not the same as !reserved)*/
  public static boolean isUnreserved(char c) {
    return (isAlphanumeric(c) || in(c, "-._~"));
  }

  /** Tells whether a string is escaped in the sense of URIs*/
  public static boolean isEscaped(String s) {
    return (s.matches("%[0-9A-Fa-f]{2}"));
  }

  /** Tells whether a char is a sub-delimiter in the sense of URIs*/
  public static boolean isSubDelim(char c) {
    return (in(c, "!$&'()*+,="));
  }

  /** Tells whether a char is a general delimiter in the sense of URIs*/
  public static boolean isGenDelim(char c) {
    return (in(c, ":/?#[]@"));
  }

  /** Tells whether a char is a valid path component in the sense of URIs*/
  public static boolean isPchar(char c) {
    return (isUnreserved(c) || isSubDelim(c) || in(c, "@"));
  }

  /** Legal path components in the sense of URIs*/
  public static final Legal uriPathComponent = new Legal() {

    public boolean isLegal(char c) {
      return isPchar(c);
    }
  };

  /** True for ASCII alphanumeric and space*/
  public static final Legal alphaNumericAndSpace = new Legal() {

    public boolean isLegal(char c) {
      return isAlphanumeric(c) || c == ' ';
    }
  };

  /** Encodes a char to percentage code, if it is not a path character in the sense of URIs*/
  public static String encodeURIPathComponent(String s) {
    return (encodePercentage(s, uriPathComponent));
  }

  /** TRUE for XML path components*/
  public static final Legal xmlPathComponent = new Legal() {

    public boolean isLegal(char c) {
      return isPchar(c) && c != '&' && c != '"';
    }
  };

  /** Encodes a char to percentage code, if it is not a path character in the sense of XMLs*/
  public static String encodeURIPathComponentXML(String string) {
    return (encodePercentage(string, xmlPathComponent));
  }

  /** Decodes a URI path component*/
  public static String decodeURIPathComponent(String s) {
    return (Char17.decodePercentage(s));
  }

  /** Decodes all codes in a String and normalizes all chars */
  public static String decodeAndNormalize(String s) {
    return (normalize(decode(s)));
  }

  /** Normalizes all chars in a String to characters 0x20-0x7F */
  public static String normalize(String s) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < s.length(); i++)
      b.append(normalize(s.charAt(i)));
    return (b.toString());
  }

  /** Returns the last character of a String or 0*/
  public static char last(CharSequence s) {
    return (s.length() == 0 ? (char) 0 : s.charAt(s.length() - 1));
  }

  /** Returns the String without the last character */
  public static String cutLast(String s) {
    return (s.length() == 0 ? "" : s.substring(0, s.length() - 1));
  }

  /** Cuts the last character */
  public static StringBuilder cutLast(StringBuilder s) {
    s.setLength(s.length() - 1);
    return (s);
  }

  /** Upcases the first character in a String*/
  public static String upCaseFirst(String s) {
    if (s == null || s.length() == 0) return (s);
    return (Character.toUpperCase(s.charAt(0)) + s.substring(1));
  }

  /** Lowcases the first character in a String*/
  public static String lowCaseFirst(String s) {
    if (s == null || s.length() == 0) return (s);
    return (Character.toLowerCase(s.charAt(0)) + s.substring(1));
  }

  /** Returns a string of the given length, fills with spaces if necessary */
  public static CharSequence truncate(CharSequence s, int len) {
    if (s.length() == len) return (s);
    if (s.length() > len) return (s.subSequence(0, len));
    StringBuilder result = new StringBuilder(s);
    while (result.length() < len) result.append(' ');
    return (result);
  }

  /** Capitalizes words and lowercases the rest*/
  public static String capitalize(String s) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (i == 0 || i > 0 && !Character.isLetterOrDigit(s.charAt(i - 1))) c = Character.toUpperCase(c);
      else c = Character.toLowerCase(c);
      result.append(c);
    }
    return (result.toString());
  }

  /** TRUE if the Charsequence ends with the string */
  public static boolean endsWith(CharSequence s, String end) {
    return (s.length() >= end.length() && s.subSequence(s.length() - end.length(), s.length()).equals(end));
  }

  /** IndexOf */
  public static int indexOf(char c, CharSequence string) {
    for (int i = 0; i < string.length(); i++) {
      if (string.charAt(i) == c) return (i);
    }
    return (-1);
  }

  /** Prints a test case*/
  public static boolean testCase(String name, String s1, String s2) {
    if (s1.equals(s2)) {
      System.out.println(name + ": OK");
      return (true);
    } else {
      System.out.println(name + ": " + s1 + "!=" + s2);
      return (false);
    }
  }

  /** Tests all methods*/
  public static void test() {
    String in, out;
    testCase("EncAmp", encodeAmpersand(in = "a+A&b�", alphaNumericAndSpace), out = "a&#43;A&amp;b&#228;");
    testCase("DecAmp", decodeAmpersand(out), in);
    testCase("DecAmp'", decodeAmpersand("&amp;&inv;&#228;&amp &"), "&&inv;�& &");
    testCase("EncBack", encodeBackslash(in = "a+A\\b�\n", alphaNumericAndSpace), out = "a\\u002bA\\\\b\\u00e4\\n");
    testCase("DecBack", decodeBackslash(out), in);
    testCase("DecBack'", decodeBackslash("\\00101a\\\\a\\n\\k\\"), "Aa\\a\n\\k\\");
    testCase("EncHex", encodeHex("a+A\\b�", alphaNumericAndSpace), "a2BA5CbE4");
    testCase("EncPerc", encodePercentage(in = "a+A%b�", alphaNumericAndSpace), out = "a%2BA%25b%C3%A4");
    testCase("DecPerc", decodePercentage(out), in);
    testCase("DecPerc'", decodePercentage("%C3%A4%X%"), "�%X%");
    testCase("EncURI", encodeURIPathComponent(in = "a+A/b �&"), out = "a+A%2Fb%20%C3%A4&");
    testCase("DecURI", decodeURIPathComponent(out), in);
    testCase("EncXML", encodeURIPathComponentXML("a+A/b �&"), "a+A%2Fb%20%C3%A4%26");
    testCase("EncUTF8", encodeUTF8("a+A/b �<"), "a+A/b ä<");
  }

  /** Test routine */
  public static void main(String argv[]) throws Exception {
    test();
  }

}
