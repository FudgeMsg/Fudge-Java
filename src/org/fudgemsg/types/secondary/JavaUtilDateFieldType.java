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
import java.util.Date;

import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.types.DateTimeFieldType;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.SecondaryFieldType;

/**
 * Secondary type for {@link Date} conversion to/from a {@link Calendar}. Also supports conversions from
 * the {@link FudgeDate} temporary class.
 *
 * @author Andrew
 */
public class JavaUtilDateFieldType extends SecondaryFieldType<Date,Object> {
  
  /**
   * Singleton instance of the type.
   */
  public static final JavaUtilDateFieldType INSTANCE = new JavaUtilDateFieldType ();
  
  @SuppressWarnings("unchecked")
  private JavaUtilDateFieldType () {
    super ((FudgeFieldType<Object>)(FudgeFieldType<? extends Object>)DateTimeFieldType.INSTANCE, Date.class);
  }

  /**
   * {@docInherit}
   */
  @Override
  public Object secondaryToPrimary (Date object) {
    final Calendar calendar = Calendar.getInstance ();
    calendar.setTime (object);
    return calendar;
  }
  
  /**
   * {@docInherit}
   */
  @Override
  public Date primaryToSecondary (Object object) {
    if (object instanceof Calendar) {
      return primaryToSecondary ((Calendar)object);
    } else if (object instanceof FudgeDate) {
      return primaryToSecondary ((FudgeDate)object);
    } else {
      throw new IllegalArgumentException ("cannot convert from type " + object.getClass ().getName ());
    }
  }
  
  /**
   * Primary to secondary conversion, where the primary type is a {@link Calendar}.
   * 
   * @param object primary object
   * @return the converted {@link Date} object
   */
  protected Date primaryToSecondary (Calendar object) {
    return object.getTime ();
  }
  
  /**
   * Primary secondary conversion, where the primary type is a {@link FudgeDate}.
   * 
   * @param object primary object
   * @return the converted {@link Date} object
   */
  protected Date primaryToSecondary (FudgeDate object) {
    return object.getDate ();
  }
  
  /**
   * {@docInherit}
   */
  @Override
  public boolean canConvertPrimary (Class<?> javaType) {
    return Calendar.class.isAssignableFrom (javaType) || FudgeDate.class.isAssignableFrom (javaType);
  }
  
}