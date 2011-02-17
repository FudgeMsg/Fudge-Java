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
import javax.time.calendar.DateTimeProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetDate;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.ZoneOffset;

/**
 * Dummy class for holding a combined Date and Time with the other data available in the
 * Fudge encoding. 
 *
 * @author Andrew Griffin
 */
public class FudgeDateTime implements DateTimeProvider, InstantProvider {
  
  private final FudgeDate _date;
  private final FudgeTime _time;
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param precision resolution of the representation
   * @param year year
   * @param month month
   * @param day day
   * @param timezoneOffset timezone offset in 15 minute intervals 
   * @param seconds seconds since midnight
   * @param nanos nanoseconds within the second
   */
  public FudgeDateTime (final DateTimeAccuracy precision, final int year, final int month, final int day, final int timezoneOffset, final int seconds, final int nanos) {
    this (new FudgeDate (year, month, day), new FudgeTime (precision, timezoneOffset, seconds, nanos));
  }
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param date the date
   * @param time the time
   */
  public FudgeDateTime (final FudgeDate date, final FudgeTime time) {
    if (date == null) throw new NullPointerException ("date cannot be null");
    if (time == null) throw new NullPointerException ("time cannot be null");
    if (date.getAccuracy ().lessThan (DateTimeAccuracy.DAY)) {
      if (date.getAccuracy ().lessThan (DateTimeAccuracy.MONTH)) {
        if (time.getAccuracy ().greaterThan (DateTimeAccuracy.YEAR)) {
          throw new IllegalArgumentException (date.getAccuracy () + " date too low precision for " + time.getAccuracy () + " datetime");
        }
      } else {
        if (time.getAccuracy ().greaterThan (DateTimeAccuracy.MONTH)) {
          throw new IllegalArgumentException (date.getAccuracy () + " date too low precision for " + time.getAccuracy () + " datetime");
        } else if (time.getAccuracy ().lessThan (DateTimeAccuracy.MONTH)) {
          throw new IllegalArgumentException (date.getAccuracy () + " date too high precision for " + time.getAccuracy () + " datetime");
        }
      }
    }
    _date = date;
    _time = time;
  }
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param accuracy resolution of the representation
   * @param instant time instant - the date and time at UTC will be used
   */
  protected FudgeDateTime (final DateTimeAccuracy accuracy, final Instant instant) {
    this (accuracy, OffsetDateTime.ofInstant (instant, ZoneOffset.UTC));
  }
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param offsetDateTime date and time
   */
  public FudgeDateTime (final OffsetDateTime offsetDateTime) {
    this (DateTimeAccuracy.NANOSECOND, offsetDateTime);
  }
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param accuracy resolution of the representation
   * @param offsetDateTime date and time
   */
  public FudgeDateTime (final DateTimeAccuracy accuracy, final OffsetDateTime offsetDateTime) {
    this (new FudgeDate (offsetDateTime.toOffsetDate ()), new FudgeTime (accuracy, offsetDateTime.toOffsetTime ()));
  }
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param offsetDate date - Midnight on this day will be used for the time
   */
  public FudgeDateTime (final OffsetDate offsetDate) {
    this (new FudgeDate (offsetDate), new FudgeTime (DateTimeAccuracy.DAY, offsetDate.atMidnight ().toOffsetTime ()));
  }
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param localDateTime date - Midnight on this day will be used for the time
   */
  protected FudgeDateTime (final LocalDateTime localDateTime) {
    this (DateTimeAccuracy.NANOSECOND, localDateTime);
  }
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param accuracy resolution of the representation 
   * @param localDateTime date and time
   */
  protected FudgeDateTime (final DateTimeAccuracy accuracy, final LocalDateTime localDateTime) {
    this (new FudgeDate (localDateTime), new FudgeTime (accuracy, localDateTime));
  }
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param instantProvider provides an instant - the date and time at UTC will be used  
   */
  public FudgeDateTime (final InstantProvider instantProvider) {
    this (DateTimeAccuracy.NANOSECOND, instantProvider);
  }
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param accuracy resolution of the representation
   * @param instantProvider provides an instant - the date and time at UTC will be used 
   */
  public FudgeDateTime (final DateTimeAccuracy accuracy, final InstantProvider instantProvider) {
    this (accuracy, instantProvider.toInstant ());
  }
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param dateTimeProvider provides the date and time 
   */
  public FudgeDateTime (final DateTimeProvider dateTimeProvider) {
    this (DateTimeAccuracy.NANOSECOND, dateTimeProvider);
  }
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param accuracy resolution of the representation 
   * @param dateTimeProvider provides the date and time
   */
  public FudgeDateTime (final DateTimeAccuracy accuracy, final DateTimeProvider dateTimeProvider) {
    this (accuracy, dateTimeProvider.toLocalDateTime ());
  }
  
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param calendar representation of the date and time
   */
  public FudgeDateTime (final Calendar calendar) {
    this (new FudgeDate (calendar), new FudgeTime (calendar));
  }
  
  /**
   * Returns the date component.
   * 
   * @return the date
   */
  public FudgeDate getDate () {
    return _date;
  }
  
  /**
   * Returns the time component.
   * 
   * @return the time
   */
  public FudgeTime getTime () {
    return _time;
  }
  
  /**
   * Returns the resolution of the representation
   * 
   * @return the resolution
   */
  public DateTimeAccuracy getAccuracy () {
    return getTime ().getAccuracy ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (!(o instanceof FudgeDateTime)) return false;
    final FudgeDateTime other = (FudgeDateTime)o;
    return getDate ().equals (other.getDate ())
        && getTime ().equals (other.getTime ());
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode () {
    return getDate ().hashCode () * 17 + getTime ().hashCode ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String toString () {
    return toOffsetDateTime ().toString ();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LocalDate toLocalDate() {
    return getDate ().toLocalDate ();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LocalDateTime toLocalDateTime() {
    return LocalDateTime.of (getDate (), getTime ());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LocalTime toLocalTime() {
    return getTime ().toLocalTime ();
  }
  
  /**
   * Returns the date representation as an {@link OffsetDate} object.
   * 
   * @return the date
   */ 
  public OffsetDate toOffsetDate () {
    return OffsetDate.of (getDate (), getTime ().getOffset ());
  }
  
  /**
   * Returns the representation as an {@link OffsetDateTime} object.
   * 
   * @return the date and time
   */
  public OffsetDateTime toOffsetDateTime () {
    return OffsetDateTime.of (getDate (), getTime (), getTime ().getOffset ());
  }
  
  /**
   * Returns the time representation as a {@link OffsetTime} object.
   * 
   * @return the time
   */
  public OffsetTime toOffsetTime () {
    return getTime ().toOffsetTime ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Instant toInstant () {
    return toOffsetDateTime ().toInstant ();
  }
  
}
