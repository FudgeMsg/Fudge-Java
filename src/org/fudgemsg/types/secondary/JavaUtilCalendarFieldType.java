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
package org.fudgemsg.types.secondary;

import java.util.Calendar;

import org.fudgemsg.types.DateTimeAccuracy;
import org.fudgemsg.types.DateTimeFieldType;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.FudgeDateTime;
import org.fudgemsg.types.FudgeTime;
import org.fudgemsg.types.SecondaryFieldTypeBase;

/**
 * Secondary type for {@link Calendar} conversion to/from a {@link FudgeTime}, {@link FudgeDate} or {@link FudgeDateTime}
 * transport object.
 *
 * @author Andrew Griffin
 */
public class JavaUtilCalendarFieldType extends SecondaryFieldTypeBase<Calendar, Object, FudgeDateTime> {

  /**
   * Singleton instance of the type.
   */
  public static final JavaUtilCalendarFieldType INSTANCE = new JavaUtilCalendarFieldType();

  private JavaUtilCalendarFieldType() {
    super(DateTimeFieldType.INSTANCE, Calendar.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeDateTime secondaryToPrimary(Calendar object) {
    return new FudgeDateTime(object);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Calendar primaryToSecondary(Object object) {
    if (object instanceof FudgeDateTime) {
      return primaryToSecondary((FudgeDateTime) object);
    } else if (object instanceof FudgeDate) {
      return primaryToSecondary((FudgeDate) object);
    } else if (object instanceof FudgeTime) {
      return primaryToSecondary((FudgeTime) object);
    } else {
      throw new IllegalArgumentException("cannot convert from type " + object.getClass().getName());
    }
  }

  private static void fudgeDateToCalendar(final Calendar cal, final FudgeDate date) {
    cal.set(Calendar.YEAR, date.getYear());
    if (date.getAccuracy().greaterThan(DateTimeAccuracy.YEAR)) {
      cal.set(Calendar.MONTH, date.getMonthOfYear() - 1);
      if (date.getAccuracy().greaterThan(DateTimeAccuracy.MONTH)) {
        cal.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
      }
    }
  }

  private static void fudgeTimeToCalendar(final Calendar cal, final FudgeTime time) {
    if (time.getAccuracy().greaterThan(DateTimeAccuracy.DAY)) {
      cal.set(Calendar.HOUR_OF_DAY, time.getHour());
      if (time.getAccuracy().greaterThan(DateTimeAccuracy.HOUR)) {
        cal.set(Calendar.MINUTE, time.getMinute());
        if (time.getAccuracy().greaterThan(DateTimeAccuracy.MINUTE)) {
          cal.set(Calendar.SECOND, time.getSeconds());
          if (time.getAccuracy().greaterThan(DateTimeAccuracy.SECOND)) {
            cal.set(Calendar.MILLISECOND, time.getMillis());
          }
        }
      }
    }
    if (time.hasTimezoneOffset()) {
      cal.set(Calendar.ZONE_OFFSET, time.getTimezoneOffset() * 900000);
    }
  }

  /**
   * Converts a Fudge date and time representation to a {@link Calendar} object.
   * 
   * @param fudgeDate date component
   * @param fudgeTime time component
   * @return the {@code Calendar}
   */
  /* package */static Calendar fudgeDateTimeToCalendar(final FudgeDate fudgeDate, final FudgeTime fudgeTime) {
    final Calendar cal = Calendar.getInstance();
    cal.clear();
    fudgeDateToCalendar(cal, fudgeDate);
    if (fudgeTime != null) {
      fudgeTimeToCalendar(cal, fudgeTime);
    }
    return cal;
  }

  /**
   * Primary to secondary conversion, where the primary type is a {@link FudgeDateTime}.
   * 
   * @param object primary object
   * @return the converted {@link Calendar} object
   */
  protected Calendar primaryToSecondary(FudgeDateTime object) {
    return fudgeDateTimeToCalendar(object.getDate(), object.getTime());
  }

  /**
   * Primary to secondary conversion, where the primary type is a {@link FudgeDate}.
   * 
   * @param object primary object
   * @return the converted {@link Calendar} object
   */
  protected Calendar primaryToSecondary(FudgeDate object) {
    final Calendar cal = Calendar.getInstance();
    cal.clear();
    fudgeDateToCalendar(cal, object);
    return cal;
  }

  /**
   * Primary to secondary conversion, where the primary type is a {@link FudgeTime}.
   * 
   * @param object primary object
   * @return the converted {@link Calendar} object
   */
  protected Calendar primaryToSecondary(FudgeTime object) {
    final Calendar cal = Calendar.getInstance();
    cal.clear();
    fudgeTimeToCalendar(cal, object);
    return cal;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canConvertPrimary(Class<?> javaType) {
    return FudgeDateTime.class.isAssignableFrom(javaType)
        || FudgeDate.class.isAssignableFrom(javaType)
        || FudgeTime.class.isAssignableFrom(javaType);
  }

}
