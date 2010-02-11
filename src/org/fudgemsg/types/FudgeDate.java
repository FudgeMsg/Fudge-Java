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
 * Dummy class for holding a date value on its own, as Java does not have a
 * standard type for doing so.
 * 
 * <p>This part of the specification is not finalized and should not be used. The {@code DateTime}
 * Fudge type should be used instead. For more details, please refer to {@link "http://wiki.fudgemsg.org/display/FDG/DateTime+encoding"}.</p>

 * 
 * @author Andrew Griffin
 */
public class FudgeDate {
  
  private final int _days;
  
  /**
   * Constructs a new {@link FudgeDate} object for a decimal date representation.
   * 
   * @param days the initial value, a decimal representation of the date
   */
  public FudgeDate (final int days) {
    _days = days;
  }
  
  /**
   * Constructs a new {@link FudgeDate} object from a {@link Date}.
   * 
   * @param d the {@code Date} to copy the date from
   */
  /*public FudgeDate (final Date d) {
  // The code below is wrong
  this ((int)(d.getTime () / (86400l * 1000l)));
}*/
  public FudgeDate (final Date d) {
    final Calendar cal = Calendar.getInstance ();
    cal.clear ();
    cal.setTime (d);
    cal.get (Calendar.MONTH);
    cal.get (Calendar.DAY_OF_MONTH);
    _days = cal.get (Calendar.YEAR) * 1000
          + cal.get (Calendar.MONTH) * 100
          + cal.get (Calendar.DAY_OF_MONTH);
  }
  
  /**
   * Avoid calling this. At the moment it is returning the decimal representation of the date, but this may change.
   * 
   * @return the decimal representation of the date
   */
  public int getDays () {
    return _days;
  }
  
  /**
   * Returns a {@link Date} representation of this date. The time on the {@code Date} object will be set to Midnight on that day.
   * 
   * @return a {@code Date}
   */
  /*public Date getDate () {
    final Calendar cal = Calendar.getInstance ();
    cal.clear ();
    cal.add (Calendar.DAY_OF_MONTH, getDays ());
    return cal.getTime ();
  }*/
  public Date getDate () {
    final Calendar cal = Calendar.getInstance ();
    cal.clear ();
    cal.set (Calendar.YEAR, getDays () / 1000);
    cal.set (Calendar.MONTH, (getDays () / 100) % 100);
    cal.set (Calendar.DAY_OF_MONTH, getDays () % 100);
    return cal.getTime ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String toString () {
    return getDate ().toString (); 
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals (final Object o) {
    if (o == null) return false;
    if (!(o instanceof FudgeDate)) return false;
    final FudgeDate other = (FudgeDate)o;
    return other.getDays () == getDays ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode () {
    return getDays ();
  }
  
}