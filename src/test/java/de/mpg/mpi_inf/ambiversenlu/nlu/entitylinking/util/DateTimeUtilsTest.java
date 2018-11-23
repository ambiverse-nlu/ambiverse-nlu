package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class DateTimeUtilsTest {

  @Test public void testGetDayRange() {
    DateTime start = new DateTime(1980, 1, 1, 0, 0);
    DateTime end = new DateTime(1980, 3, 13, 0, 0);

    List<Long> days = DateTimeUtils.getDayRange(start.getMillis(), end.getMillis());
    assertEquals(start.getMillis(), (long) days.get(0));
    assertEquals(end.getMillis(), (long) days.get(days.size() - 1));
    assertEquals(73, days.size());

    DateTime test = new DateTime(1980, 2, 1, 0, 0);
    assertEquals(test.getMillis(), (long) days.get(31));
    DateTime test2 = new DateTime(1980, 2, 1, 0, 1);
    assertNotSame(test2.getMillis(), (long) days.get(31));
  }

  @Test public void testRoundToMidnight() {
    long ts1982midnight = new DateTime(1980, 1, 1, 0, 0).getMillis();
    long ts1982eleven = new DateTime(1980, 1, 1, 11, 11).getMillis();
    assertEquals(ts1982midnight, DateTimeUtils.roundToMidnight(ts1982eleven));
  }
}
