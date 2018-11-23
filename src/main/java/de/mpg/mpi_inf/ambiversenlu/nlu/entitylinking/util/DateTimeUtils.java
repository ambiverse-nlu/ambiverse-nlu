package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class DateTimeUtils {

  public static long roundToMidnight(long timestamp) {
    DateTime dt = new DateTime(timestamp);
    DateTime midnight = new DateTime(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(), 0, 0);
    return midnight.getMillis();
  }

  public static List<Long> getDayRange(long first, long last) {
    List<Long> days = new ArrayList<Long>();
    DateTime start = new DateTime(first);
    DateTime end = new DateTime(last);
    while (!start.isAfter(end)) {
      days.add(start.getMillis());
      start = start.plusDays(1);
      long midnight = roundToMidnight(start.getMillis());
      assert (start.getMillis() == midnight) : "Day offsets are wrong";
    }
    return days;
  }

}
