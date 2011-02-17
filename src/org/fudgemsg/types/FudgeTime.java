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
package org.fudgemsg.types;

import java.util.Calendar;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.TimeProvider;
import javax.time.calendar.ZoneOffset;

/**
 * Dummy class for holding a time value on its own at varying precisions.
 * 
 * See <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime encoding</a>
 * for more details.
 * 
 * @author Andrew Griffin
 */
public class FudgeTime implements TimeProvider {
  
  /**
   * Reserved value to indicate there is no timezone offset.
   */
  /* package */ static final int NO_TIMEZONE_OFFSET = -128;
  
  private final DateTimeAccuracy _accuracy;
  
  private final int _timezoneOffset;
  
  private final LocalTime _localTime;
  
  /**
   * Creates a new {@link FudgeTime}.
   * 
   * @param accuracy resolution of the time
   * @param timezoneOffset timezoneOffset (15 minute intervals)
   * @param seconds seconds since midnight
   * @param nanos nanoseconds within the second
   */
  public FudgeTime (final DateTimeAccuracy accuracy, final int timezoneOffset, int seconds, int nanos) {
    _accuracy = accuracy;
    _timezoneOffset = timezoneOffset;
    if (seconds < 0) throw new IllegalArgumentException ("seconds cannot be negative");
    if (nanos < 0) throw new IllegalArgumentException ("nanos cannot be negative");
    
    if (accuracy.greaterThan(DateTimeAccuracy.SECOND)) {
      if (accuracy.greaterThan(DateTimeAccuracy.MILLISECOND)) {
        if (accuracy.greaterThan(DateTimeAccuracy.MICROSECOND)) {
          // As accurate as can be - no rounding needed
        } else {
          nanos -= nanos % 1000;
        }
      } else {
          nanos -= nanos % 1000000;
      }
    } else {
      nanos = 0;
      if (accuracy.greaterThan (DateTimeAccuracy.DAY)) {
        if (accuracy.greaterThan (DateTimeAccuracy.HOUR)) {
          if (accuracy.greaterThan (DateTimeAccuracy.MINUTE)) {
            // Accurate to the second - already rounded for this
          } else {
            seconds -= seconds % 60;
          }
        } else {
          seconds -= seconds % 3600;
        }
      } else {
        seconds = 0;
      }
    }
    _localTime = LocalTime.of (seconds / 3600, (seconds / 60) % 60, seconds % 60, nanos);
  }
  
  /**
   * Creates a new Fudge time representation.
   * 
   * @param accuracy granularity of the representation
   * @param timezoneOffset timezone offset in 15 minute intervals from UTC
   * @param localTime the time
   */
  protected FudgeTime (final DateTimeAccuracy accuracy, final int timezoneOffset, final LocalTime localTime) {
    this (accuracy, timezoneOffset, localTime.toSecondOfDay (), localTime.getNanoOfSecond ());
  }
  
  /**
   * Creates a new Fudge time representation.
   * 
   * @param accuracy granularity of the representation
   * @param localTime the time
   */
  protected FudgeTime (final DateTimeAccuracy accuracy, final LocalTime localTime) {
    this (accuracy, NO_TIMEZONE_OFFSET, localTime);
  }
  
  /**
   * Creates a new Fudge time representation.
   * 
   * @param accuracy granularity of the representation
   * @param instant time instant - the corresponding time at UTC will be used
   */
  protected FudgeTime (final DateTimeAccuracy accuracy, final Instant instant) {
    this (accuracy, OffsetTime.ofInstant (instant, ZoneOffset.UTC));
  }
  
  /**
   * Creates a new Fudge time representation.
   * 
   * @param offsetTime time
   */
  public FudgeTime (final OffsetTime offsetTime) {
    this (DateTimeAccuracy.NANOSECOND, offsetTime);
  }
  
  /**
   * Creates a new Fudge time representation.
   * 
   * @param accuracy granularity of the representation
   * @param offsetTime time
   */
  public FudgeTime (final DateTimeAccuracy accuracy, final OffsetTime offsetTime) {
    this (accuracy, offsetTime.getOffset ().getAmountSeconds () / 900, offsetTime.toLocalTime ());
  }
  
  /**
   * Creates a new Fudge time representation.
   * 
   * @param instantProvider provides a time instant - the corresponding time at UTC will be used
   */
  public FudgeTime (final InstantProvider instantProvider) {
    this (DateTimeAccuracy.NANOSECOND, instantProvider);
  }
  
