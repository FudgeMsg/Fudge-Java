/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and other contributors.
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
import static org.junit.Assert.assertFalse;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.DateProvider;
import javax.time.calendar.DateTimeProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetDate;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.TimeProvider;
import javax.time.calendar.ZoneOffset;

import org.fudgemsg.types.DateTimeAccuracy;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.FudgeDateTime;
import org.fudgemsg.types.FudgeTime;
import org.junit.Test;

/**
 * Tests the Date, Time and DateTime implementations
 *
 * @author Andrew Griffin
 */
public class DateTimeTest {
  
  private final FudgeContext _fudgeContext = FudgeContext.GLOBAL_DEFAULT;
  
  /**
   * 
   */
  public DateTimeTest () {
  }
  
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
    printMessage (data);
    return _fudgeContext.deserialize (data).getMessage ();
  }
  
  /**
   * 
   */
  @Test
  public void fudgeDateCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, new FudgeDate (2010));
    msg.add (1, new FudgeDate (2010, 3));
    msg.add (2, new FudgeDate (2010, 3, 4));
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (DateTimeAccuracy.YEAR, msgOut.getFieldValue (FudgeDate.class, msgOut.getByOrdinal (0)).getAccuracy ());
    assertEquals (DateTimeAccuracy.MONTH, msgOut.getFieldValue (FudgeDate.class, msgOut.getByOrdinal (1)).getAccuracy ());
    assertEquals (DateTimeAccuracy.DAY, msgOut.getFieldValue (FudgeDate.class, msgOut.getByOrdinal (2)).getAccuracy ());
    for (int i = 0; i < msg.getNumFields (); i++) {
      FudgeDate iDate = (FudgeDate)msg.getByOrdinal (i).getValue ();
      for (int j = 0; j <  msgOut.getNumFields (); j++) {
        FudgeDate jDate = (FudgeDate)msgOut.getByOrdinal (j).getValue ();
        if (i == j) {
          assertEquals (iDate, jDate);
        } else {
          assertFalse ("(" + i + ", " + j + ") " + iDate + " == " + jDate, iDate.equals (jDate));
        }
      }
    }
  }
  
  /**
   * 
   */
  @Test
  public void fudgeTimeCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    // different resolutions
    msg.add (0, new FudgeTime (DateTimeAccuracy.HOUR, 0, 11 * 3600, 0)); // 11am
    msg.add (1, new FudgeTime (DateTimeAccuracy.MINUTE, 0, 11 * 3600, 0)); // 11:00am
    msg.add (2, new FudgeTime (DateTimeAccuracy.MINUTE, 0, 11 * 3600 + 12 * 60, 0)); // 11:12am
    msg.add (3, new FudgeTime (DateTimeAccuracy.SECOND, 0, 11 * 3600 + 12 * 60, 0)); // 11:12:00am
    msg.add (4, new FudgeTime (DateTimeAccuracy.SECOND, 0, 11 * 3600 + 12 * 60 + 13, 0)); // 11:12:13am
    msg.add (5, new FudgeTime (DateTimeAccuracy.MILLISECOND, 0, 11 * 3600 + 12 * 60 + 13, 0)); // 11:12:13.0am
    msg.add (6, new FudgeTime (DateTimeAccuracy.MILLISECOND, 0, 11 * 3600 + 12 * 60 + 13, 456000000)); // 11:12:13.456
    msg.add (7, new FudgeTime (DateTimeAccuracy.MICROSECOND, 0, 11 * 3600 + 12 * 60 + 13, 456000000)); // 11:12:13.456000
    msg.add (8, new FudgeTime (DateTimeAccuracy.MICROSECOND, 0, 11 * 3600 + 12 * 60 + 13, 456789000)); // 11:12:13.456789
    msg.add (9, new FudgeTime (DateTimeAccuracy.NANOSECOND, 0, 11 * 3600 + 12 * 60 + 13, 456789000)); // 11:12:13.456789000
    msg.add (10, new FudgeTime (DateTimeAccuracy.NANOSECOND, 0, 11 * 3600 + 12 * 60 + 13, 456789001)); // 11:12:13.456789001
    // different timezone offsets
    msg.add (11, new FudgeTime (DateTimeAccuracy.NANOSECOND, -4, 11 * 3600 + 12 * 60 + 13, 456789001)); // 11:12:13.456789001 -01:00
    msg.add (12, new FudgeTime (DateTimeAccuracy.NANOSECOND, -128, 11 * 3600 + 12 * 60 + 13, 456789001)); // 11:12:13.456789001 No timezone
    msg.add (13, new FudgeTime (DateTimeAccuracy.NANOSECOND, 4, 11 * 3600 + 12 * 60 + 13, 456789001)); // 11:12:13.456789001 +01:00
    final FudgeFieldContainer msgOut = cycle (msg);
    for (int i = 0; i < msg.getNumFields (); i++) {
      FudgeTime iTime = (FudgeTime)msg.getByOrdinal (i).getValue ();
      for (int j = 0; j < msg.getNumFields (); j++) {
        FudgeTime jTime = (FudgeTime)msgOut.getByOrdinal (j).getValue ();
        if (i == j) {
          assertEquals (iTime, jTime);
        } else {
          assertFalse ("(" + i + ", " + j + ") " + iTime + " == " + jTime, iTime.equals (jTime));
        }
      }
    }
  }
  
  /**
   * 
   */
  @Test
  public void fudgeDateTimeCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    // different resolutions
    msg.add (0, new FudgeDateTime (DateTimeAccuracy.YEAR, 2010, 0, 0, 0, 0, 0)); // 2010
    msg.add (1, new FudgeDateTime (DateTimeAccuracy.MONTH, 2010, 3, 0, 0, 0, 0)); // April-2010
    msg.add (2, new FudgeDateTime (DateTimeAccuracy.DAY, 2010, 3, 4, 0, 0, 0)); // 3-April-2010
    msg.add (3, new FudgeDateTime (DateTimeAccuracy.HOUR, 2010, 3, 4, 0, 0, 0)); // 3-April-2010, midnight
    msg.add (4, new FudgeDateTime (DateTimeAccuracy.HOUR, 2010, 3, 4, 0, 11 * 3600, 0)); // 3-April-2010, 11am
    msg.add (5, new FudgeDateTime (DateTimeAccuracy.MINUTE, 2010, 3, 4, 0, 11 * 3600, 0)); // 3-April-2010, 11:00am
    msg.add (6, new FudgeDateTime (DateTimeAccuracy.MINUTE, 2010, 3, 4, 0, 11 * 3600 + 12 * 60, 0)); // 3-April-2010, 11:12am
    msg.add (7, new FudgeDateTime (DateTimeAccuracy.SECOND, 2010, 3, 4, 0, 11 * 3600 + 12 * 60, 0)); // 3-April-2010, 11:12:00am
    msg.add (8, new FudgeDateTime (DateTimeAccuracy.SECOND, 2010, 3, 4, 0, 11 * 3600 + 12 * 60 + 13, 0)); // 3-April-2010, 11:12:13am
    msg.add (9, new FudgeDateTime (DateTimeAccuracy.MILLISECOND, 2010, 3, 4, 0, 11 * 3600 + 12 * 60 + 13, 0)); // 3-April-2010, 11:12:13.0am
    msg.add (10, new FudgeDateTime (DateTimeAccuracy.MILLISECOND, 2010, 3, 4, 0, 11 * 3600 + 12 * 60 + 13, 456000000)); // 3-April-2010, 11:12:13.456
    msg.add (11, new FudgeDateTime (DateTimeAccuracy.MICROSECOND, 2010, 3, 4, 0, 11 * 3600 + 12 * 60 + 13, 456000000)); // 3-April-2010, 11:12:13.456000
    msg.add (12, new FudgeDateTime (DateTimeAccuracy.MICROSECOND, 2010, 3, 4, 0, 11 * 3600 + 12 * 60 + 13, 456789000)); // 3-April-2010, 11:12:13.456789
    msg.add (13, new FudgeDateTime (DateTimeAccuracy.NANOSECOND, 2010, 3, 4, 0, 11 * 3600 + 12 * 60 + 13, 456789000)); // 3-April-2010, 11:12:13.456789000
    msg.add (14, new FudgeDateTime (DateTimeAccuracy.NANOSECOND, 2010, 3, 4, 0, 11 * 3600 + 12 * 60 + 13, 456789001)); // 3-April-2010, 11:12:13.456789001
    // different timezone offsets
    msg.add (15, new FudgeDateTime (DateTimeAccuracy.DAY, 2010, 3, 4, -4, 0, 0)); // 3-April-2010, -01:00
    msg.add (16, new FudgeDateTime (DateTimeAccuracy.DAY, 2010, 3, 4, -128, 0, 0)); // 3-April-2010, no timezone
    msg.add (17, new FudgeDateTime (DateTimeAccuracy.DAY, 2010, 3, 4, 4, 0, 0)); // 3-April-2010, +01:00
    msg.add (18, new FudgeDateTime (DateTimeAccuracy.NANOSECOND, 2010, 3, 4, -4, 11 * 3600 + 12 * 60 + 13, 456789001)); // 3-April-2010, 11:12:13.456789001, -01:00
    msg.add (19, new FudgeDateTime (DateTimeAccuracy.NANOSECOND, 2010, 3, 4, -128, 11 * 3600 + 12 * 60 + 13, 456789001)); // 3-April-2010, 11:12:13.456789001, no timezone
    msg.add (20, new FudgeDateTime (DateTimeAccuracy.NANOSECOND, 2010, 3, 4, 4, 11 * 3600 + 12 * 60 + 13, 456789001)); // 3-April-2010, 11:12:13.456789001, +01:00
    final FudgeFieldContainer msgOut = cycle (msg);
    for (int i = 0; i < msg.getNumFields (); i++) {
      FudgeDateTime iTime = (FudgeDateTime)msg.getByOrdinal (i).getValue ();
      for (int j = 0; j < msg.getNumFields (); j++) {
        FudgeDateTime jTime = (FudgeDateTime)msgOut.getByOrdinal (j).getValue ();
        if (i == j) {
          assertEquals (iTime, jTime);
        } else {
          assertFalse ("(" + i + ", " + j + ") " + iTime + " == " + jTime, iTime.equals (jTime));
        }
      }
    }
  }
  
  /**
   * Reference Calendar for 5 March 2010, 11:12:13.987 +01:00
   */
  private Calendar getReferenceCalendar () {
    final Calendar cal = Calendar.getInstance ();
    cal.clear ();
    cal.set (2010, 2, 5, 11, 12, 13);
    cal.set (Calendar.MILLISECOND, 987);
    cal.set (Calendar.ZONE_OFFSET, 1800 * 1000);
    cal.set (Calendar.DST_OFFSET, 1800 * 1000);
    return cal;
  }
  
  private Date getReferenceDate () {
    return getReferenceCalendar ().getTime ();
  }
  
  private java.util.TimeZone getReferenceTimeZone() {
    return TimeZone.getDefault();
  }
  
  /**
   * Reference FudgeDateTime for 5 March 2010, 11:12:13.987 +01:00
   */
  private FudgeDateTime getReferenceFudgeDateTime (final DateTimeAccuracy accuracy) {
    return new FudgeDateTime (accuracy, 2010, 3, 5, 4, 11 * 3600 + 12 * 60 + 13, 987000000);
  }
  
  private FudgeDateTime getReferenceFudgeDateTimeNoTimezone (final DateTimeAccuracy accuracy) {
    return new FudgeDateTime (accuracy, 2010, 3, 5, -128, 11 * 3600 + 12 * 60 + 13, 987000000);
  }
  
  private FudgeDateTime getReferenceFudgeDateTimeUTC (final DateTimeAccuracy accuracy) {
    return new FudgeDateTime (accuracy, 2010, 3, 5, 0, 10 * 3600 + 12 * 60 + 13, 987000000);
  }
  
  /**
   * Reference OffsetDateTime for 5 March 2010, 11:12:13.987 +01:00
   */
  private OffsetDateTime getReferenceOffsetDateTime () {
    return OffsetDateTime.of (2010, 3, 5, 11, 12, 13, 987000000, ZoneOffset.ofHours (1));
  }
  
  private OffsetDate getReferenceOffsetDate () {
    return getReferenceOffsetDateTime ().toOffsetDate ();
  }
  
  private OffsetTime getReferenceOffsetTime () {
    return getReferenceOffsetDateTime ().toOffsetTime ();
  }
  
  private DateProvider getReferenceDateProvider () {
    return new DateProvider () {
      @Override
      public LocalDate toLocalDate() {
        return getReferenceOffsetDate ().toLocalDate ();
      }
    };
  }
  
  private TimeProvider getReferenceTimeProvider () {
    return new TimeProvider () {
      @Override
      public LocalTime toLocalTime () {
        return getReferenceOffsetTime ().toLocalTime ();
      }
    };
  }
  
  private DateTimeProvider getReferenceDateTimeProvider () {
    return new DateTimeProvider () {
      @Override
      public LocalDateTime toLocalDateTime () {
        return getReferenceOffsetDateTime ().toLocalDateTime ();
      }
      @Override
      public LocalDate toLocalDate () {
        return getReferenceOffsetDateTime ().toLocalDate ();
      }
      @Override
      public LocalTime toLocalTime () {
        return getReferenceOffsetDateTime ().toLocalTime ();
      }
    };
  }
  
  private LocalDate getReferenceLocalDate () {
    return getReferenceDateProvider ().toLocalDate ();
  }
  
  private LocalDateTime getReferenceLocalDateTime () {
    return getReferenceDateTimeProvider ().toLocalDateTime ();
  }
  
  private LocalTime getReferenceLocalTime () {
    return getReferenceTimeProvider ().toLocalTime ();
  }
  
  private Instant getReferenceInstant () {
    return getReferenceOffsetDateTime ().toInstant ();
  }
  
  private InstantProvider getReferenceInstantProvider () {
    return new InstantProvider () {
      @Override
      public Instant toInstant () {
        return getReferenceInstant ();
      }
    };
  }
  
  private javax.time.calendar.TimeZone getReferenceTimeZone310() {
    return javax.time.calendar.TimeZone.UTC;
  }
  
  /**
   * Verify the derived objects:
   *   Calendar (via Date) against Instant
   *   FudgeDateTime against Instant
   *   FudgeDateTimeNoTimezone against LocalDateTime
   */
  @Test
  public void testReferenceObjects () {
    assertEquals (getReferenceInstant (), getReferenceFudgeDateTime (DateTimeAccuracy.MILLISECOND).toInstant ());
    assertEquals (getReferenceFudgeDateTime (DateTimeAccuracy.MILLISECOND).toInstant (), getReferenceFudgeDateTimeUTC (DateTimeAccuracy.MILLISECOND).toInstant ());
    assertEquals (new Date (getReferenceInstant ().toEpochMillisLong ()), getReferenceDate ());
    assertEquals (getReferenceLocalDateTime (), getReferenceFudgeDateTime (DateTimeAccuracy.MILLISECOND).toLocalDateTime ());
  }
  
  /**
   * 
   */
  @Test
  public void calendarCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceCalendar ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTime (DateTimeAccuracy.MILLISECOND), (FudgeDateTime)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceCalendar ().getTime (), msgOut.getFieldValue (Calendar.class, msgOut.getByOrdinal (0)).getTime ());
  }
  
  /**
   * 
   */
  @Test
  public void dateCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceDate ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTimeUTC (DateTimeAccuracy.MILLISECOND), (FudgeDateTime)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceDate (), msgOut.getFieldValue (Date.class, msgOut.getByOrdinal (0)));
  }
  
  /**
   * Test java.util.TimeZone as secondary type.
   */
  @Test
  public void timeZoneCycle() {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage();
    msg.add(0, getReferenceTimeZone());
    final FudgeFieldContainer msgOut = cycle(msg);
    assertEquals(getReferenceTimeZone(), (java.util.TimeZone) msgOut.getFieldValue(java.util.TimeZone.class, msgOut.getByOrdinal(0)));
  }
  
  /**
   * 
   */
  @Test
  public void dateProviderCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceDateProvider ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTime (DateTimeAccuracy.NANOSECOND).getDate (), (FudgeDate)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceDateProvider ().toLocalDate (), msgOut.getFieldValue (DateProvider.class, msgOut.getByOrdinal (0)).toLocalDate ());
  }
  
  /**
   * 
   */
  @Test
  public void dateTimeProviderCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceDateTimeProvider ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTimeNoTimezone (DateTimeAccuracy.NANOSECOND), (FudgeDateTime)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceDateTimeProvider ().toLocalDateTime (), msgOut.getFieldValue (DateTimeProvider.class, msgOut.getByOrdinal (0)).toLocalDateTime ());
  }
  
  /**
   * 
   */
  @Test
  public void instantCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceInstant ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTimeUTC (DateTimeAccuracy.NANOSECOND), (FudgeDateTime)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceInstant (), msgOut.getFieldValue (Instant.class, msgOut.getByOrdinal (0)));
  }
  
  /**
   * 
   */
  @Test
  public void instantProviderCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceInstantProvider ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTimeUTC (DateTimeAccuracy.NANOSECOND), (FudgeDateTime)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceInstantProvider ().toInstant (), msgOut.getFieldValue (InstantProvider.class, msgOut.getByOrdinal (0)).toInstant ());
  }
  
  /**
   * 
   */
  @Test
  public void localDateCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceLocalDate ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTimeNoTimezone (DateTimeAccuracy.NANOSECOND).getDate (), (FudgeDate)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceLocalDate (), msgOut.getFieldValue (LocalDate.class, msgOut.getByOrdinal (0)));
  }
  
  /**
   * 
   */
  @Test
  public void localDateTimeCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceLocalDateTime ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTimeNoTimezone (DateTimeAccuracy.NANOSECOND), (FudgeDateTime)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceLocalDateTime (), msgOut.getFieldValue (LocalDateTime.class, msgOut.getByOrdinal (0)));
  }
  
  /**
   * 
   */
  @Test
  public void localTimeCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceLocalTime ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTimeNoTimezone (DateTimeAccuracy.NANOSECOND).getTime (), (FudgeTime)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceLocalTime (), msgOut.getFieldValue (LocalTime.class, msgOut.getByOrdinal (0)));
  }
  
  /**
   * 
   */
  @Test
  public void offsetDateCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceOffsetDate ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTime (DateTimeAccuracy.DAY), (FudgeDateTime)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceOffsetDate (), msgOut.getFieldValue (OffsetDate.class, msgOut.getByOrdinal (0)));
  }
  
  /**
   * 
   */
  @Test
  public void offsetDateTimeCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceOffsetDateTime ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTime (DateTimeAccuracy.NANOSECOND), (FudgeDateTime)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceOffsetDateTime (), msgOut.getFieldValue (OffsetDateTime.class, msgOut.getByOrdinal (0)));
  }
  
  /**
   * 
   */
  @Test
  public void offsetTimeCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceOffsetTime ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTime (DateTimeAccuracy.NANOSECOND).getTime (), (FudgeTime)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceOffsetTime (), msgOut.getFieldValue (OffsetTime.class, msgOut.getByOrdinal (0)));
  }
  
  /**
   * 
   */
  @Test
  public void timeProviderCycle () {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add (0, getReferenceTimeProvider ());
    final FudgeFieldContainer msgOut = cycle (msg);
    assertEquals (getReferenceFudgeDateTimeNoTimezone (DateTimeAccuracy.NANOSECOND).getTime (), (FudgeTime)msgOut.getByOrdinal (0).getValue ());
    assertEquals (getReferenceTimeProvider ().toLocalTime (), msgOut.getFieldValue (TimeProvider.class, msgOut.getByOrdinal (0)).toLocalTime ());
  }
  
  /**
   * Test javax.time.TimeZone as secondary type.
   */
  @Test
  public void timeZone310Cycle() {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage();
    msg.add(0, getReferenceTimeZone310());
    final FudgeFieldContainer msgOut = cycle(msg);
    assertEquals(getReferenceTimeZone310(), (javax.time.calendar.TimeZone) msgOut.getFieldValue(javax.time.calendar.TimeZone.class, msgOut.getByOrdinal(0)));
  }
  
}