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

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.DateTimeProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetDate;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

/**
 * Dummy class for holding a combined Date and Time with the other data available in the
 * Fudge encoding. 
 *
 * @author Andrew Griffin
 */
public class FudgeDateTime implements DateTimeProvider, InstantProvider {
  
  private final FudgeDate _date;
  private final FudgeTime _time;
  
  public FudgeDateTime (final DateTimeAccuracy precision, final int year, final int month, final int day, final int timezoneOffset, final int seconds, final int nanos) {
    this (new FudgeDate (year, month, day), new FudgeTime (precision, timezoneOffset, seconds, nanos));
  }
  
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
  
  protected FudgeDateTime (final DateTimeAccuracy accuracy, final Instant instant) {
    this (accuracy, ZonedDateTime.fromInstant (instant, TimeZone.UTC).toOffsetDateTime ());
  }
  
  public FudgeDateTime (final OffsetDateTime offsetDateTime) {
    this (DateTimeAccuracy.NANOSECOND, offsetDateTime);
  }
  
  public FudgeDateTime (final DateTimeAccuracy accuracy, final OffsetDateTime offsetDateTime) {
    this (new FudgeDate (offsetDateTime.toOffsetDate ()), new FudgeTime (accuracy, offsetDateTime.toOffsetTime ()));
  }
  
  public FudgeDateTime (final OffsetDate offsetDate) {
    this (new FudgeDate (offsetDate), new FudgeTime (DateTimeAccuracy.DAY, offsetDate.atMidnight ().toOffsetTime ()));
  }
  
  protected FudgeDateTime (final LocalDateTime localDateTime) {
    this (DateTimeAccuracy.NANOSECOND, localDateTime);
  }
  
  protected FudgeDateTime (final DateTimeAccuracy accuracy, final LocalDateTime localDateTime) {
    this (new FudgeDate (localDateTime), new FudgeTime (accuracy, localDateTime));
  }
  
  public FudgeDateTime (final InstantProvider instantProvider) {
    this (DateTimeAccuracy.NANOSECOND, instantProvider);
  }
  
  public FudgeDateTime (final DateTimeAccuracy accuracy, final InstantProvider instantProvider) {
    this (accuracy, instantProvider.toInstant ());
  }
  
  public FudgeDateTime (final DateTimeProvider dateTimeProvider) {
    this (DateTimeAccuracy.NANOSECOND, dateTimeProvider);
  }
  
  public FudgeDateTime (final DateTimeAccuracy accuracy, final DateTimeProvider dateTimeProvider) {
    this (accuracy, dateTimeProvider.toLocalDateTime ());
  }
  
  public FudgeDateTime (final Calendar calendar) {
    this (new FudgeDate (calendar), new FudgeTime (calendar));
  }
  
  public FudgeDate getDate () {
    return _date;
  }
  
  public FudgeTime getTime () {
    return _time;
  }
  
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

  @Override
  public LocalDate toLocalDate() {
    return getDate ().toLocalDate ();
  }

  @Override
  public LocalDateTime toLocalDateTime() {
    return LocalDateTime.from (getDate (), getTime ());
  }

  @Override
  public LocalTime toLocalTime() {
    return getTime ().toLocalTime ();
  }
  
  public OffsetDate toOffsetDate () {
    return OffsetDate.from (getDate (), getTime ().getOffset ());
  }
  
  public OffsetDateTime toOffsetDateTime () {
    return OffsetDateTime.from (getDate (), getTime (), getTime ().getOffset ());
  }
  
  public OffsetTime toOffsetTime () {
    return getTime ().toOffsetTime ();
  }
  
  @Override
  public Instant toInstant () {
    return toOffsetDateTime ().toInstant ();
  }
  
}
