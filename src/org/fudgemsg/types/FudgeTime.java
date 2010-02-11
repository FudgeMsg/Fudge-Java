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

/**
 * Dummy class for holding a time value on its own, as Java does not have a
 * standard type for doing so. See <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime encoding</a>
 * for more details.
 * 
 * @author Andrew Griffin
 */
public class FudgeTime {
  
  private final DateTimeAccuracy _accuracy;
  
  private final boolean _hasTimezoneOffset;
  
  private final int _timezoneOffset;
  
  private final long _nanos;
  
  /**
   * Creates a new {@link FudgeTime}.
   * 
   * @param accuracy resolution of the time
   * @param timezoneOffset timezoneOffset (15 minute intervals)
   * @param nanos nanoseconds since Midnight
   */
  public FudgeTime (final DateTimeAccuracy accuracy, final int timezoneOffset, final long nanos) {
    this (accuracy, true, timezoneOffset, nanos);
  }
  
  /**
   * Creates a new {@link FudgeTime} without a timezone.
   * 
   * @param accuracy resolution of the time
   * @param nanos nanoseconds since Midnight
   */
  public FudgeTime (final DateTimeAccuracy accuracy, final long nanos) {
    this (accuracy, false, 0, nanos);
  }
  
  private FudgeTime (final DateTimeAccuracy accuracy, final boolean hasTimezoneOffset, final int timezoneOffset, final long nanos) {
    if (accuracy == null) throw new NullPointerException ("accuracy cannot be null");
    if (accuracy.getEncodedValue () > DateTimeAccuracy.HOUR.getEncodedValue ()) throw new IllegalArgumentException ("accuracy is not valid");
    _accuracy = accuracy;
    _hasTimezoneOffset = hasTimezoneOffset;
    _timezoneOffset = timezoneOffset;
    _nanos = nanos;
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
    return _hasTimezoneOffset;
  }
  
  /**
   * @return the timezone offset (15 minute intervals) or 0 if there is no offset
   */
  public int getTimezoneOffset () {
    return hasTimezoneOffset () ? _timezoneOffset : 0;
  }
  
  /**
   * Nanoseconds since midnight
   * 
   * @return the total nanoseconds
   */
  public long getNanos () {
    return _nanos;
  }
  
  /**
   * Microseconds since midnight
   * 
   * @return the total microseconds
   */
  public long getMicros () {
    return getNanos () / 1000l;
  }
  
  /**
   * Milliseconds since midnight
   * 
   * @return the total milliseconds
   */
  public int getMillis () {
    return (int)(getNanos () / 1000000l);
  }
  
  /**
   * Seconds since midnight
   * 
   * @return the total seconds
   */
  public int getSeconds () {
    return (int)(getNanos () / 1000000000l);
  }
  
  /**
   * Nanoseconds within the second since midnight
   * 
   * @return the nanoseconds
   */
  public int getTimeNanos () {
    return (int)(getNanos () % 1000000000l);
  }
  
  /**
   * Microseconds within the second since midnight
   * 
   * @return the microseconds
   */
  public int getTimeMicros () {
    return (int)(getTimeNanos () / 1000l);
  }
  
  /**
   * Milliseconds within the second since midnight
   * 
   * @return the milliseconds
   */
  public int getTimeMillis () {
    return (int)(getTimeNanos () / 1000000l);
  }
  
  /**
   * Seconds within the minute since midnight
   * 
   * @return the seconds
   */
  public int getTimeSeconds () {
    return getSeconds () % 60;
  }
  
  /**
   * Minutes within the hour since midnight
   * 
   * @return the minutes
   */
  public int getTimeMinutes () {
    return (getSeconds () / 60) % 60;
  }
  
  /**
   * Hours since midnight
   * 
   * @return the hours
   */
  public int getTimeHours () {
    return getSeconds () / 3600;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String toString () {
    final int hours = getTimeHours ();
    final StringBuilder sb = new StringBuilder ();
    if (hours < 10) sb.append ('0');
    sb.append (hours);
    if (getAccuracy ().getEncodedValue () < DateTimeAccuracy.HOUR.getEncodedValue ()) {
      sb.append (':');
      int minutes = getTimeMinutes ();
      if (minutes < 10) sb.append ('0');
      sb.append (minutes);
      if (getAccuracy ().getEncodedValue () < DateTimeAccuracy.MINUTE.getEncodedValue ()) {
        sb.append (':');
        int seconds = getTimeSeconds ();
        if (seconds < 10) sb.append ('0');
        sb.append (seconds);
        if (getAccuracy ().getEncodedValue () < DateTimeAccuracy.SECOND.getEncodedValue ()) {
          sb.append ('.');
          sb.append (getTimeNanos ());
        }
      }
    } else {
      sb.append ('h');
    }
    if (hasTimezoneOffset ()) {
      sb.append (' ');
      int tzMins = getTimezoneOffset () * 15;
      if (tzMins < 0) {
        tzMins = -tzMins;
        sb.append ('-');
      } else {
        sb.append ('+');
      }
      sb.append (tzMins).append ('m');
    }
    return sb.toString ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals (final Object o) {
    if (o == null) return false;
    if (o == this) return true;
    if (!(o instanceof FudgeTime)) return false;
    final FudgeTime other = (FudgeTime)o;
    if (getNanos () != other.getNanos ()) return false;
    if (hasTimezoneOffset ()) {
      if (!other.hasTimezoneOffset ()) return false;
      if (getTimezoneOffset () != other.getTimezoneOffset ()) return false;
    } else {
      if (other.hasTimezoneOffset ()) return false;
    }
    return true;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode () {
    int hc = 1;
    hc += (int)(getNanos () >> 32) * 31 + (int)getNanos ();
    if (hasTimezoneOffset ()) {
      hc *= 31;
      hc += getTimezoneOffset ();
    }
    return hc;
  }
  
}
