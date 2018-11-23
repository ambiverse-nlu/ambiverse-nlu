package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

/**
 * Contains a method that will create a String from any long
 * saying how many days, hours, minutes ... the time value
 * represents.
 */
public class NiceTime {

  /**
   * takes a long value and converts it into a readable Time String
   * ie. if time eq 1234 would return the String: 1s, 234ms
   * @param time
   * @return
   */
  public static String convert(long time) {
    long seconds = -1;
    long minutes = -1;
    long hours = -1;
    StringBuffer sb = new StringBuffer(100);
    if (time < 0) {
      return "0ms";
    }
    long milliseconds = time % 1000;
    time = time / 1000;
    if (time > 0) {
      seconds = time % 60;
      time = time / 60;
    }
    if (time > 0) {
      minutes = time % 60;
      time = time / 60;
    }
    if (time > 0) {
      hours = time % 24;
      time = time / 24;
    }
    if (time > 0) {
      sb.append(time + "d, ");
    }
    if (hours != -1) {
      sb.append(hours + "h, ");
    }
    if (minutes != -1) {
      sb.append(minutes + "m, ");
    }
    if (seconds != -1) {
      sb.append(seconds + "s, ");
    }
    sb.append(milliseconds + "ms");
    return sb.toString();
  }

  public static String convert(double time) {
    return convert((long) time);
  }

}
