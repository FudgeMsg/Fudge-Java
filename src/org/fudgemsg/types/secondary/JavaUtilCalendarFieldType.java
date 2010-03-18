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
package org.fudgemsg.types.secondary;

import java.util.Calendar;

import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.types.DateTimeFieldType;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.FudgeDateTime;
import org.fudgemsg.types.FudgeTime;
import org.fudgemsg.types.SecondaryFieldType;

/**
 * Secondary type for {@link Calendar} conversion to/from a {@link FudgeTime}, {@link FudgeDate} or {@link FudgeDateTime}
 * transport object.
 *
 * @author Andrew Griffin
 */
public class JavaUtilCalendarFieldType extends SecondaryFieldType<Calendar,Object> {
  
  /**
   * Singleton instance of the type.
   */
  public static final JavaUtilCalendarFieldType INSTANCE = new JavaUtilCalendarFieldType ();
  
  @SuppressWarnings("unchecked")
  private JavaUtilCalendarFieldType () {
    super ((FudgeFieldType<Object>)(FudgeFieldType<? extends Object>)DateTimeFieldType.INSTANCE, Calendar.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object secondaryToPrimary (Calendar object) {
    return new FudgeDateTime (object);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Calendar primaryToSecondary (Object object) {
    if (object instanceof FudgeDateTime) {
      return primaryToSecondary ((FudgeDateTime)object);
    } else if (object instanceof FudgeDate) {
      return primaryToSecondary ((FudgeDate)object);
    } else if (object instanceof FudgeTime) {
      return primaryToSecondary ((FudgeTime)object);
    } else {
      throw new IllegalArgumentException ("cannot convert from type " + object.getClass ().getName ());
    }
  }
  
  /**
   * Primary to secondary conversion, where the primary type is a {@link FudgeDateTime}.
   * 
   * @param object primary object
   * @return the converted {@link Calendar} object
   */
  protected Calendar primaryToSecondary (FudgeDateTime object) {
    return object.getCalendar ();
  }
  
  /**
   * Primary secondary conversion, where the primary type is a {@link FudgeDate}.
   * 
   * @param object primary object
   * @return the converted {@link Calendar} object
   */
  protected Calendar primaryToSecondary (FudgeDate object) {
    return object.getCalendar ();
  }
  
  protected Calendar primaryToSecondary (FudgeTime object) {
    return object.getCalendar ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canConvertPrimary (Class<?> javaType) {
    return FudgeDateTime.class.isAssignableFrom (javaType) || FudgeDate.class.isAssignableFrom (javaType);
  }
  
}