  /**
   * Creates a new Fudge time representation.
   * 
   * @param accuracy granularity of the representation
   * @param instantProvider provides a time instant - the corresponding time at UTC will be used
   */
  public FudgeTime (final DateTimeAccuracy accuracy, final InstantProvider instantProvider) {
    this (accuracy, instantProvider.toInstant ());
  }
  
  /**
   * Creates a new Fudge time representation.
   * 
   * @param timeProvider provides the time
   */
  public FudgeTime (final TimeProvider timeProvider) {
    this (DateTimeAccuracy.NANOSECOND, timeProvider);
  }
  
  /**
   * Creates a new Fudge time representation.
   * 
   * @param accuracy granularity of the representation
   * @param timeProvider provides the time
   */
  public FudgeTime (final DateTimeAccuracy accuracy, final TimeProvider timeProvider) {
    this (accuracy, timeProvider.toLocalTime ());
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
    _localTime = LocalTime.of (time.get (Calendar.HOUR_OF_DAY), time.get (Calendar.MINUTE), time.get (Calendar.SECOND), time.get (Calendar.MILLISECOND) * 1000000); 
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
    return getRawTimezoneOffset () != NO_TIMEZONE_OFFSET;
  }
  
  /**
   * Returns the timezone offset as held. See also {@link #getOffset}.
   * 
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
  
  /**
   * Returns a {@link LocalTime} representation of the time.
   * 
   * @return the time
   */
  public LocalTime toLocalTime () {
    return _localTime;
  }
  
  /**
   * Returns the timezone offset as a {@link ZoneOffset} object. See also {@link #getTimezoneOffset}.
   * 
   * @return the timezone offset
   */
  protected ZoneOffset getOffset () {
    return ZoneOffset.ofTotalSeconds (getTimezoneOffset () * 900);
  }
  
  /**
   * Returns a {@link OffsetTime} representation of the time.
   * 
   * @return the time
   */
  public OffsetTime toOffsetTime () {
    return OffsetTime.of (toLocalTime (), getOffset ());
  }
  
  /**
   * Returns the number of seconds since midnight.
   * 
   * @return seconds
   */
  public int getSecondsSinceMidnight () {
    return toLocalTime ().toSecondOfDay (); 
  }
  
  /**
   * Returns the number of nanoseconds within the second.
   * 
   * @return nanoseconds
   */
  public int getNanos () {
    return toLocalTime ().getNanoOfSecond ();
  }
  
  /**
   * Returns the hour of the day.
   * 
   * @return hour
   */
  public int getHour () {
    return toLocalTime ().getHourOfDay ();
  }
  
  /**
   * Returns the minute within the hour.
   * 
   * @return minutes
   */
  public int getMinute () {
    return toLocalTime ().getMinuteOfHour ();
  }
  
  /**
   * Returns the second within the minute.
   * 
   * @return seconds
   */
  public int getSeconds () {
    return toLocalTime ().getSecondOfMinute ();
  }
  
  /**
   * Returns the milliseconds within the second.
   *
   * @return milliseconds
   */
  public int getMillis () {
    return getNanos () / 1000000;
  }
  
  /**
   * Returns the microseconds within the second.
   * 
   * @return microseconds
   */
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
            int frac = getNanos ();
            if (getAccuracy ().lessThan (DateTimeAccuracy.NANOSECOND)) {
              frac /= 1000;
              if (getAccuracy ().lessThan (DateTimeAccuracy.MICROSECOND)) {
                frac /= 1000;
              }
            }
            sb.append ('.').append (frac);
          }
        }
      }
    }
    if (hasTimezoneOffset ()) {
      int tz = getTimezoneOffset () * 15;
      if (tz == 0) {
        sb.append (" UTC");
      } else {
        sb.append ((tz > 0) ? " +" : " -");
        if (tz < 0) tz = -tz;
        sb.append (tz / 60).append (':');
        tz %= 60;
        if (tz < 10) sb.append ('0');
        sb.append (tz);
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
    return other.getAccuracy () == getAccuracy ()
        && other.toLocalTime ().equals (toLocalTime ())
        && other.getRawTimezoneOffset () == getRawTimezoneOffset ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode () {
    return (toLocalTime ().hashCode () * 17 + _timezoneOffset + 1) * 17 + _accuracy.getEncodedValue ();
  }
  
}
