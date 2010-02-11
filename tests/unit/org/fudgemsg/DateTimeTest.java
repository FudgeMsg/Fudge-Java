/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc. and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fudgemsg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;

import org.fudgemsg.types.DateTimeAccuracy;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.FudgeTime;
import org.junit.Test;

/**
 * Tests the Date, Time and DateTime implementations
 *
 * @author Andrew Griffin
 */
public class DateTimeTest {
  
  private final Calendar _reference; // 5 Feb 2010, 11:12:13.987, +1 hour
  private final FudgeContext _fudgeContext = new FudgeContext ();
  
  /**
   * 
   */
  public DateTimeTest () {
    _reference = Calendar.getInstance ();
    _reference.clear ();
    _reference.set (2010, 02, 05, 11, 12, 13);
    _reference.set (Calendar.MILLISECOND, 987);
    _reference.set (Calendar.ZONE_OFFSET, 1800 * 1000);
    _reference.set (Calendar.DST_OFFSET, 1800 * 1000);
  }
  
  private Calendar getReferenceCopy () {
    return (Calendar)_reference.clone ();
  }
  
  @SuppressWarnings("unused")
  private void printMessage (final byte[] data) {
    int i = 0;
    System.out.println ();
    System.out.println ("data: " + data.length + " bytes");
    while (i < data.length) {
      int x;
      for (x = 0; (i < data.length) && (x < 32); x++) {
        int d = (int)data[i++] & 0xFF;
        if (d < 16) System.out.print ('0');
        System.out.print (Integer.toHexString (d));
        System.out.print (' ');
      }
      i -= x;
      for (; x <= 32; x++) {
        System.out.print ("   ");
      }
      for (x = 0; (i < data.length) && (x < 32); x++) {
        int d = (int)data[i++] & 0xFF;
        if (d <= 32) System.out.print ('.'); else System.out.print ((char)d);
      }
      System.out.println ();
    }
  }
  
  private FudgeFieldContainer cycle (final FudgeFieldContainer msg) {
    final byte[] data = _fudgeContext.toByteArray (msg);
    //printMessage (data);
    return _fudgeContext.deserialize (data).getMessage ();
  }
  
  private void assertCalendarEquals (final Calendar a, final Calendar b) {
    assertNotNull (a);
    assertNotNull (b);
    assertEquals (a.getTime (), b.getTime ());
  }
  
  /**
   * 
   */
  @Test
  public void calendarCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add ("milliseconds", millisTest ());
    msg.add ("seconds", secondsTest ());
    msg.add ("minutes", minutesTest ());
    msg.add ("hours", hoursTest ());
    msg.add ("days", daysTest ());
    msg.add ("months", monthsTest ());
    msg.add ("years", yearsTest ());
    msg.add ("centuries", centuriesTest ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertCalendarEquals (_fudgeContext.getFieldValue (Calendar.class, msg.getByName ("milliseconds")), _fudgeContext.getFieldValue (Calendar.class, msgOut.getByName ("milliseconds")));
    assertCalendarEquals (_fudgeContext.getFieldValue (Calendar.class, msg.getByName ("seconds")), _fudgeContext.getFieldValue (Calendar.class, msgOut.getByName ("seconds")));
    assertCalendarEquals (_fudgeContext.getFieldValue (Calendar.class, msg.getByName ("minutes")), _fudgeContext.getFieldValue (Calendar.class, msgOut.getByName ("minutes")));
    assertCalendarEquals (_fudgeContext.getFieldValue (Calendar.class, msg.getByName ("hours")), _fudgeContext.getFieldValue (Calendar.class, msgOut.getByName ("hours")));
    assertCalendarEquals (_fudgeContext.getFieldValue (Calendar.class, msg.getByName ("days")), _fudgeContext.getFieldValue (Calendar.class, msgOut.getByName ("days")));
    assertCalendarEquals (_fudgeContext.getFieldValue (Calendar.class, msg.getByName ("months")), _fudgeContext.getFieldValue (Calendar.class, msgOut.getByName ("months")));
    assertCalendarEquals (_fudgeContext.getFieldValue (Calendar.class, msg.getByName ("years")), _fudgeContext.getFieldValue (Calendar.class, msgOut.getByName ("years")));
    assertCalendarEquals (_fudgeContext.getFieldValue (Calendar.class, msg.getByName ("centuries")), _fudgeContext.getFieldValue (Calendar.class, msgOut.getByName ("centuries")));
    //System.out.println (msg);
    //System.out.println (msgOut);
  }
  
