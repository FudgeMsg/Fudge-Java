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
 * Dummy class for holding a combined Date and Time with the other data available in the
 * Fudge encoding. 
 *
 * @author Andrew Griffin
 */
public class FudgeDateTime {
  
  private final FudgeDate _date;
  private final FudgeTime _time;
  
  /* package */ static Calendar dateToCalendar (final Date date) {
    final Calendar cal = Calendar.getInstance ();
    cal.clear ();
    cal.setTime (date);
    return cal;
  }
  
  public FudgeDateTime (final DateTimeAccuracy precision, final int year, final int month, final int day, final int timezoneOffset, final int seconds, final int nanos) {
    this (new FudgeDate (year, month, day), new FudgeTime (precision, timezoneOffset, seconds, nanos));
  }
  
  public FudgeDateTime (final FudgeDate date, final FudgeTime time) {
    if (date == null) throw new NullPointerException ("date cannot be null");
    if (time == null) throw new NullPointerException ("time cannot be null");
    if (date.getAccuracy ().lessThan (DateTimeAccuracy.DAY)) throw new IllegalArgumentException ("date cannot have less than DAY precision");
    _date = date;
    _time = time;
  }
  
  public FudgeDateTime (final Calendar cal) {
    this (new FudgeDate (cal), new FudgeTime (cal));
  }
  
  public FudgeDateTime (final Date date) {
    this (dateToCalendar (date));
  }
  
  public Calendar getCalendar () {
    final Calendar cal = getDate ().getCalendar ();
    getTime ().updateCalendar (cal);
    return cal;
  }
  
  public Date getJavaDate () {
    return getCalendar ().getTime ();
  }
  
  public FudgeDate getDate () {
    return _date;
  }
  
  public FudgeTime getTime () {
    return _time;
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
    return getDate () + " " + getTime ();
  }
  
}
