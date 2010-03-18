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
package org.fudgemsg.types;

import java.util.Calendar;
import java.util.Date;

/**
 * Dummy class for holding a time value on its own at varying precisions.
 * 
 * See <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime encoding</a>
 * for more details.
 * 
 * @author Andrew Griffin
 */
public class FudgeTime {
  
  /* package */ static final int NO_TIMEZONE_OFFSET = -128;
  
  private final DateTimeAccuracy _accuracy;
  
  private final int _timezoneOffset;
  
  private final int _seconds;
  
  private final int _nanos;
  
  /**
   * Creates a new {@link FudgeTime}.
   * 
   * @param accuracy resolution of the time
   * @param timezoneOffset timezoneOffset (15 minute intervals)
   * @param seconds seconds since midnight
   * @param nanos nanoseconds within the second
   */
  public FudgeTime (final DateTimeAccuracy accuracy, final int timezoneOffset, final int seconds, final int nanos) {
    _accuracy = accuracy;
    _timezoneOffset = timezoneOffset;
    if (seconds < 0) throw new IllegalArgumentException ("seconds cannot be negative");
    _seconds = seconds;
    if (nanos < 0) throw new IllegalArgumentException ("nanos cannot be negative");
    _nanos = nanos;
  }
  
  /**
   * Creates a new {@link FudgeTime} with the time from a {@link Calendar} object.
   * 
   * @param time the {@code Calendar} to copy the time from
   */
  public FudgeTime (final Calendar time) {
    _accuracy = DateTimeAccuracy.MILLISECOND;
    if (time.isSet (Calendar.ZONE_OFFSET) || time.isSet (Calendar.DST_OFFSET)) {
      _timezoneOffset = ((time.isSet (Calendar.ZONE_OFFSET) ? time.get (Calendar.ZONE_OFFSET) : 0) + (time.isSet (Calendar.DST_OFFSET) ? time.get (Calendar.DST_OFFSET) : 0)) / 900000;
    } else {
      _timezoneOffset = NO_TIMEZONE_OFFSET;
    }
    _seconds = time.get (Calendar.HOUR_OF_DAY) * 3600 + time.get (Calendar.MINUTE) * 60 + time.get (Calendar.SECOND);
    _nanos = time.get (Calendar.MILLISECOND) * 1000000;
  }
  
  /* package */ void updateCalendar (final Calendar calendar) {
    if (getAccuracy ().greaterThan (DateTimeAccuracy.DAY)) {
      calendar.set (Calendar.HOUR_OF_DAY, getHour ());
      if (getAccuracy ().greaterThan (DateTimeAccuracy.HOUR)) {
        calendar.set (Calendar.MINUTE, getMinute ());
        if (getAccuracy ().greaterThan (DateTimeAccuracy.MINUTE)) {
          calendar.set (Calendar.SECOND, getSeconds ());
          if (getAccuracy ().greaterThan (DateTimeAccuracy.SECOND)) {
            calendar.set (Calendar.MILLISECOND, getMillis ());
          }
        }
      }
    }
    int i = getTimezoneOffset ();
    if (i != 0) {
      calendar.set (Calendar.ZONE_OFFSET, i * 900000);
    }
  }
  
  public Calendar getCalendar () {
    final Calendar cal = Calendar.getInstance ();
    cal.clear ();
    updateCalendar (cal);
    return cal;
  }
  
  /**
   * Creates a new {@link FudgeTime} with the time from a {@link Date} object.
   * 
   * @param time the {@code Date} to copy the time from
   */
  public FudgeTime (final Date time) {
    this (FudgeDateTime.dateToCalendar (time));
  }
  
  /**
   * @return the resolution
   */
  public DateTimeAccuracy getAccuracy () {
    return _accuracy;
  }
  
  /**
   * @return {@code true} if the {@link FudgeTime} has a timezone offset, {@code false} otherwise
   */
  public boolean hasTimezoneOffset () {
    return getTimezoneOffset () != NO_TIMEZONE_OFFSET;
  }
  
  /**
   * @return the timezone offset (15 minute intervals) or 0 if there is no offset
   */
  public int getTimezoneOffset () {
    if (_timezoneOffset != NO_TIMEZONE_OFFSET) {
      return _timezoneOffset;
    } else {
      return 0;
    }
  }
  
  /**
   * Returns the timezone offset as it would be encoded. I.e. if there is no timezone, the value of -128 will
   * be returned. This is different to the behaviour of {@link #getTimezoneOffset} which would return 0 for both
   * there being no timezone information and there being a timezone offset of 0.
   * 
   * @return the timezone offset (15 minute intervals) or -128 for none
   */
  /* package */ int getRawTimezoneOffset () {
    return _timezoneOffset;
  }
  
  public int getSecondsSinceMidnight () {
    return _seconds;
  }
  
  public int getNanos () {
    return _nanos;
  }
  
  public int getHour () {
    return getSecondsSinceMidnight () / 3600;
  }
  
  public int getMinute () {
    return (getSecondsSinceMidnight () / 60) % 60;
  }
  
  public int getSeconds () {
    return getSecondsSinceMidnight () % 60;
  }
  
  public int getMillis () {
    return getNanos () / 1000000;
  }
  
  public int getMicros () {
    return getNanos () / 1000;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String toString () {
    final StringBuilder sb = new StringBuilder ();
    if (getAccuracy ().greaterThan (DateTimeAccuracy.DAY)) {
      if (getHour () < 10) sb.append ('0');
      sb.append (getHour ());
      if (getAccuracy ().greaterThan (DateTimeAccuracy.HOUR)) {
        sb.append (':');
        if (getMinute () < 10) sb.append ('0');
        sb.append (getMinute ());
        if (getAccuracy ().greaterThan (DateTimeAccuracy.MINUTE)) {
          sb.append (':');
          if (getSeconds () < 10) sb.append ('0');
          sb.append (getSeconds ());
          if (getAccuracy ().greaterThan (DateTimeAccuracy.SECOND)) {
            sb.append ('.').append (getNanos ());
          }
        }
      }
    }
    return sb.toString ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (!(o instanceof FudgeTime)) return false;
    final FudgeTime other = (FudgeTime)o;
    return other.getSecondsSinceMidnight () == getSecondsSinceMidnight ()
        && other.getNanos () == getNanos ()
        && other.getAccuracy () == getAccuracy ()
        && other.getRawTimezoneOffset () == getRawTimezoneOffset ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode () {
    return ((_seconds * 17 + _nanos + 1) * 17 + _timezoneOffset + 1) * 17 + _accuracy.getEncodedValue ();
  }
  
}
