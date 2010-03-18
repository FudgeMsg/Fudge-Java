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

import java.util.Date;
import java.util.Calendar;

/**
 * <p>Dummy class for holding a date value on its own, at varying precision. Dates can be
 * more easily used through the secondary type mechanism.</p>
 * 
 * <p>For more details, please refer to <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime encoding</a>.</p>
 * 
 * @author Andrew Griffin
 */
public class FudgeDate {
  
  private final int _year;
  private final int _month;
  private final int _day;
  
  /**
   * Constructs a new {@link FudgeDate} object representing just a year.
   * 
   * @param year the year
   */
  public FudgeDate (final int year) {
    this (year, 0, 0);
  }
  
  /**
   * Constructs a new {@link FudgeDate} object representing a year and a month.
   * 
   * @param year the year
   * @param month the month
   */
  public FudgeDate (final int year, final int month) {
    this (year, month, 0);
  }
  
  /**
   * Constructs a new {@link FudgeDate} object.
   * 
   * @param year the year
   * @param month the month
   * @param day the day
   */
  public FudgeDate (final int year, final int month, final int day) {
    _year = year;
    if (month < 0) throw new IllegalArgumentException ("month cannot be negative");
    _month = month;
    if (day < 0) throw new IllegalArgumentException ("day cannot be negative");
    if ((month == 0) && (day > 0)) throw new IllegalArgumentException ("cannot specify day without month");
    _day = day;
  }
  
  /**
   * Constructs a new {@link FudgeDate} object from a {@link Calendar}.
   * 
   * @param date the {@link Calendar} to copy the date from
   */
  public FudgeDate (final Calendar date) {
    this (date.get (Calendar.YEAR), date.isSet (Calendar.MONTH) ? (date.get (Calendar.MONTH) + 1) : 0, date.isSet (Calendar.DAY_OF_MONTH) ? date.get (Calendar.DAY_OF_MONTH) : 0);
  }
  
  /**
   * Constructs a new {@link FudgeDate} object from a {@link Date}.
   * 
   * @param d the {@code Date} to copy the date from
   */
  public FudgeDate (final Date d) {
    this (FudgeDateTime.dateToCalendar (d));
  }
  
  /**
   * Returns a {@link Date} representation of this date. The time on the {@code Date} object will be set to Midnight on that day.
   * 
   * @return a {@code Date}
   */
  public Date getDate () {
    return getCalendar ().getTime ();
  }
  
  /**
   * Returns a {@link Calendar} representation of this date.
   * 
   * @return a {@code Calendar}
   */
  public Calendar getCalendar () {
    final Calendar cal = Calendar.getInstance ();
    cal.clear ();
    cal.set (Calendar.YEAR, getYear ());
    if (getMonthOfYear () > 0) cal.set (Calendar.MONTH, getMonthOfYear () - 1);
    if (getDayOfMonth () > 0) cal.set (Calendar.DAY_OF_MONTH, getDayOfMonth ());
    return cal;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String toString () {
    final StringBuilder sb = new StringBuilder ('(').append (getYear ());
    if (getMonthOfYear () > 0) {
      sb.append (", ").append (getMonthOfYear ());
    }
    if (getDayOfMonth () > 0) {
      sb.append (", ").append (getDayOfMonth ());
    }
    return sb.append (')').toString ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (!(o instanceof FudgeDate)) return false;
    final FudgeDate other = (FudgeDate)o;
    return other.getYear () == getYear ()
        && other.getMonthOfYear () == getMonthOfYear ()
        && other.getDayOfMonth () == getDayOfMonth ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode () {
    return (getYear () * 17 + getMonthOfYear () + 1) * 17 + getDayOfMonth ();
  }
  
  /**
   * Returns the year.
   * 
   * @return the year
   */
  public int getYear () {
    return _year;
  }
  
  /**
   * Returns the month of the year, or 0 if the date just represents a year
   * 
   * @return the month of the year
   */
  public int getMonthOfYear () {
    return _month;
  }
  
  /**
   * Returns the day of the month, or 0 if the date just represents a year or year/month.
   * 
   * @return the day of the month
   */
  public int getDayOfMonth () {
    return _day;
  }
  
  /**
   * Returns the accuracy of the Date.
   * 
   * @return the accuracy
   */
  public DateTimeAccuracy getAccuracy () {
    if (getDayOfMonth () == 0) {
      if (getMonthOfYear () == 0) {
        return DateTimeAccuracy.YEAR;
      } else {
        return DateTimeAccuracy.MONTH;
      }
    } else {
      return DateTimeAccuracy.DAY;
    }
  }
  
}