  /**
   * 
   */
  @Test
  public void dateCycle () {
    final Date date = getReferenceCopy ().getTime ();
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add ("date", date);
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (date, _fudgeContext.getFieldValue (Date.class, msgOut.getByName ("date")));
    //System.out.println (msg);
    //System.out.println (msgOut);
  }
  
  /**
   * 
   */
  @Test
  public void fudgeDateCycle () {
    final FudgeDate date = new FudgeDate (getReferenceCopy ().getTime ());
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add ("date", date);
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (date, _fudgeContext.getFieldValue (FudgeDate.class, msgOut.getByName ("date")));
    //System.out.println (msg);
    //System.out.println (msgOut);
  }
  
  /**
   * 
   */
  @Test
  public void fudgeTimeCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    final long nanos = 1;
    final long micros = nanos * 1000l;
    final long millis = micros * 1000l;
    final long seconds = millis * 1000l;
    final long minutes = seconds * 60l;
    final long hours = minutes * 60l;
    long time = 11l * hours;
    msg.add (1, new FudgeTime (DateTimeAccuracy.HOUR, time)); // 11am
    time += 12l * minutes;
    msg.add (2, new FudgeTime (DateTimeAccuracy.MINUTE, time)); // 11:12am
    time += 13l * seconds;
    msg.add (3, new FudgeTime (DateTimeAccuracy.SECOND, time)); // 11:12:13am
    time += 456l * millis;
    msg.add (4, new FudgeTime (DateTimeAccuracy.MILLISECOND, time)); // 11:12:13.456
    time += 789l * micros;
    msg.add (5, new FudgeTime (DateTimeAccuracy.MICROSECOND, time)); // 11:12:13.456789
    time += 1l * nanos;
    msg.add (6, new FudgeTime (DateTimeAccuracy.NANOSECOND, time)); // 11:12:13.456789001
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (_fudgeContext.getFieldValue (FudgeTime.class, msg.getByOrdinal (1)), _fudgeContext.getFieldValue (FudgeTime.class, msgOut.getByOrdinal (1)));
    assertEquals (_fudgeContext.getFieldValue (FudgeTime.class, msg.getByOrdinal (2)), _fudgeContext.getFieldValue (FudgeTime.class, msgOut.getByOrdinal (2)));
    assertEquals (_fudgeContext.getFieldValue (FudgeTime.class, msg.getByOrdinal (3)), _fudgeContext.getFieldValue (FudgeTime.class, msgOut.getByOrdinal (3)));
    assertEquals (_fudgeContext.getFieldValue (FudgeTime.class, msg.getByOrdinal (4)), _fudgeContext.getFieldValue (FudgeTime.class, msgOut.getByOrdinal (4)));
    assertEquals (_fudgeContext.getFieldValue (FudgeTime.class, msg.getByOrdinal (5)), _fudgeContext.getFieldValue (FudgeTime.class, msgOut.getByOrdinal (5)));
    assertEquals (_fudgeContext.getFieldValue (FudgeTime.class, msg.getByOrdinal (6)), _fudgeContext.getFieldValue (FudgeTime.class, msgOut.getByOrdinal (6)));
  }
  
  private Calendar millisTest () {
    final Calendar cal = getReferenceCopy ();
    return cal;
  }
  
  private Calendar secondsTest () {
    final Calendar cal = millisTest ();
    cal.clear (Calendar.MILLISECOND);
    return cal;
  }
  
  private Calendar minutesTest () {
    final Calendar cal = secondsTest ();
    cal.clear (Calendar.SECOND);
    return cal;
  }
  
  private Calendar hoursTest () {
    final Calendar cal = minutesTest ();
    cal.clear (Calendar.MINUTE);
    return cal;
  }
  
  private Calendar daysTest () {
    final Calendar cal = hoursTest ();
    cal.clear (Calendar.AM_PM);
    cal.clear (Calendar.HOUR);
    cal.clear (Calendar.HOUR_OF_DAY);
    return cal;
  }
  
  private Calendar monthsTest () {
    final Calendar cal = daysTest ();
    cal.clear (Calendar.DAY_OF_WEEK);
    cal.clear (Calendar.DAY_OF_MONTH);
    cal.clear (Calendar.DAY_OF_YEAR);
    cal.clear (Calendar.DAY_OF_WEEK_IN_MONTH);
    return cal;
  }
  
  private Calendar yearsTest () {
    final Calendar cal = monthsTest ();
    cal.clear (Calendar.MONTH);
    return cal;
  }
  
  private Calendar centuriesTest () {
    final Calendar cal = yearsTest ();
    cal.set (Calendar.YEAR, 2000);
    return cal;
  }
  
}